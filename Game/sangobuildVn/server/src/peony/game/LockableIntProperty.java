package peony.game;

import org.apache.log4j.Logger;

import peony.game.changed.ChangedItem;

public class LockableIntProperty {
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger.getLogger(LockableIntProperty.class);
	
	protected int value;
	protected int wantAddValue;
	protected int wantDecValue;
	protected int type;
	
	public LockableIntProperty(int type, int value){
		if(value<0)
			throw new IllegalArgumentException();
		this.type = type;
		this.value = value;
	}
	
	public int getValue(){
		return value;
	}
	
	public synchronized void add(int value,PlayerTransaction tx,boolean notify){
		if(value<0)
			throw new IllegalArgumentException();
		this.wantAddValue += value;
		PropertyTransactionEntity entity = new PropertyTransactionEntity(tx,this,value,notify);
		tx.addEntity(entity);
	}
	
	public synchronized void dec(int value,PlayerTransaction tx,boolean notify) throws NoEnoughValueException{
		if(value<0)
			throw new IllegalArgumentException();
		if((this.value-wantDecValue)<value){
			throw new NoEnoughValueException();
		}
		this.wantDecValue += value;
		PropertyTransactionEntity entity = new PropertyTransactionEntity(tx,this,-value,notify);
		tx.addEntity(entity);
	}
	
	synchronized void release(PropertyTransactionEntity entity, boolean commit) {
		if (commit) {
			if (entity.value > 0) {
				value += entity.value;
				wantAddValue -= entity.value;
				if (type == ChangedItem.MONEY) {
					log.info("[GETMONEY]"
							+ LogUtil.getPlayerLogString(entity.tx.player)
							+ "COUNT[" + entity.value + "]BALANCE[" + getValue()
							+ "]OK");
				}
			} else {
				value += entity.value;
				wantDecValue += entity.value;
				if (type == ChangedItem.MONEY) {
					log.info("[REMOVEMONEY]"
							+ LogUtil.getPlayerLogString(entity.tx.player)
							+ "COUNT[" + entity.value + "]BALANCE[" + getValue()
							+ "]OK");
				}
			}
		} else {
			if (entity.value > 0) {
				wantAddValue -= entity.value;
			} else {
				wantDecValue += entity.value;
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TRANSACTIONPRO[").append(type).append(",").append(value)
				.append(",").append(wantAddValue).append(",").append(
						wantDecValue).append("]");
		return sb.toString();
	}
	
	
	@Override
	public synchronized LockableIntProperty clone(){
		LockableIntProperty ret = new LockableIntProperty(type,value);
		ret.value = value;
		ret.wantAddValue = wantAddValue;
		ret.wantDecValue = wantDecValue;
		return ret;
	}
}
