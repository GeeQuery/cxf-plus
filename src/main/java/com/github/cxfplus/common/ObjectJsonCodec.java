package com.github.cxfplus.common;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.easyframe.fastjson.JSON;

/**
 * 标准Json格式的的序列化和反序列化
 * @author jiyi
 *
 */
public class ObjectJsonCodec extends XmlAdapter<String,Object>{
	@Override
	public Object unmarshal(String v) throws Exception {
		Object o = JSON.parse(v); 
		return o;
	}

	@Override
	public String marshal(Object v) throws Exception {
		String text = JSON.toJSONString(v); 
		return text;
	}

}
