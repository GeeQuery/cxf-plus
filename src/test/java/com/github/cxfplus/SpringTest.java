package com.github.cxfplus;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ws.sample.MyWsTestImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-test-beans.xml" })
public class SpringTest extends org.junit.Assert implements InitializingBean, ApplicationContextAware {

	private ApplicationContext context;
	
	@Test
	public void testAbc(){
		Map<String,Object> obj=context.getBeansWithAnnotation(javax.jws.WebService.class);
		System.out.println(obj);
		
		System.out.println(MyWsTestImpl.class.getInterfaces()[0].getAnnotation(javax.jws.WebService.class));
		
		
		
		
		obj=context.getBeansWithAnnotation(javax.ws.rs.Path.class);
		System.out.println(obj);
	}
	
	
	public void afterPropertiesSet() throws Exception {
	}

	public void setApplicationContext(ApplicationContext arg0) throws BeansException {
		this.context = arg0;
	}
}
