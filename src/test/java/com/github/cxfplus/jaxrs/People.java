package com.github.cxfplus.jaxrs;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="People")
public class People {
	private int id;
    @NotNull private String email;
    @NotNull private String firstName;
    @NotNull private String lastName;
        
    public People(String email,String firstName,String lastName) {
    	this.email=email;
    	this.firstName=firstName;
    	this.lastName=lastName;
    }
    
    
    public People() {
    }

    public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public People( final String email ) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail( final String email ) {
        this.email = email;
    }
    
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setFirstName( final String firstName ) {
        this.firstName = firstName;
    }
    
    public void setLastName( final String lastName ) {
        this.lastName = lastName;
    }


	@Override
	public String toString() {
		return firstName+" "+lastName+" ("+email+")";
	}     
    
    
}
