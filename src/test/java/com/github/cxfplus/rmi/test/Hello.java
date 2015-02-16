package com.github.cxfplus.rmi.test;

import java.rmi.*;

public interface Hello extends Remote {
	// 接口中的具体方法声明，注意必须声明抛出RemoteException
	String sayHello(String name) throws RemoteException;
}
