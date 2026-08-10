package peony.game.changed;

import peony.game.attendant.Attendant;
import peony.game.skill.Skill;

public class AttendantSkillChangedItem extends AttendantChangedItem {

	public Skill skill;
	public boolean add;
	
	public AttendantSkillChangedItem(Attendant attendant, Skill skill, boolean add,
			boolean notify) {
		super(attendant, ChangedItem.TYPE_ATTENDANT_COMPLEX, ChangedItem.SKILL, notify);
		this.skill = skill;
		this.add = add;
	}

	public void accept(ChangedItemVisitor visitor) {
		visitor.visit(this);
	}

	public boolean merge(ChangedItem other) {
		return false;
	}

}
