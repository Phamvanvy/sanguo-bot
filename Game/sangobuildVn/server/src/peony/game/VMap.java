package peony.game;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import peony.net.Packet;
import peony.vm.ASMQuest;
import peony.vm.ASMQuestUtil;
import peony.util.IntHashMap;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapExit;
import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;

public class VMap {
	/**
	 * 同格内的玩家之间关联度缺省值。
	 */
	public static final int SAME_CELL_COUNT = 5;
	/**
	 * 相邻格子的玩家之间关联度缺省值。
	 */
	public static final int ADJ_CELL_COUNT1 = 4;
	/**
	 * 相邻（较远）格子的玩家之间关联度缺省值。
	 */
	public static final int ADJ_CELL_COUNT2 = 1;

	public int sameCellCount = SAME_CELL_COUNT;
	public int adjCellCount1 = ADJ_CELL_COUNT1;
	public int adjCellCount2 = ADJ_CELL_COUNT2;

	private static final Logger log = Logger.getLogger(VMap.class);

	public static final Random MAP_RND = new Random();

	public GameMapDefinition mapDef;
	public World world;
	public Instance instance;
	public int forceInstanceID = -1;
	public VMapManager manager;
	// protected Map<Integer,Player> id2player = new HashMap<Integer,Player>();
	// protected Map<Integer,Creature> id2creature = new
	// HashMap<Integer,Creature>();
	// protected Map<Integer,GatherUnit> id2gatherunit = new
	// HashMap<Integer,GatherUnit>();
	public IntHashMap<GameObject> instanceid2objects = new IntHashMap<GameObject>();
	public IntHashMap<GameMapExit> exits = new IntHashMap<GameMapExit>();

	protected MapCell[][] cells;
	protected int col, row;
	protected int cellWidth, cellHeight;

	// protected static final int[][] TOP_LEFT = {
	// {-1,-1},{0,-1},{-1,0},{0,0}
	// };
	//	
	// protected static final int[][] TOP_RIGHT = {
	// {0,-1},{1,-1},{1,0},{0,0}
	// };
	//	
	// protected static final int[][] DOWN_LEFT = {
	// {-1,0},{-1,1},{0,1},{0,0}
	// };
	//	
	// protected static final int[][] DOWN_RIGHT = {
	// {1,0},{1,1},{0,1},{0,0}
	// };

	public int playerCount;
	public int lastActiveTime = Time.currTime; // 记录最后一次有玩家活动的时间

	protected DieCallback dieCallback;

	protected static Random rand = new Random();

	protected static final int[][] ROUND = { { -1, -1, }, { 0, -1 }, { 1, -1 },
			{ 1, 0 }, { 1, 1 }, { 0, 1 }, { -1, 1 }, { -1, 0 }, { 0, 0 } };

	// 所有矿物的templateid
	protected int[] temps = { 281, 282, 283, 284, 285, 286, 287, 288, 289, 290 };

	protected MapCellIterator iterator; // 这个iterator是用来做优化用的，在调用getMapCellSync的时候总是返回这个iterator,并且这个iterator实例并不会重新初始化，每次调用都重新init一次，尽量减少临时内存消耗

	public VMap(VMapManager manager, World world, GameMapDefinition def,
			int cellWidth, int cellHeight) {
		this.manager = manager;
		this.world = world;
		this.mapDef = def;
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
		this.col = (getWidth() / cellWidth)
				+ (getWidth() % cellWidth != 0 ? 1 : 0);
		this.row = (getHeight() / cellHeight)
				+ (getHeight() % cellHeight != 0 ? 1 : 0);
		this.cells = new MapCell[col][row];
		initCells();
		iterator = new MapCellIterator(this);
	}

	public int getMapID() {
		return mapDef.mapInfo.getGlobalID();
	}

	public boolean isNeutral() {
		return mapDef.mapInfo.neutral;
	}

	public boolean isAllowDuel() {
		return mapDef.mapInfo.allowDuel;
	}

	protected void initCells() {
		for (int i = 0; i < this.col; i++) {
			for (int j = 0; j < this.row; j++) {
				this.cells[i][j] = new MapCell(this, i * cellWidth, j
						* cellHeight, cellWidth, cellHeight);
			}
		}
		for (int i = 0; i < this.col; i++) {
			for (int j = 0; j < this.row; j++) {
				this.cells[i][j].cells = getMapCell(ROUND, i, j);
				// this.cells[i][j].topLeft = getMapCell(TOP_LEFT,i,j);
				// this.cells[i][j].topRight = getMapCell(TOP_RIGHT,i,j);
				// this.cells[i][j].downLeft = getMapCell(DOWN_LEFT,i,j);
				// this.cells[i][j].downRight = getMapCell(DOWN_RIGHT,i,j);
				// log(i,j,this.cells[i][j]);
			}

		}
	}

