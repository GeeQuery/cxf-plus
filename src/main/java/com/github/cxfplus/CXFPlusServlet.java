package com.github.cxfplus;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.ws.rs.Path;
import javax.xml.namespace.QName;

import org.apache.commons.lang.ArrayUtils;
import org.apache.cxf.BusFactory;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.cxfplus.core.util.StringUtils;
import com.github.cxfplus.jaxrs.FastJSONProvider;
import com.github.cxfplus.jaxws.interceptors.LoggingInInterceptor;
import com.github.cxfplus.jaxws.interceptors.LoggingOutInterceptor;
import com.github.cxfplus.jaxws.interceptors.TraceHandler;
import com.github.cxfplus.jaxws.support.CXFPlusServiceFactoryBean;
import com.github.cxfplus.jmx.CXFPlus;
import com.github.cxfplus.service.factory.CXFPlusServiceBean;
import com.github.cxfplus.support.DefaultImpl;
import com.github.cxfplus.support.ServiceDefinition;
import com.github.cxfplus.support.ServiceLookup;
import com.github.cxfplus.support.ServiceProcessor;
import com.github.cxfplus.support.SpringServletAnnotationLookup;
import com.github.cxfplus.support.SpringServletServcieLookup;

/**
 * 用来发布WebService的Servlet。和Spring集成，启动时查找所有IWebService接口的服务类，自动进行发布。
 * 
 * @author jiyi
 * 
 */
public class CXFPlusServlet extends CXFNonSpringServlet {
	private static final long serialVersionUID = 1L;
	private Logger log = LoggerFactory.getLogger(getClass());
	private ServiceLookup WSlookup;
	private ServiceLookup RSlookup;
	private ServiceProcessor serviceUtill;
	private String httpPrefix = "";

	public String getRestBasePath() {
		return restBasePath;
	}

	public void setRestBasePath(String restBasePath) {
		this.restBasePath = restBasePath;
	}

	/**
	 * 如果是SimpleMode就直接用ServerFactoryBean发布，不用JaxWs方式发布
	 */
	private boolean wsSimpleMode = false;
	private String[] wsNamePattern;

	private int restUseFastJson = 1; // 0 不使用 1 使用写 2 使用读写
	private boolean restJsonWithClassname;
	private String restBasePath = "/rest";
	private String[] restNamePattern;

	public String getHttpPrefix() {
		return httpPrefix;
	}

	public void setHttpPrefix(String httpPrefix) {
		this.httpPrefix = httpPrefix;
	}

	public void init() throws ServletException {
		super.init();
	}

	protected void loadBus(ServletConfig sc) {
		super.loadBus(sc);
		initLookup(sc);
		if (isRemoteMode()) {
			this.log.info("Current mode is remote work, no Jax-rs need to publish....");
			return;
		}
		initParam(sc);
		processWebservice();
		processJaxRs();
	}

	public ServiceLookup getWSlookup() {
		return WSlookup;
	}

	public void setWSlookup(ServiceLookup wSlookup) {
		WSlookup = wSlookup;
	}

	public ServiceLookup getRSlookup() {
		return RSlookup;
	}

	public void setRSlookup(ServiceLookup rSlookup) {
		RSlookup = rSlookup;
	}

	private void initLookup(ServletConfig sc) {
		// Use init-class to get RsLookup and WsLookup
		String clz = sc.getInitParameter("init-class");
		if (StringUtils.isNotEmpty(clz)) {
			try {
				Class<?> c = this.getClass().getClassLoader().loadClass(clz);
				Constructor<?> con = c.getConstructor(CXFPlusServlet.class);
				con.newInstance(this);
			} catch (Exception e) {
				log.error("process init-class error!", e);
			}
		}
		// Set default WsLookup
		if (WSlookup == null) {
			WSlookup = new SpringServletAnnotationLookup(getServletContext(), javax.jws.WebService.class);
		}
		// Set default RsLookup
		if (RSlookup == null) {
			try {
				Class<?> c = Class.forName("javax.ws.rs.Path");
				RSlookup = new SpringServletAnnotationLookup(getServletContext(), Path.class);
			} catch (ClassNotFoundException e) {
				log.warn("Class 'javax.ws.rs.Path' was not exist. JAX-RS not supported.");
			}
		}
	}

	public void processJaxRs() {
		if (RSlookup == null)
			return;
		log.debug("Start procesing the Restful-Services.");
		Map<Class<?>, ResourceProvider> resProviders = new HashMap<Class<?>, ResourceProvider>();
		for (ServiceDefinition def : RSlookup.getServices()) {
			if (def.getServiceClass() == null)
				continue;

			if (canPublish(def.getName(), restNamePattern)) {
				ServiceDefinition context = serviceUtill.processServiceDef(def);
				if (context != null) {
					resProviders.put(context.getServiceClass(), new SingletonResourceProvider(context.getServiceBean()));
				}
			}
		}
		// 计算完成开始发布
		if (!resProviders.isEmpty()) {
			doPublishRestful(resProviders);
		}
	}

