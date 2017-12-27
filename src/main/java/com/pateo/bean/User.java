package com.pateo.bean;

public class User {

	String userId ;
	String name ;
	Double age;
	public User() {
	}
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
 
	
	public Double getAge() {
		return age;
	}
	public void setAge(Double age) {
		this.age = age;
	}
	@Override
	public String toString() {
		return "User [userId=" + userId + ", name=" + name + ", age=" + age
				+ "]";
	}
	
}
