package peony.script;

import java.util.HashMap;
import java.util.Map;

public class ParseUtil {
	
	protected static final Map<String,Integer> operators = new HashMap<String,Integer>();
	
	static{
		operators.put("==", Operator.EQ);
		operators.put(">=", Operator.GE);
		operators.put("<=", Operator.LE);
		operators.put(">", Operator.GT);
		operators.put("<", Operator.LT);
		operators.put("!=", Operator.NE);
	}
	
	public static int getOperator(String s){
		return operators.get(s);
	}
	
	public static Expression getExpression(Token token){
		Expression ret = null;
		if(token.type==Token.NUM)
			ret = new Constant(Integer.parseInt(token.value));
		else if(token.type==Token.STRING_CONSTANT)
			ret = new Constant(token.value);
		else if(token.type==Token.STRING)
			ret = new Variable(token.value);
		return ret;
	}
}
