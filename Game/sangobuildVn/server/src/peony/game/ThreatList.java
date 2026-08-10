package peony.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * 一个游戏对象的仇恨表。仇恨表包括多个对象以及这些对象对自己的威胁值。
 * @author lighthu
 */
public class ThreatList {
	
	private static final Logger log = Logger.getLogger(ThreatList.class);
	/*
	 * 存储敌人，按威胁从大到小排序
	 */
	protected GameObjectRef[] enemies;
	/*
	 * 存储敌人的威胁，按从大到小排序
	 */
	protected float[] threats;
	
	protected int[] timestamps;
	/*
	 * 仇恨表中敌人的数量
	 */
	protected int count;
	/*
	 * 仇恨表中的所有敌人的快速查找表
	 */
	protected Set<GameObjectRef> enemySet;
	
	protected int lastDirectThreatTime; //最后一次直接仇恨时间
	
	/**
	 * 创建一个空的仇恨表。
	 */
	public ThreatList() {
		enemies = new GameObjectRef[2];
		threats = new float[2];
		timestamps = new int[2];
		count = 0;
		enemySet = new HashSet<GameObjectRef>();
	}

	/*
	 * 设置仇恨表的容量。
	 * @param len 新容量，这个容量必须大于count
	 */
	protected void setCapacity(int len) {
		GameObjectRef[] arr1 = new GameObjectRef[len];
		float[] arr2 = new float[len];
		int[] arr3 = new int[len];
		System.arraycopy(enemies, 0, arr1, 0, count);
		System.arraycopy(threats, 0, arr2, 0, count);
		System.arraycopy(timestamps, 0, arr3, 0, count);
		enemies = arr1;
		threats = arr2;
		timestamps = arr3;
	}
	
	/*
	 * 扩展仇恨表容量。
	 */
	protected void expand(int newCount) {
		if (enemies.length < count + newCount) {
			setCapacity(enemies.length * 2);
		}
	}
	
	/*
	 * 压缩仇恨表存储。
	 */
	protected void pack() {
		if (enemies.length > count + 2) {
			setCapacity(count + 2);
		}
	}
	
	/**
	 * 向仇恨表中添加一个对象。
	 * @param u 敌人对象
	 * @param initThreat 初始威胁
	 */
	public synchronized void addUnit(GameObjectRef u, float initThreat,boolean direct) {
		if (enemySet.contains(u)) {
			updateThreat(u, initThreat, direct);
			if(direct){
				lastDirectThreatTime = Time.currTime;
			}
			return;
		}
//		if(!direct)
//			return;
		expand(1);
		
		// 从后往前扫描，找到第一个威胁值大于新敌人威胁的敌人，把新敌人插入到这个人的后面
		int insertPos;
		for (insertPos = count - 1; insertPos >= 0; insertPos--) {
			if (threats[insertPos] > initThreat) {
				break;
			}
			threats[insertPos + 1] = threats[insertPos];
			enemies[insertPos + 1] = enemies[insertPos];
			timestamps[insertPos + 1] = timestamps[insertPos];
		}
		threats[insertPos + 1] = initThreat;
		enemies[insertPos + 1] = u;
		if(direct)
			timestamps[insertPos + 1] = Time.currTime;
		enemySet.add(u);
		count++;
		if(direct){
			lastDirectThreatTime = Time.currTime;
		}
	}
	
	/**
	 * 从仇恨表中删除一个对象。
	 * @param u
	 */
	public synchronized void removeUnit(GameObjectRef u) {
		if (!enemySet.contains(u)) {
			return;
		}
		
		// 从前向后扫描，把被删除对象后面的对象前移
		boolean found = false;
		for (int i = 0; i < count; i++) {
			if (enemies[i].equals(u)) {
				found = true;
				enemies[i] = null;
			} else if (found) {
				threats[i - 1] = threats[i];
				enemies[i - 1] = enemies[i];
				timestamps[i - 1] = timestamps[i];
				enemies[i] = null;
			}
		}
		if (found) {
			enemySet.remove(u);
			count--;
		}
		pack();
		if(enemySet.isEmpty())
			lastDirectThreatTime = -1;
	}
	
	/**
	 * 修改某个敌人的威胁值。
	 * @param u 
	 * @param threatInc 威胁值增量，小于0表示减少
	 */
	protected void updateThreat(GameObjectRef u, float threatInc, boolean direct) {
		if (!enemySet.contains(u)) {
			return;
		}
		boolean found = false;
		float newThreat = 0.0f;
		if (threatInc > 0.0) {
			// 增加威胁值，从后往前扫描，原来在列表中此对象前面的对象需要和这个对象的
			// 新威胁值比较，如果较小则需要交换
			for (int i = count - 1; i >= 0; i--) {
				if (enemies[i].equals(u)) {
					found = true;
					threats[i] += threatInc;
					if(direct)
						timestamps[i] = Time.currTime;
					newThreat = threats[i];
				} else if (found) {
					if (threats[i] < threats[i + 1]) {
						enemies[i + 1] = enemies[i];
						threats[i + 1] = threats[i];
						timestamps[i + 1] = timestamps[i];
						enemies[i] = u;
						threats[i] = newThreat;
						if(direct)
							timestamps[i] = Time.currTime;
					} else {
						break;
					}
				}
			}
		} else {
			// 减少威胁值，从前向后扫描，原来在列表中此对象后面的对象需要和这个对象的
			// 新威胁值比较，如果较大则需要交换
			for (int i = 0; i < count; i++) {
				if (enemies[i].equals(u)) {
					found = true;
					threats[i] += threatInc;
					if(direct)
						timestamps[i] = Time.currTime;
					newThreat = threats[i];
				} else if (found) {
					if (threats[i] > threats[i - 1]) {
						enemies[i - 1] = enemies[i];
						threats[i - 1] = threats[i];
						if(direct)
							timestamps[i - 1] = Time.currTime;
						enemies[i] = u;
						threats[i] = newThreat;
					} else {
						break;
					}
				}
			}
		}
	}
	
