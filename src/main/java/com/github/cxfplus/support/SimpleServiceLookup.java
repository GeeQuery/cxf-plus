package com.github.cxfplus.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleServiceLookup implements ServiceLookup {
	final private List<ServiceDefinition> data=new ArrayList<ServiceDefinition>();

	public List<ServiceDefinition> getServices() {
		return Collections.unmodifiableList(data);
	}
	
	/**
	 * 添加服务
	 * @param bean
	 */
	public void addService(Object bean){
		Class<?> clz=bean.getClass();
		addService(clz.getSimpleName(),clz,bean);
	}

	/**
	 * 添加服务
	 * @param clz
	 * @param bean
	 */
	public void addService(Class<?> clz, Object bean) {
		addService(clz.getSimpleName(),clz,bean);
	}
	
	/**
	 * 添加服务
	 * @param name
	 * @param clz
	 * @param bean
	 */
	public void addService(String name, Class<?> clz, Object bean) {
		data.add(new ServiceDefinition(name,clz,bean));
	}
}
