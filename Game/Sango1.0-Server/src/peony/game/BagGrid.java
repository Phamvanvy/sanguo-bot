package peony.game;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * 背包格，每个各自有个id，是自身在包中的索引。包格采用两段式进行物品的添加以及删除操作
 * @author Jeffrey
 *
 */
public class BagGrid {
	
	private static final Logger log = Logger.getLogger(BagGrid.class);
	
	protected GameItem item;
	protected int count;
	protected BagGridReserved reserved;
	
	protected Bag bag;
	protected int id;
	
	public BagGrid(int id,Bag bag){
		this.id = id;
		this.bag = bag;
	}
	
//	public int addItem(Item item,int count){
//		if(this.item!=null&&this.item.getId()!=item.getId())
//			return 0;
//		if(this.item==null)
//			this.item = item;
//		int added = Math.min(item.maxBagGridCount()-this.count,count);
//		if(added>0){
//			this.count += added;
//		}
//		return added;
//	}
	
	public int getId(){
		return id;
	}
	
//	public boolean removeItem(Item item,int count){
//		if(this.item==null)
//			return false;
//		if(this.item.getId()==item.getId()&&this.item.getInstanceId()==item.getInstanceId()){
//			int last = this.count - count;
//			if(last>=0){
//				count = last;
//				return true;
//			}
//			return false;
//		}else{
//			return false;
//		}
//	}
	
	public boolean reserve(int count){
		if(count>=0)
			throw new IllegalArgumentException();
		if(item==null||this.count==0)
			throw new IllegalStateException();
		return reserve(item,count)<0;
	}
	
	public int reserve(GameItem item,int count){
		if (count > 0) {
			if (reserved == null) { //如没有预留
				if (this.item != null) {  //如果当前包格中已经有物品
					if (this.item.instanceId==item.instanceId&&this.item.template==item.template) { // 如果没有预留并且放入的物品跟原先存在的物品相同
						int v = Math.min(item.template.maxCount - this.count,
								count);
						if (v > 0) {
							if(v+this.count==item.template.maxCount){
								reserved = new BagGridReserved(id,item,v,BagGridReserved.TYPE_ADD_FULL);
							}else{
								reserved = new BagGridReserved(id,item,v,BagGridReserved.TYPE_ADD);
							}
							bag.reserve(this);
						}
						return v;
					} else {
						return 0;
					}
				} else {  //如果当前包格中没有物品
					int v = Math.min(item.template.maxCount, count);
					if(v==item.template.maxCount){
						reserved = new BagGridReserved(id,item,v,BagGridReserved.TYPE_ADD_FULL);
					}else{
						reserved = new BagGridReserved(id,item,v,BagGridReserved.TYPE_ADD);
					}
					bag.reserve(this);
					return v;
				}
			} else {
				if (this.item != null) {
					return 0; // 如果已经被放了物品并且预留物品也存在，那么说明不能再放下任何物品，暂时这么认为吧
				} else {
					if(reserved.type==BagGridReserved.TYPE_ADD){
						int v = Math.min(item.template.maxCount - this.count - reserved.count, count);
						reserved.count += v;
						if(v+this.count==item.template.maxCount){
							reserved.type = BagGridReserved.TYPE_ADD_FULL;
						}
						return v;
					}else{
						return 0;
					}
				}
			}
		}else{
			if(reserved!=null){  //如果已经存在预留
				if(reserved.type==BagGridReserved.TYPE_DEC){
					int v = Math.min(this.count - reserved.count, -count);
					reserved.count -= v;
					if(this.count==-(reserved.count)){
						reserved.type = BagGridReserved.TYPE_DEC_EMPTY;
					}
					return -v;
				}
				return 0;
			}
			else{  //如果没有预留
				if(this.item==null)  //如果没有物品
					return 0;
				else{
					if(this.item.instanceId==item.instanceId&&this.item.template==item.template){
						int v = Math.min(this.count, -count);
						if(v==this.count){
							reserved = new BagGridReserved(id,item, -v,BagGridReserved.TYPE_DEC_EMPTY);
						}else{
							reserved = new BagGridReserved(id,item, -v,BagGridReserved.TYPE_DEC);
						}
						bag.reserve(this);
						return -v;
					}else{
						return 0;
					}
				}
			}
		}
	}
	
	public void setEmpty(boolean notify){
		if(item!=null&&count>0){
			reserved = new BagGridReserved(id,item,-count,BagGridReserved.TYPE_DEC_EMPTY);
			complete(notify);
			clearReserved();
		}
	}
	
	public void replace(GameItem item,int count,boolean notify){
		if(count>item.template.maxCount)
			throw new IllegalArgumentException();
		this.item = null;
		this.count = 0;
		reserved = new BagGridReserved(id,item,count,count==item.template.maxCount?BagGridReserved.TYPE_ADD_FULL:BagGridReserved.TYPE_ADD);
		complete(notify);
		clearReserved();
	}
	
//	public void exchange(BagGrid other){
//		if(isEmpty()){
//			
//		}
//	}
	
	public GameItem getItem(){
		return item;
	}
	
	public int getCount(){
		return count;
	}
	
	public void clear(){
		item = null;
		count = 0;
	}
	
	public void clearReserved(){
		reserved = null;
	}
	
	public void complete(boolean notify){
//		log.debug(String.format("BagGrid %d OldCount %d", id,count));
		count += reserved.count;
//		log.debug(String.format("BagGrid %d count %d", id,count));
		if(count==0){
			item = null;
			
		}
		else if(count>0){
			if(item==null){
				item = reserved.item;
			}
		}
		else if(count<0)
			throw new IllegalStateException();
//		if(bag.owner.changed!=null){
//			bag.owner.changed.addChangedItem(new BagChangedItem(this));
//			if(notify){
//				log.debug("changedcount:"+reserved.count);
//				bag.owner.changed.addChangedItem(new BagChangedItem(reserved.item,reserved.count));
//			}
//		}
	}
	
	/**
	 * 背包是否为空
	 * @param reserve  算上reserve部分，如果值为true，那么如果reserve部分有值那么也返回false
	 * @return
	 */
	public boolean isEmpty(boolean reserve){
		if(!reserve)
			return item==null||count==0;
		else{
			return (item==null||count==0)&&(reserved==null);
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
}
