package peony.script;

import java.util.ArrayList;
import java.util.List;

public class Function implements Expression{

	protected String name;
	protected List<Expression> expressions = new ArrayList<Expression>();
	
	public Function(String name){
		this.name = name;
	}
	
	public void addExpression(Expression expression){
		expressions.add(expression);
	}
	
	public Value getValue(Context ctx){
		Value[] values = new Value[expressions.size()];
		for(int i=0,size=expressions.size();i<size;i++){
			values[i] = expressions.get(i).getValue(ctx);
		}
		return ctx.call(name, values);
	}
	
	public String getName(){
		return name;
	}
	
	public Expression[] getExpressions(){
		Expression[] ret = new Expression[expressions.size()];
		expressions.toArray(ret);
		return ret;
	}
	
	public int getReturnType(){
		return Value.INT;
	}
}
