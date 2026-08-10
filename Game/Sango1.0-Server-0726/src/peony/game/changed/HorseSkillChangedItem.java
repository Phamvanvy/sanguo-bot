package peony.game.changed;

import peony.game.Horse;
import peony.game.skill.Skill;

public class HorseSkillChangedItem extends HorseChangedItem {

	public Skill skill;
	public boolean add;
	
	public HorseSkillChangedItem(Horse horse,Skill skill,boolean add,boolean notify){
		super(horse,TYPE_HORSE_COMPLEX,SKILL,notify);
		this.add = add;
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
