package com.github.cxfplus.jaxws.interceptors;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.AbstractLoggingInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedWriter;
import org.apache.cxf.io.DelegatingInputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

import com.github.cxfplus.jmx.CXFPlus;



/**
 * A simple logging handler which outputs the bytes of the message to the
 * Logger.
 */
@NoJSR250Annotations
public class LoggingInInterceptor extends AbstractLoggingInterceptor {
    private static final Logger LOG = LogUtils.getLogger(LoggingInInterceptor.class);
    
    public LoggingInInterceptor() {
        super(Phase.RECEIVE);
    }
    
    public LoggingInInterceptor(String phase) {
        super(phase);
    }

    public LoggingInInterceptor(String id, String phase) {
        super(id, phase);
    }

    public LoggingInInterceptor(int lim) {
        this();
        limit = lim;
    }
    public LoggingInInterceptor(String id, int lim) {
        this(id, Phase.RECEIVE);
        limit = lim;
    }

    public LoggingInInterceptor(PrintWriter w) {
        this();
        this.writer = w;
    }
    public LoggingInInterceptor(String id, PrintWriter w) {
        this(id, Phase.RECEIVE);
        this.writer = w;
    }
    
    public void handleMessage(Message message) throws Fault {
    	if(CXFPlus.getSingleton().isTrace()){
    		if (writer != null || LOG.isLoggable(Level.INFO)) {
                logging(LOG, message);
            }	
    	}
    }

    protected void logging(Logger logger, Message message) throws Fault {
        if (message.containsKey(LoggingMessage.ID_KEY)) {
            return;
        }
        String id = (String)message.getExchange().get(LoggingMessage.ID_KEY);
        if (id == null) {
            id = LoggingMessage.nextId();
            message.getExchange().put(LoggingMessage.ID_KEY, id);
        }
        message.put(LoggingMessage.ID_KEY, id);
        final LoggingMessage buffer 
            = new LoggingMessage(String.format("\n------- Inbound Message (ID=%s)---------",id), id);

        if (!Boolean.TRUE.equals(message.get(Message.DECOUPLED_CHANNEL_MESSAGE))) {
            // avoid logging the default responseCode 200 for the decoupled responses
            Integer responseCode = (Integer)message.get(Message.RESPONSE_CODE);
            if (responseCode != null) {
                buffer.getResponseCode().append(responseCode);
            }
        }

        String encoding = (String)message.get(Message.ENCODING);

        if (encoding != null) {
            buffer.getEncoding().append(encoding);
        }
        String httpMethod = (String)message.get(Message.HTTP_REQUEST_METHOD);
        if (httpMethod != null) {
            buffer.getHttpMethod().append(httpMethod);
        }
        String ct = (String)message.get(Message.CONTENT_TYPE);
        if (ct != null) {
            buffer.setContentType(ct);
        }
        Object headers = message.get(Message.PROTOCOL_HEADERS);
        if (headers != null) {
            buffer.getHeader().append(headers);
        }
        String uri = (String)message.get(Message.REQUEST_URL);
        if (uri == null) {
            String address = (String)message.get(Message.ENDPOINT_ADDRESS);
            uri = (String)message.get(Message.REQUEST_URI);
            if (uri != null && uri.startsWith("/")) {
                if (address != null && !address.startsWith(uri)) {
                    if (address.endsWith("/") && address.length() > 1) {
                        address = address.substring(0, address.length()); 
                    }
                    uri = address + uri;
                }
            } else {
                uri = address;
            }
        } 
        if (uri != null) {
            buffer.getAddress().append(uri);
            String query = (String)message.get(Message.QUERY_STRING);
            if (query != null) {
                buffer.getAddress().append("?").append(query);
            }
        }
        
        if (!isShowBinaryContent() && isBinaryContent(ct)) {
            buffer.getMessage().append(BINARY_CONTENT_MESSAGE).append('\n');
            log(logger, buffer.toString());
            return;
        }
        
        InputStream is = message.getContent(InputStream.class);
        if (is != null) {
            logInputStream(message, is, buffer, encoding, ct);
        } else {
            Reader reader = message.getContent(Reader.class);
            if (reader != null) {
                logReader(message, reader, buffer);
            }
        }
        log(logger, buffer.toString());
    }

    protected void logReader(Message message, Reader reader, LoggingMessage buffer) {
        try {
            CachedWriter writer = new CachedWriter();
            IOUtils.copyAndCloseInput(reader, writer);
            message.setContent(Reader.class, writer.getReader());
            
            if (writer.getTempFile() != null) {
                //large thing on disk...
                buffer.getMessage().append("\nMessage (saved to tmp file):\n");
                buffer.getMessage().append("Filename: " + writer.getTempFile().getAbsolutePath() + "\n");
            }
            if (writer.size() > limit && limit != -1) {
                buffer.getMessage().append("(message truncated to " + limit + " bytes)\n");
            }
            writer.writeCacheTo(buffer.getPayload(), limit);
        } catch (Exception e) {
            throw new Fault(e);
        }
    }
    protected void logInputStream(Message message, InputStream is, LoggingMessage buffer,
                                  String encoding, String ct) {
        CachedOutputStream bos = new CachedOutputStream();
        if (threshold > 0) {
            bos.setThreshold(threshold);
        }
        try {
            // use the appropriate input stream and restore it later
            InputStream bis = is instanceof DelegatingInputStream 
                ? ((DelegatingInputStream)is).getInputStream() : is;
            

            //only copy up to the limit since that's all we need to log
            //we can stream the rest
            IOUtils.copyAtLeast(bis, bos, limit == -1 ? Integer.MAX_VALUE : limit);
            bos.flush();
            bis = new SequenceInputStream(bos.getInputStream(), bis);
            
            // restore the delegating input stream or the input stream
            if (is instanceof DelegatingInputStream) {
                ((DelegatingInputStream)is).setInputStream(bis);
            } else {
                message.setContent(InputStream.class, bis);
            }

            if (bos.getTempFile() != null) {
                //large thing on disk...
                buffer.getMessage().append("\nMessage (saved to tmp file):\n");
                buffer.getMessage().append("Filename: " + bos.getTempFile().getAbsolutePath() + "\n");
            }
            if (bos.size() > limit && limit != -1) {
                buffer.getMessage().append("(message truncated to " + limit + " bytes)\n");
            }
            writePayload(buffer.getPayload(), bos, encoding, ct); 
                
            bos.close();
        } catch (Exception e) {
            throw new Fault(e);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
