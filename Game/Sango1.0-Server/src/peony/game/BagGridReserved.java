package peony.game;

public class BagGridReserved {
	
	public static final int TYPE_ADD = 1;
	public static final int TYPE_ADD_FULL = 2;
	public static final int TYPE_DEC = 3;
	public static final int TYPE_DEC_EMPTY = 4;
	
	public int gridId;
	public GameItem item;
	public int count;
	public int type;
	
	public BagGridReserved(int gridId,GameItem item, int count, int type) {
		this.gridId = gridId;
		this.item = item;
		this.count = count;
		this.type = type;
	}


	public void clear(){
		item = null;
		count = 0;
		type = 0;
	}
}
