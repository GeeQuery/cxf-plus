package com.github.cxfplus.jaxws.interceptors;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import jef.common.log.LogUtil;
import jef.tools.XMLUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TraceHandler implements SOAPHandler<SOAPMessageContext> {
	private boolean format = true;
	public TraceHandler(){}
	
	/**
	 */
	public TraceHandler(boolean format){
		this.format=format;
	}
	
	public Set<QName> getHeaders() {
		return null;
	}

	public boolean handleMessage(SOAPMessageContext arg0) {
		trace(arg0);
		return true;
	}

	public boolean handleFault(SOAPMessageContext arg0) {
		trace(arg0);
		return true;
	}

	public void close(MessageContext arg0) {
		System.out.println("close: " + arg0.toString());
	}

	private void trace(SOAPMessageContext messageContext) {
		Boolean outMessageIndicator =  (Boolean)messageContext.get (MessageContext.MESSAGE_OUTBOUND_PROPERTY); 
		if (outMessageIndicator.booleanValue())
			System.out.println("\nOutbound SOAP:");
		else {
			System.out.println("\nInbound SOAP:");
		}
		SOAPMessage message = messageContext.getMessage();
		try {
			if (format) {
				Document doc = XMLUtils.newDocument();
				Element ele = doc.createElement("soap:Envelope");
				doc.appendChild(ele);
				ele.setAttribute("xmlns:soap", "http://schemas.xmlsoap.org/soap/envelope/");
				if (message.getSOAPHeader() != null)
					ele.appendChild(doc.importNode(message.getSOAPHeader(), true));
				if (message.getSOAPBody() != null)
					ele.appendChild(doc.importNode(message.getSOAPBody(), true));
				XMLUtils.printNode(ele, System.out);
			} else {
				message.writeTo(System.out);
				System.out.println("");
			}
		} catch (Exception exp) {
			LogUtil.exception("Exception in TraceHandler:trace(messageContext) : ", exp);
		}
	}
}
