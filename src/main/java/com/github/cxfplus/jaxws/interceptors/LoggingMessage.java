package com.github.cxfplus.jaxws.interceptors;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.cxfplus.core.util.XMLUtils;
import com.github.cxfplus.jmx.CXFPlus;

public final class LoggingMessage {
	public static final String ID_KEY = LoggingMessage.class.getName() + ".ID";
	private static final AtomicInteger ID = new AtomicInteger();
	private static Logger log = LoggerFactory.getLogger(LoggingMessage.class);
	private final String heading;
	private final StringBuilder address;
	private String contentType;
	private final StringBuilder encoding;
	private final StringBuilder httpMethod;
	private final StringBuilder header;
	private final StringBuilder message;
	private final StringBuilder payload;
	private final StringBuilder responseCode;
	private final String id;

	public LoggingMessage(String h, String i) {
		heading = h;
		id = i;

		address = new StringBuilder();
		encoding = new StringBuilder();
		httpMethod = new StringBuilder();
		header = new StringBuilder();
		message = new StringBuilder();
		payload = new StringBuilder();
		responseCode = new StringBuilder();
	}

	public String getId() {
		return id;
	}

	public static String nextId() {
		return Integer.toString(ID.incrementAndGet());
	}

	public StringBuilder getAddress() {
		return address;
	}

	public StringBuilder getEncoding() {
		return encoding;
	}

	public StringBuilder getHeader() {
		return header;
	}

	public StringBuilder getHttpMethod() {
		return httpMethod;
	}

	public void setContentType(String contType) {
		this.contentType = contType;
	}

	public StringBuilder getMessage() {
		return message;
	}

	public StringBuilder getPayload() {
		return payload;
	}

	public StringBuilder getResponseCode() {
		return responseCode;
	}

	public String toString() {
		StringBuilder buffer = new StringBuilder(512);
		buffer.append(heading);
		if (address.length() > 0) {
//			buffer.append("\nAddress: ");
			buffer.append('\n');
			if (httpMethod.length() > 0) {
				buffer.append(httpMethod).append(' ');
			}
			buffer.append(address);
			
		}
		if (responseCode.length() > 0) {
			buffer.append("\nResponse-Code: ");
			buffer.append(responseCode);
		}
//		if (encoding.length() > 0) {
//			buffer.append("\nEncoding: ");
//			buffer.append(encoding);
//		}
		buffer.append("\nContent-Type: ");
		buffer.append(contentType);
		
		if(CXFPlus.getSingleton().isTraceHttpHeader()){
			buffer.append("\nHeaders: ");
			buffer.append(header);
		}
		// if (message.length() > 0) {
		// buffer.append("\nMessages: ");
		// buffer.append(message);
		// }
		if (payload.length() > 0) {
			buffer.append("\nPayload:");
			appendPayload(buffer);
		}
		return buffer.toString();
	}

	private void appendPayload(StringBuilder buffer) {
		if (CXFPlus.getSingleton().isTracePrettyFormat() && contentType != null) {
			if (contentType.endsWith("json")) {
				appendJson(buffer, payload);
				return;
			} else if (contentType.endsWith("xml")) {
				appendXml(buffer, payload);
				return;
			}
		}
		buffer.append(payload);
	}

	private void appendXml(StringBuilder buffer, StringBuilder payload2) {
		try {
			Document doc=XMLUtils.parse(payload2.toString());
			XMLUtils.output(doc, new StringBuilderWriter(buffer),"UTF-8",true);
			if(buffer.charAt(buffer.length()-2)=='\r'){
				buffer.setLength(buffer.length()-2); //remove the last '\n'	
			}
		} catch (SAXException e) {
			log.error("Format XML error:", e);
			buffer.append(payload);
		} catch (IOException e) {
			log.error("Format XML error:", e);
			buffer.append(payload);
		}

	}

	private void appendJson(StringBuilder buffer, StringBuilder payload2) {
		try {
			String formated = JSON.toJSONString(JSON.parse(payload2.toString()), SerializerFeature.PrettyFormat);
			buffer.append(formated);
		} catch (Exception e) {
			log.error("Format JSON error:", e);
			buffer.append(payload);
		}
	}
}