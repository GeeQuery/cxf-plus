package com.github.cxfplus.rmi.test;

import java.rmi.Naming;

public class Client {
	public static void main(String[] args) {
		// 在服务器端设置安全机制
		/*
		 * if (System.getSecurityManager() == null) {
		 * System.setSecurityManager(new RMISecurityManager()); }
		 */
		/* 默认为本地主机和默认端口 */
		try {
			/* 根据指定的URL定位远程实现对象 */
			/* “h”是一个标识符，我们将用它指向实现“Hello”接口的远程对象 */
			Hello h = (Hello) Naming.lookup("rmi://" + "10.17.35.103:8888" + "/RHello");

			System.out.println("实现“Hello”接口的远程对象: " + h);
			System.out.println("我在客户端，开始调用RMI服务器端的'sayHello'方法");
			System.out.println("欢迎,  " + h.sayHello("javamxj blog"));
		} catch (Exception ex) {
			System.out.println("错误 " + ex);
		}
	}

}
