package peony.game.changed;

import peony.game.GameItem;
import peony.game.Horse;

public class HorseEquipChangedItem extends HorseChangedItem {
	
	protected int index;
	protected GameItem item;
	
	public HorseEquipChangedItem(Horse horse,int index,GameItem item){
		super(horse,ChangedItem.TYPE_HORSE_COMPLEX,ChangedItem.EQUIP,false);
		this.index = index;
		this.item = item;
	}
	
	@Override
	public void accept(ChangedItemVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean merge(ChangedItem other) {
		return false;
	}

}
