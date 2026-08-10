package peony.game.changed;

import peony.game.GameItem;
import peony.game.TransactionBagGrid;

public class InvalidItem extends ChangedItem{

	protected GameItem[] item;
	protected TransactionBagGrid[]  gridId;
		
		public InvalidItem(GameItem[] item,TransactionBagGrid[] gridId){
			super(ChangedItem.TYPE_COMPLEX,ChangedItem.INVALIDITEM,false);
			if(item!=null){
				this.item=new GameItem[item.length];
				for(int i=0;i<item.length;i++){
					this.item[i] = item[i];
				}
			}
			if(gridId!=null){
				this.gridId=new TransactionBagGrid[gridId.length];
				for(int i=0;i<gridId.length;i++){
					this.gridId[i]=gridId[i];
				}
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