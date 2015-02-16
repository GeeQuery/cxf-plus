package com.github.cxfplus.jaxws;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@WebService
@Path("/hello")
public interface HelloService {

	@WebMethod
	@Path("/{name}")
	@GET
	String sayHello( @PathParam("name") String name);
}
