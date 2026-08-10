package peony.game.changed;

import peony.game.attendant.Attendant;

public abstract class AttendantChangedItem extends ChangedItem {

	protected int attendantInstanceId;
	
	protected AttendantChangedItem(Attendant attendant, int type, int id, boolean notify) {
		super(type, id, notify);
		this.attendantInstanceId = attendant.getInstanceId();
	}

}
