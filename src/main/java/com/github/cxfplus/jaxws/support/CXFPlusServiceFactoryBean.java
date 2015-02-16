/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.github.cxfplus.jaxws.support;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebMethod;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import jef.tools.StringUtils;
import jef.tools.reflect.ClassEx;
import jef.tools.reflect.GenericUtils;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.resource.ResourceManager;
import org.apache.cxf.service.factory.AbstractServiceConfiguration;
import org.apache.cxf.service.factory.FactoryBeanListener.Event;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.service.model.UnwrappedOperationInfo;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;

import com.github.cxfplus.jaxbplus.JAXBDataBinding;
import com.github.cxfplus.jaxws.ef.PlusWrapperClassGenerator;


/**
 */
@NoJSR250Annotations
public class CXFPlusServiceFactoryBean extends org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean {
    private boolean wrapperBeanGenerated;
    private Set<Class<?>> wrapperClasses;
    private static final Logger LOG = LogUtils.getLogger(JaxWsServiceFactoryBean.class);

    @Override
    public void reset() {
        super.reset();
        wrapperBeanGenerated = false;
        wrapperClasses = null;
    }

    @Override
    protected Set<Class<?>> getExtraClass() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        if (!wrapperBeanGenerated) {
            wrapperClasses = generatedWrapperBeanClass();
        }
        if (wrapperClasses != null) {
            classes.addAll(wrapperClasses);
        }

        XmlSeeAlso xmlSeeAlsoAnno = getServiceClass().getAnnotation(XmlSeeAlso.class);

