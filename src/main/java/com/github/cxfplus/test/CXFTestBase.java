package com.github.cxfplus.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.wsdl.WSDLException;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.test.AbstractCXFTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.github.cxfplus.client.ClientFactory;
import com.github.cxfplus.client.WsClientFactoryImpl;
import com.github.cxfplus.core.reflect.BeanUtils;
import com.github.cxfplus.core.reflect.MethodEx;
import com.github.cxfplus.core.util.StringUtils;
import com.github.cxfplus.jaxws.interceptors.TraceHandler;
import com.github.cxfplus.jaxws.support.CXFPlusServiceFactoryBean;
import com.github.cxfplus.service.factory.CXFPlusServiceBean;
import com.github.cxfplus.support.DefaultImpl;
import com.github.cxfplus.support.ServiceDefinition;
import com.github.cxfplus.support.ServiceProcessor;


/**
 * 这个类用来辅助编写单元测试
 * @author jiyi
 *
 */
public abstract class CXFTestBase extends AbstractCXFTest {
	//Spring的context
	protected static ApplicationContext context;
	private ClientFactory jaxwsPlus=WsClientFactoryImpl.getDefault();
	private ClientFactory rpcPlus=new WsClientFactoryImpl(false,true);
	
	/**
	 * 可被子类覆盖，表示是否需要打印出soap报文
	 * @return
	 */
	protected boolean printTrace(){
		return true;
	}
	

	/**
	 *返回WebServiceFactory。可覆盖，以使用自定义的WebServiceFactory
	 * @see com.googlecode.jef.ws.WebServiceFactory
	 * @return ff
	 *  
	 */
	protected ServiceProcessor getFactory(){
		return new DefaultImpl();
	}

	/**
	 * Spring初始化。用于初始化SpringContext
	 * @param contextFiles
	 */
	protected static void init(String[] contextFiles) {
		System.out.println("Starting context:" + StringUtils.join(contextFiles, ","));
		context = new ClassPathXmlApplicationContext(contextFiles);
	}

	/**
	 * 将指定的接口发布为服务，然后生成WSDL文档(JaxWs模式)
	 * @param intf
	 * @return
	 * @throws WSDLException
	 */
	protected Document generateJaxWsWSDL(Class<?> intf) throws WSDLException{
		Assert.isTrue(Boolean.valueOf(intf.isInterface()), "The input class " + intf.getClass() + " is not an interface!");
		Server server = createLocalJaxWsService(intf, new Object());
		try{
			return super.getWSDLDocument(server);
		}finally{
			server.stop();
			server.destroy();
		}
	}
	
	
	/**
	 * 将指定的接口发布为服务，然后生成WSDL文档(RPC模式)
	 * @param intf
	 * @return
	 * @throws WSDLException
	 */
	protected Document generateRpcWSDL(Class<?> intf) throws WSDLException{
		Assert.isTrue(Boolean.valueOf(intf.isInterface()), "The input class " + intf.getClass() + " is not an interface!");
		Server server = createLocalService(intf,new Object() );
		try{
			return super.getWSDLDocument(server);
		}finally{
			server.stop();
			server.destroy();
		}
	}
	
	
	/**
	 * 运行ws测试  (JAX-WS模式)
	 * @param bean 服务的bean
	 * @param intf  接口类
	 * @param inboundSoapFilename 输入的soap消息文件名
	 * @return 返回的SOAP消息
	 * @throws Exception
	 */
	protected Node executeWs(Object bean, Class<?> intf, String inboundSoapFilename) throws Exception {
		Assert.isTrue(Boolean.valueOf(intf.isInterface()), "The input class " + intf.getClass() + " is not an interface!");
		Server server = createLocalJaxWsService(intf, bean);
		try{
			Node node = invoke("local://" + intf.getName(), "http://cxf.apache.org/transports/local", inboundSoapFilename);
			return node;
		}finally{
			server.destroy();
		}
	}
	
	
	
	/**
	 * 运行ws测试 (RPC模式)
	 * @param bean 服务的bean
	 * @param intf  接口类
	 * @param inboundSoapFilename 输入的soap消息文件名
	 * @return 返回的SOAP消息
	 * @throws Exception
	 */
	protected Node executeRpcWs(Object bean, Class<?> intf, String inboundSoapFilename) throws Exception {
		Assert.isTrue(Boolean.valueOf(intf.isInterface()), "The input class " + intf.getClass() + " is not an interface!");
		Server server = createLocalService(intf, bean);
		try{
			Node node = invoke("local://" + intf.getName(), "http://cxf.apache.org/transports/local", inboundSoapFilename);
			return node;
		}finally{
			server.destroy();
		}
	}

	
	
	/**
	 * 用指定的报文作为入参，测试WebService。（返回值为java对象）
	 * @param bean 要测试的服务bean
	 * @param intf webservice接口类
	 * @param inboundSoapFilename 输入的soap消息文件名
	 * @return
	 * @throws Exception
	 */
	protected Object invokeJaxWs(Object bean, Class<?> intf, String inboundSoapFilename) throws Exception {
		Assert.isTrue(Boolean.valueOf(intf.isInterface()), "The input class " + intf.getClass() + " is not an interface!");
		TestWsCallHandler ts = new TestWsCallHandler(bean);
		bean=Proxy.newProxyInstance(bean.getClass().getClassLoader(),new Class[]{intf}, ts);
		Server server= this.createLocalJaxWsService(intf, bean);
		String url = "local://" + intf.getName();
		try{
			invoke(url, "http://cxf.apache.org/transports/local", inboundSoapFilename);
			return ts.getResult();
		}finally{
			server.destroy();
		}
	}
	
