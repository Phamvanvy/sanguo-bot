package peony.game.changed;

import peony.service.cards.EquipCardChangedItem;


public interface ChangedItemVisitor {
	public void visit(IntPropertyChangedItem changedItem);
	public void visit(StringPropertyChangedItem changedItem);
	public void visit(BagChangedItem changedItem);
	public void visit(SkillChangedItem changedItem);
	public void visit(EquipChangedItem changedItem);
	public void visit(DurationChangedItem changedItem);
	public void visit(BindChangedItem changedItem);
	public void visit(AddTitleChangedItem changedItem);
	public void visit(HorseEquipChangedItem changedItem);
	public void visit(HorseIntPropertyChangedItem changedItem);
	public void visit(HorseSkillChangedItem changedItem);
	public void visit(HorseStringPropertyChangedItem changedItem);
	public void visit(HorseBagChangedItem changedItem);
	public void visit(AttendantIntProoertyChangedItem changed);
	public void visit(AttendantBagChangedItem changed);
	public void visit(AttendantSkillChangedItem changed);
	public void visit(AttendantEquipChangedItem changed);
	public void visit(AttendantStringPropertyChangedItem changed);
	public void visit(HorseFoodChange changed);
	public void visit(PlayerEnemyChangeToken changed);
	public void visit(EquipCardChangedItem changed);
	public void visit(RemoveSkillChangeItem changed);
	public void visit(InvalidItem changed);
}
