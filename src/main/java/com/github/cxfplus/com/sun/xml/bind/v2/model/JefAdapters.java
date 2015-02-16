package com.github.cxfplus.com.sun.xml.bind.v2.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.github.cxfplus.com.sun.xml.bind.v2.model.annotation.XmlJavaTypeAdapterQuick;
import jef.tools.ArrayUtils;
import jef.tools.IOUtils;
import jef.tools.reflect.ClassEx;
import jef.tools.reflect.GenericUtils;

public class JefAdapters {
	private static Map<Class, XmlJavaTypeAdapter> jefTypeAdapters;
	public static Set<String> jefQualified;
	
	static{
		try {
			initCustomTypes();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static void initCustomTypes() throws IOException {
		if (jefTypeAdapters == null) {
			jefTypeAdapters = new HashMap<Class, XmlJavaTypeAdapter>();
			URL[] in = ArrayUtils.toArray(JefAdapters.class.getClassLoader().getResources("jaxb-types.properties"), URL.class);
			for (URL url : in) {
				Map<String, String> map = IOUtils.loadProperties(IOUtils.getReader(url, null));
				for (Entry<String, String> e : map.entrySet()) {
					Class c = ClassEx.getClass(e.getKey());
					Class adapter = ClassEx.getClass(e.getValue());
					if (c == null || adapter == null)
						continue;
					if (!XmlAdapter.class.isAssignableFrom(adapter))
						continue;
					XmlJavaTypeAdapter quick = new XmlJavaTypeAdapterQuick(c, adapter.asSubclass(XmlAdapter.class));
					jefTypeAdapters.put(c, quick);
				}
			}

			{//如果用户没有配置，那么增加默认的Object的序列化支持办法
//				if(!jefTypeAdapters.containsKey(Object.class)){
//					Class adapter = null;
//					try {
//						if ("false".equals(System.getProperty("jef.ws.fulljson"))) {
//							adapter = Class.forName("jef.common.ObjectJsonCodec");	//纯正的Json序列化方案
//						} else {
//							adapter = Class.forName("jef.common.ObjectFullJsonCodec");//默认使用全序列化解决方案
//						}
//						XmlJavaTypeAdapter quick = new XmlJavaTypeAdapterQuick(Object.class, adapter.asSubclass(XmlAdapter.class));
//						jefTypeAdapters.put(Object.class, quick);
//					} catch (ClassNotFoundException e) {
//					}
//				}
			}
		}
		if (jefQualified == null) {
			Set<String> result = new HashSet<String>();
			URL[] in = ArrayUtils.toArray(JefAdapters.class.getClassLoader().getResources("jaxb-qualified.properties"), URL.class);
			for (URL url : in) {
				BufferedReader reader = IOUtils.getReader(url, "US-ASCII");
				String line;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (line.length() == 0)
						continue;
					if (line.startsWith("#"))
						continue;
					result.add(line);
				}
			}
			jefQualified = result;
		}
	}

	public static XmlJavaTypeAdapter get(Type type) {
		return jefTypeAdapters.get(GenericUtils.getRawClass((Type) type));
	}
}
