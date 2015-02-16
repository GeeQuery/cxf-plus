package com.github.cxfplus.client;

import org.apache.cxf.endpoint.Client;

/**
 * 构造客户端的工具
 * 从分类上讲Java的WS客户端有以下种类
 * <ol>
 * <li>
 * 静态代码生成Stub类型           （流行，但不推荐）
 * 	    具体又分为框架原生Stub和JAX-WS Stub两种。
 *    1.1 框架原生Stub 使用框架的专用类来构造，比如axis2生成的stub就非常丑陋。
 *    1.2 JAX-WS Stub 后者可以用wsimport来生成，通过支持JAX-WS标准的SPI来提供。
 *    </li><li>
 * 使用接口类(+WSDL)生成Client。
 *    调用时类似反射的方式。但由于接口中引用的POJO都在本地环境中的关系，调用相对容易。
 *    </li><li> 
 * 使用接口类(+WSDL)生成动态代理 （流行，推荐）
 *    生成接口的动态代理实现。调用远程服务就和调用本地实现完全一样，调用最简单。
 *    具体使用又有两种方式一种是通过编程接口生成动态代理，一种是通过Spring Factory Bean生成。
 *    3.1 使用API通过编程接口生成动态代理
 *    3.2 通过Spring Factory Bean
 *    </li><li>
 * 纯动态客户端，依赖（WSDL）生成客户端，（不流行，一定条件下推荐）
 *    调用时类似反射的方式。1 无需接口，依赖最少。2容错性强，两边接口无需强行维持一致。
 *    但由于本地环境中没有自定义Bean，所以可能复杂的类型传输不了。
 *    如果要传输复杂bean,需要利用实现生成的stub的结构类。但已经生成stub这样做有何意义？意义在于参数的可动态。即入参的个数和顺序还是可以随意调节匹配的。
 *    目前CXFPlus不支持这种以WSDL为优先的客户端生成方式。不过前面也已经讲到纯动态客户端主要用于入参出参均为简单的基本类型的场合。不建议使用复杂类型进行传输
 *    
 *    </li>
 * </ol>   
 * @author jiyi
 *
 */
public interface ClientFactory {
	
	/**
	 * 生成动态代理的客户端（3.1）方式
	 * @param url WSDL文件地址
	 * @param clz 接口类
	 * @return 接口类的一个实现(动态代理)。
	 */
	<T> T createProxy(String url, Class<T> clz);
	
	/**
	 * 生成CXF的客户端对象。可以使用invoke方法调用其中的方法。
	 * @param url WSDL文件地址
	 * @param clz 接口类
	 * @return 调用客户端(Client)
	 * @see Client
	 */
	Client createClient(String url,Class<?> clz);
	
	/**
	 * 设置生成的客户端要不要调试（输出报文）
	 * 设置后，对下次生成客户端时生效。对已经生成了的客户端无效。
	 * @param trace
	 * @return this
	 */
	ClientFactory setTrace(boolean trace);
}
