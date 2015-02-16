package com.github.cxfplus.common;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * 使java.sql.Timestamp支持JAXB序列化和反序列化
 * @author Administrator
 *
 */
public class SqlDateAdapter extends XmlAdapter<java.util.Date,java.sql.Date> {
	@Override
	public Date marshal(java.sql.Date arg0) throws Exception {
		return new Date(arg0.getTime());
	}

	@Override
	public java.sql.Date unmarshal(Date arg0) throws Exception {
		return new java.sql.Date(arg0.getTime());
	}
}
