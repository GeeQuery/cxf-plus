package com.github.cxfplus.common;

import java.util.concurrent.atomic.AtomicLong;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class AtomicLongCodec extends XmlAdapter<String,AtomicLong>{

	@Override
	public AtomicLong unmarshal(String v) throws Exception {
		return v==null?null:new AtomicLong(Long.parseLong(v));
	}

	@Override
	public String marshal(AtomicLong v) throws Exception {
		return v==null?null:String.valueOf(v.get());
	}
}
