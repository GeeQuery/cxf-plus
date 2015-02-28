package com.github.cxfplus.common;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.github.cxfplus.core.container.SimpleMap;

/**
 * 使SimpleMap对象支持JAXB序列化和反序列化
 * @author Administrator
 *
 * @param <K>
 * @param <V>
 */
public class SimpleMapAdapter<K, V> extends XmlAdapter<SimpleMap<K, V>,Map<K,V>> {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public SimpleMap<K, V> marshal(Map<K, V> arg0) throws Exception {
		if(arg0 instanceof SimpleMap){
			return (SimpleMap)arg0;
		}
		SimpleMap<K,V> result=new SimpleMap<K,V>();
		if(arg0!=null)
			result.putAll(arg0);
		return result;
	}

	@Override
	public Map<K, V> unmarshal(SimpleMap<K, V> arg0) throws Exception {
		Map<K, V> map = new LinkedHashMap<K, V>();
		map.putAll(arg0);
		return map;
	}
}
