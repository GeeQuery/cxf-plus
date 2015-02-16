/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.github.cxfplus.com.sun.xml.bind.v2.model.nav;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;

import com.github.cxfplus.com.sun.xml.bind.v2.runtime.Location;
import jef.common.annotation.ObjectName;
import jef.tools.reflect.ClassEx;
import jef.tools.reflect.FieldEx;
import jef.tools.reflect.GenericUtils;
import jef.tools.reflect.MethodEx;

/**
 * {@link Navigator} implementation for {@code java.lang.reflect}.
 *
 */
@SuppressWarnings("rawtypes")
public final class ReflectionNavigator implements Navigator<Type, ClassEx, FieldEx, MethodEx> {

    /**
     * Singleton.
     *
     * Use {@link Navigator#REFLECTION}
     */
    ReflectionNavigator() {
    }

    public ClassEx getSuperClass(ClassEx clazz) {
        if (clazz.getWrappered() == Object.class) {
            return null;
        }
        ClassEx sc = clazz.getSuperclass();
        if (sc == null) {
            sc = new ClassEx(Object.class);        // error recovery
        }
        return sc;
    }
    private static final TypeVisitor<Type, ClassEx> baseClassFinder = new TypeVisitor<Type, ClassEx>() {

        public Type onClass(Class c, ClassEx sup) {
            // t is a raw type
            if (sup.getWrappered() == c) {
                return sup.getWrappered();
            }

            Type r;

            Type sc = c.getGenericSuperclass();
            if (sc != null) {
                r = visit(sc, sup);
                if (r != null) {
                    return r;
                }
            }

            for (Type i : c.getGenericInterfaces()) {
                r = visit(i, sup);
                if (r != null) {
                    return r;
                }
            }

            return null;
        }

        public Type onParameterizdType(ParameterizedType p, ClassEx sup) {
            Class raw = (Class) p.getRawType();
            if (raw == sup.getWrappered()) {
                // p is of the form sup<...>
                return p;
            } else {
                // recursively visit super class/interfaces
                Type r = raw.getGenericSuperclass();
                if (r != null) {
                    r = visit(bind(r, raw, p), sup);
                }
                if (r != null) {
                    return r;
                }
                for (Type i : raw.getGenericInterfaces()) {
                    r = visit(bind(i, raw, p), sup);
                    if (r != null) {
                        return r;
                    }
                }
                return null;
            }
        }

        public Type onGenericArray(GenericArrayType g, ClassEx sup) {
            // not clear what I should do here
            return null;
        }

        public Type onVariable(TypeVariable v, ClassEx sup) {
            return visit(v.getBounds()[0], sup);
        }

        public Type onWildcard(WildcardType w, ClassEx sup) {
            // not clear what I should do here
            return null;
        }

        /**
         * Replaces the type variables in {@code t} by its actual arguments.
         *
         * @param decl
         *      provides a list of type variables. See {@link GenericDeclaration#getTypeParameters()}
         * @param args
         *      actual arguments. See {@link ParameterizedType#getActualTypeArguments()}
         */
        private Type bind(Type t, GenericDeclaration decl, ParameterizedType args) {
            return binder.visit(t, new BinderArg(decl, args.getActualTypeArguments()));
        }
    };

    private static class BinderArg {

        final TypeVariable[] params;
        final Type[] args;

        BinderArg(TypeVariable[] params, Type[] args) {
            this.params = params;
            this.args = args;
            assert params.length == args.length;
        }

        public BinderArg(GenericDeclaration decl, Type[] args) {
            this(decl.getTypeParameters(), args);
        }

