package peony.service.expansionbattle;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import peony.game.Creature;
import peony.game.GameObject;
import peony.game.LogUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.VMapUtil;
import peony.game.buff.BuffUtil;
import peony.game.chat.ChatService;
import peony.service.ServiceEvent;
import peony.service.stat.Achievement;
import peony.service.stat.PvpInfo;
import peony.service.stat.StatService;

/**
 * 战役副本国家状态信息
 * @author dchen
 */
public class ExpansionNation {

	protected final Logger log = Logger.getLogger(ExpansionNation.class);
	public ExpansionInstance instance; // 所在的副本
	public int faction; // 阵营
	public int state; // 阵营状态
	public static int STATE_WIN = 1; // 战役胜利
	public List<Player> players = new ArrayList<Player>(); // 副本内所有成员
	public List<ExpansionNpc> npcs = new ArrayList<ExpansionNpc>(); // 本国相关所有NPC
	
	public int t2_buff; // 箭塔2是否已经移除BUFF（0否，1是）
	public int t3_buff; // 箭塔3是否已经移除BUFF（0否，1是）
	public int door_buff; // 城门是否已经移除BUFF（0否，1是）
	
	public int flag_die; // 是否已经法国旗子被砍倒的提示
	
	public ExpansionNation(int faction){
		this.faction = faction;
	}
	
	public void initNpcs(){
		addCreature(ExpansionNpcTemplate.NPC_DOOR,1);
		addCreature(ExpansionNpcTemplate.NPC_FLAG,1);
		addCreature(ExpansionNpcTemplate.NPC_TOWER1L,1);
		addCreature(ExpansionNpcTemplate.NPC_TOWER1R,1);
		addCreature(ExpansionNpcTemplate.NPC_TOWER2L,1);
		addCreature(ExpansionNpcTemplate.NPC_TOWER2R,1);
		addCreature(ExpansionNpcTemplate.NPC_TOWER3L,1);
		addCreature(ExpansionNpcTemplate.NPC_TOWER3R,1);
		addCreature(ExpansionNpcTemplate.NPC_GENERAL,1);
		GameObject t2l = getExpansionNpc(ExpansionNpcTemplate.NPC_TOWER2L).npc;
		GameObject t2r = getExpansionNpc(ExpansionNpcTemplate.NPC_TOWER2R).npc;
		GameObject t3l = getExpansionNpc(ExpansionNpcTemplate.NPC_TOWER3L).npc;
		GameObject t3r = getExpansionNpc(ExpansionNpcTemplate.NPC_TOWER3R).npc;
		GameObject door = getExpansionNpc(ExpansionNpcTemplate.NPC_DOOR).npc;
		((Unit)t2l).buffs.addBuff(BuffUtil.createBuff(ExpansionConfig.BUFF_WUDI1, 1, (Unit)t2l, (Unit)t2l, 0));
		((Unit)t2r).buffs.addBuff(BuffUtil.createBuff(ExpansionConfig.BUFF_WUDI1, 1, (Unit)t2r, (Unit)t2r, 0));
		((Unit)t3l).buffs.addBuff(BuffUtil.createBuff(ExpansionConfig.BUFF_WUDI1, 1, (Unit)t3l, (Unit)t3l, 0));
		((Unit)t3r).buffs.addBuff(BuffUtil.createBuff(ExpansionConfig.BUFF_WUDI1, 1, (Unit)t3r, (Unit)t3r, 0));
		((Unit)door).buffs.addBuff(BuffUtil.createBuff(ExpansionConfig.BUFF_WUDI1, 1, (Unit)door, (Unit)door, 0));
	}
	
	public void addCreature(int type, int count){
		synchronized (this) {
			ExpansionConfig config = instance.manager.config;
			ExpansionNpcTemplate template = config.getNpcTemplate(type, faction);
			if(template!=null){
				for(int i=0;i<count;i++){
					ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
					GameMapObject gmo = GameMapObject.findByID(proj, template.instanceId);
					GameObject npc = VMapUtil.addCreature(instance.map, template.x+(i*10), template.y+(i*10), 
							(GameMapNPC) gmo, true, 0, null);
					ExpansionNpc expansionNpc = new ExpansionNpc(type, npc);
					npcs.add(expansionNpc);
				}
				LogUtil.logExpansionBattleNpcRefresh(type, count, faction);
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
		synchronized (this) {
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
	}
	
	public ExpansionNpc getExpansionNpc(int type){
		synchronized (this) {
			for(ExpansionNpc npc : npcs){
				if(npc.type==type)
					return npc;
			}
			return null;
		}
	}
	
	public void addPlayer(Player p){
		synchronized (this) {
			players.add(p);
			if(instance.state==ExpansionInstance.STATE_START && instance.isSafeTime()){
				p.buffs.addBuff(BuffUtil.createBuff(ExpansionConfig.BUFF_WUDI, 1, p, p, 0));
			}
		}
	}
	
	public void removeNpc(int type){
		Iterator<ExpansionNpc> it = npcs.iterator();
		while(it.hasNext()){
			ExpansionNpc npc = it.next();
			if(npc.type==type){
				npc.npc.removeFromWorld();
				it.remove();
			}
		}
	}
	
	public void removePlayer(Player p){
		synchronized (this) {
			Iterator<Player> it = players.iterator();
			while(it.hasNext()){
				Player p1 = it.next();
				if(p.id==p1.id){
					p.buffs.removeBuff(ExpansionConfig.BUFF_WUDI);
					it.remove();
				}
			}
		}
	}
	
	/** 每隔一定时间刷新NPC */
	protected void autoRefreshNpc(final int type, long dis){
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				addCreature(type,1);
			}
		}, 0, dis, TimeUnit.MILLISECONDS);
	}
	
