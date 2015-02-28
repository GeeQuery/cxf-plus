package com.github.cxfplus.jaxws.jaxb.adapters;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.apache.cxf.common.util.StringUtils;

import com.alibaba.fastjson.JSON;

@XmlType(name = "JsonEntry")
@XmlAccessorType(XmlAccessType.FIELD)
public class JsonData {
	private String data;
	private String javaType;
	public JsonData(){}
	/**
	 * 构�
	 * @param type
	 * @param data
	 */
	public JsonData(String type,String data){
		this.javaType=type;
		this.data=data;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getJavaType() {
		return javaType;
	}
	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}
	public Object toObject(){
		if(StringUtils.isEmpty(data))return null;
		try {
			Class<?> clz = Class.forName(javaType);
			if(clz==String.class){
				return data;
			}else{
				return JSON.parseObject(data, clz);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 将复杂对象序列化
	 * @param object
	 * @return
	 */
	public static JsonData serialize(Object object) {
		JsonData data=new JsonData();
		if(object==null){
			return data;
		}
		if(object.getClass()==String.class){
			data.javaType=object.getClass().getName();
			data.data=(String)object;
		}else{
			data.javaType=object.getClass().getName();
			data.data=JSON.toJSONString(object);	
		}
		return data;
	}
	@Override
	public String toString() {
		return data;
	}
	
	
}
