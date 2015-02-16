package axis2.test.sevice;

import javax.xml.namespace.QName;

import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 测试调用外部的Axis2 WebService
 * @author jiyi
 *
 */
public class CallAxis2 {
	@Test
	@Ignore
	public void testCallAxis2(){
		
		//服务位于： http://localhost/axis2-web/services/myService?wsdl
		
//		JaxWsClientFactoryBean clientBean=new JaxWsClientFactoryBean();
//		JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
		
		ClientProxyFactoryBean factoryBean=new ClientProxyFactoryBean();
		factoryBean.setAddress("http://localhost/axis2-web/services/myService?wsdl");
		factoryBean.setServiceClass(MyService.class);   //有接口调用
		
		//修复兼容性：
		//发现居然提示命名空间不对，原来是CXF认为命名空间用/结尾，多一个斜杠引起问题
		factoryBean.setServiceName(new QName("http://sevice.test.axis2","MyService"));
		
		factoryBean.getInInterceptors().add(new LoggingInInterceptor());
		factoryBean.getOutInterceptors().add(new LoggingOutInterceptor());
		MyService service=(MyService)factoryBean.create();
		
		System.out.println(service.getHello("将"));//当前类
		String result=service.toBaseString("Hello, jiyi", 100);//父类方法
		System.out.println(result);
	}

}
