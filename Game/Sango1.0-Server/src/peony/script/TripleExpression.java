package peony.script;

/**
 * 三项表达式，处理IF函数用的，跟java的?:操作符同样处理
 * @author Jeffrey
 *
 */
public class TripleExpression implements Expression{

	protected Expression one,two,three;
	
	public TripleExpression(Expression one,Expression two,Expression three){
		this.one = one;
		this.two = two;
		this.three = three;
	}

	public Value getValue(Context ctx) {
		if(one.getValue(ctx).intValue()==1)
			return two.getValue(ctx);
		else 
			return three.getValue(ctx);
	}
	
	public Expression[] getExpressions(){
		return new Expression[]{one,two,three};
	}
	
	public int getReturnType(){
		return Value.INT;
	}
}
