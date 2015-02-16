
package com.github.cxfplus.com.sun.xml.bind.v2.model.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.xml.bind.annotation.XmlElementRef;

final class XmlElementRefQuick
    extends Quick
    implements XmlElementRef
{

    private final XmlElementRef core;

    public XmlElementRefQuick(Locatable upstream, XmlElementRef core) {
        super(upstream);
        this.core = core;
    }

    protected Annotation getAnnotation() {
        return core;
    }

    protected Quick newInstance(Locatable upstream, Annotation core) {
        return new XmlElementRefQuick(upstream, ((XmlElementRef) core));
    }

    public Class<XmlElementRef> annotationType() {
        return XmlElementRef.class;
    }

    public String namespace() {
        return core.namespace();
    }

    public boolean required() {
    	try {
    		Method method=core.getClass().getMethod("required");
    		return (Boolean)method.invoke(core);
    	} catch(Throwable e) {
    		return true;    // the value defaults to true
    	}
    }

    public String name() {
        return core.name();
    }

    public Class type() {
        return core.type();
    }

}
