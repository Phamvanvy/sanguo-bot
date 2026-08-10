package peony.script;

public interface Expression extends Node{
	public Expression[] getExpressions();
	public int getReturnType();
}
