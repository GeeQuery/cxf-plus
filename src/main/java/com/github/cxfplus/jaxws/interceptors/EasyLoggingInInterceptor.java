package com.github.cxfplus.jaxws.interceptors;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.AbstractLoggingInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.LoggingMessage;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

/**
 * A simple logging handler which outputs the bytes of the message to the
 * Logger.
 */
@NoJSR250Annotations
public abstract class EasyLoggingInInterceptor extends AbstractLoggingInterceptor {
    private static final Logger LOG = LogUtils.getLogger(EasyLoggingInInterceptor.class);
    
    public EasyLoggingInInterceptor() {
        super(Phase.RECEIVE);
    }
    
    public EasyLoggingInInterceptor(String phase) {
        super(phase);
    }

    public EasyLoggingInInterceptor(String id, String phase) {
        super(id, phase);
    }

    public EasyLoggingInInterceptor(int lim) {
        this();
        limit = lim;
    }
    public EasyLoggingInInterceptor(String id, int lim) {
        this(id, Phase.RECEIVE);
        limit = lim;
    }

    public EasyLoggingInInterceptor(PrintWriter w) {
        this();
        this.writer = w;
    }
    public EasyLoggingInInterceptor(String id, PrintWriter w) {
        this(id, Phase.RECEIVE);
        this.writer = w;
    }
    
    public void handleMessage(Message message) throws Fault {
        if (writer != null || getLogger().isLoggable(Level.INFO)) {
            logging(message);
        }
    }

    protected void logging(Message message) throws Fault {
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
            = new LoggingMessage("Inbound Message\n----------------------------", id);

        Integer responseCode = (Integer)message.get(Message.RESPONSE_CODE);
        if (responseCode != null) {
            buffer.getResponseCode().append(responseCode);
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
            buffer.getContentType().append(ct);
        }
        Object headers = message.get(Message.PROTOCOL_HEADERS);

        if (headers != null) {
            buffer.getHeader().append(headers);
        }
        String uri = (String)message.get(Message.REQUEST_URL);
        if (uri != null) {
            buffer.getAddress().append(uri);
            String query = (String)message.get(Message.QUERY_STRING);
            if (query != null) {
                buffer.getAddress().append("?").append(query);
            }
        }
            
        InputStream is = message.getContent(InputStream.class);
        if (is != null) {
            CachedOutputStream bos = new CachedOutputStream();
            try {
                IOUtils.copy(is, bos);

                bos.flush();
                is.close();

                message.setContent(InputStream.class, bos.getInputStream());
                if (bos.getTempFile() != null) {
                    //large thing on disk...
                    buffer.getMessage().append("\nMessage (saved to tmp file):\n");
                    buffer.getMessage().append("Filename: " + bos.getTempFile().getAbsolutePath() + "\n");
                }
                if (bos.size() > limit) {
                    buffer.getMessage().append("(message truncated to " + limit + " bytes)\n");
                }
                writePayload(buffer.getPayload(), bos, encoding, ct); 
                    
                bos.close();
            } catch (Exception e) {
                throw new Fault(e);
            }
        }
        log(buffer);
    }

    protected abstract void log(LoggingMessage log);
    
	@Override
    protected Logger getLogger() {
        return LOG;
    }
}
