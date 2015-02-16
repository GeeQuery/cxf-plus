package com.github.cxfplus.rmi.test;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class Server {
	public static void main(String[] args) throws RemoteException {
		// 在服务器端设置安全机制
		/*
		 * if (System.getSecurityManager() == null) {
		 * System.setSecurityManager(new RMISecurityManager()); }
		 */
//    	System.setProperty("java.rmi.server.hostname","10.22.37.222");
    	System.setProperty("java.rmi.server.useLocalHostname","true");
    	//sun.rmi.transport.tcp.localHostnameTimeOut=2000
    			   
    	System.out.println(UnicastRemoteObject.exportObject(new Remote(){},8888));
		try {
			System.out.println("开始 RMI Server ...");
			/* 创建远程对象的实现实例 */
			HelloImpl hImpl = new HelloImpl();
			System.out.println("将实例注册到专有的URL ");
			
			Registry reg=LocateRegistry.createRegistry(8888);
			sun.rmi.registry.RegistryImpl regImpl=(sun.rmi.registry.RegistryImpl)reg;
			sun.rmi.server.UnicastServerRef ref=(sun.rmi.server.UnicastServerRef)regImpl.getRef();
			ref.getLiveRef();
			
	
			
			
			reg.rebind("RHello", hImpl);

//			Naming.bind("rmi://10.17.35.103:8888/RHello", hImpl);
			System.out.println("等待RMI客户端调用...");
			System.out.println("");
		} catch (Exception e) {
			System.out.println("错误: " + e);
		}
	}

}
