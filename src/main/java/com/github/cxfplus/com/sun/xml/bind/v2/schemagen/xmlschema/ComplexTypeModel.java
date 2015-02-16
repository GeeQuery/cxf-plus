
package com.github.cxfplus.com.sun.xml.bind.v2.schemagen.xmlschema;

import com.github.cxfplus.com.sun.xml.txw2.TypedXmlWriter;
import com.github.cxfplus.com.sun.xml.txw2.annotation.XmlAttribute;
import com.github.cxfplus.com.sun.xml.txw2.annotation.XmlElement;

public interface ComplexTypeModel
    extends AttrDecls, TypeDefParticle, TypedXmlWriter
{


    @XmlElement
    public SimpleContent simpleContent();

    @XmlElement
    public ComplexContent complexContent();

    @XmlAttribute
    public ComplexTypeModel mixed(boolean value);

}
