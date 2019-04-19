package com.tdl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
	 
	 private String email;
	 private String firstName;
	 private String lastName;
	 
	// @JsonCreator
	 //public User(@JsonProperty("email")String email,@JsonProperty("firstName") String firstName, @JsonProperty("lastName") String lastName) {
	//	super();
	//	this.email = email;
	//	this.firstName = firstName;
	//	this.lastName = lastName;
	//}
	 
	public String getEmail() {
	     return email;
	 }
	 public void setEmail(String email) {
	     this.email = email;
	 }
	 public String getFirstName() {
	     return firstName;
	 }
	 
	 public void setFirstName(String firstName) {
        this.firstName = firstName;
	 }
	 public String getLastName() {
        return lastName;
	 }
     public void setLastName(String lastName) {
	     this.lastName = lastName;
	 }
     public User(){
 	}

}
