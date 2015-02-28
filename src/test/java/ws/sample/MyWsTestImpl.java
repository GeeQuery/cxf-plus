package ws.sample;

import java.io.File;
import java.lang.Thread.State;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import rs.sample.Customer;

public class MyWsTestImpl implements MyWsTest {
	private final HashMap<String, String> rr;

	public MyWsTestImpl() {
		this.rr = new HashMap<String,String>();
		this.rr.put("123", "aaa");
		this.rr.put("456", "bbb");
	}

	public Map<String, String> method1() {
		return this.rr;
	}

	public Map<String, Double> method2(String[] str2) {
		Map<String,Double> map=new HashMap<String,Double>();
		for(String s:str2){
			map.put(s, 100d);
		}
		return map;
	}

	public Map<String, String> method3(Map<String, Long> param,String arg2) {
		return this.rr;
	}

	public File getFile() {
		File file=new File("c:/config.sys");
		return file;
	}

	public byte[] getBytes() {
		return "失节事大".getBytes();
	}

	public Time getTime() {
		return new java.sql.Time(132453);
	}

	public Date getDate() {
		return new java.sql.Date(System.currentTimeMillis());
	}

	public Timestamp getTimestamp() {
		return new Timestamp(System.currentTimeMillis());
	}

	public int[] getInts() {
		return new int[]{5,3,1};
	}

	public AtomicInteger getAInt() {
		return new AtomicInteger(100);
	}

	public Class getClz() {
		return String.class;
	}

	public Set<Class> getClasses() {
		Set<Class> set=new HashSet<Class>();
		set.add(String.class);
		set.add(List.class);
		return set;
	}

	public State getState() {
		return Thread.State.RUNNABLE;
	}

	public Page<String> getPage() {
		return new Page<String>(100,10).setList(Arrays.asList("ssdd","dssds"));
	}

	public Page<Map<String, String>> getComplex() {
		Page<Map<String,String>> page= new Page<Map<String,String>>(100,10);
		Map<String,String> m1=new HashMap<String,String>();
		m1.put("a1", "b2");
		m1.put("a2", "b3");
		
		List<Map<String,String>> list=new ArrayList<Map<String,String>>();
		list.add(m1);
		list.add(m1);
		page.setList(list);
		return page;
	}

	public Map<String, String>[] getMapList() {
		Map<String,String> m1=new HashMap<String,String>();
		m1.put("a1", "b2");
		m1.put("a2", "b3");
		return new Map[]{m1,m1} ;
	}

	public Map<String, Object> getAttribute() {
		Map<String,Object> m=new HashMap<String,Object>();
		m.put("a Int value", 100);
		m.put("a Long value", 100L);
		m.put("a Double value", 100.0d);
		m.put("a Float value", 10.0f);
		m.put("a complex bean", new Customer());
		m.put("a other map", Collections.singletonMap("key1", "value1"));
		m.put("a other list", Arrays.asList("1","2","3"));
		return m;
	}
}