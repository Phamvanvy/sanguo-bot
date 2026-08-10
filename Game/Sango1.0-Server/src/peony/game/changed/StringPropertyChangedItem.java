package peony.game.changed;


public class StringPropertyChangedItem extends ChangedItem {
	
	protected String value;
	
	public StringPropertyChangedItem(int id,String value,boolean notify){
		super(TYPE_STRING,id,notify);
		this.value = value;
	}
	
	@Override
	public boolean merge(ChangedItem other){
		if(other instanceof StringPropertyChangedItem){
			if(notify==other.notify&&id==other.id){
				value = ((StringPropertyChangedItem)other).value;
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
