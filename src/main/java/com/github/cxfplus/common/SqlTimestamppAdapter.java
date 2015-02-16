package com.github.cxfplus.common;

import java.sql.Timestamp;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class SqlTimestamppAdapter extends XmlAdapter<java.util.Date,java.sql.Timestamp> {
	@Override
	public Date marshal(Timestamp arg0) throws Exception {
		return new Date(arg0.getTime());
	}

	@Override
	public Timestamp unmarshal(Date arg0) throws Exception {
		return new Timestamp(arg0.getTime());
	}
}
