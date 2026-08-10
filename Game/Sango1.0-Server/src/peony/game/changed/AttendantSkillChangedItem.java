package peony.game.changed;

import peony.game.attendant.Attendant;
import peony.game.skill.Skill;

public class AttendantSkillChangedItem extends AttendantChangedItem {

	public Skill skill;
	public boolean add;
	public int index;
	
	public AttendantSkillChangedItem(Attendant attendant, Skill skill, int index, boolean add,
			boolean notify) {
		super(attendant, ChangedItem.TYPE_ATTENDANT_COMPLEX, ChangedItem.SKILL, notify);
		this.skill = skill;
		this.add = add;
		this.index = index;
	}

	public void accept(ChangedItemVisitor visitor) {
		visitor.visit(this);
	}

	public boolean merge(ChangedItem other) {
		return false;
	}

}
