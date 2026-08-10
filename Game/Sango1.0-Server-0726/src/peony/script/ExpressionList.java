package peony.script;

import java.util.ArrayList;
import java.util.List;

public class ExpressionList implements Node{

	public static final int CONDITIONS = 1;
	public static final int ACTIONS = 2;
	
	protected List<Expression> expressions = new ArrayList<Expression>();
	protected int type = CONDITIONS;
	
	public ExpressionList(){
		this(CONDITIONS);
	}
	
	public ExpressionList(int type){
		this.type = type;
	}

	public int getType(){
		return type;
	}
	
	public void setType(int type){
		this.type = type;
	}
	
	public void addExpression(Expression expression){
		expressions.add(expression);
	}
	
	public Expression[] getExpressions(){
		Expression[] ret = new Expression[expressions.size()];
		expressions.toArray(ret);
		return ret;
	}
	
	public Value getValue(Context ctx){
		if(type==CONDITIONS){
			for(Expression exp:expressions){
				if(exp.getValue(ctx).intValue()==0) //¶ÌÂ·
					return new Value(0);
			}
			return new Value(1);
		}else{
			for(Expression exp:expressions){
				exp.getValue(ctx);
			}
			return new Value(1);
		}
	}
}
