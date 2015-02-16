package com.github.cxfplus.rmi.test;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class HelloImpl extends UnicastRemoteObject  implements Hello {
	/* 构造函数 */
	public HelloImpl() throws RemoteException {
		super();
	}

	/* 实现本地接口中声明的'sayHello()'方法 */
	public String sayHello(String message) throws RemoteException {
		System.out.println("我在RMI的服务器端，客户端正在调用'sayHello'方法。 ");
		System.out.println("Hello  " + message);
		return message;
	}
}
