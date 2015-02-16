package com.github.cxfplus.jaxrs;

import java.util.List;

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

/**
 * 当两个接口的地址一样时，可能因为consumeType不一致而造成POST请求失效，get请求依然有效。。。
 * @author jiyi
 *
 */
@Path("/peoplex")
public interface PeopleServiceXml {
	
	@Produces({ MediaType.APPLICATION_XML })
	@Path("/{email}")
	@GET
	public @Valid People getPerson(@PathParam("email") final String email);
	
	/**
	 * 这个方法是不是会和meial方法重复？
	 * @param id
	 * @return
	 */
	@Produces(MediaType.APPLICATION_XML)
	@Path("{id}")
	@GET
	public People find(@PathParam("id") int id);
	
	
	@GET
	public String getDepartmentName(int id);
	
	@Produces(MediaType.APPLICATION_XML)
	@Path("")
	@GET
	public List<People> getAll();
	
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	public int create(People movie);

	@Consumes(MediaType.APPLICATION_XML)
	@Path("{id}")
	@PUT
	public boolean update(@PathParam("id") int id, People movie);
	
	@DELETE
	@Path("{id}") 
	public int delete(@PathParam("id") int id);
	
	@Produces(MediaType.APPLICATION_XML)
	@GET
	@Path("/findbyname/{name}")
	public List<People> findByName(@PathParam("name") String name);
}
