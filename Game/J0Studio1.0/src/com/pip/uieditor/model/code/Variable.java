package com.pip.uieditor.model.code;

public class Variable extends Expression{
	
	private String variable;
	
	public Variable(String variable) {
		this.variable = variable;
	}
	
	public String getVariable() {
		return this.variable;
	}
}
