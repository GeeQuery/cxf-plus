package com.github.cxfplus.client;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.endpoint.EndpointImplFactory;
import org.apache.cxf.endpoint.dynamic.DynamicClientFactory;
import org.apache.cxf.jaxws.support.JaxWsEndpointImplFactory;

public class CxfplusDynamicClientFactory extends DynamicClientFactory {

	protected CxfplusDynamicClientFactory(Bus bus) {
		super(bus);
	}

	@Override
	protected EndpointImplFactory getEndpointImplFactory() {
		return JaxWsEndpointImplFactory.getSingleton();
	}

	protected boolean allowWrapperOps() {
		return true;
	}

	/**
	 * Create a new instance using a specific <tt>Bus</tt>.
	 * 
	 * @param b
	 *            the <tt>Bus</tt> to use in subsequent operations with the
	 *            instance
	 * @return the new instance
	 */
	public static CxfplusDynamicClientFactory newInstance(Bus b) {
		return new CxfplusDynamicClientFactory(b);
	}

	/**
	 * Create a new instance using a default <tt>Bus</tt>.
	 * 
	 * @return the new instance
	 * @see CXFBusFactory#getDefaultBus()
	 */
	public static CxfplusDynamicClientFactory newInstance() {
		Bus bus = CXFBusFactory.getThreadDefaultBus();
		return new CxfplusDynamicClientFactory(bus);
	}
}
