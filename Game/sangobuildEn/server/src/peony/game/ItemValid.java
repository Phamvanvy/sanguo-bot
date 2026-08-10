package peony.game;

public class ItemValid {
	
	public static final int TYPE_ABSOLUTELY = 1;
	public static final int TYPE_RELATIVELY = 2;
	
	public int type;
	public int time;
	
	public ItemValid(int type,int time){
		this.type = type;
		this.time = time;
	}
}
