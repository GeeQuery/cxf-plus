package com.github.cxfplus;

import org.junit.Ignore;
import org.junit.Test;

import com.github.cxfplus.jaxrs.PeopleService;
import com.github.cxfplus.jaxrs.PeopleServiceImpl;
import com.github.cxfplus.jaxrs.PeopleServiceXml;
import com.github.cxfplus.jaxws.HelloService;
import com.github.cxfplus.jaxws.HelloServiceImpl;
import com.github.cxfplus.support.SimpleServiceLookup;

public class Server {
	
	/**
	 * 当依赖了cxf-rt-transport-http-jetty 之后，
	 * 可以使用此方法来启动基于Jetty服务器的WebService
	 * @throws InterruptedException 
	 */
	@Ignore
	@Test
	public void start() throws InterruptedException{
		SimpleServiceLookup data=new SimpleServiceLookup();
		PeopleServiceImpl p=new PeopleServiceImpl();
		data.addService(PeopleServiceXml.class, p);
		data.addService(HelloService.class, new HelloServiceImpl());
		data.addService(PeopleService.class, p);
		CXFPlusServlet servlet=new CXFPlusServlet();
		servlet.setRSlookup(data);
		servlet.setWSlookup(data);
		servlet.initParam();
		servlet.setHttpPrefix("http://localhost:80/services");
		servlet.processWebservice();
		servlet.processJaxRs();
		
		

		Thread.currentThread().sleep(99999999);
		System.out.println("Server terminated.");
	}
}
