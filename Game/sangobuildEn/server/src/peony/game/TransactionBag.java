package peony.game;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import peony.game.changed.BagChangedItem;
import peony.game.changed.ChangedItem;
import peony.service.fame.Fame;
import peony.service.fame.FameService;

public class TransactionBag {

	private static final Logger log = Logger.getLogger(TransactionBag.class);

	private static final ItemComparator comparator = new ItemComparator();

	protected Player owner;
	protected int size;
	protected int addedSize;
	protected List<TransactionBagGrid> grids;
	protected Lock lock = new ReentrantLock();
	// 锁定大小标志，当扩展包格时锁定，以避免刷包格
	protected boolean sizeLocked;

	protected Map<Integer, Set<Integer>> indexes = new TreeMap<Integer, Set<Integer>>();

	public TransactionBag(Player owner, int size, int addedSize) {
		this.owner = owner;
		this.size = size;
		this.addedSize = addedSize;
		grids = new ArrayList<TransactionBagGrid>(getSize());
		for (int i = 0; i < getSize(); i++) {
			TransactionBagGrid grid = new TransactionBagGrid(i, this);
			grids.add(grid);
			addIndex(-1, grid.id);
		}
	}
	
	public TransactionBag(Fame fame, int size, int addedSize) {
		Player owner = FameService.statuePlayer.get(fame.playerId);
		this.owner = owner;
		this.size = size;
		this.addedSize = addedSize;
		grids = new ArrayList<TransactionBagGrid>(getSize());
		for (int i = 0; i < getSize(); i++) {
			TransactionBagGrid grid = new TransactionBagGrid(i, this);
			grids.add(grid);
			addIndex(-1, grid.id);
		}
	}
	
	public boolean isSizeLocked() {
		return sizeLocked;
	}
	
	public void lockSize() {
		sizeLocked = true;
	}
	
	public void unlockSize() {
		sizeLocked = false;
	}
	
	public List<TransactionBagGrid> getGrids(){
		return grids;
	}

	protected void addIndex(int itemId, int gridId) {
		Set<Integer> gs = indexes.get(itemId);
		if (gs == null) {
			gs = new TreeSet<Integer>();
			indexes.put(itemId, gs);
		}
		gs.add(gridId);
	}

	protected void removeIndex(int itemId, int gridId) {
		Set<Integer> gs = indexes.get(itemId);
		if (gs != null) {
			gs.remove(gridId);
			if (gs.isEmpty()) {
				indexes.put(itemId, null);
			}
		}
	}

	public int getSize() {
		return size + addedSize;
	}

	public int getAddedSize() {
		return addedSize;
	}

	public boolean arrange() {
		lock.lock();
		try {
			Map<GameItem, Integer> tmp = new TreeMap<GameItem, Integer>(
					comparator);
			try {
				for (TransactionBagGrid grid : grids) {
					if (grid.entities != null && grid.entities.size() > 0)
						return false;
					if (grid.item != null && grid.count > 0) {
						Integer value = tmp.get(grid.item);
						if (value != null) {
							tmp.remove(grid.item);
							tmp.put(grid.item, value + grid.count);
						} else {
							tmp.put(grid.item, grid.count);
						}
					}
				}
			} catch (Exception ex) {
				return false;
			}
			indexes.clear();
			for (TransactionBagGrid grid : grids) {
				grid.item = null;
				grid.count = 0;
				addIndex(-1, grid.id);
			}
			Iterator<Map.Entry<GameItem, Integer>> ite = tmp.entrySet()
					.iterator();
			while (ite.hasNext()) {
				Map.Entry<GameItem, Integer> entry = ite.next();
				if (entry.getValue() > 1
						&& entry.getKey().instanceId != GameItem.GENERAL_INSTANCEID) {
					boolean added = addGameItem0(entry.getKey(), 1);
					if(!added){
						log.info("[BAGARRANGEERROR]"
								+ LogUtil.getPlayerLogString(owner)
								+ LogUtil.getGameItemString(entry.getKey(),
										entry.getValue()));
					}
					log.info("[BAGARRANGEINSTANCEERROR]"
							+ LogUtil.getPlayerLogString(owner)
							+ LogUtil.getGameItemString(entry.getKey(),
									entry.getValue()));
				} else {
					boolean added = addGameItem0(entry.getKey(), entry
							.getValue());
					if (!added) {
						log.info("[BAGARRANGEERROR]"
								+ LogUtil.getPlayerLogString(owner)
								+ LogUtil.getGameItemString(entry.getKey(),
										entry.getValue()));
					}
				}
			}
			return true;
		} finally {
			lock.unlock();
		}
	}

