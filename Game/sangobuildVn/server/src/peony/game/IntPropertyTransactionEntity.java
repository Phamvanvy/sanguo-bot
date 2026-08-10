package peony.game;

import peony.game.changed.ChangedItem;

public class IntPropertyTransactionEntity implements TransactionEntity {

	protected PlayerTransaction tx;
	protected TransactionIntProperty pro;
	protected int value;
	protected boolean notify;
	
	public IntPropertyTransactionEntity(PlayerTransaction tx,
			TransactionIntProperty pro, int value, boolean notify) {
		this.tx = tx;
		this.pro = pro;
		this.value = value;
		this.notify = notify;
	}
	
	public void commit() {
		pro.release(tx, this,true);
	}

	public void rollback() {
		pro.release(tx, this,false);
	}
	
	public ChangedItem[] sync(){
		return new ChangedItem[0];
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
