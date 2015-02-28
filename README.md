cxf-plus

An add-on for apache cxf to support transfer map and generic datatypes.

---
##简要说明 (Introduction)
CXF Plus是在Apache开源服务框架项目 ​CXF 的基础上针对 泛型、支持的序列化对象种类等进行增强的框架。

1. 增强的反射机制，可以对接口和POJO对象中的嵌套泛型、继承泛型等进行完整的解析，从而使得带有各种复杂泛型的接口可以正确发布和传输。

2. 简化集成，针对目前大多数服务使用SpringFramework的特点，自动查找JAX-WS和JAX-RS注解进行服务发布，用户只需配置一个Servlet即可发布WebService。

3. 运维支持：支持在日志输出报文，报文格式化等，便于开发调试。同时报文日志开关和格式可用JMX进行动态调节。

---
###构建 (How to build)
* git clone https://github.com/geequery/cxf-plus.git
* cd cxf-plus && mvn install

You will get the 'cxf-plus.jar'.

---
###使用方法 (Usage)
1 .  Add CXFPlusServlet to web.xml.
web.xml
```
<!-- WS & RS publish -->
<servlet>
	<servlet-name>WebServiceServlet</servlet-name>
	<servlet-class>com.github.cxfplus.CXFPlusServlet</servlet-class>
	<init-param>
		<param-name>trace</param-name>
		<param-value>true</param-value>
	</init-param>
	<load-on-startup>2</load-on-startup>
</servlet>
<servlet-mapping>
	<servlet-name>WebServiceServlet</servlet-name>
	<url-pattern>/ws/*</url-pattern>
</servlet-mapping>
	
<!-- Spring framework -->
<context-param>
	<param-name>contextConfigLocation</param-name>
	<param-value>classpath:spring-beans.xml</param-value>
</context-param>
<listener>
	<listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>
```
2 . Write any service with JAX-WS or JAX-RS annotations. e.g.
HelloService.java
```
@WebService
@Path("/hello")
public interface HelloService {
	@WebMethod
	@Path("/{name}")
	@GET
	String sayHello( @PathParam("name") String name);
}
```
HelloServiceImpl.java
```
public class HelloServiceImpl implements HelloService {
	public String sayHello(String name) {
		return "Hello," + name;
	}
}
```
3 . Config serive into Spring application context.

spring-beans.xml

	<?xml version="1.0" encoding="UTF-8"?>
	<beans xmlns="http://www.springframework.org/schema/beans" 
		xsi:schemaLocation="http://www.springframework.org/schema/beans 
		http://www.springframework.org/schema/beans/spring-beans-3.0.xsd">
		<bean class="HelloServiceImpl" />
	</beans>

4 . Access 'http://host:port/context/ws', you will see all published services, enjoy!

---
###在Spring中远程调用代理 (客户端)
```
	<beans xmlns="http://www.springframework.org/schema/beans"
		xmlns:context="http://www.springframework.org/schema/context" 
		xmlns:p="http://www.springframework.org/schema/p"
		xsi:schemaLocation="http://www.springframework.org/schema/beans 
		http://www.springframework.org/schema/beans/spring-beans-3.0.xsd 
		http://www.springframework.org/schema/context 
		http://www.springframework.org/schema/context/spring-context-3.0.xsd ">

		<bean id="peopleServiceWs" class="com.github.cxfplus.client.factory.JaxWsProxyBeanFactory"
			p:url="http://host:port/cxf-context/ws/PeopleService" p:serviceInterface="HelloService" />
	</beans>
```
---
### 兼容性
CXF-PLUS版本号和CXF的版本号保持一致，相同版本的CXF-PLUS和CXF能够兼容。
比如CXF-PLUS 2.7.14能够和CXF 2.7.x 保持兼容，但不保证和CXF 2.6.x兼容。

---
###原理机制
WebService的构造一般分为代码优先或schema优先，前者是依据接口代码(SEI)生成服务模型，后者是使用WSDL生成服务模型。
而JAX-WS本质上规定了从SEI转化为服务模型的基本规则，CXF Plus也基于代码优先的规则，在Java代码到Web服务模型时插入了自己的转义方法。
因此CXF-Plus可以适用于WebService的客户端或者服务端，凡是基于代码优先的服务发布方式或者客户端调用方式中，都可以使用CXF-Plus来增加发布的数据格式。

CXF-Plus重写了从JAX-WS到JAXB的类型绑定过程，从而较好的支持了泛型的解析。
此外还扩充了JAXB的适配器，可以支持File, java.sql.Time,java.sql.Date,Locale,Charset等常见java对象的发布。支持Map的泛型处理等。

CXF-Plus还提供了更多的 java序列化和反序列化机制。从而使得传输文件、Map、复杂泛型接口成为可能。 最后CXF Plus中利用Alibaba FastJson的全数据序列化支持，可以对普通的Java Object进行完全的WebService发布传输。

---
###文档 (Doucumentation)
中文 
建设中