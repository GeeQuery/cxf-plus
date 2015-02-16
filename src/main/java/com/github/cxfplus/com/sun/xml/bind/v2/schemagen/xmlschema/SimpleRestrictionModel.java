
package com.github.cxfplus.com.sun.xml.bind.v2.schemagen.xmlschema;

import javax.xml.namespace.QName;
import com.github.cxfplus.com.sun.xml.txw2.TypedXmlWriter;
import com.github.cxfplus.com.sun.xml.txw2.annotation.XmlAttribute;
import com.github.cxfplus.com.sun.xml.txw2.annotation.XmlElement;

public interface SimpleRestrictionModel
    extends SimpleTypeHost, TypedXmlWriter
{


    @XmlAttribute
    public SimpleRestrictionModel base(QName value);

    @XmlElement
    public NoFixedFacet enumeration();

}
