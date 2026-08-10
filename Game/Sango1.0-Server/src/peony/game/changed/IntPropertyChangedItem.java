package peony.game.changed;


public class IntPropertyChangedItem extends ChangedItem {

	protected int value;
	protected boolean overwrite;
	
	public IntPropertyChangedItem(int id,int value,boolean notify){
		this(id,value,notify,false);
	}
	
	/**
	 * 
	 * @param id
	 * @param value 
	 * @param notify
	 * @param overwrite merge策略是覆盖还是添加
	 */
	public IntPropertyChangedItem(int id,int value,boolean notify,boolean overwrite){
		super(TYPE_INT,id,notify);
		this.value = value;
		this.overwrite = overwrite;
	}
	
	@Override
	public boolean merge(ChangedItem other){
		if(other instanceof IntPropertyChangedItem){
			if(notify==other.notify&&id==other.id){
				if(overwrite){
					value = ((IntPropertyChangedItem)other).value;
				}else{
					value += ((IntPropertyChangedItem)other).value;
				}
				return true;
			}else{
				return false;
			}
		}else
			return false;
	}
	
	@Override
	public void accept(ChangedItemVisitor visitor){
		visitor.visit(this);
	}
}
