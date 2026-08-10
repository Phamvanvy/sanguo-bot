package peony.script;

public class Constant implements Expression {

	protected Value value;
	
	public Constant(int value){
		this.value = new Value(value);
	}
	
	public Constant(String value){
		this.value = new Value(value);
	}
	
	public Value getValue(Context ctx){
		return value;
	}
	
	public Expression[] getExpressions(){
		return null;
	}
	
	public int getReturnType(){
		return value.type;
	}
}
