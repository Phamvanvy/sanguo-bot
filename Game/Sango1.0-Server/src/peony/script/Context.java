package peony.script;


public interface Context {
	
	
	Value getVariableValue(String name);
	
	Value call(String function,Value[] parameters);
}
