package peony.game.changed;

import peony.game.Horse;

public class HorseBagChangedItem extends ChangedItem {
	protected Horse h;
	protected int count;
	
	public HorseBagChangedItem(Horse h,int count){
		super(TYPE_COMPLEX,HORSE_BAG,false);
		this.h = h;
		this.count = count;
	}
	
	@Override
	public boolean merge(ChangedItem other){
		return false;
	}
	
	@Override
	public void accept(ChangedItemVisitor visitor){
		visitor.visit(this);
	}
}