	/** 刷新董卓兵 */
	public void refreshFoeman(){
		ExpansionNpc tower1l = getExpansionNpc(ExpansionNpcTemplate.NPC_TOWER1L);
		ExpansionNpc tower1r = getExpansionNpc(ExpansionNpcTemplate.NPC_TOWER1R);
		ExpansionNpc tower2l = getExpansionNpc(ExpansionNpcTemplate.NPC_TOWER2L);
		ExpansionNpc tower2r = getExpansionNpc(ExpansionNpcTemplate.NPC_TOWER2R);
		ExpansionNpc tower3l = getExpansionNpc(ExpansionNpcTemplate.NPC_TOWER3L);
		ExpansionNpc tower3r = getExpansionNpc(ExpansionNpcTemplate.NPC_TOWER3R);
		if(tower1l.npc.isAlive() || tower1r.npc.isAlive()){
			// 刷第一波兵
			addCreature(ExpansionNpcTemplate.NPC_FOEMAN1,3);
		}else if(!tower1l.npc.isAlive() && !tower1r.npc.isAlive() && 
				(tower2l.npc.isAlive() || tower2r.npc.isAlive())){
			// 刷第二波兵
			addCreature(ExpansionNpcTemplate.NPC_FOEMAN2,3);
		}else if(!tower1l.npc.isAlive() && !tower1r.npc.isAlive() && 
				!tower2l.npc.isAlive() && !tower2r.npc.isAlive() && 
				(tower3l.npc.isAlive() || tower3r.npc.isAlive())){
			// 刷第三波兵
			addCreature(ExpansionNpcTemplate.NPC_FOEMAN3,3);
		}else if(!tower1l.npc.isAlive() && !tower1r.npc.isAlive() && 
				!tower2l.npc.isAlive() && !tower2r.npc.isAlive() && 
				!tower3l.npc.isAlive() && !tower3r.npc.isAlive()){
			// 刷第四波兵
			addCreature(ExpansionNpcTemplate.NPC_FOEMAN4,3);
		}
	}
	
	public boolean tower3IsDie(){
		return getNpcCount(ExpansionNpcTemplate.NPC_TOWER3L, true)==0 && 
			getNpcCount(ExpansionNpcTemplate.NPC_TOWER3R, true)==0;
	}
	