	public boolean exchange(int sourceId, int targetId, boolean isBag) {
		if (sourceId >= 0 && sourceId <= grids.size() && targetId >= 0
				&& targetId <= grids.size() && sourceId != targetId) {
			lock.lock();
			try {
				TransactionBagGrid sourceGrid = grids.get(sourceId);
				TransactionBagGrid targetGrid = grids.get(targetId);
				if (sourceGrid.count == 0)
					return false;
				if ((sourceGrid.entities != null && sourceGrid.entities.size() > 0)
						|| (targetGrid.entities != null && targetGrid.entities
								.size() > 0))
					return false;
				if (sourceGrid.isEmpty() && targetGrid.isEmpty())
					return false;
				if (targetGrid.isEmpty()) {
					GameItem item = sourceGrid.item;
					int count = sourceGrid.count;
					sourceGrid.item = null;
					sourceGrid.count = 0;
					removeIndex(-1, targetGrid.id);
					removeIndex(item.template.id, sourceGrid.id);
					targetGrid.item = item;
					targetGrid.count = count;
					addIndex(item.template.id, targetGrid.id);
					addIndex(-1, sourceGrid.id);
					if (isBag) {
						owner.changed.addChangedItem(new BagChangedItem(sourceGrid));
						owner.changed.addChangedItem(new BagChangedItem(targetGrid));
					}
					return true;
				}
				if (sourceGrid.item.equals(targetGrid.item)) {
					if (targetGrid.count >= targetGrid.item.template.maxCount)
						return false;
					int v = Math.min(sourceGrid.count,
							targetGrid.item.template.maxCount
									- targetGrid.count);
					sourceGrid.count -= v;
					targetGrid.count += v;
					if (sourceGrid.count == 0) {
						removeIndex(sourceGrid.item.template.id, sourceGrid.id);
						sourceGrid.item = null;
						addIndex(-1, sourceGrid.id);
					}
					if (isBag) {
						owner.changed.addChangedItem(new BagChangedItem(sourceGrid));
						owner.changed.addChangedItem(new BagChangedItem(targetGrid));
					}
					return true;
				} else {
					removeIndex(sourceGrid.item.template.id, sourceGrid.id);
					removeIndex(targetGrid.item.template.id, targetGrid.id);
					GameItem item = sourceGrid.item;
					int count = sourceGrid.count;
					sourceGrid.item = targetGrid.item;
					sourceGrid.count = targetGrid.count;
					targetGrid.item = item;
					targetGrid.count = count;
					addIndex(sourceGrid.item.template.id, sourceGrid.id);
					addIndex(targetGrid.item.template.id, targetGrid.id);
					if (isBag) {
						owner.changed.addChangedItem(new BagChangedItem(sourceGrid));
						owner.changed.addChangedItem(new BagChangedItem(targetGrid));
					}
					return true;
				}
			} finally {
				lock.unlock();
			}
		} else {
			return false;
		}
	}

	protected Set<Integer> getIndexSet(int itemId) {
		return indexes.get(itemId);
	}

	public int getFreeBagCount() {
		lock.lock();
		try {
			int count = 0;
			for (TransactionBagGrid grid : grids) {
				if (grid.isEmpty())
					count++;
			}
			return count;
		} finally {
			lock.unlock();
		}
	}