	/**
	 * 用指定的报文作为入参，测试WebService。（返回值为java对象）
	 * @param bean
	 * @param intf
	 * @param inboundSoapFilename
	 * @return
	 * @throws Exception
	 */
	protected Object invokeRPC(Object bean, Class<?> intf, String inboundSoapFilename) throws Exception {
		Assert.isTrue(Boolean.valueOf(intf.isInterface()), "The input class " + intf.getClass() + " is not an interface!");
		TestWsCallHandler ts = new TestWsCallHandler(bean);
		bean=Proxy.newProxyInstance(bean.getClass().getClassLoader(),new Class[]{intf}, ts);
		Server server= this.createLocalService(intf, bean);
		String url = "local://" + intf.getName();
		try{
			invoke(url, "http://cxf.apache.org/transports/local", inboundSoapFilename);
			return ts.getResult();
		}finally{
			server.destroy();
		}
	}

	
	/**
	 * 使用客户端代理调用
	 * @param bean
	 * @param intf
	 * @param method
	 * @param params
	 * @return
	 * @throws Exception
	 */
	protected Object invokeJaxWsMethod(Object bean, Class<?> intf, String method,Object... params) throws Exception {
		Assert.isTrue(Boolean.valueOf(intf.isInterface()), "The input class " + intf.getClass() + " is not an interface!");
		List<Class<?>> list = new ArrayList<Class<?>>();
		for (Object pobj : params) {
			list.add(pobj==null?null:pobj.getClass());
		}
		MethodEx me = BeanUtils.getCompatibleMethod(intf,method, list.toArray(new Class[list.size()]));
		Assert.notNull(me,"The Method "+method+" with "+params.length+" params was not found");
		//开始发布
		Server server = createLocalJaxWsService(intf, bean);
		//创建客户端并运行
		try{
			String url = "local://" + intf.getName();
			Object client=jaxwsPlus.createProxy(url, intf);
			Object result=me.invoke(client, params);
			return result;
		}finally{
			server.destroy();
		}
	}
	
	/**
	 * 使用客户端代理调用
	 * @param bean
	 * @param intf
	 * @param method
	 * @param params
	 * @return
	 * @throws Exception
	 */
	protected  Object[]  invokeRpcMethod(Object bean, Class<?> intf, String method,Object... params) throws Exception {
		Assert.isTrue(Boolean.valueOf(intf.isInterface()), "The input class " + intf.getClass() + " is not an interface!");
		List<Class<?>> list = new ArrayList<Class<?>>();
		for (Object pobj : params) {
			list.add(pobj==null?null:pobj.getClass());
		}
		//开始发布
		Server server = createLocalService(intf, bean);
		//创建客户端并运行
		try{
			String url = "local://" + intf.getName();
			Client client=rpcPlus.createClient(url, intf);
			 Object[]  result=client.invoke(method, params);
			return result;
		}finally{
			server.destroy();
		}
	}
	
	/**
	 * 注意这个类不支持并发
	 * @author jiyi
	 *
	 */
	static private class TestWsCallHandler implements InvocationHandler {
		private Object bean;
		private Object result;
		
		public TestWsCallHandler(Object originalBean){
			Assert.notNull(originalBean);
			this.bean=originalBean;
		}
		
		
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			try{
				result=method.invoke(bean, args);
				return result;
			}catch(InvocationTargetException e){
				throw e.getCause();
			}catch(IllegalAccessException e){
				throw e;
			}
		}


		public Object getResult() {
			return result;
		}
	}
	
	
	/**
	 * 构造服务，注意要自己释放
	 * @param intf
	 * @param bean
	 * @return
	 */
	private Server createLocalJaxWsService(Class<?> intf, Object bean) {
		String url = "local://" + intf.getName();
		ServiceDefinition ws = getFactory().processServiceDef(new ServiceDefinition(intf.getSimpleName(),intf,bean));
		if (ws == null)
			return null;
		JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean(new CXFPlusServiceFactoryBean());
		sf.setAddress(url);
		sf.setServiceBean(ws.getServiceBean());
		sf.setServiceClass(ws.getServiceClass());
		if (printTrace()){
			sf.getInInterceptors().add(new LoggingInInterceptor());
			sf.getOutInterceptors().add(new LoggingOutInterceptor());
//			sf.getHandlers().add(TraceHandler.getSingleton());
		}
		Server server = sf.create();
		return server;
	}
	
	/**
	 * 构造服务，注意要自己释放
	 * @param intf
	 * @param bean
	 * @return
	 */
	private Server createLocalService(Class<?> intf, Object bean) {
		String url = "local://" + intf.getName();
		ServiceDefinition ws = getFactory().processServiceDef(new ServiceDefinition(intf.getSimpleName(), intf, bean));
		if (ws == null)
			return null;
		ServerFactoryBean sf = new ServerFactoryBean(new CXFPlusServiceBean());
		sf.setAddress(url);
		sf.setServiceBean(ws.getServiceBean());
		sf.setServiceClass(ws.getServiceClass());
		if (printTrace()){
			sf.getInInterceptors().add(new LoggingInInterceptor());
			sf.getOutInterceptors().add(new LoggingOutInterceptor());
		}
		Server server = sf.create();
		return server;
	}


	/**
	 * 改用 {@link #generateJaxWsWSDL}
	 * @deprecated
	 * @param class1
	 * @return
	 * @throws WSDLException 
	 */
	public Document generateWsdl(Class<?> intf) throws WSDLException {
		return this.generateJaxWsWSDL(intf);
	}
	
}
