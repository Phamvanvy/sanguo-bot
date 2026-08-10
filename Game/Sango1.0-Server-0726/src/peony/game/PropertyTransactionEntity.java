package peony.game;

import peony.game.changed.ChangedItem;
import peony.game.changed.IntPropertyChangedItem;

public class PropertyTransactionEntity implements TransactionEntity {
	
	protected PlayerTransaction tx;
	protected LockableIntProperty pro;
	protected int value;
	protected boolean notify;
	
	public PropertyTransactionEntity(PlayerTransaction tx,LockableIntProperty pro,int value,boolean notify){
		this.tx = tx;
		this.pro = pro;
		this.value = value;
		this.notify = notify;
	}
	
	public void commit() {
		pro.release(this, true);
	}

	public void rollback() {
		pro.release(this, false);
	}
	
	public ChangedItem[] sync(){
		ChangedItem[] ret = null;
		if(notify){
			ret = new ChangedItem[2];
			ret[0] = new IntPropertyChangedItem(pro.type, pro.value,
					false, true);
			ret[1] = new IntPropertyChangedItem(pro.type, value, true, true);
		}else{
			ret = new ChangedItem[1];
			ret[0] = new IntPropertyChangedItem(pro.type, pro.value,
					false, true);
		}
		return ret;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[ENTITY]").append(pro).append("VALUE[").append(value).append("]");
		return sb.toString();
	}
	
	public Transaction getTransaction() {
		return tx;
	}
}
