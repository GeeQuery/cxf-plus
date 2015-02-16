package ws.sample;

import java.util.Arrays;
import java.util.Map;

import jef.common.log.LogUtil;

import org.junit.Ignore;
import org.junit.Test;

import com.github.cxfplus.client.Clients;

public class HttpWebServiceTest {
	@Test
	public void test(){
		String url="http://localhost:8080/cxf-plus/ws/MyWsTest";
		MyWsTest bean=Clients.createProxy(url, MyWsTest.class);
		System.out.println(new String(bean.getBytes()));
		System.out.println(bean.getFile());
		
		System.out.println(bean.getDate());
		System.out.println(bean.getTime());
		System.out.println(bean.getTimestamp());
		
		System.out.println(bean.getAInt());
		System.out.println(Arrays.toString(bean.getInts()));
		System.out.println(bean.getClz());
		System.out.println(bean.getClasses());
		System.out.println(bean.getState());
		
		System.out.println(bean.getPage());
		System.out.println(bean.getComplex());
		System.out.println(bean.getMapList());
		
	}

	/**
	 * 原计划用JSON对对象进行编码，目前Object直接映射为JSON字符串有点问题，故此案例暂不支持
	 */
	@Test
	@Ignore
	public void test2(){
		String url="http://localhost:8080/cxf-plus/ws/MyWsTest";
		MyWsTest bean=Clients.createProxy(url, MyWsTest.class);
		Map<String,Object> objmap=bean.getAttribute();
		LogUtil.show(objmap);
	}
}
