package peony.game.changed;

import peony.game.attendant.Attendant;

public class AttendantBagChangedItem extends ChangedItem {

	protected Attendant attendant;
	protected int count;
	
	public AttendantBagChangedItem(Attendant attendant, int count) {
		super(ChangedItem.TYPE_COMPLEX, ChangedItem.ATTENDANT_BAG, false);
		this.attendant = attendant;
		this.count = count;
	}

	public void accept(ChangedItemVisitor visitor) {
		visitor.visit(this);
	}

	public boolean merge(ChangedItem other) {
		return false;
	}

}
