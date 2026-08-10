package com.pip.server.account.entity;

import java.util.HashMap;
import java.util.Map;

public class Entity {
	
	private Map<String,Property> values = new HashMap<String,Property>(); 

	public void setString(String name,String value){
		Property pro = new Property(name,value);
		values.put(name, pro);
	}
	
	public String getString(String name){
		Property pro = values.get(name);
		if(pro!=null)
			return (String)pro.getValue();
		return null;
	}
	
	public void setInt(String name,int value){
		Property pro = new Property(name,value);
		values.put(name,pro);
	}
	
	public void setInteger(String name,Integer value){
		Property pro = new Property(name,value);
		values.put(name, pro);
	}
	
	public Integer getInteger(String name){
		Property pro = values.get(name);
		if(pro!=null)
			return (Integer)pro.getValue();
		return null;
	}
	
	public void setObject(String name,Object value){
		Property pro = new Property(name,value);
		values.put(name, pro);
	}
	
	public Object getObject(String name){
		Property pro = values.get(name);
		if(pro!=null)
			return pro.getValue();
		return null;
	}
	
}
