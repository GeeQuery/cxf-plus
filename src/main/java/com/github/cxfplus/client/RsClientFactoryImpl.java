package com.github.cxfplus.client;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import com.github.cxfplus.jaxrs.FastJSONProvider;

public class RsClientFactoryImpl implements ClientFactory{

	private static final ClientFactory DEFAULT=new RsClientFactoryImpl();
	
	public static ClientFactory getDefault(){
		return DEFAULT;
	}
	
	public RsClientFactoryImpl(){
	}
	
	private boolean trace;
	
	public boolean isTrace() {
		return trace;
	}

	@SuppressWarnings("unchecked")
	public <T> T createProxy(String url, Class<T> clz) {
		JAXRSClientFactoryBean proxyFactoryBean = new JAXRSClientFactoryBean();
		proxyFactoryBean.setAddress(url);
		proxyFactoryBean.setServiceClass(clz);
		proxyFactoryBean.setProvider(new FastJSONProvider(true,false));
		if (trace) {
			proxyFactoryBean.getInInterceptors().add(new LoggingInInterceptor());
			proxyFactoryBean.getOutInterceptors().add(new LoggingOutInterceptor());
		}
		T client = (T) proxyFactoryBean.create();
		return client;
	}

	public Client createClient(String url, Class<?> clz) {
		throw new UnsupportedOperationException();
	}

	public RsClientFactoryImpl setTrace(boolean trace) {
		this.trace=trace;
		return this;
	}
}
