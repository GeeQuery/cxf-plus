package com.github.cxfplus.jaxrs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class PeopleServiceImpl implements PeopleService,PeopleServiceXml {
	AtomicInteger count = new AtomicInteger();

	Map<Integer, People> data = new ConcurrentHashMap<Integer, People>();

	
	public PeopleServiceImpl() {
		People person=new People("admin@hikvision.com","admin","administrator");
		create(person);
	}

	public People getPerson(String email) {
		for (People p : data.values()) {
			if (email.contains(p.getEmail())) {
				return p;
			}
		}
		throw new WebApplicationException(Response.Status.NOT_FOUND);
	}

	public People find(int id) {
		People p = data.get(id);
		if (p != null)
			return p;
		throw new WebApplicationException(Response.Status.NOT_FOUND);
	}

	public String getDepartmentName(int id) {
		return null;
	}

	public List<People> getAll() {
		return new ArrayList<People>(data.values());
	}

	public int create(People movie) {
		movie.setId(count.incrementAndGet());
		data.put(movie.getId(), movie);
		return movie.getId();
	}

	public boolean update(int id, People movie) {
		People p = data.get(id);
		if (p == null)
			return false;
		p.setEmail(movie.getEmail());
		p.setFirstName(movie.getFirstName());
		p.setLastName(movie.getLastName());
		return true;
	}

	public int delete(int id) {
		People p = data.remove(id);
		if (p == null)
			return 0;
		return 1;
	}

	public List<People> findByName(String name) {
		List<People> result = new ArrayList<People>();
		for(People p: data.values()){
			if(name.contains(p.getFirstName()) || name.contains(p.getLastName())){
				result.add(p);
			}
		}
		return result;
	}
}
