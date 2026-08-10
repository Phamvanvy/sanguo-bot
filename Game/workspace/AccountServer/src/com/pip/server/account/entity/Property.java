package com.pip.server.account.entity;

public class Property{
	private String name;
	private Object value;
	
	public Property(String name){
		this(name,null);
	}
	
	public Property(String name,Object value){
		this.name = name;
		this.value = value;
	}
	
	public String getName(){
		return name;
	}
	
	public Object getValue(){
		return value;
	}
	
	public void setValue(Object value){
		this.value = value;
	}
}
