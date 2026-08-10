package peony.script;

public class Value {
	public static final int INT = 0;
	public static final int STRING = 1;
	
	protected int type;
	protected Object value;
	
	public Value(String stringValue){
		this.type = STRING;
		this.value = stringValue;
	}
	
	public Value(int intValue){
		this.type = INT;
		this.value = intValue;
	}
	
	public Object get(){
		return value;
	}
	
	public int getType(){
		return type;
	}
	
	public int intValue(){
		return ((Integer)value).intValue();
	}
	
	public String stringValue(){
		return (String)value;
	}
}
