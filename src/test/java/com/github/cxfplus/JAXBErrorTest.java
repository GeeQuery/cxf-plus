package com.github.cxfplus;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.xml.bind.JAXBException;

import jef.tools.io.ReaderInputStream;
import jef.tools.reflect.GenericUtils;

import org.apache.cxf.jaxrs.provider.JAXBElementProvider;
import com.github.cxfplus.jaxrs.People;
import org.junit.Test;

public class JAXBErrorTest extends JAXBElementProvider<List>{

	
	
	@Test
	public void test123() throws JAXBException, IOException{
		String s="<?xml version=\"1.0\" encoding=\"UTF-8\"?><Peoples><People><email>admin@hikvision.com</email><firstName>admin</firstName><id>1</id><lastName>administrator</lastName></People></Peoples>";
		Object obj=this.readFrom(List.class,GenericUtils.newListType(People.class) , null, MediaType.APPLICATION_XML_TYPE, new MultivaluedHashMap(), new ReaderInputStream(new StringReader(s)));
		
		
		 System.out.println(obj);
		
		
	}

}
