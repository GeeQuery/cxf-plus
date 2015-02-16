package com.github.cxfplus;

import com.github.cxfplus.jaxrs.PeopleService;
import com.github.cxfplus.jaxrs.PeopleServiceImpl;
import com.github.cxfplus.jaxrs.PeopleServiceXml;
import com.github.cxfplus.jaxws.HelloService;
import com.github.cxfplus.jaxws.HelloServiceImpl;
import com.github.cxfplus.support.SimpleServiceLookup;

public class InitClass {
	public InitClass(CXFPlusServlet servlet){
		SimpleServiceLookup data=new SimpleServiceLookup();
		PeopleServiceImpl p=new PeopleServiceImpl();
		data.addService(PeopleServiceXml.class, p);
		data.addService(HelloService.class, new HelloServiceImpl());
		data.addService(PeopleService.class, p);
		servlet.setRSlookup(data);
		servlet.setWSlookup(data);
	}

}
