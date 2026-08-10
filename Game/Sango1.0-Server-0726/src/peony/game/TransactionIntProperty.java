package peony.game;

public abstract class TransactionIntProperty {

	protected int wantAddValue;
	protected int wantDecValue;

	public abstract int getValue();

	public synchronized void add(int value, PlayerTransaction tx, boolean notify) {
		if (value < 0)
			throw new IllegalArgumentException();
		this.wantAddValue += value;
		IntPropertyTransactionEntity entity = new IntPropertyTransactionEntity(
				tx, this, value, notify);
		tx.addEntity(entity);
	}

	public synchronized void dec(int value, PlayerTransaction tx, boolean notify)
			throws NoEnoughValueException {
		if (value < 0)
			throw new IllegalArgumentException();
		if ((getValue() - wantDecValue) < value) {
			throw new NoEnoughValueException();
		}
		this.wantDecValue += value;
		IntPropertyTransactionEntity entity = new IntPropertyTransactionEntity(
				tx, this, -value, notify);
		tx.addEntity(entity);
	}

	public synchronized void release(Transaction tx, IntPropertyTransactionEntity entity, boolean commit) {
		if(commit){
			if (entity.value > 0) {
				modifyValue(entity.value,entity.notify,tx.getCause());
				wantAddValue -= entity.value;
			} else {
				modifyValue(entity.value,entity.notify,tx.getCause());
				wantDecValue += entity.value;
			}
		}else{
			if (entity.value > 0) {
				wantAddValue -= entity.value;
			} else {
				wantDecValue += entity.value;
			}
		}
	}
	
	/**
	 * 修改真实值 
	 * @param value 如果大于0是添加，否则是减少
	 */
	protected abstract void modifyValue(int value,boolean notify,String cause);
}
