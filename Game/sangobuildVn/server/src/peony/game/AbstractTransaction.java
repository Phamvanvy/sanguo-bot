package peony.game;

import java.util.List;

public abstract class AbstractTransaction implements Transaction{
	
	protected int state;
	protected List<TransactionEntity> entities;

	public boolean isActive() {
		return state == ACTIVE;
	}

	public boolean wasCommitted() {
		return state == COMMITED;
	}

	public boolean wasRolledback() {
		return state == ROLLEDBACK;
	}
	
	public void addEntity(TransactionEntity entity){
		entities.add(entity);
	}
}
