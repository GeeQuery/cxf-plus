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
import java.util.Map;

import javax.xml.namespace.QName;

import jef.tools.reflect.ClassEx;
import jef.tools.reflect.FieldEx;
import jef.tools.reflect.MethodEx;

import com.github.cxfplus.com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.github.cxfplus.com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.github.cxfplus.com.sun.xml.bind.v2.model.nav.Navigator;
import com.github.cxfplus.com.sun.xml.bind.v2.model.nav.ReflectionNavigator;
import com.github.cxfplus.com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.github.cxfplus.com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfoSet;

/**
 * {@link TypeInfoSet} specialized for runtime.
 *
 * @author Kohsuke Kawaguchi
 */
final class RuntimeTypeInfoSetImpl extends TypeInfoSetImpl<Type,ClassEx,FieldEx,MethodEx> implements RuntimeTypeInfoSet {
    public RuntimeTypeInfoSetImpl(AnnotationReader<Type,ClassEx,FieldEx,MethodEx> reader) {
        super(Navigator.REFLECTION,reader,RuntimeBuiltinLeafInfoImpl.LEAVES);
    }

    @Override
    protected RuntimeNonElement createAnyType() {
        return RuntimeAnyTypeImpl.theInstance;
    }

    public ReflectionNavigator getNavigator() {
        return (ReflectionNavigator)super.getNavigator();
    }

    public RuntimeNonElement getTypeInfo( Type type ) {
        return (RuntimeNonElement)super.getTypeInfo(type);
    }

    public RuntimeNonElement getAnyTypeInfo() {
        return (RuntimeNonElement)super.getAnyTypeInfo();
    }

    public RuntimeNonElement getClassInfo(ClassEx clazz) {
        return (RuntimeNonElement)super.getClassInfo(clazz);
    }

    public Map<ClassEx,RuntimeClassInfoImpl> beans() {
        return (Map<ClassEx,RuntimeClassInfoImpl>)super.beans();
    }

    public Map<Type,RuntimeBuiltinLeafInfoImpl<?>> builtins() {
        return (Map<Type,RuntimeBuiltinLeafInfoImpl<?>>)super.builtins();
    }

    public Map<ClassEx,RuntimeEnumLeafInfoImpl<?,?>> enums() {
        return (Map<ClassEx,RuntimeEnumLeafInfoImpl<?,?>>)super.enums();
    }

    public Map<Type,RuntimeArrayInfoImpl> arrays() {
        return (Map<Type,RuntimeArrayInfoImpl>)super.arrays(); 
    }

    public RuntimeElementInfoImpl getElementInfo(ClassEx scope,QName name) {
        return (RuntimeElementInfoImpl)super.getElementInfo(scope,name);
    }

    public Map<QName,RuntimeElementInfoImpl> getElementMappings(ClassEx scope) {
        return (Map<QName,RuntimeElementInfoImpl>)super.getElementMappings(scope);
    }

    public Iterable<RuntimeElementInfoImpl> getAllElements() {
        return (Iterable<RuntimeElementInfoImpl>)super.getAllElements();
    }
}
