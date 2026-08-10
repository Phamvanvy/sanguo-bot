package com.pip.uieditor.model.code;

public class VariableDeclaration extends Expression{
	
	public String type;
	
	public String variable;
	
	public VariableDeclaration(String type, String variable) {
		this.type = type;
		this.variable = variable;
	}
}
