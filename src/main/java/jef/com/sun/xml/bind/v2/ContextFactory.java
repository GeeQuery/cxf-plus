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

package jef.com.sun.xml.bind.v2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import jef.tools.ArrayUtils;
import jef.tools.reflect.ClassEx;

import jef.com.sun.istack.FinalArrayList;
import jef.com.sun.xml.bind.Util;
import jef.com.sun.xml.bind.api.JAXBRIContext;
import jef.com.sun.xml.bind.api.TypeReference;
import jef.com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader;
import jef.com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import jef.com.sun.xml.bind.v2.util.TypeCast;

/**
 * This class is responsible for producing RI JAXBContext objects.  In
 * the RI, this is the class that the javax.xml.bind.context.factory
 * property will point to.
 *
 * <p>
 * Used to create JAXBContext objects for v1.0.1 and forward
 *
 * @since 2.0
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings("rawtypes") 
public class ContextFactory {
	public static JAXBContext createContext(Class[] classes, Map<String,Object> properties ) throws JAXBException {
		Type[] types=ArrayUtils.cast(classes, Type.class);
		return createContext(types,properties);
	}
    /**
     * The API will invoke this method via reflection
     */
    public static JAXBContext createContext( Type[] classes, Map<String,Object> properties ) throws JAXBException {
        // fool-proof check, and copy the map to make it easier to find unrecognized properties.
        if(properties==null)
            properties = Collections.emptyMap();
        else
            properties = new HashMap<String,Object>(properties);

        String defaultNsUri = getPropertyValue(properties,JAXBRIContext.DEFAULT_NAMESPACE_REMAP,String.class);

        Boolean c14nSupport = getPropertyValue(properties,JAXBRIContext.CANONICALIZATION_SUPPORT,Boolean.class);
        if(c14nSupport==null)
            c14nSupport = false;

        Boolean allNillable = getPropertyValue(properties,JAXBRIContext.TREAT_EVERYTHING_NILLABLE,Boolean.class);
        if(allNillable==null)
            allNillable = false;

        Boolean retainPropertyInfo = getPropertyValue(properties, JAXBRIContext.RETAIN_REFERENCE_TO_INFO, Boolean.class);
        if(retainPropertyInfo==null)
            retainPropertyInfo = false;

        Boolean xmlAccessorFactorySupport = getPropertyValue(properties,
           JAXBRIContext.XMLACCESSORFACTORY_SUPPORT,Boolean.class);
        if(xmlAccessorFactorySupport==null){
            xmlAccessorFactorySupport = false;
            Util.getClassLogger().log(Level.FINE, "Property " + 
                JAXBRIContext.XMLACCESSORFACTORY_SUPPORT + 
                "is not active.  Using JAXB's implementation");
        }

        RuntimeAnnotationReader ar = getPropertyValue(properties,JAXBRIContext.ANNOTATION_READER,RuntimeAnnotationReader.class);

        Map<Class,Class> subclassReplacements;
        try {
            subclassReplacements = TypeCast.checkedCast(
                getPropertyValue(properties, JAXBRIContext.SUBCLASS_REPLACEMENTS, Map.class), Class.class, Class.class);
        } catch (ClassCastException e) {
            throw new JAXBException(Messages.INVALID_TYPE_IN_MAP.format(),e);
        }

        if(!properties.isEmpty()) {
            throw new JAXBException(Messages.UNSUPPORTED_PROPERTY.format(properties.keySet().iterator().next()));
        }

        return createContext(classes,Collections.<TypeReference>emptyList(),
                subclassReplacements,defaultNsUri,c14nSupport,ar,xmlAccessorFactorySupport,allNillable, retainPropertyInfo);
    }

    /**
     * If a key is present in the map, remove the value and return it.
     */
    private static <T> T getPropertyValue(Map<String, Object> properties, String keyName, Class<T> type ) throws JAXBException {
        Object o = properties.remove(keyName);
        if(o==null)return null;
        if(!type.isInstance(o))
            throw new JAXBException(Messages.INVALID_PROPERTY_VALUE.format(keyName,o));
        else
            return type.cast(o);
    }
    public static JAXBRIContext createContext( Class[] classes, 
            Collection<TypeReference> typeRefs, Map<Class,Class> subclassReplacements, 
            String defaultNsUri, boolean c14nSupport, RuntimeAnnotationReader ar, 
            boolean xmlAccessorFactorySupport, boolean allNillable, boolean retainPropertyInfo) throws JAXBException {
    	Type[] types=ArrayUtils.cast(classes, Type.class);
    	return createContext(types,typeRefs,subclassReplacements,defaultNsUri,c14nSupport,ar,xmlAccessorFactorySupport,allNillable,retainPropertyInfo);
    }
    
    public static JAXBRIContext createContext( Type[] classes, 
            Collection<TypeReference> typeRefs, Map<Class,Class> subclassReplacements, 
            String defaultNsUri, boolean c14nSupport, RuntimeAnnotationReader ar, 
            boolean xmlAccessorFactorySupport, boolean allNillable, boolean retainPropertyInfo) throws JAXBException {

        JAXBContextImpl.JAXBContextBuilder builder = new JAXBContextImpl.JAXBContextBuilder();
        builder.setClasses(classes);
        builder.setTypeRefs(typeRefs);
        builder.setSubclassReplacements(wrapperedMap(subclassReplacements));
        builder.setDefaultNsUri(defaultNsUri);
        builder.setC14NSupport(c14nSupport);
        builder.setAnnotationReader(ar);
        builder.setXmlAccessorFactorySupport(xmlAccessorFactorySupport);
        builder.setAllNillable(allNillable);
        builder.setRetainPropertyInfo(retainPropertyInfo);
        return builder.build();
    }

	private static Map<ClassEx, ClassEx> wrapperedMap(Map<Class, Class> subclassReplacements) {
    	if(subclassReplacements==null)return null;
    	Map<ClassEx,ClassEx> result=new HashMap<ClassEx,ClassEx>();
    	for(Entry<Class,Class> e: subclassReplacements.entrySet()){
    		result.put(new ClassEx(e.getKey()), new ClassEx(e.getValue()));
    	}
		return result;
	}

	/**
     * The API will invoke this method via reflection.
     */
    public static JAXBContext createContext( String contextPath,
                                             ClassLoader classLoader, Map<String,Object> properties ) throws JAXBException {
        FinalArrayList<Class> classes = new FinalArrayList<Class>();
        StringTokenizer tokens = new StringTokenizer(contextPath,":");
        List<Class> indexedClasses;

        // at least on of these must be true per package
        boolean foundObjectFactory;
        boolean foundJaxbIndex;

        while(tokens.hasMoreTokens()) {
            foundObjectFactory = foundJaxbIndex = false;
            String pkg = tokens.nextToken();

            // look for ObjectFactory and load it
            final Class<?> o;
            try {
                o = classLoader.loadClass(pkg+".ObjectFactory");
                classes.add(o);
                foundObjectFactory = true;
            } catch (ClassNotFoundException e) {
                // not necessarily an error
            }

            // look for jaxb.index and load the list of classes
            try {
                indexedClasses = loadIndexedClasses(pkg, classLoader);
            } catch (IOException e) {
                //TODO: think about this more
                throw new JAXBException(e);
            }
            if(indexedClasses != null) {
                classes.addAll(indexedClasses);
                foundJaxbIndex = true;
            }

            if( !(foundObjectFactory || foundJaxbIndex) ) {
                throw new JAXBException( Messages.BROKEN_CONTEXTPATH.format(pkg));
            }
        }


        return createContext(classes.toArray(new Class[classes.size()]),properties);
    }

    /**
     * Look for jaxb.index file in the specified package and load it's contents
     *
     * @param pkg package name to search in
     * @param classLoader ClassLoader to search in
     * @return a List of Class objects to load, null if there weren't any
     * @throws IOException if there is an error reading the index file
     * @throws JAXBException if there are any errors in the index file
     */
    private static List<Class> loadIndexedClasses(String pkg, ClassLoader classLoader) throws IOException, JAXBException {
        final String resource = pkg.replace('.', '/') + "/jaxb.index";
        final InputStream resourceAsStream = classLoader.getResourceAsStream(resource);

        if (resourceAsStream == null) {
            return null;
        }

        BufferedReader in =
                new BufferedReader(new InputStreamReader(resourceAsStream, "UTF-8"));
        try {
            FinalArrayList<Class> classes = new FinalArrayList<Class>();
            String className = in.readLine();
            while (className != null) {
                className = className.trim();
                if (className.startsWith("#") || (className.length() == 0)) {
                    className = in.readLine();
                    continue;
                }

                if (className.endsWith(".class")) {
                    throw new JAXBException(Messages.ILLEGAL_ENTRY.format(className));
                }

                try {
                    classes.add(classLoader.loadClass(pkg + '.' + className));
                } catch (ClassNotFoundException e) {
                    throw new JAXBException(Messages.ERROR_LOADING_CLASS.format(className, resource),e);
                }

                className = in.readLine();
            }
            return classes;
        } finally {
            in.close();
        }
    }

    public static final String USE_JAXB_PROPERTIES = "_useJAXBProperties";
}
