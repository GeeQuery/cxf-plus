/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.github.cxfplus.com.sun.xml.bind.v2.model.impl;

import java.lang.reflect.Type;
import java.util.List;

import javax.xml.namespace.QName;

import com.github.cxfplus.com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.github.cxfplus.com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.github.cxfplus.com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.github.cxfplus.core.reflect.ClassEx;
import com.github.cxfplus.core.reflect.FieldEx;
import com.github.cxfplus.core.reflect.MethodEx;

/**
 * @author Kohsuke Kawaguchi
 */
class RuntimeElementPropertyInfoImpl extends ElementPropertyInfoImpl<Type,ClassEx,FieldEx,MethodEx>
    implements RuntimeElementPropertyInfo {

    private final Accessor acc;

    RuntimeElementPropertyInfoImpl(RuntimeClassInfoImpl classInfo, PropertySeed<Type,ClassEx,FieldEx,MethodEx> seed) {
        super(classInfo, seed);
        Accessor rawAcc = ((RuntimeClassInfoImpl.RuntimePropertySeed)seed).getAccessor();
        if(getAdapter()!=null && !isCollection())
            // adapter for a single-value property is handled by accessor.
            // adapter for a collection property is handled by lister.
            rawAcc = rawAcc.adapt(getAdapter());
        this.acc = rawAcc;
    }

    public Accessor getAccessor() {
        return acc;
    }

    public boolean elementOnlyContent() {
        return true;
    }

    public List<? extends RuntimeTypeInfo> ref() {
        return (List<? extends RuntimeTypeInfo>)super.ref();
    }

    @Override
    protected RuntimeTypeRefImpl createTypeRef(QName name, Type type, boolean isNillable, String defaultValue) {
        return new RuntimeTypeRefImpl(this,name,type,isNillable,defaultValue);
    }

    public List<RuntimeTypeRefImpl> getTypes() {
        return (List<RuntimeTypeRefImpl>)super.getTypes();
    }
}
