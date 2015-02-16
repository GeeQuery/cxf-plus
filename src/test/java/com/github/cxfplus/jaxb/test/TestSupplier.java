package com.github.cxfplus.jaxb.test;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "suppid", "suppidL", "suppidS", "suppidD", "suppidF", "suppidB", "name", "status", "addr1", "addr2", "city", "state", "zip", "phone", "statusName" })
public class TestSupplier {
	private Integer suppid;

	private Long suppidL;

	private Short suppidS;

	private Double suppidD;
	private Float suppidF;

	private Boolean suppidB;

	@XmlElement(name = "name", nillable = true)
	private String name;

	@XmlElement(name = "status", nillable = false)
	private int status;

	@XmlElement(name = "addr1", nillable = true)
	private String addr1;

	@XmlElement(name = "addr2", nillable = true)
	private String addr2;

	@XmlElement(name = "city", nillable = true)
	private String city;

	@XmlElement(name = "state", nillable = true)
	private String state;

	@XmlElement(name = "zip", nillable = true)
	private String zip;

	@XmlElement(name = "phone", nillable = true)
	private String phone;

	@XmlElement(name = "statusName", nillable = true)
	private List<TestSupplier> statusName;

	public Long getSuppidL() {
		return suppidL;
	}

	public void setSuppidL(Long suppidL) {
		this.suppidL = suppidL;
	}

	public Short getSuppidS() {
		return suppidS;
	}

	public void setSuppidS(Short suppidS) {
		this.suppidS = suppidS;
	}

	public Double getSuppidD() {
		return suppidD;
	}

	public void setSuppidD(Double suppidD) {
		this.suppidD = suppidD;
	}

	public Float getSuppidF() {
		return suppidF;
	}

	public void setSuppidF(Float suppidF) {
		this.suppidF = suppidF;
	}

	public Boolean getSuppidB() {
		return suppidB;
	}

	public void setSuppidB(Boolean suppidB) {
		this.suppidB = suppidB;
	}

	public void setName(String obj) {
		this.name = obj;
	}

	public String getName() {
		return name;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setAddr1(String obj) {
		this.addr1 = obj;
	}

	public String getAddr1() {
		return addr1;
	}

	public void setAddr2(String obj) {
		this.addr2 = obj;
	}

	public String getAddr2() {
		return addr2;
	}

	public void setCity(String obj) {
		this.city = obj;
	}

	public String getCity() {
		return city;
	}

	public void setState(String obj) {
		this.state = obj;
	}

	public String getState() {
		return state;
	}

	public void setZip(String obj) {
		this.zip = obj;
	}

	public String getZip() {
		return zip;
	}

	public void setPhone(String obj) {
		this.phone = obj;
	}

	public String getPhone() {
		return phone;
	}

	public TestSupplier() {
	}

	public Integer getSuppid() {
		return suppid;
	}

	public void setSuppid(Integer suppid) {
		this.suppid = suppid;
	}

	public void setStatusName(List<TestSupplier> obj) {
		this.statusName = obj;
	}

	public List<TestSupplier> getStatusName() {
		return statusName;
	}
}
