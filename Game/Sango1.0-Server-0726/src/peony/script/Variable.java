package peony.script;

public class Variable implements Expression {
	
	protected String name;
	
	public Variable(String name){
		this.name = name;
	}
	
	public Value getValue(Context ctx){
		return ctx.getVariableValue(name);
	}
	
	public String getName(){
		return name;
	}
	
	public Expression[] getExpressions(){
		return null;
	}
	
	public int getReturnType(){
		if("_CLASSNAME".equals(name))
			return Value.STRING;
        if("_FACTIONNAME".equals(name))
            return Value.STRING;
		if("_SEXNAME".equals(name))
			return Value.STRING;
		if("_NAME".equals(name))
			return Value.STRING;
		return Value.INT;
	}
}