	public List<Player> getPlayersByFaction(int faction) {
		List<Player> ret = new ArrayList<Player>(instanceid2objects.size());
		for (GameObject o : instanceid2objects.values()) {
			if (o.type == GameObject.TYPE_PLAYER && o.faction == faction) {
				ret.add((Player) o);
			}
		}
		return ret;
	}

	public boolean allowFollow() {
		if (instance != null) {
			return false;
		}
		return true;
	}

	/**
	 * 是否隔离不同阵营的玩家使其不可见。
	 * 
	 * @return
	 */
	public boolean splitFaction() {
		return mapDef.mapInfo.splitFaction;
	}

	// protected void log(int col,int row,MapCell cell){
	// log.debug(String.format("CENTER[%d][%d][%d][%d]",col,row,cell.startX,cell.startY));
	// log.debug("TopLeft");
	// for(int i=0;i<cell.topLeft.length;i++){
	// log(cell.topLeft[i]);
	// }
	// log.debug("TopRight");
	// for(int i=0;i<cell.topRight.length;i++){
	// log(cell.topRight[i]);
	// }
	// log.debug("DownLeft");
	// for(int i=0;i<cell.downLeft.length;i++){
	// log(cell.downLeft[i]);
	// }
	// log.debug("downRight");
	// for(int i=0;i<cell.downRight.length;i++){
	// log(cell.downRight[i]);
	// }
	// }
	//	
	// protected void log(MapCell cell){
	// log.debug(String.format("cell[%d][%d]",cell.startX,cell.startY));
	// }

	public int[] getRelivePoint(int faction) {
		if (faction == GameObject.FACTION_WEI) {
			return mapDef.mapInfo.renascenceWei;
		}
		if (faction == GameObject.FACTION_SHU) {
			return mapDef.mapInfo.renascenceShu;
		}
		if (faction == GameObject.FACTION_WU) {
			return mapDef.mapInfo.renascenceWu;
		}
		return null;
	}

	protected MapCell[] getMapCell(int[][] offsets, int col, int row) {
		List<MapCell> mcs = new ArrayList<MapCell>(9);
		for (int i = 0; i < offsets.length; i++) {
			int x = col + offsets[i][0];
			int y = row + offsets[i][1];
			if (x >= 0 && x < this.col && y >= 0 && y < this.row) {
				mcs.add(this.cells[x][y]);
			}
		}
		MapCell[] mc = new MapCell[mcs.size()];
		mcs.toArray(mc);
		return mc;
	}

	protected MapCell getMapCell(int x, int y) {
		int c = x / cellWidth;
		int r = y / cellHeight;
		if (c < 0) {
			c = 0;
		} else if (c >= col) {
			c = col - 1;
		}
		if (r < 0) {
			r = 0;
		} else if (r >= row) {
			r = row - 1;
		}
		return cells[c][r];
	}

	public MapCellIterator getMapCellsSync(Unit u, int x, int y, int dist) {
		int startcx = (x - dist) / cellWidth;
		if (startcx < 0) {
			startcx = 0;
		}
		if (startcx > this.col) {
			startcx = this.col;
		}
		int endcx = (x + dist + cellWidth - 1) / cellWidth;
		if (endcx < 0) {
			endcx = 0;
		}
		if (endcx > this.col) {
			endcx = this.col;
		}
		int startcy = (y - dist) / cellHeight;
		if (startcy < 0) {
			startcy = 0;
		}
		if (startcy > this.row) {
			startcy = this.row;
		}
		int endcy = (y + dist + cellHeight - 1) / cellHeight;
		if (endcy < 0) {
			endcy = 0;
		}
		if (endcy > this.row) {
			endcy = this.row;
		}
		iterator.init(startcx, startcy, endcx, endcy);
		return iterator;
		// int size = (endcx - startcx) * (endcy - startcy);
		// if(size<=0){
		// //
		// log.info("[MAPCELLERROR]X["+x+"]Y["+y+"]DIST["+dist+"]Unit["+u.id+"]");
		// return new MapCell[0];
		// }
		// MapCell[] ret = new MapCell[(endcx - startcx) * (endcy - startcy)];
		// int index = 0;
		// for (int i = startcx; i < endcx; i++) {
		// for (int j = startcy; j < endcy; j++) {
		// ret[index] = cells[i][j];
		// index++;
		// }
		// }
		// return ret;
	}

