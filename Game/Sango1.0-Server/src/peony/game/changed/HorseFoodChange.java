package peony.game.changed;

import peony.game.Horse;

public class HorseFoodChange extends HorseChangedItem{
	protected Horse h;
	protected int foodId;

	public HorseFoodChange(Horse h,int foodId) {
		super(h,TYPE_HORSE_COMPLEX, HORSE_FOOD, false);
		this.h = h;
		this.foodId = foodId;
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
