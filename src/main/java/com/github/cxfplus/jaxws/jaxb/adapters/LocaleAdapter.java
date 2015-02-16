package com.github.cxfplus.jaxws.jaxb.adapters;

import java.util.Locale;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LocaleAdapter extends XmlAdapter<LocaleWs, Locale> {
	public Locale unmarshal(LocaleWs v) throws Exception {
		return new Locale(v.getLanguage(), v.getContry());
	}

	public LocaleWs marshal(Locale v) throws Exception {
		LocaleWs l = new LocaleWs();
		l.setContry(v.getCountry());
		l.setLanguage(v.getLanguage());
		return l;
	}
}
