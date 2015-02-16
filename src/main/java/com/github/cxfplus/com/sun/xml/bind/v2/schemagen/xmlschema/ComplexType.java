
package com.github.cxfplus.com.sun.xml.bind.v2.schemagen.xmlschema;

import com.github.cxfplus.com.sun.xml.txw2.TypedXmlWriter;
import com.github.cxfplus.com.sun.xml.txw2.annotation.XmlAttribute;
import com.github.cxfplus.com.sun.xml.txw2.annotation.XmlElement;

@XmlElement("complexType")
public interface ComplexType
    extends Annotated, ComplexTypeModel, TypedXmlWriter
{


    @XmlAttribute("final")
    public ComplexType _final(String value);

    @XmlAttribute("final")
    public ComplexType _final(String[] value);

    @XmlAttribute
    public ComplexType block(String value);

    @XmlAttribute
    public ComplexType block(String[] value);

    @XmlAttribute("abstract")
    public ComplexType _abstract(boolean value);

    @XmlAttribute
    public ComplexType name(String value);

}