        if (xmlSeeAlsoAnno != null && xmlSeeAlsoAnno.value() != null) {
            for (int i = 0; i < xmlSeeAlsoAnno.value().length; i++) {
                Class<?> value = xmlSeeAlsoAnno.value()[i];
                if (value == null) {
                    LOG.log(Level.WARNING, "XMLSEEALSO_NULL_CLASS",
                            new Object[] {getServiceClass().getName(), i});
                } else {
                    classes.add(value);
                }

            }
        }
        return classes;
    }

    private Set<Class<?>> generatedWrapperBeanClass() {
        DataBinding b = getDataBinding();
        if (b.getClass().getName().endsWith("JAXBDataBinding")
            && schemaLocations == null) {
            ServiceInfo serviceInfo = getService().getServiceInfos().get(0);
            PlusWrapperClassGenerator wrapperGen = new PlusWrapperClassGenerator(this,
                                                                         serviceInfo.getInterface(),
                                                                         getQualifyWrapperSchema());
            return wrapperGen.generate();
        }
        return Collections.emptySet();
    }
    
    protected boolean isValidMethod(final Method method) {
        for (AbstractServiceConfiguration c : getServiceConfigurations()) {
        	WebMethod wm = method.getAnnotation(WebMethod.class);
        	if(wm!=null && method.getDeclaringClass().isInterface() && wm.exclude()){
        		return false;
        	}
            Boolean b = c.isOperation(method);
            if (b != null) {
                return b.booleanValue();
            }
        }
        return true;
    }
    
    private Type[] getMethodGenericTypes(Method method) {
    	ClassEx clz=new ClassEx(getServiceClass());
        return clz.getMethodParamTypes(method) ;
 	}

	private Type getGenericReturnType(Method method) {
		ClassEx clz=new ClassEx(getServiceClass());
		return clz.getMethodReturnType(method);
	}

    
    protected void createMessageParts(InterfaceInfo intf, OperationInfo op, Method method) {
        final Type[] paramTypes =getMethodGenericTypes(method) ;
        final Class<?>[] paramClasses=new Class[paramTypes.length];
        for(int i=0;i<paramTypes.length;i++){
        	paramClasses[i]=GenericUtils.getRawClass(paramTypes[i]);
        }
        // Setup the input message
        op.setProperty(METHOD, method);
        MessageInfo inMsg = op.createMessage(this.getInputMessageName(op, method), MessageInfo.Type.INPUT);
        op.setInput(inMsg.getName().getLocalPart(), inMsg);
        for (int j = 0; j < paramClasses.length; j++) {
            if (Exchange.class.equals(paramClasses[j])) {
                continue;
            }
            if (isInParam(method, j)) {
                final QName q = getInParameterName(op, method, j);
                MessagePartInfo part = inMsg.addMessagePart(getInPartName(op, method, j));

                initializeParameter(part, paramClasses[j], paramTypes[j]);
                //TODO:remove method param annotations
                part.setProperty(METHOD_PARAM_ANNOTATIONS, method.getParameterAnnotations());
                part.setProperty(PARAM_ANNOTATION, method.getParameterAnnotations()[j]);
                if (getJaxbAnnoMap(part).size() > 0) {
                    op.setProperty(WRAPPERGEN_NEEDED, true);
                }
                if (!isWrapped(method) && !isRPC(method)) {
                    part.setProperty(ELEMENT_NAME, q);
                }

                if (isHeader(method, j)) {
                    part.setProperty(HEADER, Boolean.TRUE);
                    if (isRPC(method) || !isWrapped(method)) {
                        part.setElementQName(q);
                    } else {
                        part.setProperty(ELEMENT_NAME, q);
                    }
                }
                part.setIndex(j);
            }
        }
        sendEvent(Event.OPERATIONINFO_IN_MESSAGE_SET, op, method, inMsg);

        boolean hasOut = hasOutMessage(method);
        if (hasOut) {
            // Setup the output message
            MessageInfo outMsg = op.createMessage(createOutputMessageName(op, method),
                                                  MessageInfo.Type.OUTPUT);
            op.setOutput(outMsg.getName().getLocalPart(), outMsg);
            final Class<?> returnType = GenericUtils.getRawClass(this.getGenericReturnType(method));
            if (!returnType.isAssignableFrom(void.class)) {
                final QName q = getOutPartName(op, method, -1);
                final QName q2 = getOutParameterName(op, method, -1);
                MessagePartInfo part = outMsg.addMessagePart(q);
                Type rType=this.getGenericReturnType(method);
                initializeParameter(part,GenericUtils.getRawClass(rType),rType);
                if (!isRPC(method) && !isWrapped(method)) {
                    part.setProperty(ELEMENT_NAME, q2);
                }
                part.setProperty(METHOD_ANNOTATIONS, method.getAnnotations());
                part.setProperty(PARAM_ANNOTATION, method.getAnnotations());
                if (isHeader(method, -1)) {
                    part.setProperty(HEADER, Boolean.TRUE);
                    if (isRPC(method) || !isWrapped(method)) {
                        part.setElementQName(q2);
                    } else {
                        part.setProperty(ELEMENT_NAME, q2);
                    }
                }

                part.setIndex(0);
            }

            for (int j = 0; j < paramClasses.length; j++) {
                if (Exchange.class.equals(paramClasses[j])) {
                    continue;
                }
                if (isOutParam(method, j)) {
                    if (outMsg == null) {
                        outMsg = op.createMessage(createOutputMessageName(op, method),
                                                  MessageInfo.Type.OUTPUT);
                    }
                    QName q = getOutPartName(op, method, j);
                    QName q2 = getOutParameterName(op, method, j);

                    if (isInParam(method, j)) {
                        q = op.getInput().getMessagePartByIndex(j).getName();
                        q2 = (QName)op.getInput().getMessagePartByIndex(j).getProperty(ELEMENT_NAME);
                        if (q2 == null) {
                            q2 = op.getInput().getMessagePartByIndex(j).getElementQName();
                        }
                    }

                    MessagePartInfo part = outMsg.addMessagePart(q);
                    part.setProperty(METHOD_PARAM_ANNOTATIONS, method.getParameterAnnotations());
                    part.setProperty(PARAM_ANNOTATION, method.getParameterAnnotations()[j]);
                    initializeParameter(part, paramClasses[j], paramTypes[j]);
                    part.setIndex(j + 1);

                    if (!isRPC(method) && !isWrapped(method)) {
                        part.setProperty(ELEMENT_NAME, q2);
                    }

                    if (isInParam(method, j)) {
                        part.setProperty(MODE_INOUT, Boolean.TRUE);
                    }
                    if (isHeader(method, j)) {
                        part.setProperty(HEADER, Boolean.TRUE);
                        if (isRPC(method) || !isWrapped(method)) {
                            part.setElementQName(q2);
                        } else {
                            part.setProperty(ELEMENT_NAME, q2);
                        }
                    }
                }
            }
            sendEvent(Event.OPERATIONINFO_OUT_MESSAGE_SET, op, method, outMsg);
        }

        //setting the parameterOrder that
        //allows preservation of method signatures
        //when doing java->wsdl->java
        setParameterOrder(method, paramClasses, op);

        if (hasOut) {
            // Faults are only valid if not a one-way operation
            initializeFaults(intf, op, method);
        }
    }

    

    /**
     * set the holder generic type info into message part info
     *
     * @param o
     * @param method
     */
    protected boolean initializeClassInfo(OperationInfo o, Method method, List<String> paramOrder) {
        OperationInfo origOp = o;
        if (isWrapped(method)) {
            if (o.getUnwrappedOperation() == null) {
                //the "normal" algorithm didn't allow for unwrapping,
                //but the annotations say unwrap this.   We'll need to
                //make it.
                WSDLServiceBuilder.checkForWrapped(o, true);
            }
            if (o.getUnwrappedOperation() != null) {
                if (o.hasInput()) {
                    MessageInfo input = o.getInput();
                    MessagePartInfo part = input.getMessageParts().get(0);
                    part.setTypeClass(getRequestWrapper(method));
                    part.setProperty("REQUEST.WRAPPER.CLASSNAME", getRequestWrapperClassName(method));
                    part.setIndex(0);
                }

                if (o.hasOutput()) {
                    MessageInfo input = o.getOutput();
                    MessagePartInfo part = input.getMessageParts().get(0);
                    part.setTypeClass(getResponseWrapper(method));
                    part.setProperty("RESPONSE.WRAPPER.CLASSNAME", getResponseWrapperClassName(method));
                    part.setIndex(0);
                }
                setFaultClassInfo(o, method);
                o = o.getUnwrappedOperation();
            } else {
                LOG.warning(new Message("COULD_NOT_UNWRAP", LOG, o.getName(), method).toString());
                setFaultClassInfo(o, method);
            }
        } else if (o.isUnwrappedCapable()) {
            // remove the unwrapped operation because it will break the
            // the WrapperClassOutInterceptor, and in general makes
            // life more confusing
            o.setUnwrappedOperation(null);

            setFaultClassInfo(o, method);
        }
        o.setProperty(METHOD_PARAM_ANNOTATIONS, method.getParameterAnnotations());
        o.setProperty(METHOD_ANNOTATIONS, method.getAnnotations());
        Type[] genericTypes = getMethodGenericTypes(method);
        Class<?>[] paramClasses = new Class[genericTypes.length];
        for (int i = 0; i < genericTypes.length; i++) {
        	paramClasses[i]=GenericUtils.getRawClass(genericTypes[i]);
            if (Exchange.class.equals(paramClasses[i])) {
                continue;
            }
            Class paramType = paramClasses[i];
            Type genericType = genericTypes[i];
            if (!initializeParameter(o, method, i, paramType, genericType)) {
                return false;
            }
        }
        sendEvent(Event.OPERATIONINFO_IN_MESSAGE_SET, origOp, method, origOp.getInput());
        // Initialize return type
        Type genericType = getGenericReturnType(method);
        Class paramType = GenericUtils.getRawClass(genericType);
        if (o.hasOutput()
            && !initializeParameter(o, method, -1, paramType, genericType)) {
            return false;
        }
        if (origOp.hasOutput()) {
            sendEvent(Event.OPERATIONINFO_OUT_MESSAGE_SET, origOp, method, origOp.getOutput());
        }

        setFaultClassInfo(o, method);
        return true;
    }
    protected DataBinding createDefaultDataBinding() {

        DataBinding retVal = null;

        if (getServiceClass() != null) {
            org.apache.cxf.annotations.DataBinding db
                = getServiceClass().getAnnotation(org.apache.cxf.annotations.DataBinding.class);
            if (db != null) {
                try {
                    if (!StringUtils.isEmpty(db.ref())) {
                        return getBus().getExtension(ResourceManager.class).resolveResource(db.ref(),
                                                                                            db.value());
                    }
                    retVal = db.value().newInstance();
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Could not create databinding "
                            + db.value().getName(), e);
                }
            }
        }
        if (retVal == null) {
            JAXBDataBinding db = new JAXBDataBinding(getQualifyWrapperSchema());
            Map props = this.getProperties();
            if (props != null && props.get("jaxb.additionalContextClasses") != null) {
                Object o = this.getProperties().get("jaxb.additionalContextClasses");
                if (o instanceof Type) {
                    o = new Type[] {(Type)o};
                }
                Type[] extraClass = (Type[])o;
                db.setExtraClass(extraClass);
            }
            retVal = db;
        }
        return retVal;
    }

	private boolean initializeParameter(OperationInfo o, Method method, int i,
                                     Class paramType, Type genericType) {
        boolean isIn = isInParam(method, i);
        boolean isOut = isOutParam(method, i);
        boolean isHeader = isHeader(method, i);
        Annotation[] paraAnnos = null;
        if (i != -1 && o.getProperty(METHOD_PARAM_ANNOTATIONS) != null) {
            Annotation[][] anns = (Annotation[][])o.getProperty(METHOD_PARAM_ANNOTATIONS);
            paraAnnos = anns[i];
        } else if (i == -1 && o.getProperty(METHOD_ANNOTATIONS) != null) {
            paraAnnos = (Annotation[])o.getProperty(METHOD_ANNOTATIONS);
        }

        MessagePartInfo part = null;
        if (isIn && !isOut) {
            QName name = getInPartName(o, method, i);
            part = o.getInput().getMessagePart(name);
            if (part == null && isFromWsdl()) {
                part = o.getInput().getMessagePartByIndex(i);
            }
            if (part == null && isHeader && o.isUnwrapped()) {
                part = ((UnwrappedOperationInfo)o).getWrappedOperation().getInput().getMessagePart(name);
                if (part != null) {
                    //header part in wsdl, need to get this mapped in to the unwrapped form
                    MessagePartInfo inf = o.getInput().addMessagePart(part.getName());
                    inf.setTypeQName(part.getTypeQName());
                    inf.setElement(part.isElement());
                    inf.setElementQName(part.getElementQName());
                    inf.setConcreteName(part.getConcreteName());
                    inf.setXmlSchema(part.getXmlSchema());
                    part = inf;
                    inf.setProperty(HEADER, Boolean.TRUE);
                }
            }
            if (part == null) {
                return false;
            }
            initializeParameter(part, paramType, genericType);

            part.setIndex(i);
        } else if (!isIn && isOut) {
            QName name = getOutPartName(o, method, i);
            part = o.getOutput().getMessagePart(name);
            if (part == null && isFromWsdl()) {
                part = o.getOutput().getMessagePartByIndex(i + 1);
            }
            if (part == null) {
                return false;
            }
            part.setProperty(ReflectionServiceFactoryBean.MODE_OUT, Boolean.TRUE);
            initializeParameter(part, paramType, genericType);
            part.setIndex(i + 1);
        } else if (isIn && isOut) {
            QName name = getInPartName(o, method, i);
            part = o.getInput().getMessagePart(name);
            if (part == null && this.isFromWsdl()) {
                part = o.getInput().getMessagePartByIndex(i);
            }
            if (part == null && isHeader && o.isUnwrapped()) {
                part = o.getUnwrappedOperation().getInput().getMessagePart(name);
            }
            if (part == null) {
                return false;
            }
            part.setProperty(ReflectionServiceFactoryBean.MODE_INOUT, Boolean.TRUE);
            initializeParameter(part, paramType, genericType);
            part.setIndex(i);

            part = o.getOutput().getMessagePart(name);
            if (part == null) {
                return false;
            }
            part.setProperty(ReflectionServiceFactoryBean.MODE_INOUT, Boolean.TRUE);
            initializeParameter(part, paramType, genericType);
            part.setIndex(i + 1);
        }
        if (paraAnnos != null && part != null) {
            part.setProperty(PARAM_ANNOTATION, paraAnnos);
        }

        return true;
    }
    private void setFaultClassInfo(OperationInfo o, Method selected) {
        Class[] types = selected.getExceptionTypes();
        for (int i = 0; i < types.length; i++) {
            Class exClass = types[i];
            Class beanClass = getBeanClass(exClass);
            if (beanClass == null) {
                continue;
            }

            QName name = getFaultName(o.getInterface(), o, exClass, beanClass);

            for (FaultInfo fi : o.getFaults()) {
                for (MessagePartInfo mpi : fi.getMessageParts()) {
                    String ns = null;
                    if (mpi.isElement()) {
                        ns = mpi.getElementQName().getNamespaceURI();
                    } else {
                        ns = mpi.getTypeQName().getNamespaceURI();
                    }
                    if (mpi.getConcreteName().getLocalPart().equals(name.getLocalPart())
                        && name.getNamespaceURI().equals(ns)) {
                        fi.setProperty(Class.class.getName(), exClass);
                        mpi.setTypeClass(beanClass);
                        sendEvent(Event.OPERATIONINFO_FAULT, o, exClass, fi);
                    }
                }
            }
        }
    }

    private void setParameterOrder(Method method, Class[] paramClasses, OperationInfo op) {
        if (isRPC(method) || !isWrapped(method)) {
            List<String> paramOrdering = new LinkedList<String>();
            boolean hasOut = false;
            for (int j = 0; j < paramClasses.length; j++) {
                if (Exchange.class.equals(paramClasses[j])) {
                    continue;
                }
                if (isInParam(method, j)) {
                    paramOrdering.add(getInPartName(op, method, j).getLocalPart());
                    if (isOutParam(method, j)) {
                        hasOut = true;
                    }
                } else if (isOutParam(method, j)) {
                    hasOut = true;
                    paramOrdering.add(getOutPartName(op, method, j).getLocalPart());
                }
            }
            if (!paramOrdering.isEmpty() && hasOut) {
                op.setParameterOrdering(paramOrdering);
            }
        }
    }
    private Map<Class, Boolean> getJaxbAnnoMap(MessagePartInfo mpi) {
        Map<Class, Boolean> map = new ConcurrentHashMap<Class, Boolean>();
        Annotation[] anns = getMethodParameterAnnotations(mpi);

        if (anns != null) {
            for (Annotation anno : anns) {
                if (anno instanceof XmlList) {
                    map.put(XmlList.class, true);
                }
                if (anno instanceof XmlAttachmentRef) {
                    map.put(XmlAttachmentRef.class, true);
                }
                if (anno instanceof XmlJavaTypeAdapter) {
                    map.put(XmlJavaTypeAdapter.class, true);
                }
                if (anno instanceof XmlElementWrapper) {
                    map.put(XmlElementWrapper.class, true);
                }
            }
        }
        return map;
    }
    

    private Annotation[] getMethodParameterAnnotations(final MessagePartInfo mpi) {
        Annotation[][] paramAnno = (Annotation[][])mpi.getProperty(METHOD_PARAM_ANNOTATIONS);
        int index = mpi.getIndex();
        if (paramAnno != null && index < paramAnno.length && index >= 0) {
            return paramAnno[index];
        }
        return null;
    }
}
