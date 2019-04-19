package com.tdl.model;

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
	public ArrayResponsePojo(User[] data, String message) {
		super();
		this.data = data;
		this.message = message;
	}
	
}
