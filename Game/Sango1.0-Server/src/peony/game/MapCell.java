package peony.game;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import peony.game.attendant.Attendant;
import peony.net.Packet;
import peony.util.IntHashMap;

public class MapCell {
	private static Logger log = Logger.getLogger(MapCell.class);

	protected MapCell[] cells;

	protected int startX, startY, width, height;
	protected int midX, midY;

	protected IntHashMap<GameObject> objects = new IntHashMap<GameObject>();

	protected int playerCount;
	protected int layerCount = 1;

	protected VMap map;

	public MapCell(VMap map, int startX, int startY, int width, int height) {
		this.startX = startX;
		this.startY = startY;
		this.width = width;
		this.height = height;
		this.midX = startX + width / 2;
		this.midY = startY + height / 2;
		this.map = map;
	}
	
	/**
	 * 取得本格子以及附近格子的玩家总数。
	 */
	public int getNearPlayerCount() {
		int total = 0;
		for (MapCell cell : cells) {
			total += cell.playerCount;
		}
		return total;
	}
	
	/**
	 * 重算分组数。
	 */
	public void resetGroupRatio() {
		int count = getNearPlayerCount();
		layerCount = (count / 15) + 1;
	}
	
	/**
	 * 判断在此格子中的一个玩家是否能看见另外一个玩家。
	 * @param p1
	 * @param p2
	 * @return 如果两个玩家分到同一层，或者是队友，或者在临时关系表中存在，则允许看见
	 */
	public boolean canSee(Player p1, Player p2) {
		int key = (p1.mapNumber ^ p2.mapNumber) & 0xFFFF;
		if (key < 65536 / layerCount) {
			return true;
		}
		if (p1.party != null && p1.party == p2.party) {
			return true;
		}
		if (p1.relations != null) {
			if (p1.relations.friends.exists(p2.id) || p1.relations.enemies.exists(p2.id) ||
					p1.relations.tempList.exists(p2.id)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 返回两个MapCell的差别，返回值中MapCell[0]的是被移除的MapCell，MapCell[1]的是被添加的MapCell
	 * 
	 * @param oldCells
	 * @param newCells
	 *            不能为null，否则抛出IllegalArgumentException
	 * @return
	 */
	private static MapCell[][] diff(MapCell[] oldCells, MapCell[] newCells) {
		if (newCells == null)
			throw new IllegalArgumentException();
		MapCell[][] ret = new MapCell[3][];
		if (oldCells != null) {
			if (oldCells == newCells) {
				return ret;
			}
			List<MapCell> addedCells = new ArrayList<MapCell>(9);
			List<MapCell> removedCells = new ArrayList<MapCell>(9);
			List<MapCell> remained = new ArrayList<MapCell>(9);
			begin: for (int i = 0; i < oldCells.length; i++) {
				for (int j = 0; j < newCells.length; j++) {
					if (oldCells[i] == newCells[j]) {
						remained.add(oldCells[i]);
						continue begin;
					}
				}
				removedCells.add(oldCells[i]);
			}
			begin1: for (int i = 0; i < newCells.length; i++) {
				for (int j = 0; j < oldCells.length; j++) {
					if (newCells[i] == oldCells[j])
						continue begin1;
				}
				addedCells.add(newCells[i]);
			}
			ret[0] = new MapCell[removedCells.size()];
			ret[1] = new MapCell[addedCells.size()];
			ret[2] = new MapCell[remained.size()];
			removedCells.toArray(ret[0]);
			addedCells.toArray(ret[1]);
			remained.toArray(ret[2]);
			return ret;
		} else {
			ret[1] = newCells;
			return ret;
		}
	}
	
	/**
	 * 返回两个MapCell的差别，返回值中MapCell[0]的是被移除的MapCell，MapCell[1]的是被添加的MapCell
	 * 
	 * @param oldCells
	 * @param newCells
	 *            不能为null，否则抛出IllegalArgumentException
	 * @return
	 */
	public static MapCell[][] diff(MapCell oldCell, MapCell newCell) {
		if (oldCell == null) {
			return new MapCell[][] { null, newCell.cells };
		} else {
			return diff(oldCell.cells, newCell.cells);
		}
	}

	/**
	 * 向Cell中添加一个游戏对象
	 * @param g
	 */
	public void addGameObject(GameObject g) {
		objects.put(g.getInstanceId(), g);
		if (g.type == GameObject.TYPE_PLAYER)
			playerCount++;
	}

	/**
	 * 从Cell中移除一个游戏对象
	 * @param g
	 * @return
	 */
	public boolean removeGameObject(GameObject g) {
		GameObject o = objects.remove(g.getInstanceId());
		boolean ret = o != null;
		if (o!=null && o.type == GameObject.TYPE_PLAYER)
			playerCount--;
		return ret;
	}

	/**
	 * 向此CELL以及相邻CELL的所有玩家广播一个玩家的信息。
	 * @param p 发出信息的玩家
	 * @param pt 信息包
	 * @param self 是否发送给此玩家自己
	 * @param ignoreParty 是否不发送给队友（某些包已经通过队友特送接口发送，这里不发送可以避免浪费）
	 */
	public void broadcast(Player player, Packet pt,
			boolean self, boolean ingoreParty) {
		for (int i = 0; i < cells.length; i++) {
			cells[i].broadcastThis(player, pt, self, ingoreParty);
		}
	}
	
	/*
	 * 向一个CELL里的所有玩家广播一个玩家的信息。
	 * @param player 发出信息的玩家
	 * @param pt 信息包
	 * @param self 是否发送给此玩家自己
	 * @param ignoreParty 是否不发送给队友（某些包已经通过队友特送接口发送，这里不发送可以避免浪费）
	 */
	private void broadcastThis(Player player, Packet pt, boolean self, boolean ignoreParty) {
		if (playerCount == 0) {
			return;
		}
		int count = objects.size();
		int counter = 0;
		for (GameObject object : objects.values()) {
			counter++;
			if(counter>count)
				break;
			if (object.type == GameObject.TYPE_PLAYER) {
				// 检查是否隔离阵营
				if (map.splitFaction() && object.faction != player.faction) {
					continue;
				}
				
				Player p = (Player) object;
				if (p.systemState != Player.SYSTEMSTATE_READY) {
					continue;
				}
				if (!self && player == object) {
					continue;
				}
				boolean isParty = player.party != null && player.party == p.party;
				if (ignoreParty && isParty) {
					continue;
				}
				if (player == p || canSee(p, player)) {
					p.send(pt);
				}
			}
		}
	}
	
	/**
	 * 向此CELL以及相邻CELL的所有玩家广播一个信息（可能是NPC对玩家，也可能是玩家对玩家）。
	 * @param player 信息来源玩家（null表示非玩家）
	 * @param target 信息目标玩家（null表示非玩家）
	 * @param pt 信息包
	 * @param self 是否发送给此玩家自己
	 * @param ignoreParty 是否不发送给队友（某些包已经通过队友特送接口发送，这里不发送可以避免浪费）
	 * @param isAttack 是否是攻击包，攻击包在处理流量控制时需要额外注意。
	 */
	public void broadcast(Player player, Player target, Packet pt,
			boolean self, boolean ingoreParty, boolean isAttack) {
		for (int i = 0; i < cells.length; i++) {
			cells[i].broadcastThis(player, target, pt, self, ingoreParty, isAttack);
		}
	}
	
	/*
	 * 向此CELL的所有玩家广播一个信息（可能是NPC对玩家，也可能是玩家对玩家）。
	 * @param player 信息来源玩家（null表示非玩家）
	 * @param target 信息目标玩家（null表示非玩家）
	 * @param pt 信息包
	 * @param self 是否发送给此玩家自己
	 * @param ignoreParty 是否不发送给队友（某些包已经通过队友特送接口发送，这里不发送可以避免浪费）
	 * @param isAttack 是否是攻击包，攻击包在处理流量控制时需要额外注意。
	 */
	private void broadcastThis(Player player, Player target, Packet pt, 
			boolean self, boolean ignoreParty, boolean isAttack) {
		if (playerCount == 0) {
			return;
		}
		int count = objects.size();
		int counter = 0;
		for (GameObject object : objects.values()) {
			counter++;
			if(counter>count)
				break;
			if (object.type == GameObject.TYPE_PLAYER) {
				// 检查是否隔离阵营
				if (map.splitFaction() && player != null && object.faction != player.faction) {
					continue;
				}
				
				Player p = (Player) object;
				if (p.systemState != Player.SYSTEMSTATE_READY) {
					continue;
				}
				if (player == null) {
					// 此包来自非玩家对象
					if (isAttack) {
						// 如果是攻击包，需要考虑目标用户设置的流量选项
						if (p.getShowOption()==0) {
							// 目标用户选择高流量模式，那么任何周围目标的战斗信息都发给他
							if(Server.isStepServer && pt.getOpCode()==OpCode.UNIT_MOVE_SERVER && player!=null)
								pt = player.getStepBattleMovePacket(p, player.moveType);
							p.send(pt);
						} else {
							// 目标用户选择不查看别人的战斗信息，那么只有攻击的目标是他才发送
							if (target == p) {
								if(Server.isStepServer && pt.getOpCode()==OpCode.UNIT_MOVE_SERVER && player!=null)
									pt = player.getStepBattleMovePacket(p, player.moveType);
								p.send(pt);
							}
						}
					} else {
						// 非攻击包，全部广播
						if(Server.isStepServer && pt.getOpCode()==OpCode.UNIT_MOVE_SERVER && player!=null)
							pt = player.getStepBattleMovePacket(p, player.moveType);
						p.send(pt);
					}
				} else {
					// 此包来自玩家，需要考虑分组
					if (!self) {
						// 检查是否需要发给自己
						if (player == object)
							continue;
					}
					boolean isParty = player.party != null && player.party == p.party;
					boolean isPk = (player.pkInfo!=null ? true : false);
					if (isAttack) {
						// 如果是攻击包，需要考虑目标用户设置的流量选项
						if (p.getShowOption() == 0) {
							// 目标用户选择高流量模式，那么任何周围目标的战斗信息都发给他
							if (target == null || canSee(p, player) || canSee(p, target)) {
								if(Server.isStepServer && pt.getOpCode()==OpCode.UNIT_MOVE_SERVER && player!=null)
									pt = player.getStepBattleMovePacket(p, player.moveType);
								p.send(pt);
							}
						} else {
							// 目标用户选择不查看别人的战斗信息，那么只有自己或队友的信息才发送
							if (p == target || p == player || isParty || player.battleType==Player.TYPE_ASYNC_PLAYER){
								if(Server.isStepServer && pt.getOpCode()==OpCode.UNIT_MOVE_SERVER && player!=null)
									pt = player.getStepBattleMovePacket(p, player.moveType);
								p.send(pt);
							}
						}
					} else {
						// 非攻击包，全部广播
						if (ignoreParty && isParty) {
							continue;
						}
						if(isPk && player.pkInfo==p.pkInfo){
							if(pt.getOpCode()!=OpCode.SHOW_HPMP_SERVER){
								p.send(pt);
							}
						}
						if(target!=null && target.attack!=null && target.attack.targetRef!=null && target.attack.targetRef.instanceId==p.id){
							if(pt.getOpCode()!=OpCode.SHOW_HPMP_SERVER){
								p.send(pt);
							}
						}
						if (player == p) {
							if(Server.isStepServer && pt.getOpCode()==OpCode.UNIT_MOVE_SERVER && player!=null)
								pt = player.getStepBattleMovePacket(p, player.moveType);
							p.send(pt);
						} else if (p.getShowOption() != 2 && canSee(p, player)) {
							if(Server.isStepServer && pt.getOpCode()==OpCode.UNIT_MOVE_SERVER && player!=null)
								pt = player.getStepBattleMovePacket(p, player.moveType);
							p.send(pt);
						}
					}
				}
			}
		}
	}

	/**
	 * 向一组CELL广播一个NPC刷新/刷没的消息。
	 * @param cells 相关CELL
	 * @param g 目标对象
	 * @param visible true表示刷出,false表示刷没
	 */
	public static void broadcastRefreshNPC(MapCell[] cells, GameObject g, boolean visible) {
		for (int i = 0; i < cells.length; i++) {
			cells[i].broadcastRefreshNPCImpl(g, visible);
		}
	}
	
	/**
	 * 向本CELL以及所有相邻CELL的玩家广播一个NPC刷出/刷没的消息.
	 * @param g
	 * @param visible
	 */
	public void broadcastRefreshNPC(GameObject g, boolean visible) {
		broadcastRefreshNPC(cells, g, visible);
	}
	
	/*
	 * 向本CELL广播一个NPC刷新/刷没的消息。
	 * @param g 目标对象
	 * @param visible true表示刷出,false表示刷没
	 */
	private void broadcastRefreshNPCImpl(GameObject g, boolean visible) {
		Packet pt = g.getRefreshPacket(visible);
		Packet pt1 = null;
		if (visible) {
			pt1 = g.getMovePacket(GameObject.MOVE_ALL);
		}
		int count = objects.size();
		int counter = 0;
		for (GameObject o : objects.values()) {
			counter++;
			if(counter>count)
				break;
			if (o.type == GameObject.TYPE_PLAYER) {
				Player p = (Player)o;
				if (p.systemState == Player.SYSTEMSTATE_READY) {
					p.send(pt);
					if (visible) {
						p.send(pt1);
					}
				}
			}
		}
	}

	/**
	 * 向一组CELL广播一个玩家刷新/刷没的消息。
	 * @param cells 相关CELL
	 * @param player 状态变化的玩家
	 * @param visible true表示刷出,false表示刷没
	 */
	public static void broadcastRefreshPlayer(MapCell[] cells, Player player, boolean visible) {
		for (int i = 0; i < cells.length; i++) {
			cells[i].broadcastRefreshPlayerImpl(player, visible);
		}
	}
	
	/**
	 * 向本CELL以及相邻CELL广播一个玩家刷新/刷没的消息。
	 * @param player 状态变化的玩家
	 * @param visible true表示刷出,false表示刷没
	 */
	public void broadcastRefreshPlayer(Player player, boolean visible) {
		broadcastRefreshPlayer(cells, player, visible);
	}
	
	/**
	 * 向本CELL中所有玩家广播一个玩家刷出/刷没的消息，同时向此玩家发送此CELL中所有其他对象刷出/刷没的消息。
	 * @param player 状态变化的玩家
	 * @param visible true表示刷出,false表示刷没
	 */
	private void broadcastRefreshPlayerImpl(Player player, boolean visible) {
		Packet pt = player.getRefreshPacket(visible);
		Packet pt1 = null;
		if (visible) {
			pt1 = player.getMovePacket(GameObject.MOVE_ALL);
		}
		List<GameObject> l1 = new ArrayList<GameObject>();//进入此玩家视野的对象
		List<GameObject> l2 = new ArrayList<GameObject>();//离开此玩家视野的对象
		int count = objects.size();
		int counter = 0;
		for (GameObject o : objects.values()) {
			counter++;
			if(counter>count)
				break;
			if (o.type == GameObject.TYPE_PLAYER) {
				// 检查是否隔离阵营
				if (map.splitFaction() && o.faction != player.faction) {
					continue;
				}
				if (o == player) {
					continue;
				}
				Player p = (Player) o;
				if (p.systemState != Player.SYSTEMSTATE_READY) {
					continue;
				}
				if (canSee(player, p) && player.getShowOption() != 2 || player.battleType==Player.TYPE_ASYNC_PLAYER || p.battleType==Player.TYPE_ASYNC_PLAYER) {
					// 如果状态变化的玩家A可以看到另外一个玩家B，那么如果A离开CELL，则需要向A通知B消失；
					// 反之如果A进入CELL，则需要向A通知B出现
					if (visible) {
						l1.add(p);
					} else {
						l2.add(p);
					}
				}
				if (canSee(p, player) && p.getShowOption() != 2 || player.battleType==Player.TYPE_ASYNC_PLAYER || p.battleType==Player.TYPE_ASYNC_PLAYER) {
					// 如果B能看到A，那么如果A离开CELL，需要向B通知A消失；反之如果A进入CELL，需要向B通知A出现
					p.send(pt);
					if (visible) {
						p.send(pt1);
					}
				}
			} else {
				if(o instanceof Attendant){
					if(player.getShowOption()!=0 && (player.attendant==null || player.attendant.getInstanceId()!=o.getInstanceId())){
						l2.add(o);
						continue;
					}
				}
				// 如果目标是NPC，且不是静态的，则需要刷出或刷没
				if (!o.isStatic() && o.isVisibleAndAlive()) {
					if (visible) {
						l1.add(o);
					} else {
						l2.add(o);
					}
				}
			}
		}
		sendRefresh(player,l1,true);
		sendRefresh(player,l2,false);
	}
	
	/*
	 * 向玩家发送一批对象刷出/刷没的消息。
	 * @param p 目标玩家
	 * @param l 刷出/刷没的对象
	 * @param visible true表示刷出，false表示刷没
	 */
	private void sendRefresh(Player p, List<GameObject> l, boolean visible) {
		if (l.size() > 0) {
			if (l.size() == 1) {
				p.send(l.get(0).getRefreshPacket(visible));
				if (visible) {
					p.send(l.get(0).getMovePacket(GameObject.MOVE_ALL));
				}
			} else {
				Packet pt = new Packet(OpCode.UNIT_MULTI_REFRESH_SERVER);
				pt.put(l.size());
				for (GameObject o:l) {
					o.getRefreshPacket(pt, visible);
				}
				p.send(pt);
				if (visible) {
					for (GameObject o : l) {
						p.send(o.getMovePacket(GameObject.MOVE_ALL));
					}
				}
			}
		}
	}
}
