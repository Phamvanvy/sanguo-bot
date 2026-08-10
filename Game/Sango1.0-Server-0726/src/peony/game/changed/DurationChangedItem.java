package peony.game.changed;

import peony.game.GameItem;

public class DurationChangedItem extends ChangedItem {

	protected GameItem item;
	
	public DurationChangedItem(GameItem item){
		super(ChangedItem.TYPE_COMPLEX,ChangedItem.DURATION,false);
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
