package com.github.cxfplus.client.factory;

import jef.tools.StringUtils;

import com.github.cxfplus.client.ClientFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public abstract class AbstractProxyBeanFactory implements FactoryBean<Object>,InitializingBean{
private static Logger log=LoggerFactory.getLogger(JaxWsProxyBeanFactory.class);
	
	private String serviceInterface;
	private String url;
	//当url是相对路径时，才会启用此部分内容
	private String host;
	private String port;
	private String contextPath;
	//缓存下来的对象
	private Class<?> serviceClass;
	private Object service;
	private ClientFactory factory;
	
	public void setFactory(ClientFactory factory) {
		this.factory = factory;
	}

	public void setServiceInterface(String serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public Object getObject() throws Exception {
		if(service==null){
			ClientFactory factory=this.factory;
			log.info("Initializing remote bean: {}",url);
			if(!url.startsWith("http://")){
				String server=host;
				int port=StringUtils.toInt(this.port, 80);
				String context=this.contextPath;
				if(!context.startsWith("/"))context="/".concat(context);
				if(!url.startsWith("/"))url="/".concat(url);
				if(port==80){
					url=StringUtils.concat("http://",server,context,url);
				}else{
					url=StringUtils.concat("http://",server,":",String.valueOf(port),context,url);
				}
			}
			this.service=factory.createProxy(url, serviceClass);
		}
		log.debug("Return remote service bean {}" , service);
		return service;
	}

	public Class<?> getObjectType() {
		return serviceClass;
	}

	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() throws Exception {
		if(StringUtils.isEmpty(serviceInterface)){
			throw new IllegalArgumentException("Please set the 'serviceInterface'!");
		}
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if( loader==null ){
			loader = JaxWsProxyBeanFactory.class.getClassLoader();
		}
		this.serviceClass=loader.loadClass(serviceInterface);
		if(factory==null){
			this.factory=getFactory();
		}
	}

	protected abstract ClientFactory getFactory();

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}
}
