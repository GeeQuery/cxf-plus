
package com.github.cxfplus.com.sun.xml.bind.v2.schemagen.xmlschema;

import com.github.cxfplus.com.sun.xml.txw2.TypedXmlWriter;
import com.github.cxfplus.com.sun.xml.txw2.annotation.XmlElement;

public interface SimpleDerivation
    extends TypedXmlWriter
{


    @XmlElement
    public SimpleRestriction restriction();

    @XmlElement
    public Union union();

    @XmlElement
    public List list();

}
