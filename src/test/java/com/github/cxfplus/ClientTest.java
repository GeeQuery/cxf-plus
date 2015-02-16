package com.github.cxfplus;

import java.util.List;

import javax.annotation.Resource;

import jef.tools.Assert;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import com.github.cxfplus.jaxrs.People;
import com.github.cxfplus.jaxrs.PeopleService;
import com.github.cxfplus.jaxrs.PeopleServiceXml;
import com.github.cxfplus.jaxws.HelloService;
import com.github.cxfplus.jaxws.interceptors.TraceHandler;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-test-client.xml" })
public class ClientTest extends org.junit.Assert implements InitializingBean{

	
	@Resource
	private PeopleService peopleServiceWs;
	@Resource 
	private HelloService helloServiceWs;
	
	@Resource
	private PeopleService peopleService;
	@Resource
	private PeopleServiceXml peopleServiceXml;
	
	@Resource
	private HelloService helloService;
	
	
	
	@Test
	public void testAllClients(){
		{
			System.out.println("======== 测试 sayHello =========");
			String result=helloService.sayHello("Jiyi");
			System.out.println(result);
			assertEquals("Hello,Jiyi", result);	
			
			result=helloServiceWs.sayHello("Rensu");
			System.out.println(result);
			assertEquals("Hello,Rensu", result);
		}
		
		{
			System.out.println("======== 测试 REST/JSON 下插入 =========");
			int id=peopleService.create(new People("jiyi@154.com","Yi","Ji"));
			System.out.println("Created People id="+id);
			assertTrue(id>0);	
		}
	
		{
			System.out.println("======== 测试 REST/JSON 下读取 =========");
			List<People> peoples=peopleService.getAll();
			System.out.println("目前库中有"+peoples.size()+"个用户。");
			for(People p: peoples){
				System.out.println(p);
			}
			assertFalse(peoples.isEmpty());	
		}
		
		{
			System.out.println("======== 测试 REST/XML 下插入 =========");
			int id=peopleServiceXml.create(new People("jiyi@154.com","Yi","Ji"));
			System.out.println("Created People id="+id);
			assertTrue(id>0);	
		}
		
		{
			System.out.println("======== 测试 REST/XML 下读取 =========");
			List<People> peoples=peopleServiceXml.getAll();
			System.out.println("目前库中有"+peoples.size()+"个用户。");
			for(People p: peoples){
				System.out.println(p);
			}
			assertFalse(peoples.isEmpty());	
		}
		
		
		{
			System.out.println("======== 测试 REST接口类直接发布为WebService后下的插入 =========");
			int id=peopleServiceWs.create(new People("jiyi@154.com","Yi","Ji"));
			System.out.println("Created People id="+id);
			assertTrue(id>0);	
		}
		
		{
			System.out.println("======== 测试 REST接口类直接发布为WebService后下的读取 =========");
			List<People> peoples=peopleServiceWs.getAll();
			System.out.println("目前库中有"+peoples.size()+"个用户。");
			for(People p: peoples){
				System.out.println(p);
			}
			assertFalse(peoples.isEmpty());	
		}
	}

	//调试用
	@Ignore
	@Test
	public void rawTestWs(){
		JaxWsProxyFactoryBean bean=new JaxWsProxyFactoryBean();
		bean.setAddress("http://10.17.35.103:8080/dubbo-test/services/vision.apollo.jaxws.HelloService");
		bean.setServiceClass(HelloService.class);
			bean.getHandlers().add(new TraceHandler());
		HelloService s=(HelloService)bean.create();
		String r=s.sayHello("ZHAHH");
		System.out.println(r);
		
	}
	
	//调试用
	@Test
	@Ignore
	public void rawTest(){
		JAXRSClientFactoryBean bean=new JAXRSClientFactoryBean();
		bean.setAddress("http://localhost:8080/cxf-plus/ws/rest/");
		                 
		bean.setServiceClass(PeopleServiceXml.class);
//		bean.setProvider(new FastJSONProvider(true, false));
		
		bean.getInInterceptors().add(new LoggingInInterceptor());
		bean.getOutInterceptors().add(new LoggingOutInterceptor());
		
		PeopleServiceXml s=(PeopleServiceXml)bean.create();
		List<People> r=s.getAll();
		
		System.out.println("-------------------");
		System.out.println("得到用户"+r.size());
//		int id=s.create(new People("jiyi@163.net","jiyi","lu"));
//		System.out.println(id);
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(peopleService);
		Assert.notNull(peopleServiceWs);
		Assert.notNull(peopleServiceXml);
		Assert.notNull(helloService);
		Assert.notNull(helloServiceWs);
	}
	

}
