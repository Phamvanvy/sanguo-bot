package peony.game;

import org.apache.log4j.Logger;


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class TransactionBagGrid {
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger.getLogger(TransactionBagGrid.class);
	
	protected TransactionBag bag;
	public int id;
	
	protected GameItem item;
	protected int count;
	protected int wantAddCount;
	protected int wantDecCount;
	protected List<BagGridTransactionEntity> entities = null;
	
	
	public TransactionBagGrid(int id, TransactionBag bag){
		this.id = id;
		this.bag = bag;
	}
	
	int addGameItem0(GameItem item,int count){
		if (count <= 0)
			throw new IllegalArgumentException();
		if (this.item==null) {
			this.item = item;
		} else {
			if (!item.equals(this.item)) {
				return 0;
			}
		}
		int max = item.template.maxCount;
		if (this.count + this.wantAddCount >= max) {
			return 0;
		} else {
			int v = Math.min(max - this.count - wantAddCount, count);
			this.count += v;
			bag.addIndex(item.template.id, id);
			return v;
		}
	}
	
	int addGameItem(GameItem item, int count,
			PlayerTransaction tx,boolean notify) {
		if (count <= 0)
			throw new IllegalArgumentException();
		if (this.item==null) {
			this.item = item;
		} else {
			if (!item.equals(this.item)) {
				return 0;
			}
		}
		int max = item.template.maxCount;
		if (this.count + this.wantAddCount >= max) {
			return 0;
		} else {
			int v = Math.min(max - this.count - wantAddCount, count);
			BagGridTransactionEntity entity = newTransactionEntity(tx, item,v,notify);
			tx.addEntity(entity);
			wantAddCount += v;
			return v;
		}
	}
	
	/**
	 * 用于仓库
	 */
	int addDepotGameItem(GameItem item, int count,
			PlayerTransaction tx,boolean notify) {
		if (count <= 0)
			throw new IllegalArgumentException();
		if (this.item==null) {
			this.item = item;
		} else {
			if (!item.equals(this.item)) {
				return 0;
			}
		}
		int max = item.template.maxCount;
		if (this.count + this.wantAddCount >= max) {
			return 0;
		} else {
			int v = Math.min(max - this.count - wantAddCount, count);
			BagGridTransactionEntity entity = newDepotTransactionEntity(tx, item,v,notify);
			tx.addEntity(entity);
			wantAddCount += v;
			return v;
		}
	}
	
	/**
	 * 
	 * @param itemId
	 * @param instanceId
	 * @param count
	 * @param tx
	 * @param notify
	 * @return 返回被移除的数量
	 */
	int removeGameItem(int itemId,int instanceId,int count,PlayerTransaction tx,boolean notify){
		if(count<=0)
			throw new IllegalArgumentException();
		if(this.item==null||this.count==0)
			return 0;
		if(this.item.template.id!=itemId||this.item.instanceId!=instanceId)
			return 0;
		int leave = this.count - this.wantDecCount;
		if(leave>0){
			int v = leave>count?count:leave;
			BagGridTransactionEntity entity = newTransactionEntity(tx, this.item,-v,notify);
			tx.addEntity(entity);
			wantDecCount += v;
			return v;
		}else{
			return 0;
		}
	}
	
	/**
	 * 从现有包格中拆分出一部分来。
	 * @return 返回被移除的数量
	 */
	GameItem split(int itemID, int count) {
		// 只有可堆叠的物品才能拆分
		if (count<=0)
			throw new IllegalArgumentException();
		if(this.item==null||this.count==0)
			throw new IllegalArgumentException();
		if (this.item.template.id != itemID || this.item.instanceId != GameItem.GENERAL_INSTANCEID) {
			throw new IllegalArgumentException();
		}
		
		// 检查数量是否够
		int leave = this.count - this.wantDecCount;
		if (leave < count) {
			throw new IllegalArgumentException();
		}
		
		// 扣除物品，生成新物品
		this.count -= count;
		return this.item;
	}

	/**
	 * 用于仓库
	 */
	int removeDepotGameItem(int itemId,int instanceId,int count,PlayerTransaction tx,boolean notify){
		if(count<=0)
			throw new IllegalArgumentException();
		if(this.item==null||this.count==0)
			return 0;
		if(this.item.template.id!=itemId||this.item.instanceId!=instanceId)
			return 0;
		int leave = this.count - this.wantDecCount;
		if(leave>0){
			int v = leave>count?count:leave;
			BagGridTransactionEntity entity = newDepotTransactionEntity(tx, this.item,-v,notify);
			tx.addEntity(entity);
			wantDecCount += v;
			return v;
		}else{
			return 0;
		}
	}
	
	private BagGridTransactionEntity newTransactionEntity(Transaction trans, GameItem item,int count,boolean notify){
		if(entities==null){
			entities = new LinkedList<BagGridTransactionEntity>();
		}
		BagGridTransactionEntity entity = new BagGridTransactionEntity(trans, this,item,count,notify);
		entities.add(entity);
		return entity;
	}
	
	/**
	 * 用于仓库
	 */
	private BagGridTransactionEntity newDepotTransactionEntity(Transaction trans, GameItem item,int count,boolean notify){
		if(entities==null){
			entities = new LinkedList<BagGridTransactionEntity>();
		}
		BagGridTransactionEntity entity = new BagGridTransactionEntity(trans, this,item,count,notify,BagGridTransactionEntity.DEPOT);
		entities.add(entity);
		return entity;
	}
	
	boolean isEmpty(){
		return item == null;
	}
	
	void release(BagGridTransactionEntity entity, boolean commit) {
		boolean removeOk = entities.remove(entity);
		if(!removeOk){
			log.info("[TRANSACTIONERROR]"+LogUtil.getPlayerLogString(bag.owner)+"ENITYT["+entity.item.template.id+","+entity.count+"]");
		}
		if (commit) {
			if (entity.count > 0) {
				count += entity.count;
				wantAddCount -= entity.count;
				LogUtil.logGetItem(bag.owner, item, entity.count, entity.getTransaction().getCause());
			} else {
				count += entity.count;
				wantDecCount += entity.count;
				LogUtil.logRemoveItem(bag.owner, item, -entity.count, entity.getTransaction().getCause());
			}
		} else {
			if (entity.count > 0) {
				wantAddCount -= entity.count;
			} else {
				wantDecCount += entity.count;
			}
		}
		if (entities.isEmpty())
			entities = null;
		if (count == 0 && wantAddCount == 0 && wantDecCount == 0) {
			if (item != null) {
				bag.removeIndex(item.template.id, id);
				bag.addIndex(-1, id);
			}
			item = null;
		} else {
			if (item != null) {
				bag.removeIndex(-1, id);
				bag.addIndex(item.template.id, id);
			}
		}
	}
	
	public byte[] toClientByte(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(id);
			dos.write(count);
			if(count>0)
				dos.write(item.toClientBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	public GameItem getItem(){
		return item;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("GRID[").append(id);
		if (item != null) {
			sb.append("ITEM[").append(item.template.id).append(",").append(
					item.instanceId).append(",").append(count).append("]");
		}
		sb.append("WA[").append(wantAddCount).append("]");
		sb.append("WD[").append(wantDecCount).append("]");
		return sb.toString();
	}
}