	public void addGainComplete(Gain gain, PlayerTransaction tx, boolean notify)
			throws NoEnoughSpaceException {
		lock.lock();
		try {
			if (gain.isEmpty())
				return;
			GainItem[] items = gain.getGainItems();
			boolean ret = true;
			for (GainItem item : items) {
				boolean ok = addGameItem(item.item, item.count, tx, notify);
				if (!ok)
					ret = false;
			}
			if (!ret)
				throw new NoEnoughSpaceException();
		} catch (Exception ex) {
			// log.error(ex,ex);
			if (ex instanceof NoEnoughSpaceException) {
				throw (NoEnoughSpaceException) ex;
			} else {
				log.error(ex, ex);
			}
		} finally {
			lock.unlock();
		}
	}

	public boolean addGain(Gain gain, PlayerTransaction tx, boolean notify) {
		lock.lock();
		try {
			if (gain.isEmpty())
				return true;
			GainItem[] items = gain.getGainItems();
			boolean ret = true;
			for (GainItem item : items) {
				boolean ok = addGameItem(item.item, item.count, tx, notify);
				if (!ok)
					ret = false;
			}
			return ret;
		} catch (Exception ex) {
			log.error(ex, ex);
			return false;
		} finally {
			lock.unlock();
		}
	}
	
	public GameItem removeGameItemIngoreInstanceId(int itemId,int count,PlayerTransaction tx, boolean notify){
		lock.lock();
		try {
			Set<Integer> set = getIndexSet(itemId);
			if (set == null) {
				return null;
			}
			int all = count;
			for (int gridId : set) {
				TransactionBagGrid grid = grids.get(gridId);
				int v = grid
						.removeGameItem(itemId, grid.item.instanceId, all, tx, notify);
				all -= v;
				if (all == 0)
					return grid.item;
			}
			return null;

		} catch (Exception ex) {
			log.error(ex, ex);
			return null;
		} finally {
			lock.unlock();
		}
	}

	public GameItem removeGameItem(int itemId, int instanceId, int count,
			PlayerTransaction tx, boolean notify) {
		lock.lock();
		try {
			Set<Integer> set = getIndexSet(itemId);
			if (set == null) {
				return null;
			}
			int all = count;
			for (int gridId : set) {
				TransactionBagGrid grid = grids.get(gridId);
				int v = grid
						.removeGameItem(itemId, instanceId, all, tx, notify);
				all -= v;
				if (all == 0)
					return grid.item;
			}
			return null;

		} catch (Exception ex) {
			log.error(ex, ex);
			return null;
		} finally {
			lock.unlock();
		}
	}

