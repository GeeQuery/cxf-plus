package com.github.cxfplus.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import jef.tools.Assert;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class SpringServletServcieLookup implements ServiceLookup {
	private ServletContext sc;
	private ApplicationContext applicationContext;
	private Class<?> lookupInterface;

	public SpringServletServcieLookup(ServletContext sc, Class<?> lookupInterface) {
		this.sc = sc;
		this.lookupInterface = lookupInterface;
	}

	public List<ServiceDefinition> getServices() {
		if (applicationContext == null) {
			initContext();
		}
		// 获取要发布的服务
		List<ServiceDefinition> result = new ArrayList<ServiceDefinition>();

		Map<String, ?> beans = applicationContext.getBeansOfType(lookupInterface);
		for (String key : beans.keySet()) {
			Object impl = beans.get(key);

			List<Class<?>> seis = new ArrayList<Class<?>>();
			for (Class<?> clz : impl.getClass().getInterfaces()) {
				if (lookupInterface.isAssignableFrom(clz)) {
					seis.add(clz);
				}
			}
			for (Class<?> sei : seis) {
				result.add(new ServiceDefinition(seis.size()>1?sei.getSimpleName():key, sei, impl));
			}
		}
		return result;
	}

	private void initContext() {
		this.applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
		Assert.notNull(this.applicationContext, "Spring Context not found in servletContext...");
	}

	public boolean isRemoteMode() {
		if (applicationContext == null) {
			initContext();
		}
		try {
			return applicationContext.getBean("_REMOTE_MODE") != null;
		} catch (Exception e) {
		}
		return false;
	}

	public ServiceProcessor getWebServiceProcessor() {
		if (applicationContext == null) {
			initContext();
		}
		try {
			return ((ServiceProcessor) applicationContext.getBean(ServiceProcessor.class));
		} catch (Exception e) {
			return null;
		}
	}
}
