package peony.game;

public class GainItem {
	
	public static final GainItem[] EMPTY = new GainItem[0];
	
	protected GameItem item;
	protected int count;
	
	public GainItem(GameItem item,int count){
		this.item = item;
		this.count = count;
	}
	
	public GameItem getItem(){
		return item;
	}
	
	public int getCount(){
		return count;
	}
	
	public void add(int v){
		count += v;
	}
}
