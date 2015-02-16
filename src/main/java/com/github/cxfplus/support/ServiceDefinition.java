package com.github.cxfplus.support;

/**
 * WebService的发布描述信息，包括一个服务接口和一个服务实例
 * 可以用
 * <ul> {@link #getServiceBean()}</ul>
 *  <ul>{@link #serviceClass}</ul>获得需要的参数
 * @author jiyi
 *
 */
public class ServiceDefinition {
	private String name;
	private Class<?> serviceClass;
	private Object serviceBean;
	private String path;
	
	/**
	 * 返回服务名。服务名同时也是服务的HTTP URL的一部分。比如
	 * 服务名为 fooService的 服务，发布到 http://host:port/context/ws/fooService的URL上。
	 * @return 服务名
	 */
	public String getName() {
		if(name==null)return serviceClass.getName();
		return name;
	}

	/**
	 * 设置服务名
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 构造
	 * @param name 服务名
	 * @param sei  服务接口
	 * @param serviceInstance 服务实现对象
	 */
	public ServiceDefinition(String name,Class<?> sei,Object serviceInstance) {
		this.name=name;
		this.serviceClass = sei;
		this.serviceBean = serviceInstance;
	}

	/**
	 * 空构造
	 */
	public ServiceDefinition() {
	}

	/**
	 * 获得服务接口类
	 * @return 服务接口类
	 */
	public Class<?> getServiceClass() {
		return this.serviceClass;
	}

	/**
	 * 获得服务实现对象
	 * @return 服务实现对象
	 */
	public Object getServiceBean() {
		return this.serviceBean;
	}

	/**
	 * 获得服务的路径
	 * @return 服务的路径
	 */
	public String getPath() {
		if(path==null){
			 return "/" + getName();
		}
		return path;
	}

	/**
	 * 设置服务路径，服务路径为null时，即使用/+服务名 
	 * @param path
	 */
	public void setPath(String path) {
		this.path = path;
	}
}
