package peony.game;

import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;

import peony.game.pk.PkInfo;
import peony.net.Packet;

@MappedSuperclass
@AccessType("field")
public abstract class GameObject {
	
	protected static final AtomicInteger ids = new AtomicInteger(100000000);

	
	
	public static final short STATE_IDLE = 0;
	public static final short STATE_RUN = 1;
	public static final short STATE_ATTACK = 1<<1;
	public static final short STATE_RIDE = 1<<2;
	public static final short STATE_DIE = 1<<3;
	public static final short STATE_PARTY = 1<<4;
	public static final short STATE_PARTY_LEADER = 1<<5;
	public static final short STATE_FEAR = 1<<6; //恐惧
	public static final short STATE_PARALYZE = 1<<7; //麻痹
	public static final short STATE_STAY = 1<<8; //定身
	public static final short STATE_PVP = 1<<9; //同阵营PVP状态
	public static final short STATE_PVP_FACTION = 1<<10;
	public static final short STATE_DUMB = 1<<11; 
	public static final short STATE_FLAG = 1<<13;
	public static final short STATE_KING = 1<<14;
	public static final short STATE_INVISIBLE = (short)(1<<15);
	
	public static final short MASK_CLEAR = STATE_PARTY|STATE_PARTY_LEADER|STATE_PVP|STATE_PVP_FACTION|STATE_FLAG|STATE_RIDE;
	
	public static final short MASK_STATE_CHAOS = STATE_FEAR|STATE_PARALYZE;
    public static final short MASK_STATE_STOP = STATE_FEAR|STATE_PARALYZE|STATE_STAY;
	
	public static final short STATE_STOP = STATE_FEAR|STATE_PARALYZE|STATE_STAY;
	public static final int FACTION_ENV = 0;
	public static final int FACTION_WEI = 1;
	public static final int FACTION_SHU = 2;
	public static final int FACTION_WU = 3;
	public static final int FACTION_CREATURE = 4;
	public static final int FACTION_NEUTRAL = 5;
	public static final int FACTION_GATHER = 6;
	
	public static final String[] FACTION_NAME = {"環境","魏國","蜀國","吳國","怪物","中立","采集"};
	
	@Transient
	public byte type;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	public int id;
	@Transient
	public short state;
	@Column(name = "name", nullable = false)
	public String name;
	@Column(name = "x", nullable = false)
	public int x;
	@Column(name = "y", nullable = false)
	public int y;
	@Column(name = "level", nullable = false)
	public int level;
	@Column(name = "faction", nullable = false)
	public int faction;
	@Embedded
	public VMapReference map;
	public static final byte TYPE_PLAYER = 1;
	public static final byte TYPE_NPC = 2;
	public static final byte TYPE_CREATURE = 3;
	public static final byte TYPE_CORPSE = 4;
	public static final byte TYPE_GATHER = 5;

//	 * x							short (第一段)
//	 * y							short (第一段) 
//	 * 角度							byte（角度/2）(第二段)
//	 * 时间							int(从系统启动开始计算的毫秒速) (第二段)
//	 * 速度							byte(每秒的像素） (第二段)
//	 * hp百分比						byte(200为单位)	(第三段)
//	 * mp百分比						byte(200为单位) (第三段)
//	 * state						short (第四段)
//	 * name							string (第五段)
//	 * level						byte (第五段)
//	 * faction						byte (第五段)
	public static final short MOVE_POINT = 1<<7;
	public static final short MOVE_ANGLE = 1<<6;
	public static final short MOVE_HPMP = 1<<5;
	public static final short MOVE_STATE = 1<<4;
	public static final short MOVE_DETAIL = 1<<3;
	
	public static final short MOVE_NAME = 1<<8;
	public static final short MOVE_LEVEL = 1<<9;
	public static final short MOVE_FACTION = 1<<10;
	public static final short MOVE_EQUIPMENT = 1<<11;
	public static final short MOVE_SEX = 1<<12;
	public static final short MOVE_OWNER = 1<<13;
	public static final short MOVE_CLAZZ = 1<<14;
	public static final short MOVE_HORSE = (short)(1<<15);
	
	public static final short MOVE_NORUNNING_INIT = MOVE_POINT|MOVE_HPMP|MOVE_STATE|MOVE_DETAIL|MOVE_NAME|MOVE_LEVEL|MOVE_FACTION|MOVE_EQUIPMENT|MOVE_SEX|MOVE_OWNER|MOVE_CLAZZ|MOVE_HORSE;
	public static final short MOVE_RUNNING = MOVE_POINT|MOVE_ANGLE;
	public static final short MOVE_RUNNING_STATE = MOVE_RUNNING|MOVE_STATE;
	public static final short MOVE_POINT_STATE = MOVE_POINT|MOVE_STATE;
	public static final short MOVE_ALL = MOVE_POINT|MOVE_ANGLE|MOVE_HPMP|MOVE_STATE|MOVE_DETAIL|MOVE_NAME|MOVE_LEVEL|MOVE_FACTION|MOVE_EQUIPMENT|MOVE_SEX|MOVE_OWNER|MOVE_CLAZZ|MOVE_HORSE;
	public static final short MOVE_MOVE = MOVE_POINT|MOVE_ANGLE;
	
