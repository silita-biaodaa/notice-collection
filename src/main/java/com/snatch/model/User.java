package com.snatch.model;

public class User {
	public static String USER_KEY="MI_SHU_STAFF_KEY";
	private String userId;
	private String name;
	
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String name(){
		return name;
	}
}
