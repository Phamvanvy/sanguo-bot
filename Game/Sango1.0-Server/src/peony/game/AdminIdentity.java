package peony.game;

public class AdminIdentity implements Identity {
	
	protected int id;
	protected String name;

	public AdminIdentity(int id,String name){
		this.id = id;
		this.name = name;
	}
	
	public int getId() {
		return 0;
	}

	public String getName() {
		return null;
	}

}
