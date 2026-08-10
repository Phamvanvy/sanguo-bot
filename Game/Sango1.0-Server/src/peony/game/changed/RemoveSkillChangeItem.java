package peony.game.changed;

public class RemoveSkillChangeItem extends ChangedItem{

    public int size;
    public int[] skillIds;
	public RemoveSkillChangeItem(int size, int[] skillIds, boolean notify) {
		super(TYPE_COMPLEX, REMOVE_SKILL,notify);
		this.size = size;
		this.skillIds = new int[skillIds.length];
		for(int i=0;i<skillIds.length;i++){
		   this.skillIds[i] = skillIds[i];
		}
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
