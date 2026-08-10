package peony.script;

/**
 * 双项表达式，包括左边表达式，右边表达式以及操作符
 * @author Jeffrey
 *
 */
public class BinaryExpression implements Expression{
	
	protected Expression left,right;
	protected int operator;
	
	public BinaryExpression(int operator,Expression left,Expression right){
		this.operator = operator;
		this.left = left;
		this.right = right;
	}
	
	public Value getValue(Context ctx){
		int ret = 0;
		switch(operator){
		case Operator.EQ:
			ret = left.getValue(ctx).intValue()==right.getValue(ctx).intValue()?1:0;
			break;
		case Operator.LT:
			ret = left.getValue(ctx).intValue()<right.getValue(ctx).intValue()?1:0;
			break;
		case Operator.GT:
			ret = left.getValue(ctx).intValue()>right.getValue(ctx).intValue()?1:0;
			break;
		case Operator.LE:
			ret = left.getValue(ctx).intValue()<=right.getValue(ctx).intValue()?1:0;
			break;
		case Operator.GE:
			ret = left.getValue(ctx).intValue()<=right.getValue(ctx).intValue()?1:0;
			break;
		case Operator.NE:
			ret = left.getValue(ctx).intValue()!=right.getValue(ctx).intValue()?1:0;
			break;
		default:
			throw new IllegalArgumentException();
		}
		return new Value(ret);
	}
	
	public int getOperator(){
		return operator;
	}
	
	public Expression[] getExpressions(){
		return new Expression[]{left,right};
	}
	
	public int getReturnType(){
		return Value.INT;
	}
}
