package com.github.cxfplus.jaxb.test;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

public class JAXBTest {
	@Test
	@Ignore
	public void testMarshall() throws JAXBException{
		JAXBContext context = JAXBContext.newInstance(TestSupplier.class);
		System.out.println(context.getClass());

		Marshaller marshaller = context.createMarshaller();
		TestSupplier boy = new TestSupplier();
		marshaller.marshal(boy, System.out);
		System.out.println();
	}
	
	@Test
	@Ignore
	public void testUnmarshall() throws JAXBException{
		JAXBContext context = JAXBContext.newInstance(TestSupplier.class);
		System.out.println(context.getClass());
		Unmarshaller unmarshaller = context.createUnmarshaller();
		String xml = "<supplier><suppid></suppid><suppidL></suppidL><suppidS></suppidS><suppidF></suppidF><suppidD></suppidD><suppidB></suppidB><status></status></supplier>";
		TestSupplier entity = (TestSupplier) unmarshaller.unmarshal(new StringReader(xml));
		Assert.assertNull(entity.getSuppid());
		Assert.assertNull(entity.getSuppidL());
		Assert.assertNull(entity.getSuppidS());
		Assert.assertNull(entity.getSuppidD());
		Assert.assertNull(entity.getSuppidF());
		Assert.assertNull(entity.getSuppidB());
		System.out.println(entity.getStatus());
	}
}
