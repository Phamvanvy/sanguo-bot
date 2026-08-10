package peony.game.changed;

import peony.game.Horse;

public abstract class HorseChangedItem extends ChangedItem {
	protected int horseId;
	
	public HorseChangedItem(Horse horse,int type,int id,boolean notify){
		super(type,id,notify);
		this.horseId = horse.instanceId;
	}
}