	public void addGameItemComplete(GameItem item, int count,
			PlayerTransaction tx, boolean notify) throws NoEnoughSpaceException {
		lock.lock();
		try {
			Set<Integer> set = null;
			int all = count;
			if (item.instanceId == GameItem.GENERAL_INSTANCEID) {
				set = getIndexSet(item.template.id);
				if (set != null) {
					for (int gridId : set) {
						TransactionBagGrid grid = grids.get(gridId);
						int v = grid.addGameItem(item, all, tx, notify);
						all -= v;
						if (all == 0)
							return;
					}
				}
			}
			set = getIndexSet(-1);
			if (set != null) {
				for (int gridId : set) {
					TransactionBagGrid grid = grids.get(gridId);
					int v = grid.addGameItem(item, all, tx, notify);
					all -= v;
					if (all == 0)
						return;
				}
			}
			owner.addIntPropertyChangedItem(ChangedItem.GRIDFULL, 1, false,
					true);
			throw new NoEnoughSpaceException();
		} catch (Exception ex) {
			log.error(ex, ex);
			if (ex instanceof NoEnoughSpaceException)
				throw (NoEnoughSpaceException) ex;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 用于仓库
	 */
	public List<TransactionBagGrid> addDepotGameItemComplete(GameItem item, int count,
			PlayerTransaction tx, boolean notify) throws NoEnoughSpaceException {
		lock.lock();
		List<TransactionBagGrid> list = new ArrayList<TransactionBagGrid>();
		try {
			Set<Integer> set = null;
			int all = count;
			if (item.instanceId == GameItem.GENERAL_INSTANCEID) {
				set = getIndexSet(item.template.id);
				if (set != null) {
					for (int gridId : set) {
						TransactionBagGrid grid = grids.get(gridId);
						int v = grid.addDepotGameItem(item, all, tx, notify);
						all -= v;
						list.add(grid);
						if (all == 0){
							return list;
						}
					}
				}
			}
			set = getIndexSet(-1);
			if (set != null) {
				for (int gridId : set) {
					TransactionBagGrid grid = grids.get(gridId);
					int v = grid.addDepotGameItem(item, all, tx, notify);
					all -= v;
					if (all == 0){
						list.add(grid);
						return list;
					}
				}
			}
//			owner.addIntPropertyChangedItem(ChangedItem.GRIDFULL, 1, false,
//					true);
			throw new NoEnoughSpaceException();
		} catch (Exception ex) {
			log.error(ex, ex);
			if (ex instanceof NoEnoughSpaceException)
				throw (NoEnoughSpaceException) ex;
		} finally {
			lock.unlock();
		}
		return list;
	}
	
	protected boolean addGameItem0(GameItem item, int count) {
		lock.lock();
		try {
			Set<Integer> set = null;
			int all = count;
			if (item.instanceId == GameItem.GENERAL_INSTANCEID) {
				set = getIndexSet(item.template.id);
				if (set != null) {
					for (int gridId : set) {
						TransactionBagGrid grid = grids.get(gridId);
						int v = grid.addGameItem0(item, all);
						all -= v;
						if (all == 0)
							return true;
					}
				}
			}
			set = getIndexSet(-1);
			if (set != null) {
				for (int gridId : set) {
					TransactionBagGrid grid = grids.get(gridId);
					int v = grid.addGameItem0(item, all);
					all -= v;
					if (all == 0)
						return true;
				}
			}
			return false;
		} catch (Exception ex) {
			log.error(ex, ex);
			return false;
		} finally {
			lock.unlock();
		}
	}

	public boolean addGameItem(GameItem item, int count, PlayerTransaction tx,
			boolean notify) {
		lock.lock();
		try {
			Set<Integer> set = null;
			int all = count;
			if (item.instanceId == GameItem.GENERAL_INSTANCEID) {
				set = getIndexSet(item.template.id);
				if (set != null) {
					for (int gridId : set) {
						TransactionBagGrid grid = grids.get(gridId);
						int v = grid.addGameItem(item, all, tx, notify);
						all -= v;
						if (all == 0)
							return true;
					}
				}
			}
			set = getIndexSet(-1);
			if (set != null) {
				for (int gridId : set) {
					TransactionBagGrid grid = grids.get(gridId);
					int v = grid.addGameItem(item, all, tx, notify);
					all -= v;
					if (all == 0)
						return true;
				}
			}
			owner.addIntPropertyChangedItem(ChangedItem.GRIDFULL, 1, false,
					true);
			return false;
		} catch (Exception ex) {
			log.error(ex, ex);
			return false;
		} finally {
			lock.unlock();
		}
	}
	
	public TransactionBagGrid getGrid(int index){
		return grids.get(index);
	}

	/**
	 * 只使用于移除有InstanceId的物品
	 * 
	 * @param itemId
	 * @param instanceId
	 * @param tx
	 * @param notify
	 * @return
	 */
	public TransactionBagGrid removeGameItemInstance(int itemId,
			int instanceId, PlayerTransaction tx, boolean notify) {
		lock.lock();
		try {
			if (instanceId == -1)
				throw new IllegalArgumentException();
			for (TransactionBagGrid grid : grids) {
				if (grid.removeGameItem(itemId, instanceId, 1, tx, notify) == 1) {
					return grid;
				}
			}
			return null;
		} catch (Exception e) {
			log.error(e, e);
			return null;
		} finally {
			lock.unlock();
		}
	}

	public TransactionBagGrid removeGameItemInOneGrid(int itemId,
			int instanceId, int count, PlayerTransaction tx, boolean notify) {
		lock.lock();
		try {
			for (TransactionBagGrid grid : grids) {
				if (grid.removeGameItem(itemId, instanceId, count, tx, notify) == count) {
					return grid;
				}
			}
			return null;
		} catch (Exception e) {
			log.error(e, e);
			return null;
		} finally {
			lock.unlock();
		}
	}

	public TransactionBagGrid removeGridGameItem(int gridId, int itemId,
			int instanceId, int count, PlayerTransaction tx, boolean notify) {
		lock.lock();
		try {
			if (gridId == -1)
				return removeGameItemInOneGrid(itemId, instanceId, count, tx,
						notify);
			else if (gridId < 0 || gridId >= getSize())
				return null;
			TransactionBagGrid grid = grids.get(gridId);
			if (grid.removeGameItem(itemId, instanceId, count, tx, notify) == count) {
				return grid;
			}
			return null;
		} catch (Exception e) {
			log.error(e, e);
			return null;
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * 拆分物品。
	 * @param gridId 包格ID
	 * @param itemId 物品ID
	 * @param instanceId 物品instanceID
	 * @param count 拆分数量
	 * @throws NoEnoughSpaceException 背包已满
	 * @throws RuntimeException 其他错误
	 */
	public ChangedItem[] splitGridGameItem(int gridId, int itemId, int count) throws NoEnoughSpaceException {
		lock.lock();
		try {
			if (gridId < 0 || gridId >= getSize()) {
				throw new RuntimeException();
			}
			
			// 查找一个空的包格
			TransactionBagGrid newGrid = null;
			for (TransactionBagGrid grid : grids) {
				if (grid.isEmpty()) {
					newGrid = grid;
					break;
				}
			}
			if (newGrid == null) {
				throw new NoEnoughSpaceException();
			}
		
			// 从旧格中拆分出物品来
			TransactionBagGrid grid = grids.get(gridId);
			GameItem item = grid.split(itemId, count);
			
			// 在一个空格中加入物品
			newGrid.addGameItem0(item, count);
			
			// 向客户端同步改动
			ChangedItem[] ret = new ChangedItem[2];
			ret[0] = new BagChangedItem(grid);
			ret[1] = new BagChangedItem(newGrid);
			return ret;
		} finally {
			lock.unlock();
		}
	}

	/**
	 *用于仓库
	 */
	public TransactionBagGrid removeDepotGameItemInOneGrid(int itemId,
			int instanceId, int count, PlayerTransaction tx, boolean notify) {
		lock.lock();
		try {
			for (TransactionBagGrid grid : grids) {
				if (grid.removeDepotGameItem(itemId, instanceId, count, tx, notify) == count) {
					return grid;
				}
			}
			return null;
		} catch (Exception e) {
			log.error(e, e);
			return null;
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * 获取指定ID的物品的数量
	 * @param itemId	物品ID
	 * @return			物品数量
	 */
	public int getGameItemCount(int itemId) {
		int total = 0;
		for (TransactionBagGrid g : grids) {
			if (g.item != null && g.item.template.id == itemId)
				total += g.count;
		}
		return total;
	}
	
	/**
	 * 获取指定包格位置的物品的数量
	 * @param gridId	包格ID
	 * @return			物品数量
	 */
	public int getGridItemCount(int gridId) {
		int num = 0;
		for (TransactionBagGrid g : grids) {
			if (g.item != null && g.id == gridId) {
				num = g.count;
				break;
			}
		}
		return num;
	}

	/**
	 * 用于仓库移除包格
	 */
	public TransactionBagGrid removeDepotGridGameItem(int gridId, int itemId,
			int instanceId, int count, PlayerTransaction tx, boolean notify) {
		lock.lock();
		try {
			if (gridId == -1)
				return removeDepotGameItemInOneGrid(itemId, instanceId, count, tx,
						notify);
			else if (gridId < 0 || gridId >= getSize())
				return null;
			TransactionBagGrid grid = grids.get(gridId);
			if (grid.removeDepotGameItem(itemId, instanceId, count, tx, notify) == count) {
				return grid;
			}
			return null;
		} catch (Exception e) {
			log.error(e, e);
			return null;
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * 找到第一个id相同的物品立即返回
	 * 
	 * @param itemId
	 * @return
	 */
	public GameItem getGameItem(int itemId) {
		lock.lock();
		try {
			for (TransactionBagGrid g : grids) {
				if (g.item != null && g.item.template.id == itemId)
					return g.item;
			}
			return null;
		} finally {
			lock.unlock();
		}
	}

	public GameItem getGameItem(int gridId, int itemId, int instanceId) {
		lock.lock();
		try {
			TransactionBagGrid grid = null;
			if (gridId == -1) {
				for (TransactionBagGrid g : grids) {
					if (g.item != null && g.item.instanceId == instanceId
							&& g.item.template.id == itemId) {
						grid = g;
						break;
					}
				}
			} else {
				if (gridId < 0 || gridId >= getSize())
					return null;
				TransactionBagGrid g = grids.get(gridId);
				if (g.item != null && g.item.instanceId == instanceId
						&& g.item.template.id == itemId)
					grid = g;
			}
			if (grid != null) {
				return grid.item;
			}
			return null;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 将背包扩展到
	 * 
	 * @param count
	 */
	public void extend(int count, boolean isAddedSize) {
		lock.lock();
		try {
			int c = 0;
			if (isAddedSize) {
				c = count - addedSize;
			} else {
				c = count - size;
			}
			if (c <= 0)
				return;
			int oldSize = getSize();
			for (int i = 0; i < c; i++) {
				TransactionBagGrid g = new TransactionBagGrid(oldSize + i, this);
				grids.add(g);
				addIndex(-1, g.id);
			}
			if (isAddedSize) {
				addedSize = count;
			} else {
				size = count;
			}
			owner.addIntPropertyChangedItem(ChangedItem.GRIDCOUNT, getSize(),
					false, true);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * 用于仓库扩展
	 */
	public TransactionBagGrid[] extendDepot(int count, boolean isAddedSize) {
		lock.lock();
		try {
			int c = 0;
			if (isAddedSize) {
				c = count - addedSize;
			} else {
				c = count - size;
			}
			if (c <= 0)
				return new TransactionBagGrid[0];
			int oldSize = getSize();
			TransactionBagGrid[] ret = new TransactionBagGrid[c];
			for (int i = 0; i < c; i++) {
				TransactionBagGrid g = new TransactionBagGrid(oldSize + i, this);
				grids.add(g);
				addIndex(-1, g.id);
				ret[i] = g;
			}
			if (isAddedSize) {
				addedSize = count;
			} else {
				size = count;
			}
			return ret;
		} finally {
			lock.unlock();
		}
	}

	void lock() {
		lock.lock();
	}

	void unlock() {
		lock.unlock();
	}

	@Override
	public TransactionBag clone() {
		TransactionBag clone = new TransactionBag(owner, size, addedSize);
		for (int i = 0, size = grids.size(); i < size; i++) {
			TransactionBagGrid cloneBg = clone.grids.get(i);
			TransactionBagGrid bg = grids.get(i);
			cloneBg.item = bg.item;
			cloneBg.count = bg.count;
		}
		return clone;
	}
	
	/**
	 * 仅用于测试，使用cheat中的clearbag命令。
	 */
	public void clear(PlayerTransaction tx, boolean notify) {
		for (TransactionBagGrid grid : grids) {
			if (grid == null ||  grid.item == null) {
				continue;
			}
			removeGridGameItem(grid.id, grid.item.template.id, grid.item.instanceId, grid.count, tx, notify);
		}
	}

	public byte[] toClientBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(getSize());
			for (TransactionBagGrid grid : grids) {
				dos.write(grid.toClientByte());
			}
		} catch (Exception ex) {

		}
		return baos.toByteArray();
	}
}

// class GameItemComparator implements Comparator<GameItem> {
//
// public int compare(GameItem o1, GameItem o2) {
// int ret = o1.template.showType - o2.template.showType;
// if (ret == 0) {
// int ret1 = o1.template.id - o2.template.id;
// if (ret1 == 0) {
// if (o1.instanceId == GameItem.GENERAL_INSTANCEID
// && o2.instanceId == GameItem.GENERAL_INSTANCEID) {
// return o1.template.id - o2.template.id;
// } else {
// return o1.instanceId - o2.instanceId;
// }
// }
// return ret1;
// }
// return ret;
// }
//
// }
