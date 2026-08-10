package peony.game.changed;

import peony.game.attendant.Attendant;

public class AttendantStringPropertyChangedItem extends AttendantChangedItem {

	protected String value;
	
	public AttendantStringPropertyChangedItem(Attendant attendant, String value,
			int id, boolean notify) {
		super(attendant, ChangedItem.TYPE_ATTENDANT_STRING, ChangedItem.HORSE_NAME, notify);
		this.value = value;
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
