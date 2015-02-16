
package com.github.cxfplus.com.sun.xml.bind.v2.schemagen.xmlschema;

import com.github.cxfplus.com.sun.xml.txw2.TypedXmlWriter;
import com.github.cxfplus.com.sun.xml.txw2.annotation.XmlAttribute;

public interface FixedOrDefault
    extends TypedXmlWriter
{


    @XmlAttribute("default")
    public FixedOrDefault _default(String value);

    @XmlAttribute
    public FixedOrDefault fixed(String value);

}
