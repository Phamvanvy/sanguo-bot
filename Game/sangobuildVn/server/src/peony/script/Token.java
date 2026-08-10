package peony.script;

public class Token {
	
	public static final int NUM = 0;
	public static final int STRING = 1;
	public static final int STRING_CONSTANT = 2;
	public static final int OPERATOR = 3;
	
	public int type;
	public String value;

	public Token(int type,String value){
		this.type = type;
		this.value = value;
	}
}