        Type replace(TypeVariable v) {
            for (int i = 0; i < params.length; i++) {
                if (params[i].equals(v)) {
                    return args[i];
                }
            }
            return v;   // this is a free variable
        }
    }
    private static final TypeVisitor<Type, BinderArg> binder = new TypeVisitor<Type, BinderArg>() {

        public Type onClass(Class c, BinderArg args) {
            return c;
        }

        public Type onParameterizdType(ParameterizedType p, BinderArg args) {
            Type[] params = p.getActualTypeArguments();

            boolean different = false;
            for (int i = 0; i < params.length; i++) {
                Type t = params[i];
                params[i] = visit(t, args);
                different |= t != params[i];
            }

            Type newOwner = p.getOwnerType();
            if (newOwner != null) {
                newOwner = visit(newOwner, args);
            }
            different |= p.getOwnerType() != newOwner;

            if (!different) {
                return p;
            }

            return new ParameterizedTypeImpl((Class<?>) p.getRawType(), params, newOwner);
        }

        public Type onGenericArray(GenericArrayType g, BinderArg types) {
            Type c = visit(g.getGenericComponentType(), types);
            if (c == g.getGenericComponentType()) {
                return g;
            }

            return new GenericArrayTypeImpl(c);
        }

        public Type onVariable(TypeVariable v, BinderArg types) {
            return types.replace(v);
        }

        public Type onWildcard(WildcardType w, BinderArg types) {
            // TODO: this is probably still incorrect
            // bind( "? extends T" ) with T= "? extends Foo" should be "? extends Foo",
            // not "? extends (? extends Foo)"
            Type[] lb = w.getLowerBounds();
            Type[] ub = w.getUpperBounds();
            boolean diff = false;

            for (int i = 0; i < lb.length; i++) {
                Type t = lb[i];
                lb[i] = visit(t, types);
                diff |= (t != lb[i]);
            }

            for (int i = 0; i < ub.length; i++) {
                Type t = ub[i];
                ub[i] = visit(t, types);
                diff |= (t != ub[i]);
            }

            if (!diff) {
                return w;
            }

            return new WildcardTypeImpl(lb, ub);
        }
    };

    public Type getBaseClass(Type t, ClassEx sup) {
        return baseClassFinder.visit(t, sup);
    }
    
    public Type getBaseClass(Type t, Class sup) {
        return baseClassFinder.visit(t, new ClassEx(sup));
    }

    public String getClassName(ClassEx clazz) {
    	String genericName=clazz.getGenericName();
        return genericName;
    }

    public String getTypeName(Type type) {
    	String result=null;
        if (type instanceof Class) {
            Class c = (Class) type;
            if (c.isArray()) {
                result=getTypeName(c.getComponentType()) + "[]";
            }else{
            	result=c.getName();
            }
        }else{
        	result=type.toString();
        }
        return result;
    }

    public String getClassShortName(ClassEx clazz) {
    	String s;
    	if(clazz.getGenericType() instanceof ParameterizedType){
    		s= toGenericSimpleName((ParameterizedType)clazz.getGenericType());
    	}else{
    		s= clazz.getSimpleName();	
    	}
    	//log.debug("return shortName:" + s);
    	return s;
    }

    /**
     * 这个过程中其实丢弃了包名，是有可能会引起类型冲突的。不过可以用注解@ObjectName来回避。
     * @param genericType
     * @return
     */
    private String toGenericSimpleName(ParameterizedType genericType) {
    	Class raw=(Class)genericType.getRawType();
    	ObjectName na=(ObjectName)raw.getAnnotation(ObjectName.class);
    	StringBuilder sb=new StringBuilder(na==null?raw.getSimpleName():na.value());
		sb.append("Of");
		for(Type t:genericType.getActualTypeArguments()){
			if(t instanceof ParameterizedType){
				sb.append("").append(toGenericSimpleName((ParameterizedType)t));
			}else if(t instanceof Class){
				sb.append("").append(((Class) t).getSimpleName());
			}else{
				sb.append(t.toString());
			}
		}
		return sb.toString();
	}

	public Collection<? extends FieldEx> getDeclaredFields(ClassEx clazz) {
        return Arrays.asList(clazz.getDeclaredFields());
    }

