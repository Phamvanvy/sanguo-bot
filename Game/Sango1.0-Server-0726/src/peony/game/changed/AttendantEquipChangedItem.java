package peony.game.changed;

import peony.game.GameItem;
import peony.game.attendant.Attendant;

public class AttendantEquipChangedItem extends AttendantChangedItem {

	protected int index;
	protected GameItem item;
	
	public AttendantEquipChangedItem(Attendant attendant, int index,GameItem item,
			boolean notify) {
		super(attendant, ChangedItem.TYPE_ATTENDANT_COMPLEX, ChangedItem.EQUIP, notify);
		this.index = index;
		this.item = item;
	}

	public void accept(ChangedItemVisitor visitor) {
		visitor.visit(this);
	}

	public boolean merge(ChangedItem other) {
		return false;
	}

}
