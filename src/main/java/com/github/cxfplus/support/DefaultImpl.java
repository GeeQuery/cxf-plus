package com.github.cxfplus.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单的WsFactory实现。
 * @author jiyi
 *
 */
public class DefaultImpl implements ServiceProcessor {
	private static DefaultImpl instance = new DefaultImpl();
	private final Map<String, ServiceDefinition> data = new HashMap<String, ServiceDefinition>();
	private boolean trace;

	public static DefaultImpl getInstance() {
		return instance;
	}
	
	public static DefaultImpl getInstance(boolean trace) {
		instance.trace=trace;
		return instance;
	}

	public ServiceDefinition processServiceDef(ServiceDefinition def) {
		return def;
	}

	public Map<String, ServiceDefinition> getRegistedService() {
		return Collections.unmodifiableMap(this.data);
	}
	
	public void register(String name, Object service, Class<?> serviceClass) {
		this.data.put(name, new ServiceDefinition(name,serviceClass,service));
	}

	public void init() {
	}
}
