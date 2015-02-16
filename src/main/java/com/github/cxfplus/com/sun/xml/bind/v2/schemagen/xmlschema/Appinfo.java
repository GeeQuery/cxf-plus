
package com.github.cxfplus.com.sun.xml.bind.v2.schemagen.xmlschema;

import com.github.cxfplus.com.sun.xml.txw2.TypedXmlWriter;
import com.github.cxfplus.com.sun.xml.txw2.annotation.XmlAttribute;
import com.github.cxfplus.com.sun.xml.txw2.annotation.XmlElement;

@XmlElement("appinfo")
public interface Appinfo
    extends TypedXmlWriter
{


    @XmlAttribute
    public Appinfo source(String value);

}