	public static final short MOVEEXT_GUILD = 1;
	public static final short MOVEEXT_CREDIT = 1<<1;
	public static final short MOVEEXT_BUFFS = 1<<2;
	public static final short MOVEEXT_TITLE = 1<<3;
	
	@Transient
	public short moveExtended;
	
	@Transient
	public int instanceId; //所有的Player的此属性都是自身的id，每个怪物有自己的InstanceId，即使怪物的Id是相同的
	
	@Transient
	GameObjectRef lastRef;
	
	@Transient
	public short moveType; //用来发送position信息的Mask，在需要发送position信息的时候只是按位或这个变量，然后在一个cycle最后统一发送。一个cycle最多只发送一次position信息

//	@Transient
//	public Move move;  //每个cycle只处理一次位置变化信息，所有新的position信息只设置这个值，在cycle最后查看新值是否跟旧值相同，如果不同，那么进行相关处理（unit_refresh,move.....)

	@Transient
	public PkInfo pkInfo;
	
	@Transient
	public MapCell mapCell;
	
	@Transient
	public int instanceTime; //创建时间，同一个Id的怪物在换了InstanceId以后都应该重新设置这个时间
	
	
	/**
	 * 把一个和本对象有关的信息包广播给周围玩家。
	 * @param pt 信息包
	 * @param p 生成此信息包的源玩家，如果此信息不是来自玩家，此参数传null
	 * @param target 此信息包作用的目标玩家，如果此目标不是玩家，此参数传null
	 * @param self 是否发送给玩家自己
	 * @param ingoreParty 是否不发送给队友（队友广播通过特殊接口发送）
	 * @param isAttack 是否是攻击包
	 */
	public void broadcast(Packet pt, Player p, Player target, boolean self, boolean ingoreParty, boolean isAttack){
		if (mapCell != null) {
		    if (isStatic()) {
		    	// 如果是静态NPC的信息，向所有玩家广播
		        if (map != null && map.map != null) {
		            map.map.broadcast(pt);
		        }
		    } else {
		    	// 否则向本CELL以及相邻CELL的玩家广播
		        mapCell.broadcast(p, target, pt, self, ingoreParty, isAttack);
		    }
		}
	}
	
	/**
	 * 取得显示名称。
	 * @return
	 */
	public String getShowName() {
		int split = name.indexOf('|');
		if (split > 0) {
			return name.substring(0, split);
		}
		return name;
	}
	
	public String getFactionName(){
		return FACTION_NAME[faction];
	}
	
	public static String getFactionName(int faction){
		return FACTION_NAME[faction];
	}
	
	public GameObject(byte type) {
		this.type = type;
		instanceTime = Time.currTime;
//		this.move = new Move();
	}
	
	public boolean isAlive(){
		return (state&STATE_DIE) == 0;
	}
	
	public boolean hasFlag(){
		return (state&STATE_FLAG) != 0;
	}
	
	public boolean isVisible(){
		return (state&STATE_INVISIBLE) == 0;
	}
	
	public void setInvisible(){
		state |= STATE_INVISIBLE;
	}
	
	public void setKing(){
		state |= STATE_KING;
	}
	
	public void unKing(){
		state &= (~STATE_KING);
	}
	
	public void setVisible(){
		state &= (~STATE_INVISIBLE);
	}
	
	public boolean isVisibleAndAlive(){
		return isAlive()&isVisible();
	}
	
	public boolean isStatic() {
	    return false;
	}
	
	public boolean isRide(){
		return (state&STATE_RIDE) !=0;
	}
	
