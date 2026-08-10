package peony.game.changed;

import peony.game.Title;


public class AddTitleChangedItem extends ChangedItem {

	public Title title;
	
	public AddTitleChangedItem(Title title,boolean notify){
		super(TYPE_COMPLEX,ADD_TITLE,notify);
		this.title = title;
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
