package com.github.cxfplus.support;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;

import jef.tools.Assert;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class SpringServletAnnotationLookup implements ServiceLookup {
	private ServletContext sc;
	private ApplicationContext applicationContext;
	private Class<? extends Annotation> lookupAnnotation;

	public SpringServletAnnotationLookup(ServletContext sc,Class<? extends Annotation> lookupInterface) {
		this.sc = sc;
		this.lookupAnnotation=lookupInterface;
	}

	public List<ServiceDefinition> getServices() {
		if(applicationContext==null){
			initContext();
		}
		//获取要发布的服务
		List<ServiceDefinition> result=new ArrayList<ServiceDefinition>();
		
		Map<String, ?> beans = applicationContext.getBeansWithAnnotation(lookupAnnotation);
		for (Entry<String,?> entry : beans.entrySet()){
			//服务名
			String name=entry.getKey();
			if(name.indexOf("#")>-1)name=null;
			//服务实例
			Object impl = entry.getValue();
			//查找服务接口
			List<Class<?>> seis=getSei(impl.getClass());
			for (Class<?> sei : seis) {
				result.add(new ServiceDefinition((seis.size()>1||name==null)?sei.getSimpleName():name, sei, impl));
			}
		}
		return result;
	}

	private List<Class<?>> getSei(Class<? extends Object> clz) {
		List<Class<?>> seis = new ArrayList<Class<?>>();
		addSei(clz, seis);
		return seis;
	}

	private void addSei(Class<?> clz,List<Class<?>> seis) {
		if(clz.getAnnotation(lookupAnnotation)!=null){
			seis.add(clz);
			return;
		}
		if(clz.getInterfaces()!=null){
			for(Class<?> intf: clz.getInterfaces()){
				addSei(intf,seis);
			}	
		}
	}

	private void initContext() {
		this.applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
		Assert.notNull(this.applicationContext, "Spring Context not found in servletContext...");
	}

	public boolean isRemoteMode() {
		if(applicationContext==null){
			initContext();
		}
		try {
			return applicationContext.getBean("_REMOTE_MODE")!=null;
		} catch (Exception e) {
		}
		return false;
	}

	public ServiceProcessor getWebServiceProcessor() {
		if(applicationContext==null){
			initContext();
		}
		try{
			return ((ServiceProcessor) applicationContext.getBean(ServiceProcessor.class));	
		}catch(Exception e){
			return null;
		}
	}
}
