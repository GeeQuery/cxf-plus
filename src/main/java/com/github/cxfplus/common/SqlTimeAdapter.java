package com.github.cxfplus.common;

import java.sql.Time;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *  使java.sql.Time支持JAXB序列化和反序列化
 * @author Administrator
 *
 */
public class SqlTimeAdapter  extends XmlAdapter<java.util.Date,java.sql.Time> {
	@Override
	public Date marshal(Time arg0) throws Exception {
		return new Date(arg0.getTime());
	}

	@Override
	public Time unmarshal(Date arg0) throws Exception {
		return new java.sql.Time(arg0.getTime());
	}
}
