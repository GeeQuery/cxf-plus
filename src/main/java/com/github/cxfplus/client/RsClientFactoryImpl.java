package com.github.cxfplus.client;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;

import com.github.cxfplus.jaxrs.FastJSONProvider;
import com.github.cxfplus.jaxws.interceptors.LoggingInInterceptor;
import com.github.cxfplus.jaxws.interceptors.LoggingOutInterceptor;

public class RsClientFactoryImpl implements ClientFactory {

	private static final ClientFactory DEFAULT = new RsClientFactoryImpl();

	public static ClientFactory getDefault() {
		return DEFAULT;
	}

	public RsClientFactoryImpl() {
	}

	@SuppressWarnings("unchecked")
	public <T> T createProxy(String url, Class<T> clz) {
		JAXRSClientFactoryBean proxyFactoryBean = new JAXRSClientFactoryBean();
		proxyFactoryBean.setAddress(url);
		proxyFactoryBean.setServiceClass(clz);
		proxyFactoryBean.setProvider(new FastJSONProvider(true, false));
		proxyFactoryBean.getInInterceptors().add(new LoggingInInterceptor());
		proxyFactoryBean.getOutInterceptors().add(new LoggingOutInterceptor());
		T client = (T) proxyFactoryBean.create();
		return client;
	}

	public Client createClient(String url, Class<?> clz) {
		throw new UnsupportedOperationException();
	}
}
