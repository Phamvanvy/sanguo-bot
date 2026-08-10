package peony.service.expansionbattle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import peony.game.GameObject;
import peony.game.Instance;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapUtil;
import peony.game.buff.BuffUtil;
import peony.game.chat.ChatService;
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

/**
 * 战役副本
 * @author dchen
 */
public class ExpansionInstance implements Instance, ServiceEventListener {

	protected final Logger log = Logger.getLogger(ExpansionInstance.class);
	public static Random random = new Random();
	public ExpansionService manager; // 副本管理器
	protected static final AtomicInteger IDS = new AtomicInteger(0);
	protected int id; 
	protected VMap map; 
	protected int state = 2; // 战役状态
	public static int STATE_START = 1; // 战役开始
	public static int STATE_END = 2; // 战役结束
	public static int STATE_WIN = 3; // 战役胜利
	public ExpansionNation wei; // 魏国
	public ExpansionNation shu; // 蜀国
	public ExpansionNation wu; // 吴国
	public ExpansionNation winner; // 获胜国
	public ExpansionConfig config; // 战役配置信息
	public long instanceOpenTime;
	public List<ExpansionNpc> npcs = new ArrayList<ExpansionNpc>(); // 副本中的公共NPC
	public long LAST_REFRESH_ROEMAN_TIME;
	public long LAST_REFRESH_GUARD_TIME;
	public long LAST_REFRESH_YUJI_TIME;
	public int REFRESH_LVBU;
	public int DOOR;
	public Timer bagTimer; 
	
	public ExpansionInstance(ExpansionService manager, VMap map, ExpansionNation wei, 
			ExpansionNation shu, ExpansionNation wu, ExpansionConfig config){
		this.id = IDS.incrementAndGet();
		this.manager = manager;
		this.map = map;
		this.wei = wei;
		this.shu = shu;
		this.wu = wu;
		wei.instance = this;
		shu.instance = this;
		wu.instance = this;
		this.config = config;
		map.instance = this;
		addCreature(ExpansionNpcTemplate.NPC_NOTBATTLE);
		Server.server.getEventManager().registerListener(this);
	}
	
	/** 开启战役 */
	public void openInstance(){
		if(state==STATE_END){
			state = STATE_START;
			instanceOpenTime = System.currentTimeMillis();
			// 副本一开将所有副本内的玩家加一个无敌BUFF
			for(Player p : wei.players)
				p.buffs.addBuff(BuffUtil.createBuff(ExpansionConfig.BUFF_WUDI, 1, p, p, 0));
			for(Player p : shu.players)
				p.buffs.addBuff(BuffUtil.createBuff(ExpansionConfig.BUFF_WUDI, 1, p, p, 0));
			for(Player p : wu.players)
				p.buffs.addBuff(BuffUtil.createBuff(ExpansionConfig.BUFF_WUDI, 1, p, p, 0));
			initNpcs();
			bagTimer = new Timer();
			bagTimer.schedule(new TimerTask(){
				public void run() {
					Server.server.syncRunner.add(new Runnable(){
						public void run() {
							try {
								wei.checkBag();
								shu.checkBag();
								wu.checkBag();
							} catch (Exception e) {
								
							}
						}
					});
				}
			}, 0, 2000L);
			map.shout("董卓已经发起了进攻！让我们奋起反击吧！", 0xff5555, 20000);
			LogUtil.logExpansionBattleStart();
		}
	}
	
	/** 初始化NPC */
	protected void initNpcs(){
		wei.initNpcs();
		shu.initNpcs();
		wu.initNpcs();
		removeNpc(ExpansionNpcTemplate.NPC_NOTBATTLE);
	}
	
	/** 战役结束 */
	public void closeInstance(){
		state = STATE_END;
		instanceOpenTime = 0;
		LAST_REFRESH_YUJI_TIME = 0;
		LAST_REFRESH_ROEMAN_TIME = 0;
		LAST_REFRESH_GUARD_TIME = 0;
		REFRESH_LVBU = 0;
		DOOR = 0;
		wei.resetStat();
		shu.resetStat();
		wu.resetStat();
		winner = null;
		removeNpcs();
		bagTimer.cancel();
		new Timer().schedule(new TimerTask(){
			public void run() {
				Server.server.syncRunner.add(new Runnable(){
					public void run() {
						try {
							wei.checkBag();
							shu.checkBag();
							wu.checkBag();
						} catch (Exception e) {
							
						}
					}
				});
			}
		}, 2000);
	}
	
