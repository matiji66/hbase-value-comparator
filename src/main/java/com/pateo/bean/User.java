package com.pateo.bean;

public class User {

	String userid ;
	String name ;
	Double age;
	public User() {
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
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
		return "User [userid=" + userid + ", name=" + name + ", age=" + age
				+ "]";
	}
	
}
