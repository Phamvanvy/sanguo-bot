package com.pip.uieditor.model.code;

public class BinaryExpression extends Expression{
	
	private Expression left, right;
	private String operation;
	
	public BinaryExpression(Expression left, Expression right, String operation) {
		this.left = left;
		this.right = right;
		this.operation = operation;
	}
	
	public Expression getLeft() {
		return this.left;
	}
	
	public Expression getRight() {
		return this.right;
	}
	
	public String getOperation() {
		return operation;
	}
	
}
