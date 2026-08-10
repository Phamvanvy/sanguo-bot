package peony.game;

import peony.game.changed.BagChangedItem;
import peony.game.changed.ChangedItem;
import peony.service.ItemTrackService;
import peony.service.ServiceEvent;

public class BagGridTransactionEntity implements TransactionEntity {
	
	public static final int BAG = 1;
	public static final int DEPOT = 2;

	TransactionBagGrid grid;
	GameItem item;
	int count;
	boolean notify;
	int type;
	Transaction trans;
	
	public BagGridTransactionEntity(Transaction trans, TransactionBagGrid grid, GameItem item,
			int count, boolean notify) {
		this(trans, grid, item, count, notify, BAG);
	}
	
	public BagGridTransactionEntity(Transaction trans, TransactionBagGrid grid,GameItem item,int count,boolean notify,int type){
		this.trans = trans;
		this.grid = grid;
		this.item = item;
		this.count = count;
		this.notify = notify;
		this.type = type;
	}
	
	public void commit() {
		grid.release(this, true);
		notifyPlayerGetItem();
	}
	
	private void notifyPlayerGetItem(){
		try {
			Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_GETITEM, grid.bag.owner, item.template.id));
		} catch (Exception e) {}
	}

	public void rollback() {
		grid.release(this, false);
	}
	
	public ChangedItem[] sync(){
		if(type==DEPOT){
			return null;
		}
		ChangedItem[] ret = null;
		if(notify){
			ret = new ChangedItem[2];
		}else{
			ret = new ChangedItem[1];
		}
		int totle = 0;
		try {
			Player player = grid.bag.owner;
			ItemTrackService service = Server.server.getServiceRegistry().getItemTrackService();
			totle = service.getItemTrackById(player).getTotleByItemId(item.template.id);
		} catch (Exception e) {
		}
		ret[0] = new BagChangedItem(grid, totle);
		if(notify){
			ret[1] = new BagChangedItem(item,count,totle);
		}
		return ret;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[ENTITY]");
		if(grid!=null){
			sb.append(grid.toString());
		}
		sb.append("COUNT[").append(count).append("]");
		return sb.toString();
	}

	public Transaction getTransaction() {
		return trans;
	}
}