	public void update(int diff){
		if(flag_die==0){
			if(getNpcCount(ExpansionNpcTemplate.NPC_FLAG, true)==0){
				Server.server.getServiceRegistry().getChatService().
					sendAreaSystemMessage(MessageFormat.format(peony.Messages.STRING_01146,
							GameObject.getFactionName(faction),GameObject.getFactionName(faction)),instance.map.getId());
				flag_die = 1;
			}
		}
		if(getNpcCount(ExpansionNpcTemplate.NPC_TOWER1L, true)==0 && 
				getNpcCount(ExpansionNpcTemplate.NPC_TOWER1R, true)==0 && 
				t2_buff==0){
			((Creature)getExpansionNpc(ExpansionNpcTemplate.NPC_TOWER2L).npc).buffs.removeBuff(ExpansionConfig.BUFF_WUDI1);
			((Creature)getExpansionNpc(ExpansionNpcTemplate.NPC_TOWER2R).npc).buffs.removeBuff(ExpansionConfig.BUFF_WUDI1);
			t2_buff = 1;
		}
		if(getNpcCount(ExpansionNpcTemplate.NPC_TOWER2L, true)==0 && 
				getNpcCount(ExpansionNpcTemplate.NPC_TOWER2R, true)==0 && 
				t3_buff==0){
			((Creature)getExpansionNpc(ExpansionNpcTemplate.NPC_TOWER3L).npc).buffs.removeBuff(ExpansionConfig.BUFF_WUDI1);
			((Creature)getExpansionNpc(ExpansionNpcTemplate.NPC_TOWER3R).npc).buffs.removeBuff(ExpansionConfig.BUFF_WUDI1);
			t3_buff = 1;
		}
		if(getNpcCount(ExpansionNpcTemplate.NPC_TOWER3L, true)==0 && 
				getNpcCount(ExpansionNpcTemplate.NPC_TOWER3R, true)==0 && 
				door_buff==0 && 
				(getNpcCount(ExpansionNpcTemplate.NPC_LVBU, true)==0)){
			((Creature)getExpansionNpc(ExpansionNpcTemplate.NPC_DOOR).npc).buffs.removeBuff(ExpansionConfig.BUFF_WUDI1);
			door_buff = 1;
		}
		if(!getExpansionNpc(ExpansionNpcTemplate.NPC_DOOR).npc.isAlive() && 
				getExpansionNpc(ExpansionNpcTemplate.NPC_FLAG).npc.isAlive()){
			state = STATE_WIN;
			ChatService chatService = Server.server.getServiceRegistry().getChatService();
			chatService.sendWorldMessage(MessageFormat.format(peony.Messages.STRING_01147, GameObject.getFactionName(faction)));
			
			//统计在一场司隶战役中获得胜利的成就
			try{
				StatService statService = Server.server.getServiceRegistry().getStatService();
			    for(Player p:players){
			    	if(p.faction == faction){
			    		PvpInfo pvpInfo = statService.getPvpInfo(p.id, p.faction);
			    		Achievement a = statService.getAchievementById(134);
						if(a!=null){
							int type = Integer.parseInt(a.param1);
	                        if(type == 6){
					    		if(pvpInfo.pool.getString(StatService.PROPERTY_FINISHTIME_EXPANSIONBATTLE) == ""){
					    			pvpInfo.pool.setString(StatService.PROPERTY_FINISHTIME_EXPANSIONBATTLE,statService.getFinishTime(System.currentTimeMillis()));
					    			statService.setMessage(p, a, false,true);
					    		}
	                        }
						}
					}
			    }
			}catch(Exception e){
				
			}
			
			// 十分钟之后刷华雄、任务NPC
			Server.server.scheduExec.schedule(new Runnable(){
				public void run() {
					addCreature(ExpansionNpcTemplate.NPC_HUAXIONG, 1);
					addCreature(ExpansionNpcTemplate.NPC_CANJIANG, 1);
				}
			}, 10*60*1000L, TimeUnit.MILLISECONDS);
			Server.server.scheduExec.schedule(new Runnable(){
				public void run() {
					removeNpc(ExpansionNpcTemplate.NPC_HUAXIONG);
					removeNpc(ExpansionNpcTemplate.NPC_CANJIANG);
				}
			}, 30*60*1000L, TimeUnit.MILLISECONDS);
		}
	}
	
	public void resetStat(){
		state = 0;
		t2_buff = 0;
		t3_buff = 0;
		flag_die = 0;
		door_buff = 0;
	}
	
	public void checkBag(){
		synchronized (this) {
			try {
				Iterator<Player> it = players.iterator();
				while(it.hasNext()){
					Player p = it.next();
					if(instance.state==ExpansionInstance.STATE_START){
						int itemCount = p.bag.getGameItemCount(ExpansionConfig.ITEM);
						if(p.bag.getGameItem(ExpansionConfig.ITEM)!=null && p.pool.getInt("EXPANSIONBUFF", 0)==0){
							p.buffs.addBuff(BuffUtil.createBuff(ExpansionConfig.BUFF_ITEM, 1, p, p, 0));
							p.pool.setInt("EXPANSIONBUFF", 1);
							log.info("[EXPANSIONBATTLEBUFF]"+LogUtil.getPlayerLogString(p)+"BUFFID["+ExpansionConfig.BUFF_ITEM+"]");
						}
						if(itemCount>1){
							PlayerTransaction tx = p.newTransaction("EXPANSIONITEM");
							p.bag.removeGameItemIngoreInstanceId(ExpansionConfig.ITEM, itemCount-1, tx, false);
							tx.commit();
						}
					}else{
						if(p!=null && p.map.getId()==instance.map.getId()){
							if(p.pool.getInt("EXPANSIONBUFF", 0)==1){
								p.buffs.removeBuff(ExpansionConfig.BUFF_ITEM);
								p.pool.remove("EXPANSIONBUFF");
								PlayerTransaction tx = p.newTransaction("EXPANSION");
								p.bag.removeGameItemIngoreInstanceId(ExpansionConfig.ITEM, 1, tx, false);
								tx.commit();
							}
						}
					}
					Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_JOIN_SILI,p));
					if(p.pool.getInt(Player.WELFARE_JOIN_SILI,0)==0){
						p.pool.setInt(Player.WELFARE_JOIN_SILI,1);
						Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,p));
					}
				}
			} catch (Exception e) {
			}
		}
	}
	
}
