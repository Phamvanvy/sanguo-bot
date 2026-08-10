package peony.game.changed;

import peony.game.Horse;

public class HorseStringPropertyChangedItem extends HorseChangedItem {

	protected String value;
	
	public HorseStringPropertyChangedItem(Horse horse,int id,String value,boolean notify){
		super(horse,TYPE_HORSE_STRING,id,notify);
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
