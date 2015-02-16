package com.github.cxfplus.support;

import java.util.List;

/**
 * 服务查找器
 * @author jiyi
 *
 */
public interface ServiceLookup{
	
	/**
	 * 提供各种服务
	 * @return
	 */
	List<ServiceDefinition> getServices();
}
