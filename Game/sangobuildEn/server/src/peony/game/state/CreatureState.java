package peony.game.state;

import peony.game.Creature;
import peony.game.State;

public abstract class CreatureState implements State {
	
	protected Creature creature;
	
	public CreatureState(Creature creature){
		this.creature = creature;
	}
}
