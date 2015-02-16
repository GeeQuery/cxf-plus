package com.github.cxfplus.com.sun.xml.bind.v2.model.annotation;

import java.lang.annotation.Annotation;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@SuppressWarnings("rawtypes")
public class XmlJavaTypeAdapterQuick implements javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter{
	private Class type;
	private Class<? extends XmlAdapter> value;
	
	public XmlJavaTypeAdapterQuick(Class<?> type,Class<? extends XmlAdapter> value){
		this.type=type;
		this.value=value;
	}
	public Class<? extends Annotation> annotationType() {
		return XmlJavaTypeAdapter.class;
	}

	public Class<? extends XmlAdapter> value() {
		return value;
	}
	public Class type() {
		return type;
	}
}
