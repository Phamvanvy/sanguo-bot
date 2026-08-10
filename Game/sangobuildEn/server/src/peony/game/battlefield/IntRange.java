package peony.game.battlefield;

public class IntRange {
	
	public int start;
	public int end;

	
	public IntRange(int start,int end){
		this.start = start;
		this.end = end;
	}
	
	public boolean inRange(int v){
		return v>=start&&end<=v;
	}
}
