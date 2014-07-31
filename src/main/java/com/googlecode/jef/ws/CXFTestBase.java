package com.googlecode.jef.ws;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.wsdl.WSDLException;

import jef.tools.Assert;
import jef.tools.StringUtils;
import jef.tools.reflect.BeanUtils;
import jef.tools.reflect.MethodEx;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.jaxws.support.CXFPlusServiceFactoryBean;
import org.apache.cxf.service.factory.CXFPlusServiceBean;
import org.apache.cxf.test.AbstractCXFTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.googlecode.jef.ws.interceptors.TraceHandler;


/**
 * 这个类用来辅助编写单元测试
 * @author jiyi
 *
 */
public abstract class CXFTestBase extends AbstractCXFTest {
	//Spring的context
	protected static ApplicationContext context;

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
	protected WebServiceFactory getFactory(){
		return new DefaultWsFactory();
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
		Server server = createLocalJaxWsService(intf, DUMMY);
		try{
			return super.getWSDLDocument(server);
		}finally{
			server.stop();
			server.destroy();
		}
	}
	
	private IWebService DUMMY=new IWebService(){};
	
	/**
	 * 将指定的接口发布为服务，然后生成WSDL文档(RPC模式)
	 * @param intf
	 * @return
	 * @throws WSDLException
	 */
	protected Document generateRpcWSDL(Class<?> intf) throws WSDLException{
		Assert.isTrue(Boolean.valueOf(intf.isInterface()), "The input class " + intf.getClass() + " is not an interface!");
		Server server = createLocalService(intf,DUMMY );
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
	protected Node executeWs(IWebService bean, Class<?> intf, String inboundSoapFilename) throws Exception {
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
	protected Node executeRpcWs(IWebService bean, Class<?> intf, String inboundSoapFilename) throws Exception {
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
	protected Object invokeJaxWs(IWebService bean, Class<?> intf, String inboundSoapFilename) throws Exception {
		Assert.isTrue(Boolean.valueOf(intf.isInterface()), "The input class " + intf.getClass() + " is not an interface!");
		TestWsCallHandler ts = new TestWsCallHandler(bean);
		bean=(IWebService) Proxy.newProxyInstance(bean.getClass().getClassLoader(),new Class[]{intf}, ts);
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
	protected Object invokeRPC(IWebService bean, Class<?> intf, String inboundSoapFilename) throws Exception {
		Assert.isTrue(Boolean.valueOf(intf.isInterface()), "The input class " + intf.getClass() + " is not an interface!");
		TestWsCallHandler ts = new TestWsCallHandler(bean);
		bean=(IWebService) Proxy.newProxyInstance(bean.getClass().getClassLoader(),new Class[]{intf}, ts);
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
	protected Object invokeJaxWsMethod(IWebService bean, Class<?> intf, String method,Object... params) throws Exception {
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
			Object client=getFactory().createClientProxy(url, intf);
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
	protected  Object[]  invokeRpcMethod(IWebService bean, Class<?> intf, String method,Object... params) throws Exception {
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
			Client client=getFactory().createNoneJaxwsClient(url, intf);
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
	private Server createLocalJaxWsService(Class<?> intf, IWebService bean) {
		String url = "local://" + intf.getName();
		WsContext ws = getFactory().createServerBean(bean, intf);
		if (ws == null)
			return null;
		JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean(new CXFPlusServiceFactoryBean());
		sf.setAddress(url);
		sf.setServiceBean(ws.getServiceBean());
		sf.setServiceClass(ws.getServiceClass());
		if (printTrace())
			sf.getHandlers().add(new TraceHandler());
		Server server = sf.create();
		return server;
	}
	
	/**
	 * 构造服务，注意要自己释放
	 * @param intf
	 * @param bean
	 * @return
	 */
	private Server createLocalService(Class<?> intf, IWebService bean) {
		String url = "local://" + intf.getName();
		WsContext ws = getFactory().createServerBean(bean, intf);
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