	public void processWebservice() {
		/*
		 * Force use JEF's JAXB+ engine. 让JDK内置的JAXB实现查找器固定JEF实现上。目前暂不需用
		 * System.setProperty("javax.xml.bind.JAXBContext",
		 * "com.github.cxfplus.com.sun.xml.bind.v2.ContextFactory");
		 */
		log.debug("Start processing the WebServices.");
		for (ServiceDefinition def : WSlookup.getServices()) {
			if (def.getServiceClass() == null)
				continue;
			if (canPublish(def.getName(), wsNamePattern)) {
				try {
					doPublishWebservice(serviceUtill.processServiceDef(def));
				} catch (Throwable e) {
					this.log.error("The service " + def.getServiceClass() + " can not publish as a WebService.", e);
				}
			}
		}
	}

	public void initParam() {
		initParam(null);
		this.setBus(BusFactory.getDefaultBus());
	}

	private void initParam(ServletConfig sc) {
		if (sc == null) {
			CXFPlus.getSingleton().setTrace(true);
			this.wsSimpleMode = false;
		} else {
			CXFPlus.getSingleton().setTrace(StringUtils.toBoolean(sc.getInitParameter("trace"), false));
			this.wsSimpleMode = StringUtils.toBoolean(sc.getInitParameter("simpleMode"), false);
			this.wsNamePattern = StringUtils.split(sc.getInitParameter("ws-service-name"), ',');

			String restBasepath = sc.getInitParameter("rest-basepath");
			if (StringUtils.isNotEmpty(restBasepath)) {
				this.restBasePath = restBasepath;
			}
			this.restUseFastJson = StringUtils.toInt(sc.getInitParameter("rest-use-fastjson"), 2);
			this.restJsonWithClassname = StringUtils.toBoolean(sc.getInitParameter("rest-json-withclassname"), false);
			this.restNamePattern = StringUtils.split(sc.getInitParameter("rest-service-name"), ',');
		}
		if (WSlookup instanceof SpringServletServcieLookup) {
			this.serviceUtill = ((SpringServletServcieLookup) WSlookup).getWebServiceProcessor();
		}
		if (serviceUtill == null) {
			this.serviceUtill = DefaultImpl.getInstance();
		}
	}

	/*
	 * 判断是不是处理客户端模式
	 */
	private boolean isRemoteMode() {
		if (WSlookup instanceof SpringServletServcieLookup) {
			return ((SpringServletServcieLookup) WSlookup).isRemoteMode();
		}
		return false;
	}

	/*
	 * 判断是否要发布
	 */
	private boolean canPublish(String name, String[] namePatterns) {
		if ((namePatterns == null) || (namePatterns.length == 0))
			return true;
		for (String key : namePatterns) {
			if (key.length() == 0)
				return true;
			if (StringUtils.matches(name, key, true)) {
				return true;
			}
		}
		return false;
	}

	private void doPublishWebservice(ServiceDefinition service) {
		if (service == null)
			return;
		Object serviceBean = service.getServiceBean();
		Class<?> serviceClass = service.getServiceClass();
		String[] packageName = StringUtils.split(serviceClass.getPackage().getName(), ".");
		ArrayUtils.reverse(packageName);
		String servName = service.getServiceClass().getSimpleName();
		String address = httpPrefix + service.getPath();
		ServerFactoryBean sf;
		if (this.wsSimpleMode) {
			sf = new ServerFactoryBean(new CXFPlusServiceBean());
		} else {
			sf = new JaxWsServerFactoryBean(new CXFPlusServiceFactoryBean());
		}
		sf.setBus(getBus());
		sf.setServiceClass(serviceClass);
		sf.setServiceBean(serviceBean);
		sf.setServiceName(new QName("http://" + StringUtils.join(packageName, ".") + "/", servName));
		sf.setAddress(address);
		sf.getInInterceptors().add(new LoggingInInterceptor());
		sf.getOutInterceptors().add(new LoggingOutInterceptor());
		sf.create();
		log.info("Starting Webservice: " + address);
	}

	private void doPublishRestful(Map<Class<?>, ResourceProvider> springResources) {
		JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
		sf.setBus(getBus());
		for (Entry<Class<?>, ResourceProvider> e : springResources.entrySet()) {
			sf.setResourceClasses(e.getKey());
			sf.setResourceProvider(e.getKey(), e.getValue());
			log.info("Publishing JAX-RS Service {} at {}", e, httpPrefix + restBasePath);
		}
		sf.setAddress(httpPrefix + restBasePath);
		if (restUseFastJson == 1) {
			sf.setProvider(new FastJSONProvider(false, restJsonWithClassname));
		} else {
			sf.setProvider(new FastJSONProvider(true, restJsonWithClassname));
		}
		sf.getInInterceptors().add(new LoggingInInterceptor());
		sf.getOutInterceptors().add(new LoggingOutInterceptor());
		sf.create();

	}
}
