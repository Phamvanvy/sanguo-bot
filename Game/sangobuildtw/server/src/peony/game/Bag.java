package peony.game;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


public class Bag implements Cloneable{

	private static final Logger log = Logger.getLogger(Bag.class);

	protected Unit owner;
	protected int size;
	protected List<BagGrid> grids;
	protected List<BagGrid> reserved;
//	protected IntHashMap<BagGrid> changed = new IntHashMap<BagGrid>();

	public Bag(Unit owner,int size) {
		this.owner = owner;
		this.size = size;
		grids = new ArrayList<BagGrid>(size);
		for (int i = 0; i < size; i++) {
			grids.add(new BagGrid(i, this));
		}
		reserved = new ArrayList<BagGrid>(size);
	}
	
	/**
	 * 获取装装有指定物品Id，并且instanceId也相同的包格
	 * @param itemId
	 * @param instanceId
	 * @return
	 */
	public BagGrid getBagGrid(int itemId,int instanceId){
		for(BagGrid grid:grids){
			if(!grid.isEmpty(false)){
				if(grid.item.template.id==itemId&&grid.item.instanceId==instanceId)
					return grid;
			}
		}
		return null;
	}
	
	public int getFreeBagCount(){
		int count = 0;
		for(BagGrid grid:grids){
			if(grid.isEmpty(true))
				count++;
		}
		return count;
	}

	public boolean addGainComplete(Gain gain,boolean notify) {
		if (gain.isEmpty())
			return true;
		GainItem[] items = gain.getGainItems();
		for (GainItem item : items) {
			if (!tryReserve(item.getItem(), item.getCount())) {
				clearReserved();
				return false;
			}
		}
		addReserveToGrid(notify);
		clearReserved();
		return true;
	}
	
	public void addGain(Gain gain,boolean notify){
		if (gain.isEmpty())
			return;
		GainItem[] items = gain.getGainItems();
		for (GainItem item : items) {
			tryReserve(item.getItem(),item.getCount());
		}
		addReserveToGrid(notify);
		clearReserved();
	}

	public boolean addItem(GameItem item, int count,boolean notify) {
		if (tryReserve(item, count)) {
			addReserveToGrid(notify);
			clearReserved();
			return true;
		}
		clearReserved();
		return false;
	}
	

	
	public boolean removeItem(GameItem item,int count,boolean notify){
		if (tryReserve(item, -count)) {
			addReserveToGrid(notify);
			clearReserved();
			return true;
		} else {
			clearReserved();
			return false;
		}
	}
	
	
	
	public boolean removeItemByGrid(int gridId,int count,boolean notify){
		BagGrid grid = getBagGrid(gridId);
		boolean ret = grid.reserve(-count);
		if(ret){
			addReserveToGrid(notify);
			clearReserved();
		}
		return ret;
	}
	
	public boolean hasItem(ItemTemplate template,int count){
		int all = count;
		for(BagGrid g:grids){
			GameItem it = g.getItem();
			if(it!=null&&it.template.id==template.id){
				all -= g.getCount();
				if(all<=0)
					return true;
			}
		}
		return false;
	}
	
	public GameItem getItem(ItemTemplate template,int instanceId){
		for(BagGrid g:grids){
			GameItem it = g.getItem();
			if(it!=null&&it.template.id==template.id&&it.instanceId==instanceId){
				return it;
			}
		}
		return null;
	}

	public boolean hasItem(GameItem item, int count) {
		int all = count;
		for (BagGrid g : grids) {
			GameItem it = g.getItem();
			if (it != null && it.equals(item)) {
				all -= g.getCount();
				if (all <= 0)
					return true;
			}
		}
		return false;
	}

	
	/**
	 * 先把指定的物品保留下来,等最后一次性提交物品改变
	 * @param template
	 * @param instanceId
	 * @param count
	 * @return
	 */
	protected boolean tryReserve(GameItem item,int count){
		int all = count;
		for (BagGrid g : grids) {
			int added = g.reserve(item, all);
			all -= added;
			if (all == 0)
				return true;
		}
		return false;
	}

	void reserve(BagGrid grid) {
		reserved.add(grid);
	}

	void addReserveToGrid(boolean notify) {
		for (BagGrid g : reserved) {
			g.complete(notify);
		}
	}
	
	public BagGrid getBagGrid(int id){
		return grids.get(id);
	}

	protected void clearReserved() {
		for (BagGrid g : reserved) {
			g.clearReserved();
//			changed.put(g.id, g);
		}
		reserved.clear();
	}

//	public Collection<BagGrid> getChanged() {
//		Collection<BagGrid> ret = new ArrayList<BagGrid>(changed.values());
//		return ret;
//	}
//	
//	public void cleanChanged(){
//		changed.clear();
//	}
	
	@Override
	protected Bag clone()  {
		Bag clone = new Bag(owner,size);
		for(int i=0,size=grids.size();i<size;i++){
			BagGrid cloneBg = clone.grids.get(i);
			BagGrid bg = grids.get(i);
			cloneBg.item = bg.item;
			cloneBg.count = bg.count;
		}
		return clone;
	}
	
	
//	public byte[] toClientBytes(){
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		DataOutputStream dos = new DataOutputStream(baos);
//		try {
//			dos.write(size);
//			for(BagGrid grid:grids){
//				dos.write(grid.id);
//				dos.write(grid.count);
//				if(grid.item!=null)
//					dos.write(grid.item.toClientBytes());
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return baos.toByteArray();
//	}
	
//	public boolean isChanged(){
//		return changed.size()>0;
//	}
//	
//	public Collection<BagGrid> getChangedAndClean(){
//		Collection<BagGrid> ret = new ArrayList<BagGrid>(changed.values());
//		changed.clear();
//		return ret;
//	}
	
//	public static byte[] toSyncClientBytesAndCleanChanged(Bag bag){
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		DataOutputStream dos = new DataOutputStream(baos);
//		try {
//			dos.write(-1);  //表明是包格同步
//			dos.write(bag.changed.size());
//			for(BagGrid grid:bag.changed.values()){
//				dos.write(grid.id);
//				log.debug(String.format("SyncBagGridId:%d", grid.id));
//				dos.write(grid.count);
//				log.debug(String.format("SyncBagGridItemCount:%d", grid.count));
//				if(grid.item!=null){
//					log.debug(String.format("SyncBagGridItem:%s", grid.item.template.name));
//					dos.write(grid.item.toClientBytes());
//				}
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		bag.cleanChanged();
//		return baos.toByteArray();
//	}
}
