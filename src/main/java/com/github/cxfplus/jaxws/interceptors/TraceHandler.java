package com.github.cxfplus.jaxws.interceptors;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.github.cxfplus.core.util.XMLUtils;
import com.github.cxfplus.jmx.CXFPlus;

/**
 * JAX-WS标准的SOAP拦截器，用于输出XML报文信息
 * @author jiyi
 *
 */
public class TraceHandler implements SOAPHandler<SOAPMessageContext> {
	private static Logger log=LoggerFactory.getLogger(TraceHandler.class);

	private static TraceHandler instance=new TraceHandler();
	
	private TraceHandler(){
	}
	
	public static TraceHandler getSingleton(){
		return instance;
	}

	public Set<QName> getHeaders() {
		return null;
	}

	public boolean handleMessage(SOAPMessageContext arg0) {
		if(CXFPlus.getSingleton().isTrace()){
			trace(arg0);
		}
		return true;
	}

	public boolean handleFault(SOAPMessageContext arg0) {
		if(CXFPlus.getSingleton().isTrace()){
			trace(arg0);
		}
		return true;
	}

	public void close(MessageContext arg0) {
		log.debug("close: {}", arg0);
	}

	private void trace(SOAPMessageContext messageContext) {
		Boolean outMessageIndicator =  (Boolean)messageContext.get (MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		StringBuilderWriter output=new StringBuilderWriter(512);
		if (outMessageIndicator.booleanValue())
			output.append("\nOutbound SOAP:");
		else {
			output.append("\nInbound SOAP:");
		}
		SOAPMessage message = messageContext.getMessage();
		
		try {
			if (CXFPlus.getSingleton().isTracePrettyFormat()) {
				Document doc = XMLUtils.newDocument();
				Element ele = doc.createElement("soap:Envelope");
				doc.appendChild(ele);
				ele.setAttribute("xmlns:soap", "http://schemas.xmlsoap.org/soap/envelope/");
				if (message.getSOAPHeader() != null)
					ele.appendChild(doc.importNode(message.getSOAPHeader(), true));
				if (message.getSOAPBody() != null)
					ele.appendChild(doc.importNode(message.getSOAPBody(), true));
				XMLUtils.output(ele, output, null, true);
			} else {
				message.writeTo(new WriterOutputStream(output));
				output.write("\r\n");
			}
			log.info(output.toString());
		} catch (Exception exp) {
			log.error("Exception in TraceHandler:trace(messageContext) : ", exp);
		}
	}
}
