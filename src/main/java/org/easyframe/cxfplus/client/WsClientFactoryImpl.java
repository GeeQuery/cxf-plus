package org.easyframe.cxfplus.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientFactoryBean;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.CXFPlusClientFactoryBean;
import org.apache.cxf.jaxws.JaxWsClientFactoryBean;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.service.factory.CXFPlusServiceBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.easyframe.jaxws.interceptors.TraceHandler;

public class WsClientFactoryImpl implements ClientFactory{
	private boolean isJaxws;
	private boolean isCxfPlus;
	private long connectTimeout;
	private long receiveTimeout;
	private boolean trace;
	
	private static ClientFactory DEFAULT= new WsClientFactoryImpl();
	public static ClientFactory getDefault(){
		return DEFAULT;
	}
	
	private WsClientFactoryImpl() {
		this(true,true);
	}
	
	public WsClientFactoryImpl(boolean isjaxws, boolean isCxfPlus) {
		this.isJaxws=isjaxws;
		this.isCxfPlus=isCxfPlus;
	}
	
	public WsClientFactoryImpl setTrace(boolean trace){
		this.trace=trace;
		return this;
	}
	
	public <T> T createProxy(String url, Class<T> clz) {
		ClientProxyFactoryBean factoryBean;
		if(isJaxws && isCxfPlus){
			factoryBean =  new JaxWsProxyFactoryBean(new CXFPlusClientFactoryBean());
		}else if(isJaxws){
			factoryBean =  new JaxWsProxyFactoryBean();
		}else if(isCxfPlus){
			factoryBean = new ClientProxyFactoryBean(new ClientFactoryBean(new CXFPlusServiceBean()));
		}else{
			factoryBean = new ClientProxyFactoryBean();
		}
		if(trace){
			if(factoryBean instanceof JaxWsProxyFactoryBean){
				((JaxWsProxyFactoryBean) factoryBean).getHandlers().add(new TraceHandler());
			}else{
				factoryBean.getInInterceptors().add(new LoggingInInterceptor());
				factoryBean.getOutInterceptors().add(new LoggingOutInterceptor());
			}
		}
		
		Map<String, Object> prop=factoryBean.getProperties();
		if(prop==null){
			prop=new HashMap<String,Object>();
			factoryBean.setProperties(prop);
		}
		prop.put("set-jaxb-validation-event-handler", false);
		factoryBean.setAddress(url);
		factoryBean.setServiceClass(clz);
		@SuppressWarnings("unchecked")
		T ref = (T) factoryBean.create();
		if(connectTimeout>0 || receiveTimeout>0){
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
		if(isJaxws && isCxfPlus){
			factoryBean =  new CXFPlusClientFactoryBean();
		}else if(isJaxws){
			factoryBean =  new JaxWsClientFactoryBean();
		}else if(isCxfPlus){
			factoryBean = new ClientFactoryBean(new CXFPlusServiceBean());
		}else{
			factoryBean = new ClientFactoryBean();
		}
		if(trace){
			factoryBean.getInInterceptors().add(new LoggingInInterceptor());
			factoryBean.getOutInterceptors().add(new LoggingOutInterceptor());
		}
		factoryBean.setAddress(url);
		factoryBean.setServiceClass(clz);
		return factoryBean.create();
	}
}
