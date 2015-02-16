package com.github.cxfplus.jaxrs;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@WebService
@Path("/people")
public interface PeopleService {
	
	@WebMethod
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/{email}")
	@GET
	public @Valid People getPerson(@PathParam("email") final String email);
	
	/**
	 * 这个方法是不是会和meial方法重复？
	 * @param id
	 * @return
	 */
	@WebMethod
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}")
	@GET
	public People find(@PathParam("id") int id);
	
	
	@WebMethod
	@GET
	public String getDepartmentName(int id);
	
	@WebMethod
	@Produces(MediaType.APPLICATION_JSON)
	@Path("")
	@GET
	public List<People> getAll();
	
	@WebMethod
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public int create(People movie);

	@WebMethod
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("{id}")
	@PUT
	public boolean update(@PathParam("id") int id, People movie);
	
	@WebMethod
	@DELETE
	@Path("{id}") 
	public int delete(@PathParam("id") int id);
	
	
	@WebMethod
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Path("/findbyname/{name}")
	public List<People> findByName(@PathParam("name") String name);
}
