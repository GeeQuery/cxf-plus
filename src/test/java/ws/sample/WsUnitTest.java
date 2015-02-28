package ws.sample;
import java.io.File;
import java.util.Map;

import org.apache.cxf.helpers.XMLUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.github.cxfplus.test.CXFTestBase;
import com.google.common.collect.ImmutableMap;


public class WsUnitTest extends CXFTestBase{
	private MyWsTest serviceBean=new MyWsTestImpl();
	
	/**
	 * 案例：打印出生成的WSDL
	 * @throws Exception
	 */
	@Test
	public void testGenrateWSDL() throws Exception{
		Document wsdl=super.generateJaxWsWSDL(MyWsTest.class);
		assertValid("//wsdl:types", wsdl);
		XMLUtils.printDOM(wsdl);
	}

	@Test
	public void testGenrateWSDL2() throws Exception{
		Document wsdl=super.generateJaxWsWSDL(CxfPlusFeature.class);
		com.github.cxfplus.core.util.XMLUtils.saveDocument(wsdl, new File("c:/cxf.wsdl"));
	}
	
	/**
	 * 案例，用类似反射的方法调用一个经过WebServeice发布的服务。
	 * @throws Exception
	 */
	@Test
	public void testMyServiceClientProxy() throws Exception{
		Map<String,Long> ppp=ImmutableMap.of("key1", 123L, "key2", 456L, "key3", 789L);
		Map map=(Map)super.invokeJaxWsMethod(serviceBean, MyWsTest.class, "method3",ppp,"aaa");
		assertTrue(map.size()>0);
		System.out.println("result:"+map);
	}
	
	/**
	 * 案例:运行method1，使用指定的报文
	 * @throws Exception
	 */
	@Test
	public void testMyMethod1() throws Exception{
		Node node=super.executeWs(serviceBean, MyWsTest.class, "/testMethod1.xml");
		assertValid("//return", node);
	}
	
	@Test
	public void testMyMethod2() throws Exception{
		Node node=super.executeWs(serviceBean, MyWsTest.class, "/testMethod2.xml");
		assertValid("//return", node);
	}
	
	@Test
	public void testMyMethod3() throws Exception{
		Node node=super.executeWs(serviceBean, MyWsTest.class, "/testMethod3.xml");
		assertValid("//return", node);
	}
	
}
