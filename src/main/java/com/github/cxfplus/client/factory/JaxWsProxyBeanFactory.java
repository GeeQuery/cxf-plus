package com.github.cxfplus.client.factory;

import com.github.cxfplus.client.ClientFactory;
import com.github.cxfplus.client.WsClientFactoryImpl;



public class JaxWsProxyBeanFactory extends AbstractProxyBeanFactory{
	@Override
	protected ClientFactory getFactory() {
		return WsClientFactoryImpl.getDefault();
	}
}
