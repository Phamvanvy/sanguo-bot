package peony.script;

public class Field {
	
	protected int id;
	protected String name;
	
	public Field(int id,String name){
		this.id = id;
		this.name = name;
	}
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	@Override
	public int hashCode(){
		return id;
	}
	
}
