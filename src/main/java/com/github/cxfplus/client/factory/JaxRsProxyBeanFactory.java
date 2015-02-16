package com.github.cxfplus.client.factory;

import com.github.cxfplus.client.ClientFactory;
import com.github.cxfplus.client.RsClientFactoryImpl;



public class JaxRsProxyBeanFactory extends AbstractProxyBeanFactory{
	@Override
	protected ClientFactory getFactory() {
		return RsClientFactoryImpl.getDefault();
	}
}
