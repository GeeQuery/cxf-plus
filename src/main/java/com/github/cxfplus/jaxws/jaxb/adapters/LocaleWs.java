package com.github.cxfplus.jaxws.jaxb.adapters;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class LocaleWs {
	private String contry;
	private String language;

	public String getContry() {
		return this.contry;
	}

	public void setContry(String contry) {
		this.contry = contry;
	}

	public String getLanguage() {
		return this.language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
