package com.pip.uieditor.model.code;

import java.util.ArrayList;
import java.util.List;

public class Function extends Expression{
	
	private String name;
	
	private List<Expression> expressions;
	
	public Function(String name) {
		this.name = name;
		this.expressions = new ArrayList<Expression>();
	}
	
	public String getName() {
		return name;
	}
	
	public List getExpressions(){
		return this.expressions;
	}
	
	public void addExpression(Expression expression){
		expressions.add(expression);
	}
}
