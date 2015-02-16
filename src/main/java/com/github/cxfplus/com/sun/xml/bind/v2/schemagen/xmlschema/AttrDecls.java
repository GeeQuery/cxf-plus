
package com.github.cxfplus.com.sun.xml.bind.v2.schemagen.xmlschema;

import com.github.cxfplus.com.sun.xml.txw2.TypedXmlWriter;
import com.github.cxfplus.com.sun.xml.txw2.annotation.XmlElement;

public interface AttrDecls
    extends TypedXmlWriter
{


    @XmlElement
    public LocalAttribute attribute();

    @XmlElement
    public Wildcard anyAttribute();

}
