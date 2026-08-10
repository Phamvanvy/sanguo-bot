package peony.game.drop;



public abstract class SimpleDrop extends RangeDrop {

	protected int questId=-1;
	
	public SimpleDrop(int questId, int min, int max) {
		super(min, max);
		this.questId = questId;
	}

}