	public void addPlayer(Player player) throws VMapException {
		if(player!=null){
			int faction = player.faction;
			if(faction==1){
				wei.addPlayer(player);
			}else if(faction==2){
				shu.addPlayer(player);
			}else if(faction==3){
				wu.addPlayer(player);
			}
		}
	}
	
	/**
	 * 副本中添加NPC
	 * @param type NPC类型
	 */
	public void addCreature(int type){
		ExpansionConfig config = manager.config;
		List<ExpansionNpcTemplate> templates = config.getNpcTemplate(type);
		for(ExpansionNpcTemplate template : templates){
			if(template!=null){
				ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
				GameMapObject gmo = GameMapObject.findByID(proj, template.instanceId);
				GameObject npc = VMapUtil.addCreature(map, template.x, template.y, 
						(GameMapNPC) gmo, true, 0, null);
				ExpansionNpc expansionNpc = new ExpansionNpc(type, npc);
				npcs.add(expansionNpc);
				LogUtil.logExpansionBattleNpcRefresh(expansionNpc.type, 1, -1);
			}
		}
	}
	
	/**
	 * 当前地图中某类型NPC的数量
	 * @param type 类型
	 * @param isAlive 是否要求活着的
	 * @return
	 */
	public int getNpcCount(int type, boolean isAlive){
		int count = 0;
		for(ExpansionNpc npc : npcs){
			if(isAlive){
				if(npc.type==type && npc.npc.isAlive())
					count++;
			}else{
				if(npc.type==type)
					count++;
			}
		}
		return count;
	}
	
	public ExpansionNpc getExpansionNpc(int type){
		for(ExpansionNpc npc : npcs){
			if(npc.type==type)
				return npc;
		}
		return null;
	}
	
	/**
	 * 自动刷兵(过期)
	 * @param level 
	 * @param npcs
	 * @param dis1
	 * @param dis2
	 */
	protected void autoRefreshNpc(final int level,final int[] npcs, final int dis1, final int dis2){
		final Timer timer = new Timer();
		timer.schedule(new TimerTask(){
			public void run() {
				addCreature(npcs[level]);
			}
		}, 0, dis1);
		timer.schedule(new TimerTask(){
			public void run() {
				timer.cancel();
				if(level<npcs.length-1)
					autoRefreshNpc(level+1, npcs, dis1, dis2);
			}
		}, dis2);
	}
	