    public FieldEx getDeclaredField(ClassEx clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    public Collection<? extends MethodEx> getDeclaredMethods(ClassEx clazz) {
        return Arrays.asList(clazz.getDeclaredMethods());
    }

    public ClassEx getDeclaringClassForField(FieldEx field) {
        return new ClassEx(field.getDeclaringClass());
    }

    public ClassEx getDeclaringClassForMethod(MethodEx method) {
        return new ClassEx(method.getDeclaringClass());
    }

    public Type getFieldType(FieldEx field) {
//        if (field.getType().isArray()) {
//            Class c = field.getType().getComponentType();
//            if (c.isPrimitive()) {
//                return Array.newInstance(c, 0).getClass();
//            }
//        }
        return fix(field.getGenericType());
    }

    public String getFieldName(FieldEx field) {
        return field.getName();
    }

    public String getMethodName(MethodEx method) {
        return method.getName();
    }

    public Type getReturnType(MethodEx method) {
        return fix(method.getGenericReturnType());
    }

    public Type[] getMethodParameters(MethodEx method) {
        return method.getGenericParameterTypes();
    }

    public boolean isStaticMethod(MethodEx method) {
        return Modifier.isStatic(method.getModifiers());
    }

    public boolean isFinalMethod(MethodEx method) {
        return Modifier.isFinal(method.getModifiers());
    }

    public boolean isSubClassOf(Type sub, Type sup) {
        return erasure(sup).isAssignableFrom(erasure(sub));
    }

    public Class ref(Class c) {
        return c;
    }

    public Class use(ClassEx c) {
        return c.getWrappered();
    }

    public ClassEx asDecl(Type t) {
//        return erasure(t);
    	return new ClassEx(t);
    }

    public ClassEx asDecl(Class c) {
        return new ClassEx(c);
    }
    /**
     * Implements the logic for {@link #erasure(Type)}.
     */
    private static final TypeVisitor<Class, Void> eraser = new TypeVisitor<Class, Void>() {
        public Class onClass(Class c, Void _) {
            return c;
        }

        public Class onParameterizdType(ParameterizedType p, Void _) {
            //why getRawType returns Type? not Class?
            return visit(p.getRawType(), null);
        }

        public Class onGenericArray(GenericArrayType g, Void _) {
            return Array.newInstance(
                    visit(g.getGenericComponentType(), null),
                    0).getClass();
        }

        public Class onVariable(TypeVariable v, Void _) {
            return visit(v.getBounds()[0], null);
        }

        public Class onWildcard(WildcardType w, Void _) {
            return visit(w.getUpperBounds()[0], null);
        }
    };

    /**
     * Returns the runtime representation of the given type.
     *
     * This corresponds to the notion of the erasure in JSR-14.
     *
     * <p>
     * Because of the difference in the way APT and the Java reflection
     * treats primitive type and array type, we can't define this method
     * on {@link Navigator}.
     *
     * <p>
     * It made me realize how difficult it is to define the common navigation
     * layer for two different underlying reflection library. The other way
     * is to throw away the entire parameterization and go to the wrapper approach.
     */
    @SuppressWarnings("unchecked")
	public <T> Class<T> erasure(Type t) {
    	return (Class<T>) GenericUtils.getRawClass(t);
        //return eraser.visit(t, null);
    }

    public boolean isAbstract(ClassEx clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    public boolean isFinal(ClassEx clazz) {
        return Modifier.isFinal(clazz.getModifiers());
    }

    /**
     * Returns the {@link Type} object that represents {@code clazz&lt;T1,T2,T3>}.
     */
    public Type createParameterizedType(ClassEx rawType, Type... arguments) {
        return new ParameterizedTypeImpl(rawType.getWrappered(), arguments, null);
    }

    public boolean isArray(Type t) {
        if (t instanceof Class) {
            Class c = (Class) t;
            return c.isArray();
        }
        if (t instanceof GenericArrayType) {
            return true;
        }
        return false;
    }

    public boolean isArrayButNotByteArray(Type t) {
        if (t instanceof Class) {
            Class c = (Class) t;
            return c.isArray() && c != byte[].class;
        }
        if (t instanceof GenericArrayType) {
            t = ((GenericArrayType) t).getGenericComponentType();
            return t != Byte.TYPE;
        }
        return false;
    }

    public Type getComponentType(Type t) {
        if (t instanceof Class) {
            Class c = (Class) t;
            return c.getComponentType();
        }
        if (t instanceof GenericArrayType) {
            return ((GenericArrayType) t).getGenericComponentType();
        }

        throw new IllegalArgumentException();
    }

    public Type getTypeArgument(Type type, int i) {
        if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            return fix(p.getActualTypeArguments()[i]);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public boolean isParameterizedType(Type type) {
        return type instanceof ParameterizedType;
    }

    public boolean isPrimitive(Type type) {
        if (type instanceof Class) {
            Class c = (Class) type;
            return c.isPrimitive();
        }
        return false;
    }

    public Type getPrimitive(Class primitiveType) {
        assert primitiveType.isPrimitive();
        return primitiveType;
    }

    public Location getClassLocation(final ClassEx clazz) {
        return new Location() {
            @Override
            public String toString() {
                return clazz.getName();
            }
        };
    }

    public Location getFieldLocation(final FieldEx field) {
        return new Location() {
            @Override
            public String toString() {
                return field.getJavaField().toString();
            }
        };
    }

    public Location getMethodLocation(final MethodEx method) {
        return new Location() {
            @Override
            public String toString() {
                return method.getJavaMethod().toString();
            }
        };
    }

    public boolean hasDefaultConstructor(ClassEx c) {
        try {
            c.getDeclaredConstructor();
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public boolean isStaticField(FieldEx field) {
        return Modifier.isStatic(field.getModifiers());
    }

    public boolean isPublicMethod(MethodEx method) {
        return Modifier.isPublic(method.getModifiers());
    }

    public boolean isPublicField(FieldEx field) {
        return Modifier.isPublic(field.getModifiers());
    }

    public boolean isEnum(ClassEx c) {
        return Enum.class.isAssignableFrom(c.getWrappered());
    }

    public FieldEx[] getEnumConstants(ClassEx clazz) {
        try {
            Object[] values = clazz.getEnumConstants();
            FieldEx[] fields = new FieldEx[values.length];
            for (int i = 0; i < values.length; i++) {
            	String name=((Enum) values[i]).name();
            	FieldEx field = clazz.getField(name);
            	if(field==null)throw new NoSuchFieldException(name);
            	fields[i]=field;
            }
            return fields;
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }

    public Type getVoidType() {
        return Void.class;
    }

    public String getPackageName(ClassEx clazz) {
        String name = clazz.getName();
        int idx = name.lastIndexOf('.');
        if (idx < 0) {
            return "";
        } else {
        	String pkgName=name.substring(0, idx);
            return pkgName;
        }
    }

    public ClassEx findClass(String className, ClassEx referencePoint) {
        try {
            ClassLoader cl = referencePoint.getWrappered().getClassLoader();
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }
            return new ClassEx(cl.loadClass(className));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public boolean isBridgeMethod(MethodEx method) {
        return method.isBridge();
    }

    public boolean isOverriding(MethodEx method, ClassEx base) {
        // this isn't actually correct,
        // as the JLS considers
        // class Derived extends Base<Integer> {
        //   Integer getX() { ... }
        // }
        // class Base<T> {
        //   T getX() { ... }
        // }
        // to be overrided. Handling this correctly needs a careful implementation

        String name = method.getName();
        Class[] params = method.getParameterTypes();

        while (base != null) {
            try {
                if (base.getDeclaredMethod(name, params) != null) {
                    return true;
                }
            } catch (NoSuchMethodException e) {
                // recursively go into the base class
            }
            base = base.getSuperclass();
        }

        return false;
    }

    public boolean isInterface(ClassEx clazz) {
        return clazz.isInterface();
    }

    public boolean isTransient(FieldEx f) {
        return Modifier.isTransient(f.getModifiers());
    }

    public boolean isInnerClass(ClassEx clazz) {
        return clazz.getEnclosingClass() != null && !Modifier.isStatic(clazz.getModifiers());
    }

    /**
     * JDK 5.0 has a bug of creating {@link GenericArrayType} where it shouldn't.
     * fix that manually to work around the problem.
     *
     * See bug 6202725.
     */
    private Type fix(Type t) {
        if (!(t instanceof GenericArrayType)) {
            return t;
        }

        GenericArrayType gat = (GenericArrayType) t;
        if (gat.getGenericComponentType() instanceof Class) {
            Class c = (Class) gat.getGenericComponentType();
            return Array.newInstance(c, 0).getClass();
        }

        return t;
    }
}
