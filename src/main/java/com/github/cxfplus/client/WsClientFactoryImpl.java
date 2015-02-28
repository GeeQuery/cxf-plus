package com.github.cxfplus.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientFactoryBean;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsClientFactoryBean;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import com.github.cxfplus.jaxws.CXFPlusClientFactoryBean;
import com.github.cxfplus.jaxws.interceptors.LoggingInInterceptor;
import com.github.cxfplus.jaxws.interceptors.LoggingOutInterceptor;
import com.github.cxfplus.jaxws.interceptors.TraceHandler;
import com.github.cxfplus.service.factory.CXFPlusServiceBean;

public class WsClientFactoryImpl implements ClientFactory {
	private boolean isJaxws;
	private boolean isCxfPlus;
	private long connectTimeout;
	private long receiveTimeout;

	private static ClientFactory DEFAULT = new WsClientFactoryImpl();

	public static ClientFactory getDefault() {
		return DEFAULT;
	}

	private WsClientFactoryImpl() {
		this(true, true);
	}

	public WsClientFactoryImpl(boolean isjaxws, boolean isCxfPlus) {
		this.isJaxws = isjaxws;
		this.isCxfPlus = isCxfPlus;
	}

	public <T> T createProxy(String url, Class<T> clz) {
		ClientProxyFactoryBean factoryBean;
		if (isJaxws && isCxfPlus) {
			factoryBean = new JaxWsProxyFactoryBean(new CXFPlusClientFactoryBean());
		} else if (isJaxws) {
			factoryBean = new JaxWsProxyFactoryBean();
		} else if (isCxfPlus) {
			factoryBean = new ClientProxyFactoryBean(new ClientFactoryBean(new CXFPlusServiceBean()));
		} else {
			factoryBean = new ClientProxyFactoryBean();
		}

//		if (factoryBean instanceof JaxWsProxyFactoryBean) {
//			((JaxWsProxyFactoryBean) factoryBean).getHandlers().add(TraceHandler.getSingleton());
//		} else {
			factoryBean.getInInterceptors().add(new LoggingInInterceptor());
			factoryBean.getOutInterceptors().add(new LoggingOutInterceptor());
//		}

		Map<String, Object> prop = factoryBean.getProperties();
		if (prop == null) {
			prop = new HashMap<String, Object>();
			factoryBean.setProperties(prop);
		}
		prop.put("set-jaxb-validation-event-handler", false);
		factoryBean.setAddress(url);
		factoryBean.setServiceClass(clz);
		@SuppressWarnings("unchecked")
		T ref = (T) factoryBean.create();
		if (connectTimeout > 0 || receiveTimeout > 0) {
			Client proxy = ClientProxy.getClient(ref);
			HTTPConduit conduit = (HTTPConduit) proxy.getConduit();
			HTTPClientPolicy policy = new HTTPClientPolicy();
			policy.setConnectionTimeout(connectTimeout);
			policy.setReceiveTimeout(receiveTimeout);
			conduit.setClient(policy);
		}
		return ref;
	}

	public Client createClient(String url, Class<?> clz) {
		ClientFactoryBean factoryBean;
		if (isJaxws && isCxfPlus) {
			factoryBean = new CXFPlusClientFactoryBean();
		} else if (isJaxws) {
			factoryBean = new JaxWsClientFactoryBean();
		} else if (isCxfPlus) {
			factoryBean = new ClientFactoryBean(new CXFPlusServiceBean());
		} else {
			factoryBean = new ClientFactoryBean();
		}
		factoryBean.getInInterceptors().add(new LoggingInInterceptor());
		factoryBean.getOutInterceptors().add(new LoggingOutInterceptor());
		factoryBean.setAddress(url);
		factoryBean.setServiceClass(clz);
		return factoryBean.create();
	}
}
