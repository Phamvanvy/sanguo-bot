package peony.game.battlefield;

public class LevelDef {
	public int min,max;
	public int credit;
	
	public LevelDef(int min,int max,int credit){
		this.min = min;
		this.max = max;
		this.credit = credit;
	}
	
	public boolean in(int level){
		return level>=min&&level<=max;
	}
}
