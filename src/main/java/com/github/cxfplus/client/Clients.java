package com.github.cxfplus.client;

/**
 * 用于生成各种客户端
 * @author jiyi
 *
 */
public class Clients {
	
	/**
	 * 生成Ws代理
	 * @param url WebService所在位置
	 * @param clz 接口类
	 * @return
	 */
	public static <T> T createProxy(String url, Class<T> clz) {
		return WsClientFactoryImpl.getDefault().createProxy(url, clz);
	}
	
	/**
	 * 生成Rs代理
	 * @param url REST服务所在的位置
	 * @param clz 接口类
	 * @return 
	 */
	public static <T> T createJaxRsProxy(String url,Class<T> clz){
		return RsClientFactoryImpl.getDefault().createProxy(url, clz);
	}
}
