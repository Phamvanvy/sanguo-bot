package peony.game.changed;

import peony.game.skill.Skill;

public class SkillChangedItem extends ChangedItem {

	public Skill skill;
	
	public SkillChangedItem(Skill skill,boolean notify){
		super(TYPE_COMPLEX,SKILL,notify);
		this.skill = skill;
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