	public void update(Unit owner) {
		try {
			List<GameObjectRef> invalidRefs = null;
			synchronized (this) {
				if (owner.type == GameObject.TYPE_PLAYER) { // 如果跟其他玩家超出6秒且无直接仇恨，清除仇恨
					if (!enemySet.isEmpty()) {
						GameObjectRef[] tmps = new GameObjectRef[enemies.length];
						System.arraycopy(enemies, 0, tmps, 0, enemies.length);
						int[] tstmp = new int[timestamps.length];
						System.arraycopy(timestamps, 0, tstmp, 0, tstmp.length);
						for (int i = 0; i < tmps.length; i++) {
							if (tmps[i] != null) {
								if (tmps[i].type == GameObject.TYPE_PLAYER) {
									if ((Time.currTime - tstmp[i]) >= 6000) {
										if (invalidRefs == null) {
											invalidRefs = new ArrayList<GameObjectRef>();
										}
										invalidRefs.add(tmps[i]);
									}
								}
							}
						}
					}
				} else if (owner.type == GameObject.TYPE_CREATURE) { // 如果超出追击范围10码并且无直接仇恨5秒清除仇恨
					if (!enemySet.isEmpty()) {
						GameObjectRef[] tmps = new GameObjectRef[enemies.length];
						System.arraycopy(enemies, 0, tmps, 0, enemies.length);
						int[] ts = new int[timestamps.length];
						System.arraycopy(timestamps, 0, ts, 0, timestamps.length);
						int dist = ((Creature)owner).chaseDistance + 80;
						for (int i = 0; i < tmps.length; i++) {
							if (tmps[i] != null) {
								GameObject g = ObjectAccessor.getGameObject(tmps[i]);
								if (g == null
										|| (!g.inRange(owner, dist) && Time.currTime
												- ts[i] >= 5000)) {
									if (invalidRefs == null) {
										invalidRefs = new ArrayList<GameObjectRef>();
									}
									invalidRefs.add(tmps[i]);
								}
							}
						}
					}
				}
			}
			
			// 移除过期仇恨必须拿到sync块外面来，否则可能会引起死锁。
			if (invalidRefs != null) {
				for (GameObjectRef ref : invalidRefs) {
					owner.removeThreatUnit(ref, true);
				}
			}
		} catch (NullPointerException e) {
			// 如果由于数据不一致造成仇恨表中有空的对象，则集中核查一次
			checkForConsistence(owner);
		}
	}
	
	/**
	 * 判断仇恨表中是否包含某个对象。
	 * @param u
	 * @return
	 */
	public boolean contains(GameObjectRef u) {
		return enemySet.contains(u);
	}
	
	/**
	 * 取得仇恨表中包含的敌人数量。
	 * @return
	 */
	public int getCount() {
		return count;
	}
	
	/**
	 * 取得某个对象的威胁值。
	 * @param u
	 * @return 如果列表中没有，返回0.0
	 */
	public synchronized float getThreat(GameObjectRef u) {
		if (!enemySet.contains(u)) {
			return 0.0f;
		}
		for (int i = 0; i < count; i++) {
			if (enemies[i] == u) {
				return threats[i];
			}
		}
		return 0.0f;
	}
	
	/**
	 * 取得仇恨表中排名最高的目标。
	 * @return 如果仇恨表为空，返回null。
	 */
	public synchronized GameObjectRef getFirstThreat() {
		if (count > 0) {
			return enemies[0];
		} else {
			return null;
		}
	}
	
	/**
	 * 取得所有仇恨表中的敌人。
	 */
	public synchronized GameObjectRef[] getAllThreats() {
		GameObjectRef[] ret = new GameObjectRef[count];
		System.arraycopy(enemies, 0, ret, 0, count);
		return ret;
	}
	
	/**
	 * 清除仇恨表。
	 */
	public synchronized void clear() {
		count = 0;
		pack();
		enemySet.clear();
		lastDirectThreatTime = -1;
	}
	
	/**
	 * 检查最后一次攻击是否在有效时间范围内。
	 * @param mills
	 * @return
	 */
	public boolean isLastDirectThreatTimeInRange(int mills){
		if(lastDirectThreatTime==-1)
			return false;
		else{
			return (Time.currTime - lastDirectThreatTime)<mills; 
		}
	}
	
	/*
	 * 全面检查仇恨表中的数据是否正常。
	 */
	private synchronized void checkForConsistence(Unit owner) {
		// 检查是否所有仇恨表中的对象不为空且存在，并重构enemySet
		enemySet.clear();
		for (int i = 0; i < count; i++) {
			boolean valid = true;
			if (enemies[i] == null) {
				valid = false;
			} else {
				// 对方必须存在并且仇恨表中有我
				Unit obj = (Unit)ObjectAccessor.getGameObject(enemies[i]);
				if (obj == null) {
					valid = false;
				} else if (!obj.threats.contains(owner.ref())) {
					valid = false;
				}
			}
			if (!valid) {
				// 删除这个条目
				for (int j = i + 1; j < count; j++) {
					enemies[j - 1] = enemies[j];
					threats[j - 1] = threats[j];
					timestamps[j - 1] = timestamps[j];
				}
				i--;
				count--;
			} else {
				// 此条目有效
				enemySet.add(enemies[i]);
			}
		}
	}
}
