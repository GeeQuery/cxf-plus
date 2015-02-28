package com.github.cxfplus.com.sun.xml.bind.v2.model;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.cxfplus.com.sun.xml.bind.v2.model.annotation.XmlJavaTypeAdapterQuick;
import com.github.cxfplus.core.reflect.ClassEx;
import com.github.cxfplus.core.reflect.GenericUtils;
import com.github.cxfplus.core.util.StringUtils;

public class JefAdapters {
	private static Map<Class, XmlJavaTypeAdapter> jefTypeAdapters;
	public static Set<String> jefQualified;
	private static Logger log=LoggerFactory.getLogger("com.github.cxfplus.com.sun.xml.bind.v2.model.JefAdapters");
	static{
		try {
			initCustomTypes();
		} catch (Exception e) {
			log.error("class init error!",e);
		}
	}
	
	private static BufferedReader getReader(URL file, String charSet) {
		if (file == null)
			return null;
		try {
			InputStreamReader isr;
			if(charSet==null){
				isr=new InputStreamReader(file.openStream());
			}else{
				isr=new InputStreamReader(file.openStream(), charSet);
			}
			return new BufferedReader(isr);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Map<String, String> loadProperties(BufferedReader in) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		loadProperties(in, result);
		return result;
	}
	
	static final void loadProperties(BufferedReader in, Map<String, String> map) {
		if (in == null)
			return;
		try {
			String s;
			while ((s = in.readLine()) != null) {
				if (StringUtils.isBlank(s) || s.startsWith("#"))
					continue;
				int index = s.indexOf("=");
				String key;
				String value;
				if (index > -1) {
					key = s.substring(0, index).trim();
					value = s.substring(index + 1).trim();
				} else {
					key = s.trim();
					value = "";
				}
				if (StringUtils.isEmpty(key))
					continue;
				while (value.endsWith("\\") && (s = in.readLine()) != null) {
					value = value.concat(s);
				}
				map.put(key, value);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			closeQuietly(in);
		}
	}
	/**
	 * 关闭指定的对象，不会抛出异常
	 * 
	 * @param input
	 */
	public static void closeQuietly(Closeable input) {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				log.error("",e);
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static void initCustomTypes() throws IOException {
		if (jefTypeAdapters == null) {
			jefTypeAdapters = new HashMap<Class, XmlJavaTypeAdapter>();
			Enumeration<URL> in = JefAdapters.class.getClassLoader().getResources("jaxb-types.properties");
			for (;in.hasMoreElements();) {
				URL url = in.nextElement();
				Map<String, String> map = loadProperties(getReader(url, null));
				for (Entry<String, String> e : map.entrySet()) {
					Class c = ClassEx.getClass(e.getKey());
					Class adapter = ClassEx.getClass(e.getValue());
					if (c == null || adapter == null){
						log.warn(e.toString()+" is a invalid JAXB type mapping. Class not found.");
						continue;
					}
					if (!XmlAdapter.class.isAssignableFrom(adapter)){
						log.warn(e.toString()+" is a invalid JAXB type mapping.");
						continue;
					}
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
			Enumeration<URL> in = JefAdapters.class.getClassLoader().getResources("jaxb-qualified.properties");
			for (;in.hasMoreElements();) {
				URL url = in.nextElement();
				BufferedReader reader = getReader(url, "US-ASCII");
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
