package com.github.cxfplus.support;


/**
 * 这个接口可以定制WebService类的发布行为。
 * 要定制发布行为，你可以实现这个接口
 * 
 * @author jiyi
 *
 */
public interface ServiceProcessor {
	
	/**
	 * 实现这个方法后，可以体检要发布的WebService对象和接口
	 * @param def  服务定义
	 * @return  实际将要发布的Web服务接口类和对象实例。这两个值包装在一个{@link ServiceDefinition} 对象中。
	 */
	ServiceDefinition processServiceDef(ServiceDefinition def);
}
