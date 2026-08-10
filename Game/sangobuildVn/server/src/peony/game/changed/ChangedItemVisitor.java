package peony.game.changed;


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
}
