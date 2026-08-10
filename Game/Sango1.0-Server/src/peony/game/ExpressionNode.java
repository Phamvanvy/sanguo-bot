package peony.game;

import peony.script.Expression;

public class ExpressionNode implements StringExpressionNode {
	public Expression value;
	
	public ExpressionNode(Expression value){
		this.value = value;
	}
}
