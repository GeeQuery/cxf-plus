
package com.github.cxfplus.com.sun.xml.bind.v2.schemagen.xmlschema;

import com.github.cxfplus.com.sun.xml.txw2.TypedXmlWriter;
import com.github.cxfplus.com.sun.xml.txw2.annotation.XmlAttribute;
import com.github.cxfplus.com.sun.xml.txw2.annotation.XmlElement;

@XmlElement("simpleType")
public interface SimpleType
    extends Annotated, SimpleDerivation, TypedXmlWriter
{


    @XmlAttribute("final")
    public SimpleType _final(String[] value);

    @XmlAttribute("final")
    public SimpleType _final(String value);

    @XmlAttribute
    public SimpleType name(String value);

}