	/** 每隔一定时间刷新NPC */
	protected void autoRefreshNpc(final int type, long dis){
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				addCreature(type);
			}
		}, 0, dis, TimeUnit.MILLISECONDS);
	}

	public int getId() {
		return id;
	}

	public VMap getMap(int mapId) {
		if(map!=null && map.getId()==mapId){
			return map;
		}
		return null;
	}

	public String getName() {
		return map.mapDef.mapInfo.name;
	}

	public void loadingFinished(Player player) {
		Packet pt = new Packet(OpCode.MAP_INFO_SERVER);
		pt.putShort(map.getId());
		pt.putString("");
		player.send(pt);
	}

	public void removePlayer(Player player) {
		int faction = player.faction;
		if(faction==1){
			wei.removePlayer(player);
		}else if(faction==2){
			shu.removePlayer(player);
		}else if(faction==3){
			wu.removePlayer(player);
		}
	}

	public void update(int diff) {
		if(state==STATE_START){
			if(instanceOpenTime>0 && !isSafeTime()){
				for(Player p : wei.players){
					p.buffs.removeBuff(ExpansionConfig.BUFF_WUDI);
				}
				for(Player p : shu.players){
					p.buffs.removeBuff(ExpansionConfig.BUFF_WUDI);
				}
				for(Player p : wu.players){
					p.buffs.removeBuff(ExpansionConfig.BUFF_WUDI);
				}
				instanceOpenTime = 0; //bug
			}
			// 刷新于吉
			if(System.currentTimeMillis()-LAST_REFRESH_YUJI_TIME>=5*60*1000){
				refreshYUJI();
				LAST_REFRESH_YUJI_TIME = System.currentTimeMillis();
			}
			// 刷新董卓兵
			if(System.currentTimeMillis()-LAST_REFRESH_ROEMAN_TIME>120000){
//				if(wei.getNpcCount(ExpansionNpcTemplate.NPC_DOOR, true)==1 && 
//						shu.getNpcCount(ExpansionNpcTemplate.NPC_DOOR, true)==1 && 
//						wu.getNpcCount(ExpansionNpcTemplate.NPC_DOOR, true)==1){
					if(getTotalGuardAndForman()<30){
						if(getTotalRorman(wei)<13)
							wei.refreshFoeman();
						if(getTotalRorman(shu)<13)
							shu.refreshFoeman();
						if(getTotalRorman(wu)<13)
							wu.refreshFoeman();
						LAST_REFRESH_ROEMAN_TIME = System.currentTimeMillis();
					}
//				}
			}
			// 刷新讨逆兵
			if(System.currentTimeMillis()-LAST_REFRESH_GUARD_TIME>120000){
				if(getTotalGuardAndForman()<30){
					if(wei.getNpcCount(ExpansionNpcTemplate.NPC_FLAG, true)==1)
						wei.addCreature(ExpansionNpcTemplate.NPC_GUARD, 3);
					if(shu.getNpcCount(ExpansionNpcTemplate.NPC_FLAG, true)==1)
						shu.addCreature(ExpansionNpcTemplate.NPC_GUARD, 3);
					if(wu.getNpcCount(ExpansionNpcTemplate.NPC_FLAG, true)==1)
						wu.addCreature(ExpansionNpcTemplate.NPC_GUARD, 3);
					LAST_REFRESH_GUARD_TIME = System.currentTimeMillis();
				}
			}
			// 刷吕布
			if(REFRESH_LVBU==0){
				if(wei.tower3IsDie() && wei.getNpcCount(ExpansionNpcTemplate.NPC_LVBU, false)==0){
					wei.addCreature(ExpansionNpcTemplate.NPC_LVBU, 1);
					REFRESH_LVBU = 1;
				}else if(shu.tower3IsDie() && shu.getNpcCount(ExpansionNpcTemplate.NPC_LVBU, false)==0){
					shu.addCreature(ExpansionNpcTemplate.NPC_LVBU, 1);
					REFRESH_LVBU = 1;
				}else if(wu.tower3IsDie() && wu.getNpcCount(ExpansionNpcTemplate.NPC_LVBU, false)==0){
					wu.addCreature(ExpansionNpcTemplate.NPC_LVBU, 1);
					REFRESH_LVBU = 1;
				}	
			}
			if(wei!=null && shu!=null && wu!=null){
				wei.update(diff);
				shu.update(diff);
				wu.update(diff);
				listenWinnerAndProcess();
			}
			if(wei.getNpcCount(ExpansionNpcTemplate.NPC_FLAG, true)==0 && 
					shu.getNpcCount(ExpansionNpcTemplate.NPC_FLAG, true)==0 && 
					wu.getNpcCount(ExpansionNpcTemplate.NPC_FLAG, true)==0){
				closeInstance();
				ChatService chatService = Server.server.getServiceRegistry().getChatService();
				chatService.sendWorldMessage("不幸的消息,由于董卓军太过残暴，我们没有将其打败，只能期待下一场战斗了。");
				LogUtil.logExpansionBattleEnd(-1, "RUNOFF");
			}
		}else if(state==STATE_END){
			
		}else if(state==STATE_WIN){
			if(DOOR==0){
				removeNpcs();
				DOOR = 1;
			}
		}
	}
	
	protected int getTotalGuardAndForman(){
		return getNpcCount(ExpansionNpcTemplate.NPC_FOEMAN1, true) + 
			getNpcCount(ExpansionNpcTemplate.NPC_FOEMAN2, true) + 
			getNpcCount(ExpansionNpcTemplate.NPC_FOEMAN3, true) + 
			getNpcCount(ExpansionNpcTemplate.NPC_FOEMAN4, true) + 
			wei.getNpcCount(ExpansionNpcTemplate.NPC_GUARD, true) + 
			shu.getNpcCount(ExpansionNpcTemplate.NPC_GUARD, true) + 
			wu.getNpcCount(ExpansionNpcTemplate.NPC_GUARD, true);
 	}
	
	protected int getTotalRorman(ExpansionNation nation){
		return nation.getNpcCount(ExpansionNpcTemplate.NPC_FOEMAN1, true) + 
			nation.getNpcCount(ExpansionNpcTemplate.NPC_FOEMAN2, true) + 
			nation.getNpcCount(ExpansionNpcTemplate.NPC_FOEMAN3, true) + 
			nation.getNpcCount(ExpansionNpcTemplate.NPC_FOEMAN4, true);
	}
	
	/** 随机刷于吉 */
	protected void refreshYUJI(){
		removeNpc(ExpansionNpcTemplate.NPC_YUJI1);
		removeNpc(ExpansionNpcTemplate.NPC_YUJI2);
		removeNpc(ExpansionNpcTemplate.NPC_YUJI3);
		removeNpc(ExpansionNpcTemplate.NPC_YUJI4);
		removeNpc(ExpansionNpcTemplate.NPC_YUJI5);
		removeNpc(ExpansionNpcTemplate.NPC_YUJI6);
		int[] types = new int[]{
				ExpansionNpcTemplate.NPC_YUJI1,
				ExpansionNpcTemplate.NPC_YUJI2,
				ExpansionNpcTemplate.NPC_YUJI3,
				ExpansionNpcTemplate.NPC_YUJI4,
				ExpansionNpcTemplate.NPC_YUJI5,
				ExpansionNpcTemplate.NPC_YUJI6
		};
		int index1 = random.nextInt(6);
		addCreature(types[index1]);
	}
	
	protected void listenWinnerAndProcess(){
		if(wei.state==ExpansionNation.STATE_WIN){
			winner = wei;
		}else if(shu.state==ExpansionNation.STATE_WIN){
			winner = shu;
		}else if(wu.state==ExpansionNation.STATE_WIN){
			winner = wu;
		}
		if(winner!=null){
			state = STATE_WIN;
			LogUtil.logExpansionBattleEnd(winner.faction, "WIN");
			Server.server.scheduExec.schedule(new Runnable(){
				public void run() {
					Server.server.syncRunner.add(new Runnable(){
						public void run() {
							try {
								wei.checkBag();
								shu.checkBag();
								wu.checkBag();
							} catch (Exception e) {
								
							}
						}
					});
				}
			}, 2000, TimeUnit.MILLISECONDS);
		}
	}
	
	public void removeNpcs(){
		for(ExpansionNpc npc : wei.npcs){
			npc.npc.removeFromWorld();
		}
		for(ExpansionNpc npc : shu.npcs){
			npc.npc.removeFromWorld();
		}
		for(ExpansionNpc npc : wu.npcs){
			npc.npc.removeFromWorld();
		}
		wei.npcs.clear();
		shu.npcs.clear();
		wu.npcs.clear();
		for(ExpansionNpc npc : npcs){
			npc.npc.removeFromWorld();
		}
		npcs.clear();
		addCreature(ExpansionNpcTemplate.NPC_NOTBATTLE);
	}
	
	public void removeNpc(int type){
		synchronized (this) {
			Iterator<ExpansionNpc> it = npcs.iterator();
			while(it.hasNext()){
				ExpansionNpc npc = it.next();
				if(npc.type==type){
					npc.npc.removeFromWorld();
					it.remove();
				}
			}
		}
	}
	
	/** 胜利国家 */
	public ExpansionNation getWinnerNation(){
		return winner;
	}
	
	/** 是否是保护时间 */
	public boolean isSafeTime(){
		return instanceOpenTime>0 && (System.currentTimeMillis()-instanceOpenTime < 20*1000L);
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_MAP_PLAYER_REMOVED,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_MAP_PLAYER_REMOVED:
			processRemoveFromMap((VMap)event.param1,(Player)event.param2);
			break;
		}
	}
	
	protected void processRemoveFromMap(VMap map, Player p){
		if(p!=null && this.map.getId()==map.getId()){
			if(p.pool.getInt("EXPANSIONBUFF", 0)==1){
				p.buffs.removeBuff(ExpansionConfig.BUFF_ITEM);
				p.pool.remove("EXPANSIONBUFF");
				PlayerTransaction tx = p.newTransaction("EXPANSION");
				p.bag.removeGameItemIngoreInstanceId(ExpansionConfig.ITEM, 1, tx, false);
				tx.commit();
			}
		}
	}

}
