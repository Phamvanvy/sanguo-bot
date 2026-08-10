package peony.game;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import peony.game.changed.ChangedItem;

public class PlayerTransaction extends AbstractTransaction {
	
	protected Player player;
	protected String cause;
	protected List<GameItem> noticeItems;
	protected List<GainItem> mailItems;
	
	public PlayerTransaction(Player player, String cause){
		this.player = player;
		this.state = ACTIVE;
		this.cause = cause;
		this.entities = new LinkedList<TransactionEntity>();
	}
	
	void internalCommit() throws TransactionException {
		if (state != ACTIVE)
			throw new TransactionException();
		for (TransactionEntity entity : entities) {
			entity.commit();
		}
	}
	
	void internalRollback() throws TransactionException{
		if(state != ACTIVE)
			throw new TransactionException();
		for (TransactionEntity entity : entities) {
			entity.rollback();
		}
	}
	
	public void commit() throws TransactionException {
		player.release(this, true);
		this.state = COMMITED;
		syncWithClient();
	}

	public void rollback() throws TransactionException {
		player.release(this, false);
		this.state = ROLLEDBACK;
	}

	public void syncWithClient(){
		if(this.state!=COMMITED)
			throw new IllegalStateException();
		for(TransactionEntity entity:entities){
			ChangedItem[] cis = entity.sync();
			if(cis!=null&&cis.length>0){
				for(int i=0;i<cis.length;i++){
					player.changed.addChangedItem(cis[i]);
				}
			}
		}
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[PLAYERTRANSACTION]");
		for(TransactionEntity entity:entities){
			sb.append(entity.toString());
		}
		return sb.toString();
	}
	
	public String getCause() {
		return cause;
	}
	
	public void setCause(String cause) {
		this.cause = cause;
	}
	
	public void addNoticeItem(GameItem item) {
		if (noticeItems == null) {
			noticeItems = new ArrayList<GameItem>();
		}
		noticeItems.add(item);
	}
	
	public List<GameItem> getNoticeItems() {
		return noticeItems;
	}

	public void addMailItem(GainItem item) {
		if (mailItems == null) {
			mailItems = new ArrayList<GainItem>();
		}
		mailItems.add(item);
	}
	
	public List<GainItem> getMailItems() {
		return mailItems;
	}
}
