package peony.game.changed;

import peony.game.GameItem;

public class BindChangedItem extends ChangedItem {

	protected GameItem item;
	
	public BindChangedItem(GameItem item){
		super(ChangedItem.TYPE_COMPLEX,ChangedItem.BIND,false);
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
