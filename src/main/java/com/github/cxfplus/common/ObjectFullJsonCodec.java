package com.github.cxfplus.common;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.easyframe.fastjson.JSON;
import org.easyframe.fastjson.serializer.SerializerFeature;

/**
 * 使用Alibaba的FastJson的权序列化解决方案，通过在json中写入类名的方式，实现完整信息的序列化和反序列化
 * @author jiyi
 *
 */
public class ObjectFullJsonCodec extends XmlAdapter<String,Object>{
	@Override
	public Object unmarshal(String v) throws Exception {
		Object o = JSON.parse(v); 
		return o;
	}

	@Override
	public String marshal(Object v) throws Exception {
		String text = JSON.toJSONString(v, SerializerFeature.WriteClassName); 
		return text;
	}

}