	/**
	 * 取得覆盖指定位置周围指定范围的所有可能的cell（可能有冗余）
	 * 
	 * @param x
	 *            指定点
	 * @param y
	 *            指定点
	 * @param dist
	 *            距离（像素）
	 * @return
	 */
	public MapCell[] getMapCells(Unit u, int x, int y, int dist) {
		int startcx = (x - dist) / cellWidth;
		if (startcx < 0) {
			startcx = 0;
		}
		if (startcx > this.col) {
			startcx = this.col;
		}
		int endcx = (x + dist + cellWidth - 1) / cellWidth;
		if (endcx < 0) {
			endcx = 0;
		}
		if (endcx > this.col) {
			endcx = this.col;
		}
		int startcy = (y - dist) / cellHeight;
		if (startcy < 0) {
			startcy = 0;
		}
		if (startcy > this.row) {
			startcy = this.row;
		}
		int endcy = (y + dist + cellHeight - 1) / cellHeight;
		if (endcy < 0) {
			endcy = 0;
		}
		if (endcy > this.row) {
			endcy = this.row;
		}
		int size = (endcx - startcx) * (endcy - startcy);
		if (size <= 0) {
			// log.info("[MAPCELLERROR]X["+x+"]Y["+y+"]DIST["+dist+"]Unit["+u.id+"]");
			return new MapCell[0];
		}
		MapCell[] ret = new MapCell[(endcx - startcx) * (endcy - startcy)];
		int index = 0;
		for (int i = startcx; i < endcx; i++) {
			for (int j = startcy; j < endcy; j++) {
				ret[index] = cells[i][j];
				index++;
			}
		}
		return ret;
	}

	public GameMapExit getExit(int exitId) {
		return exits.get(exitId);
	}

	public void addExits() {
		for (GameMapObject obj : mapDef.mapInfo.objects) {
			if (obj instanceof GameMapExit) {
				GameMapExit mExit = (GameMapExit) obj;
				if (mExit.exitType < GameMapExit.TYPE_INTERNAL) {
					addExit(mExit);
				}
			}
		}
	}

	public void addExit(GameMapExit exit) {
		exits.put(exit.getGlobalID(), exit);
	}

	public int getHeight() {
		return mapDef.map.height;
	}

	public int getWidth() {
		return mapDef.map.width;
	}

	public int getStageId() {
		return getId() >> 4;
	}

	public World getWorld() {
		return world;
	}

	public int getId() {
		return mapDef.mapInfo.getGlobalID();
	}

	public int getInstanceId() {
		if (forceInstanceID != -1) {
			return forceInstanceID;
		}
		return instance == null ? -1 : instance.getId();
	}

	public int getFaction() {
		return mapDef.mapInfo.faction != null ? mapDef.mapInfo.faction.id : -1;
	}

	public void update(int diff) {
		List<ASMQuest> quests = ASMQuestUtil.getMapQuest(getId());
		Iterator<GameObject> itor = instanceid2objects.iterator2();
		Calendar now = Calendar.getInstance();
		while (itor.hasNext()) {
			GameObject object = itor.next();
			if (object == null) {
				break;
			}
			if (object.type == GameObject.TYPE_PLAYER) {
				Player p = (Player) object;
				if (p.systemState == Player.SYSTEMSTATE_READY) {
					if (quests != null) {
						for (ASMQuest quest : quests) {
							p.asmVm.runPreCondition(quest, now);
						}
					}
				}
			}
			if (object.type == GameObject.TYPE_GATHER) {
				GatherUnit gu = (GatherUnit)object;
				if (in(gu.template.getID())) {
					if (gu.lastRefreshTime <= System.currentTimeMillis()-300000 && gu.isGathering == 0) {
						boolean find = false;
						int count = 0;
						if (gu.isAlive()) {
							for (int i = 0; i < 5; i++) {
								int numX = findPoint()[0];
								int numY = findPoint()[1];
								if (mapDef.mapInfo.getPathFinder().canReach(numX, numY)) {
									find = true;
								} else {
									count++;
								}
								if (find) {
									gu.removeFromWorld();
									gu.x = numX;
									gu.y = numY;
									addGatherUnit(gu);
									ObjectAccessor.addGameObject(gu);
									gu.lastRefreshTime = System.currentTimeMillis();
									break;
								}
							}
						}
					}
				}
			}
			if (object.getVMap() == this)
				object.update(diff);
		}

		// 每1分钟重新计算一次分组
		if ((Time.tick % 600) == 0) {
			resetGroupRatio();
		}
		if (playerCount > 0) {
			this.lastActiveTime = Time.currTime;
		}
		// Iterator<Player> ite = id2player.values().iterator();
		// while(ite.hasNext()){
		// Player p = ite.next();
		// p.update(diff);
		// }
		// Iterator<Creature> ite1 = id2creature.values().iterator();
		// while(ite1.hasNext()){
		// Creature c = ite1.next();
		// c.update(diff);
		// }
	}

