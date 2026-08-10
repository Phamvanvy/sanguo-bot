package peony.game;

public class Corpse extends Unit {

	protected GameObject owner;
	
	public Corpse(int id,GameObject owner){
		super(GameObject.TYPE_CORPSE);
		this.owner = owner;
	}
	
	@Override
	public void update(int diffTime) {
		
	}

}
