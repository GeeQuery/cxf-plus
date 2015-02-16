package com.github.cxfplus.jaxws;

public class HelloServiceImpl implements HelloService {
	public String sayHello(String name) {
		return "Hello," + name;
	}
}
