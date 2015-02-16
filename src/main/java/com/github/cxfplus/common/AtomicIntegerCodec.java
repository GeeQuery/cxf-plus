package com.github.cxfplus.common;

import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class AtomicIntegerCodec extends XmlAdapter<String,AtomicInteger>{

	@Override
	public AtomicInteger unmarshal(String v) throws Exception {
		return v==null?null:new AtomicInteger(Integer.parseInt(v));
	}

	@Override
	public String marshal(AtomicInteger v) throws Exception {
		return v==null?null:String.valueOf(v.get());
	}
}
