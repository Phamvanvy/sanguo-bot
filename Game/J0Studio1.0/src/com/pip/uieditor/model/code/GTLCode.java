package com.pip.uieditor.model.code;

import java.util.ArrayList;
import java.util.List;

public class GTLCode {
	
	private String name;
	
	private Data data;
	
	private List<Function> functions;
	
	public GTLCode(String name) {
		this.name = name;
		this.data = new Data();
		this.functions = new ArrayList<Function>();
	}
	
	public Data getData() {
		return data;
	}
	
	public void addFunction(Function function) {
		this.functions.add(function);
	}
	
	public String getName() {
		return name;
	}
	
	public List<Function> getFunctions() {
		return this.functions;
	}
}
