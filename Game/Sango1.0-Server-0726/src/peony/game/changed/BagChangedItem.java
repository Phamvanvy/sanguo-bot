package peony.game.changed;

import peony.game.GameItem;
import peony.game.TransactionBagGrid;

public class BagChangedItem extends ChangedItem {
	
	protected GameItem item;
	protected int count;
	protected int totle;
	protected TransactionBagGrid grid;
	
	public BagChangedItem(TransactionBagGrid grid, int totle){
		super(TYPE_COMPLEX,BAGGRID,false);
		this.grid = grid;
		this.totle = totle;
	}
	
	public BagChangedItem(GameItem item,int count,int totle){
		super(TYPE_COMPLEX,ITEM_COUNT_CHANGED,true);
		this.item = item;
		this.count = count;
		this.totle = totle;
	}
	
	@Override
	public boolean merge(ChangedItem other){
		if(other instanceof BagChangedItem){
			if(notify==other.notify){
				BagChangedItem o = (BagChangedItem)other;
				if(notify){
					if(item.equals(o.item)){
						count += o.count;
						return true;
					}
					return false;
				}else{
					if(grid.id==o.grid.id){
						grid = o.grid;
						return true;
					}
					return false;
				}
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
	
	public TransactionBagGrid getGrid() {
		return grid;
	}
}
