package peony.game;

import peony.game.changed.ChangedItem;

/**
 * 每个TransactionEntity是Transaction中的一项，当前的实现中，
 * 必须保证TransactionEntity的commit以及rollback绝对成功的，不能抛出任何异常
 * 
 * @author Jeffrey
 * 
 */
public interface TransactionEntity {
	public void commit();

	public void rollback();
	
	public ChangedItem[] sync();
	
	public Transaction getTransaction();
}