	/**
	 * 在地图上随机找一点
	 * 
	 * @return
	 */
	public int[] findPoint() {
		Random random = new Random();
		int[] rect = new int[2];
		int numX = random.nextInt(col * cellWidth);
		int numY = random.nextInt(row * cellHeight);
		rect[0] = numX;
		rect[1] = numY;
		return rect;
	}

	public boolean in(int x) {
		for (int i = 0; i < temps.length; i++) {
			if (x == temps[i]) {
				return true;
			}
		}
		return false;
	}

	/*
	 * 重新计算所有CELL里的分组层数。
	 */
	protected void resetGroupRatio() {
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				cells[i][j].resetGroupRatio();
			}
		}
	}

	public void addCreature(Creature creature) {
		addCreature(creature, creature.x, creature.y);
		// creature.map.setMap(this);
		// creature.mapCell = creature.getVMap()
		// .getMapCell(creature.x, creature.y);
		// creature.mapCell.addGameObject(creature);
		// if (creature.isVisibleAndAlive()) {
		// notifyAppear(creature); // 通知周围玩家，静态NPC通知所有人
		// creature.moveType |= GameObject.MOVE_ALL;
		// }
		// instanceid2objects.put(creature.instanceId, creature);
	}

	public void addCreature(Creature creature, int x, int y) {
		creature.map.setMap(this);
		creature.x = x;
		creature.y = y;
		creature.mapCell = creature.getVMap()
				.getMapCell(creature.x, creature.y);
		creature.mapCell.addGameObject(creature);
		if (creature.isVisibleAndAlive()) {
			notifyAppear(creature); // 通知周围玩家，静态NPC通知所有人
			creature.moveType |= GameObject.MOVE_ALL;
		}
		instanceid2objects.put(creature.instanceId, creature);
	}

	public void addGatherUnit(GatherUnit gu) {
		gu.map.setMap(this);
		gu.mapCell = gu.getVMap().getMapCell(gu.x, gu.y);
		gu.mapCell.addGameObject(gu);
		if (gu.isVisibleAndAlive()) {
			if (gu.isStatic()) {
				notifyAppear(gu);
				gu.moveType |= GameObject.MOVE_ALL;
			} else {
				MapCell.broadcastRefreshNPC(gu.mapCell.cells, gu, true);
				gu.moveType |= GameObject.MOVE_ALL;
			}
		}
		instanceid2objects.put(gu.instanceId, gu);
	}

	// public Creature getCreature(int id){
	// return id2creature.get(id);
	// }
	//	
	// public Player getPlayer(int id){
	// return id2player.get(id);
	// }

	public void addPlayer(Player player, int x, int y) {
		player.map.setMap(this);
		player.x = x;
		player.y = y;
		player.mapCell = player.getVMap().getMapCell(x, y);
		player.mapCell.addGameObject(player);
		// if(player.mapCell!=null){
		// player.mapCell.broadcastRefresh(player, true);
		// }
		// id2player.put(player.id, player);
		player.mapNumber = MAP_RND.nextInt();
		instanceid2objects.put(player.id, player);
		playerCount++;
		player.unMoving();
		if (player.systemState == Player.SYSTEMSTATE_READY) {
			// Packet pt = player.getRefreshPacket(true);
			// broadcast(pt, player);
			// sendOtherUnitRefresh(player);
		}
		world.notifyPlayerAddedToMap(this, player);
		player.refreshProperties(false);
	}

	public void playerLoadingFinished(Player player) {
		if (player.map.map == this) {
			// 刷新本场景所有的静态NPC给玩家
			for (GameObject obj : instanceid2objects.values()) {
				if (obj.isStatic() && obj.isVisibleAndAlive()) {
					player.send(obj.getRefreshPacket(true));
					player.send(obj.getMovePacket(GameObject.MOVE_ALL));
				}
			}

			// 刷新附近格子的非静态NPC和玩家给他
			player.mapCell.broadcastRefreshPlayer(player, true);

			player.moveType |= GameObject.MOVE_ALL;
			player.moveExtended |= GameObject.MOVEEXT_BUFFS;
			world.notifyPlayerLoadingFinish(this, player);
			if (instance != null) {
				instance.loadingFinished(player);
			}
		}
	}

	public boolean removeGameObject(GameObject object, boolean notify) {
		boolean ret = instanceid2objects.remove(object.instanceId) != null;
		object.mapCell.removeGameObject(object);
		if (object.type == GameObject.TYPE_PLAYER) {
			Player player = (Player) object;
			if (notify) {
				// 广播玩家消失消息
				object.mapCell.broadcastRefreshPlayer(player, false);
			}
			playerCount--;
			manager.removeFromMap((Player) object);
			VMap oldMap = player.getVMap();
			if (oldMap != null) {
				world.notifyPlayerRemoveFromMap(oldMap, player);
			}

			player.asmVm.clear();
			object.mapCell = null;
			object.map.map = null;
			player.systemState = Player.SYSTEMSTATE_LOGINED;
		} else {
			if (notify) {
				if (!(object instanceof Creature)) {
					notifyDisappear(object);
				} else {
					if (object.isAlive()) { // 如果客户端同时收到怪死了以及怪刷没了就会出错，如果怪的刷新时间设置成<0就会同时发，要排除这种情况
						notifyDisappear(object);
					}
				}
			}
			object.mapCell = null;
			object.map.map = null;

		}
		return ret;
	}

	/**
	 * 向附近的玩家广播通知一个玩家离开本场景的消息。
	 * 
	 * @param player
	 * @param exitID
	 */
	public void notifyExit(Player player, int exitID) {
		if (player.mapCell != null) {
			Packet packet = new Packet(OpCode.PLAYER_EXIT_SERVER);
			packet.putInt(player.id);
			packet.putInt(exitID);
			player.mapCell.broadcast(player, packet, false, false);
		}
	}

	/**
	 * 把一个NPC消失的消息通知给所有相关玩家。如果NPC是静态的，需要通知给本场景的所有玩家。
	 */
	public static void notifyDisappear(GameObject object) {
		if (object.map != null && object.map.map != null) {
			if (object.isStatic()) {
				// 如果是静态NPC，需要广播给所有人
				Packet pkt = object.getRefreshPacket(false);
				object.map.map.broadcast(pkt);
			} else if (object.mapCell != null) {
				object.mapCell.broadcastRefreshNPC(object, false);
			}
		}
	}

	/**
	 * 把一个NPC出现的消息通知给所有相关玩家。如果NPC是静态的，需要通知给本场景的所有玩家。
	 */
	public static void notifyAppear(GameObject object) {
		if (object.map != null && object.map.map != null) {
			if (object.isStatic()) {
				// 如果是静态NPC，需要广播给所有人
				Packet pkt = object.getRefreshPacket(true);
				object.map.map.broadcast(pkt);
			} else if (object.mapCell != null) {
				object.mapCell.broadcastRefreshNPC(object, true);
			}
		}
	}

	/**
	 * 把一个包发送给场景里所有玩家
	 * 
	 * @param pkt
	 */
	public void broadcast(Packet pkt) {
		for (GameObject obj : instanceid2objects.values()) {
			if (obj.type == GameObject.TYPE_PLAYER) {
				((Player) obj).send(pkt);
			}
		}
	}

	// protected void sendOtherUnitMove(Player player){
	// Iterator<Player> ite = id2player.values().iterator();
	// while(ite.hasNext()){
	// Player p = ite.next();
	// if(p!=player&&!p.isLoading&&p.isAlive()){
	// Packet pt = buildMovePacket(p);
	// player.session.send(pt);
	// }
	// }
	// Iterator<Creature> ite1 = id2creature.values().iterator();
	// while(ite1.hasNext()){
	// Creature c = ite1.next();
	// if (c.isAlive()) {
	// Packet pt = buildMovePacket(c);
	// player.session.send(pt);
	// }
	// }
	// }

	// public GameObject getUnit(byte type,int id){
	// if(type==GameObject.TYPE_PLAYER){
	// return getPlayer(id);
	// }
	// else if(type==GameObject.TYPE_CREATURE){
	// return getCreature(id);
	// }
	// return null;
	// }

	@SuppressWarnings("unchecked")
	public GameObject[] getUnits(GameObject u, int dist) {
		List l = new ArrayList();
		for (GameObject object : instanceid2objects.values()) {
			if (object.type == GameObject.TYPE_PLAYER) {
				Player p = (Player) object;
				if (p.type != u.type || p.id != u.id) {
					if (p.inRange(u, dist)
							&& p.systemState == Player.SYSTEMSTATE_READY
							&& p.isAlive())
						l.add(p);
				}
			} else if (object.type == GameObject.TYPE_CREATURE) {
				Creature c = (Creature) object;
				if ((c.type != u.type || c.id != u.id) && c.isVisibleAndAlive()) {
					if (c.inRange(u, dist))
						l.add(c);
				}
			}
		}
		GameObject[] ret = new GameObject[l.size()];
		l.toArray(ret);
		return ret;
	}

	public List<Player> getPlayersInRange(GameObject u, int dist) {
		List<Player> l = new ArrayList<Player>();
		for (GameObject object : instanceid2objects.values()) {
			if (object.type == GameObject.TYPE_PLAYER) {
				Player p = (Player) object;
				if (p.type != u.type || p.id != u.id) {
					if (p.inRange(u, dist)
							&& p.systemState == Player.SYSTEMSTATE_READY) {
						l.add(p);
					}
				}
			}
		}
		return l;
	}

	public void shout(String message, int color, int duration) {
		Packet pt = new Packet(OpCode.SHOUT_SERVER);
		pt.putString(message);
		pt.putInt(color);
		pt.putInt(duration);
		for (GameObject object : instanceid2objects.values()) {
			if (object.type == GameObject.TYPE_PLAYER) {
				Player p = (Player) object;
				if (p.systemState == Player.SYSTEMSTATE_READY) {
					p.send(pt);
				}
			}
		}
	}

	public void notifyMapInfo(String message, int faction) {
		Packet pt = new Packet(OpCode.MAP_INFO_SERVER);
		pt.putShort(getId());
		pt.putString(message);
		for (GameObject object : instanceid2objects.values()) {
			if (object.type == GameObject.TYPE_PLAYER
					&& object.faction == faction) {
				Player p = (Player) object;
				if (p.systemState == Player.SYSTEMSTATE_READY) {
					p.send(pt);
				}
			}
		}
	}

	public void notifyMapInfo(String message) {
		Packet pt = new Packet(OpCode.MAP_INFO_SERVER);
		pt.putShort(getId());
		pt.putString(message);
		for (GameObject object : instanceid2objects.values()) {
			if (object.type == GameObject.TYPE_PLAYER) {
				Player p = (Player) object;
				if (p.systemState == Player.SYSTEMSTATE_READY) {
					p.send(pt);
				}
			}
		}
	}

	public List<GameObject> filter(GameObjectFilter filter) {
		List<GameObject> ret = new ArrayList<GameObject>();
		for (GameObject object : instanceid2objects.values()) {
			if (filter.apply(object)) {
				ret.add(object);
			}
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	public void getEnemies(GameObject u, GameObject ref, int dist, int max,
			Set<Unit> candidates) {
		// 找出所有符合条件的对象
		List<Unit> l = new ArrayList<Unit>();
		if (u instanceof Unit) {
			Unit uu = (Unit) u;
			for (GameObject object : instanceid2objects.values()) {
				if (object.type == GameObject.TYPE_PLAYER) {
					Player p = (Player) object;
					if ((p.type != u.type || p.id != u.id)
							&& uu.canAttack(p) == 0) {
						if (p.inRange(ref, dist)
								&& p.systemState == Player.SYSTEMSTATE_READY
								&& p.isAlive() && !candidates.contains(p))
							l.add(p);
					}
				} else if (object.type == GameObject.TYPE_CREATURE) {
					Creature c = (Creature) object;
					if ((c.type != u.type || c.id != u.id)
							&& c.isVisibleAndAlive() && uu.canAttack(c) == 0) {
						if (c.inRange(ref, dist) && !candidates.contains(c))
							l.add(c);
					}
				}
			}
		}

		// 从中随机抽取max个
		if (l.size() <= max) {
			for (Unit uu : l) {
				candidates.add(uu);
			}
		} else {
			int start = rand.nextInt(l.size() - 1);
			int ls = l.size();
			for (int i = 0; i < max && i < ls; i++) {
				int index = (start + i) % ls;
				candidates.add(l.get(index));
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void getAidUnits(GameObject u, GameObject ref, int dist, int max,
			Set<Unit> candidates) {
		// 找出所有符合条件的对象
		List<Unit> l = new ArrayList<Unit>();
		if (u instanceof Unit) {
			Unit uu = (Unit) u;
			for (GameObject object : instanceid2objects.values()) {
				if (object.type == GameObject.TYPE_PLAYER) {
					Player p = (Player) object;
					if ((p.type != u.type || p.id != u.id) && uu.canAid(p)) {
						if (p.inRange(ref, dist)
								&& p.systemState == Player.SYSTEMSTATE_READY
								&& p.isAlive() && !candidates.contains(p))
							l.add(p);
					}
				} else if (object.type == GameObject.TYPE_CREATURE) {
					Creature c = (Creature) object;
					if ((c.type != u.type || c.id != u.id)
							&& c.isVisibleAndAlive() && uu.canAid(c)) {
						if (c.inRange(ref, dist) && !candidates.contains(c))
							l.add(c);
					}
				}
			}
		}

		// 从中随机抽取max个
		if (l.size() <= max) {
			for (Unit uu : l) {
				candidates.add(uu);
			}
		} else {
			int start = rand.nextInt(l.size() - 1);
			int ls = l.size();
			for (int i = 0; i < max && i < ls; i++) {
				int index = (start + i) % ls;
				candidates.add(l.get(index));
			}
		}
	}

	public Unit getNearestEnemy(Unit u, int dist) {
		// MapCell[] checkCells = getMapCells(u, u.x, u.y, dist);
		// Unit ret = null;
		// int nearestDist = (dist + 1) * (dist + 1);
		// for (MapCell cell : checkCells) {
		// if (cell != null) {
		// for (GameObject object : cell.objects.values()) {
		// if (object != u && object.isEnemy(u)) {
		// if (object.type == GameObject.TYPE_PLAYER) {
		// Player p = (Player) object;
		// if (!p.isAlive()
		// || p.systemState != Player.SYSTEMSTATE_READY) {
		// continue;
		// }
		// } else if (object.type == GameObject.TYPE_CREATURE) {
		// Creature c = (Creature) object;
		// if (!c.isVisibleAndAlive())
		// continue;
		// } else
		// continue;
		// if (!u.isEnemy(object))
		// continue;
		// int d = object.distance(u);
		// if (d < nearestDist
		// && mapDef.mapInfo.canSee(u.x, u.y, object.x,
		// object.y)) { // 视野能看见
		// if (u.type == GameObject.TYPE_CREATURE
		// && object.type == GameObject.TYPE_CREATURE) {
		// Creature c1 = (Creature) u;
		// Creature c2 = (Creature) object;
		// if ((c1.isGuard ^ c2.isGuard)) {
		// continue;
		// }
		// }
		// nearestDist = d;
		// ret = (Unit) object;
		// }
		// }
		// }
		// }
		// }
		// return ret;
		MapCellIterator ite = getMapCellsSync(u, u.x, u.y, dist);
		Unit ret = null;
		int nearestDist = (dist + 1) * (dist + 1);
		while (ite.hasNext()) {
			MapCell cell = ite.next();
			for (GameObject object : cell.objects.values()) {
				if (object != u && object.isEnemy(u)) {
					if (object.type == GameObject.TYPE_PLAYER) {
						Player p = (Player) object;
						if (!p.isAlive()
								|| p.systemState != Player.SYSTEMSTATE_READY) {
							continue;
						}
					} else if (object.type == GameObject.TYPE_CREATURE) {
						Creature c = (Creature) object;
						if (!c.isVisibleAndAlive() || !c.canBeAttacked)
							continue;
					} else
						continue;
					if (!u.isEnemy(object))
						continue;
					int d = object.distance(u);
					if (d < nearestDist
							&& mapDef.mapInfo.canSee(u.x, u.y, object.x,
									object.y)) { // 视野能看见
						if (u.type == GameObject.TYPE_CREATURE
								&& object.type == GameObject.TYPE_CREATURE) {
							Creature c1 = (Creature) u;
							Creature c2 = (Creature) object;
							if ((c1.isGuard ^ c2.isGuard)) {
								continue;
							}
						}
						nearestDist = d;
						ret = (Unit) object;
					}
				}
			}
		}
		return ret;
	}
	
	/** 获取某范围内的所有Creature */
	public List<Creature> getNearestCreatures(Unit u, int x, int y, int dist){
		MapCellIterator ite = getMapCellsSync(u, x, y, dist);
		List<Creature> list = new ArrayList<Creature>();
		int nearestDist = (dist + 1) * (dist + 1);
		while (ite.hasNext()) {
			MapCell cell = ite.next();
			for (GameObject object : cell.objects.values()) {
				if (object != u) {
					int d = object.distance(u);
					if (object.type == GameObject.TYPE_CREATURE && d<nearestDist
							&& mapDef.mapInfo.canSee(u.x, u.y, object.x,object.y)) { // 视野能看见
						list.add((Creature)object);
					}
				}
			}
		}
		return list;
	}

	/**
	 * 获得第一个Id相等的Creature，不管InstanceId
	 * 
	 * @param id
	 * @return
	 */
	public Creature getCreatureById(int id) {
		for (GameObject object : instanceid2objects.values()) {
			if (object.type == GameObject.TYPE_CREATURE && object.id == id) {
				return (Creature) object;
			}
		}
		return null;
	}
	
	public Creature getCreatureByIdAndInstanceId(int id, int instanceId){
		for (GameObject object : instanceid2objects.values()) {
			if (object.type == GameObject.TYPE_CREATURE && object.id == id && object.instanceId==instanceId) {
				return (Creature) object;
			}
		}
		return null;
	}

	/**
	 * 处理地图数据变化，尽可能地更新已有对象的属性。
	 */
	public void mapChanged() {
		refreshExits();
		refreshCreatures();
	}

	/*
	 * 刷新所有出口的信息。
	 */
	private void refreshExits() {
		exits.clear();
		addExits();
	}

	public void refreshNPC(int id, boolean visible) {
		for (GameObject o : instanceid2objects.values()) { // 现在内存里寻找，如果找到就操作，否则去projectdata里找
			if (o.id == id && o.type == GameObject.TYPE_CREATURE) {
				if (visible) {
					((Creature) o).setVisibleOnly();
					// log.info("[NPCVISIBLE]"+id);
					return;
				} else {
					((Creature) o).setInvisibleOnly();
					// log.info("[NPCINVISIBLE]"+id);
					return;
				}
			}
		}
		if (visible) {
			ProjectData proj = Server.server.getServiceRegistry()
					.getDataService().data;
			GameMapObject gmo = GameMapObject.findByID(proj, id);
			if (gmo == null || !(gmo instanceof GameMapNPC)) {
				return;
			}
			Creature npc = (Creature) VMapUtil.addCreature(this,
					(GameMapNPC) gmo, true, 0, Server.server.revision);
			// log.info("[NPCVISIBLE]"+id);
			// npc.setVisibleOnly();
			// npc.ownerRef = player.ref();
			// if (npc != null) {
			// setValue(currentQuestId, index, npc.instanceId);
			// return 1;
			// } else {
			// return 0;
			// }
		}
	}

	/*
	 * 刷新所有NPC（包括采集NPC）的信息。
	 */
	private void refreshCreatures() {
		GameMapInfo mi = mapDef.mapInfo;

		// 首先遍历已有的NPC，删除所有新地图数据中没有的NPC，并更新新数据中存在的NPC的属性
		HashSet<Integer> existNpcs = new HashSet<Integer>();
		for (GameObject gobj : instanceid2objects.values()) {
			if (gobj instanceof Creature) {
				Creature c = (Creature) gobj;
				if (!c.isDynamic()) {
					GameMapObject gmo = mi.findObject(c.id & 0xFFF);
					if (gmo == null || !(gmo instanceof GameMapNPC)) {
						// 被删除
						c.disappearTime = Time.currTime;
					} else {
						// 属性更新
						c.updateSetting((GameMapNPC) gmo);
					}
				}
				existNpcs.add(c.id);
			} else if (gobj instanceof GatherUnit) {
				GatherUnit g = (GatherUnit) gobj;
				if (!g.isDynamic()) {
					GameMapObject gmo = mi.findObject(g.id & 0xFFF);
					if (gmo == null || !(gmo instanceof GameMapNPC)) {
						// 被删除
						g.disappearTime = Time.currTime;
					} else {
						// 属性更新
						g.updateSetting((GameMapNPC) gmo);
					}
				}
				existNpcs.add(g.id);
			}
		}

		// 遍历新数据中的NPC设置，创建还没有创建的NPC
		for (GameMapObject gmo : mi.objects) {
			if (gmo instanceof GameMapNPC
					&& !existNpcs.contains(gmo.getGlobalID())) {
				VMapUtil.addCreature(this, (GameMapNPC) gmo, false, 0, Server.server.revision);
			}
		}
	}

	/** 取得地图的中心坐标 */
	protected int[] getCenter() {
		int centerX = col * cellWidth / 2;
		int centerY = row * cellHeight / 2;
		return new int[] { centerX, centerY };
	}

	/** 根据坐标确定方位 */
	public String getDirection(int x, int y) {
		int centerX = getCenter()[0];
		int centerY = getCenter()[1];
		if (Math.abs(x - centerX) <= cellWidth
				&& Math.abs(y - centerY) <= cellHeight) {
			return "中心";
		} else if (Math.abs(x - centerX) <= cellWidth
				&& y - centerY > cellHeight) {
			return "正南";
		} else if (Math.abs(x - centerX) <= cellWidth
				&& y - centerY < -cellHeight) {
			return "正北";
		} else if (Math.abs(y - centerY) <= cellHeight
				&& x - centerX > cellWidth) {
			return "正东";
		} else if (Math.abs(y - centerY) <= cellHeight
				&& x - centerX < -cellWidth) {
			return "正西";
		}
		if (x > centerX) {
			if (y > centerY) {
				return "东南";
			} else {
				return "东北";
			}
		} else {
			if (y > centerY) {
				return "西南";
			} else {
				return "西北";
			}
		}
	}
}
