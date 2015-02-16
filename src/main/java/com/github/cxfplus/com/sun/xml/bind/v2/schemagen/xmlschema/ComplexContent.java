
package com.github.cxfplus.com.sun.xml.bind.v2.schemagen.xmlschema;

import com.github.cxfplus.com.sun.xml.txw2.TypedXmlWriter;
import com.github.cxfplus.com.sun.xml.txw2.annotation.XmlAttribute;
import com.github.cxfplus.com.sun.xml.txw2.annotation.XmlElement;

@XmlElement("complexContent")
public interface ComplexContent
    extends Annotated, TypedXmlWriter
{


    @XmlElement
    public ComplexExtension extension();

    @XmlElement
    public ComplexRestriction restriction();

    @XmlAttribute
    public ComplexContent mixed(boolean value);

}
