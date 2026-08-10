package peony.game.changed;

import peony.game.attendant.Attendant;

public class AttendantIntProoertyChangedItem extends AttendantChangedItem {

	protected int value;
	protected boolean overwrite = true;
	
	public AttendantIntProoertyChangedItem(Attendant attendant, int type,
			int id, int value, boolean notify) {
		super(attendant, type, id, notify);
		this.value = value;
	}

	public void accept(ChangedItemVisitor visitor) {
		visitor.visit(this);
	}

	public boolean merge(ChangedItem other) {
		if (other instanceof AttendantIntProoertyChangedItem) {
			AttendantChangedItem otherChange = (AttendantChangedItem)other;
			if (notify == other.notify && id == other.id && otherChange.attendantInstanceId==attendantInstanceId) {
				if (overwrite) {
					value = ((AttendantIntProoertyChangedItem) other).value;
				} else {
					value += ((AttendantIntProoertyChangedItem) other).value;
				}
				return true;
			} else {
				return false;
			}
		} else
			return false;
	}

}
