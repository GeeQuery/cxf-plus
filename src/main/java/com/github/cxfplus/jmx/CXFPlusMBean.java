package com.github.cxfplus.jmx;

/**
 * 对运行中的服务端进行管理JMX接口
 * @author jiyi
 */
public interface CXFPlusMBean {
	/**
	 * 是否开启报文跟踪 
	 * @return 是否打印报文
	 */
	boolean isTrace();

	/**
	 * 设置是否跟踪报文
	 * @param trace 是否打印报文
	 */
	void setTrace(boolean trace);
	
	/**
	 * 当开启跟踪报文后，是否对报文重新格式化以方便阅读。<br>
	 * 注意开启后性能会明显下降，一般供调试用。
	 * @return 是否格式化
	 */
	boolean isTracePrettyFormat();
	
	/**
	 * 当开启跟踪报文后，是否对报文重新格式化以方便阅读。<br>
	 * 注意开启后性能会明显下降，一般供调试用。
	 * @param prettyFormat 是否格式化
	 */
	void setTracePrettyFormat(boolean prettyFormat);
	
	boolean isTraceHttpHeader();
	void setTraceHttpHeader(boolean traceHttpHeader);
	
}