	public void move(int x, int y) {
		if (this.x != x || this.y != y) {
			VMap v = map.map;
			if (v != null) {
				int oldX = this.x;
				int oldY = this.y;
				this.x = x;
				this.y = y;
				if (type != GameObject.TYPE_PLAYER) {
					// 非玩家角色且非静态NPC，当移动时可能移动到新的格子，这时原来部分能看到此NPC的格子将不再
					// 能看到此NPC，需要想所有这些格子里的玩家发送刷没的消息；同时对于新加入视野的新格子里的玩
					// 家，需要发送刷新消息。
					MapCell cell = v.getMapCell(x, y);
					if (cell != null && cell != mapCell) {
						MapCell oldCell = mapCell;
						if (oldCell != null) {
							oldCell.removeGameObject(this);
						}
						mapCell = cell;
						mapCell.addGameObject(this);
						
						// 非静态NPC需要根据CELL的变化来通知refresh
						if (!isStatic()) {
    						MapCell[][] diff = MapCell.diff(oldCell, cell);
    						MapCell[] removed = diff[0];
    						if (removed != null) {
    							MapCell.broadcastRefreshNPC(removed, this, false);
    						}
    						MapCell[] added = diff[1];
    						if (added != null) {
    							MapCell.broadcastRefreshNPC(added, this, true);
    						}
						}
					}
				} else {
					MapCell cell = v.getMapCell(x, y);
					if (cell != null) {
						if (cell != mapCell) {
							// 如果玩家角色移动到新的CELL里了，需要处理新进入视野的CELL和离开视野的CELL
							MapCell oldCell = mapCell;
							if (oldCell != null) {
								oldCell.removeGameObject(this);
							}
							mapCell = cell;
							mapCell.addGameObject(this);
							
							// 计算视野CELL变化
							MapCell[][] diff = MapCell.diff(oldCell, mapCell);
							
							// 从走出视野的CELL刷没
							MapCell[] removed = diff[0];
							if (removed != null) {
								MapCell.broadcastRefreshPlayer(removed, (Player)this, false);
							}

							// 在走入视野的CELL刷出
							MapCell[] added = diff[1];
							if (added != null) {
								MapCell.broadcastRefreshPlayer(added, (Player)this, true);
							}
						} else {
							// 新算法中，格子不改变时不会刷出/刷没
						}
					}
				}
			}
		}
	}

	public abstract void update(int diffTime);

	public void addToMap(VMap map,int x,int y) {
		this.map.setMap(map);
		this.x = x;
		this.y = y;
	}

	public GameObjectRef ref() {
		if (lastRef == null) {
			lastRef = new GameObjectRef(type,id,instanceId);
		}
		return lastRef;
	}

	public boolean inRange(GameObject t,int dist){
		if(getVMap()!=t.getVMap())
			return false;
		int xdist = Math.abs(x - t.x);
		int ydist = Math.abs(y - t.y);
		return xdist*xdist+ydist*ydist <= dist*dist;	
	}
	
	public int distance(int x,int y){
		int xdist = Math.abs(this.x - x);
		int ydist = Math.abs(this.y - y);
		return ydist*ydist+xdist*xdist;
	}
	
	public int distance(GameObject u){
		int xdist = Math.abs(x - u.x);
		int ydist = Math.abs(y - u.y);
		return ydist*ydist+xdist*xdist;
	}
	
	public VMap getVMap(){
		return map.map;
	}
	
	/**
	 * 判断unit是不是自己的敌人。
	 * @param unit
	 * @return
	 */
	public boolean isEnemy(GameObject unit) {
		if(this instanceof Unit && unit instanceof Unit){
			Unit self = (Unit)this;
			Unit other = (Unit)unit;
			if(self.minorFaction!=0)
				if(self.minorFaction!=other.minorFaction)
					return true;
				else
					return false;
		}
		if (faction != unit.faction) {
			if (faction == FACTION_NEUTRAL || faction == FACTION_ENV
					|| unit.faction == FACTION_NEUTRAL
					|| unit.faction == FACTION_ENV || unit.faction == FACTION_GATHER)
				return false;
			return true;
		} else {
			return false;
		}
	}
	
	public Packet getMovePacket(short moveType){
		throw new UnsupportedOperationException();
	}
	

	public Packet getRefreshPacket(boolean visible){
		Packet pt = new Packet(OpCode.UNIT_REFRESH_SERVER);
		byte b = type;
		if(!visible){
			b |= 1<<7;
		}
		pt.put(b);
		pt.putInt(id);
		pt.putInt(instanceId);
		return pt;
	}
	
	public void getRefreshPacket(Packet pt,boolean visible){
		byte b = type;
		if(!visible){
			b |= 1<<7;
		}
		pt.put(b);
		pt.putInt(id);
		pt.putInt(instanceId);
	}
	
	public Packet getInfoPacket(){
		throw new UnsupportedOperationException();
	}
	
	/**
	 * 在每个Cycle完之后统一发送move信息，排除掉p
	 * @param p
	 */
	public void processMove(Player p) {
		if (moveType != 0) {
			Packet pt = getMovePacket(moveType);
			broadcast(pt,null,p,false,true,false);
			moveType = 0;
		}
	}
	
	public void removeFromWorld(){
		VMap map = getVMap();
		if(map!=null){
			map.removeGameObject(this, true);
		}
		ObjectAccessor.removeGameObject(this);
	}

}

class Move{
	public boolean moved = false;
	public int x;
	public int y;
	public int mapId;
}