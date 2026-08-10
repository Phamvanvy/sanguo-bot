package peony.game.drop;



public abstract class RangeDrop implements Drop {
	
	protected int min,max;
	
	protected RangeDrop(int min,int max){
		this.min = min;
		this.max = max;
	}

}
