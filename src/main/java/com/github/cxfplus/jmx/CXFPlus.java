package com.github.cxfplus.jmx;

import org.apache.cxf.common.util.StringUtils;

public class CXFPlus implements CXFPlusMBean {
	private static CXFPlus instance = new CXFPlus();

	public static CXFPlus getSingleton() {
		return instance;
	}

	private volatile boolean trace;
	private volatile boolean tracePrettyFormat;
	private volatile boolean traceHttpHeader;
	
	private CXFPlus() {
		String config=System.getProperty("cxfplus.trace");
		boolean notFlag=StringUtils.isEmpty(config) || "0".equals(config) || "OFF".equalsIgnoreCase(config);
		trace=!notFlag;
			
		config=System.getProperty("cxfplus.prettyFormat");
		notFlag="0".equals(config) || "OFF".equalsIgnoreCase(config);
		tracePrettyFormat=!notFlag;
	}

	public boolean isTrace() {
		return trace;
	}

	public void setTrace(boolean trace) {
		this.trace = trace;
	}

	public boolean isTracePrettyFormat() {
		return tracePrettyFormat;
	}

	public void setTracePrettyFormat(boolean prettyFormat) {
		this.tracePrettyFormat = prettyFormat;
	}

	public boolean isTraceHttpHeader() {
		return traceHttpHeader;
	}

	public void setTraceHttpHeader(boolean traceHttpHeader) {
		this.traceHttpHeader = traceHttpHeader;
	}
}
