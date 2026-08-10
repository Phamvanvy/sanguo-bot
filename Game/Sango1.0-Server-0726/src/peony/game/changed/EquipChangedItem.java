package peony.game.changed;

import peony.game.GameItem;

public class EquipChangedItem extends ChangedItem {
	
	protected int index;
	protected GameItem item;
	
	public EquipChangedItem(int index,GameItem item){
		super(ChangedItem.TYPE_COMPLEX,ChangedItem.EQUIP,false);
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
