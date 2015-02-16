package axis2.test.sevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 这是孙喜欢的调用方式，将远程WS当做反射类一样去调用。
 * @author jiyi
 *
 */
public class DynamicCall {
	/**
	 * 调用远程WS的服务
	 * 
	 * 这种调用方法的优点是——
	 * 1、无需生成类
	 * 2、无需对方的接口
	 * 3、容错性强，两边接口无需强行维持一致。
	 * 
	 * 缺点是——校验薄弱，schema和数据类型不一致等错误可能发生，需要调用方人为确保调用参数类型的正确性。.
	 * 此外似乎不能传输基本类型以外的自定义 Bean。
	 * 
	 * 
	 * @param url    服务地址
	 * @param method 调用方法
	 * @param namespaceURI 服务命名空间
	 * @param args     参数
	 * @param returnType 返回类型
	 * @return 远程调用后的结果
	 * @throws Exception
	 */
	public static <T> T callWsCxf(String url, String method, String namespaceURI, Object[] args, Class<T> returnType) throws Exception {
		Client client = getCxfClient(url);
		QName name = new QName(namespaceURI, method);
		Object[] obj = client.invoke(name, args);
		return (T) obj[0];
	}

	/**
	 * 由于动态方式没有作CXFPlus改造，因此只能适用于原生CXF支持的接口上
	 * @throws Exception
	 */
	@Test
	public void testCxfSimple() throws Exception {
		List result = callWsCxf("http://localhost:8080/cxf-plus/ws/MyWsTest?wsdl",
				"getInts", 
				"http://sample.ws/", new Object[] {}, List.class);
		System.out.println(result);
	}
	
	/**
	 * 由于动态方式没有作CXFPlus改造，因此复杂的接口用不了
	 * @throws Exception
	 */
	@Test
	public void testCxfComplex() throws Exception {
		Map result = callWsCxf("http://localhost:8080/cxf-plus/ws/MyWsTest?wsdl",
				"method3", 
				"http://sample.ws/", new Object[] {new HashMap<String,Long>(),"testMethod"}, Map.class);
		System.out.println(result);
	}
	
	/**
	 * 这是孙希望使用纯动态调用时，前来咨询修改URL的API时做的测试。
	 * 忽略
	 * @throws Exception
	 */
	@Test
	@Ignore 
	public void testCxfForSun() throws Exception{
		JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();  
		org.apache.cxf.endpoint.Client client = dcf.createClient("http://pc-sunyingjie/cxf/services/cxfTestService?wsdl");
		
		//////////////// KEY!!!!!!!!!!!!!!!!! ////////////////
		client.getConduit().getTarget().getAddress().setValue("http://pc-sunyingjie/cxf/services/cxfTestService");
		//////////////KEY!!!!!!!!!!!!!!!!! ////////////////
		
		QName name=new QName("http://foo/","hello");  
		List<String> emails=new ArrayList<String>();
		emails.add("sss@sina.com");
		emails.add("ddd@163.com");
		Object[] objects=client.invoke(name,new Object[]{"sun",15,emails,15l});   
		System.out.println(objects[0].toString());
	}

	/**
	 * 动态客户端工厂
	 */
	private static JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
	/**
	 * 动态客户端缓存
	 */
	private static Map<String, Client> cxfClients = new ConcurrentHashMap<String, Client>();

	/**
	 * 从缓存中取用Client
	 * @param url
	 * @return
	 */
	private static Client getCxfClient(String url) {
		Client client = cxfClients.get(url);
		if (client == null) {
			client = dcf.createClient(url);
			cxfClients.put(url, client);
			client.getConduit().getTarget().getAddress().setValue(url);
		}
		return client;
	}
}
