package com.pip.itimes.net;

import java.util.HashMap;
import java.util.Map;


public class Config {
	
	private Map properties;

	public Config(){
		properties = new HashMap();
	}
	
	public Object getProperty(String key){
		return properties.get(key);
	}
	public void setProperty(String key,Object value){
		properties.put(key,value);
	}
}
