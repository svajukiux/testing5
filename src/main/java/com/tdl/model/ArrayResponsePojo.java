package com.tdl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ArrayResponsePojo {
	private User[] data;
	private String message;
	
	public User[] getData() {
		return data;
	}
	public void setData(User[] data) {
		this.data = data;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	
	public ArrayResponsePojo(){
	}
	@JsonCreator
	public ArrayResponsePojo(@JsonProperty("data") User[] data, @JsonProperty ("message") String message) {
		super();
		this.data = data;
		this.message = message;
	}
	
}
