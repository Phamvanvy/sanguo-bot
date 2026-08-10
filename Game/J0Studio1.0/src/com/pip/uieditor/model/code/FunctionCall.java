package com.pip.uieditor.model.code;

import java.util.ArrayList;
import java.util.List;

public class FunctionCall {
	
	private String function;
	private List<Expression> parameters;
	
	public FunctionCall(String function) {
		this.function = function;
		this.parameters = new ArrayList<Expression>();
	}
	
	public String getFunction() {
		return function;
	}
	
	public List<Expression> getParameers() {
		return this.parameters;
	}
	
	public void addExpression(Expression expression) {
		this.parameters.add(expression);
	}
}
