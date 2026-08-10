package peony.service.activity;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import peony.game.Creature;
import peony.game.GameObject;
import peony.game.NoInstanceVMapManager;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.VMapUtil;
import peony.game.attendant.Attendant;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;

/**
 * 击退年兽
 * @author bqzhang
 */
public class NewYearAnimalActivity implements IActivityImpl,ServiceEventListener {
	
	protected Activity act;
	
	/** 场景地图ID */
	protected static int mapId = 2016;
	
	/** NPCID 新春年兽,暴怒的年兽 */
	protected static int[] npcIds = {8257777, 8257778};
	
	/** 年兽持续时间 20分钟 */
	protected static int LASTTIME = 20*60*1000;
	
	/** 文艺年兽需要被攻击的最大次数 */
	protected static int MAX_COUNT_AttED = 100;
	
	/** 年兽被攻击次数 */
	protected static int attackedCnt = 0;
	
	/** 年兽状态:0-普通,1-暴怒 */
	protected static int npcState = 0;
	
	public static Random random = new Random();
	
	public Map<Integer,GameObject> refreshedNpc = new HashMap<Integer,GameObject>();
	
	public NewYearAnimalActivity(Activity act){
		this.act = act;
	}
	
	public void startup() throws Exception {
		timeHandler();
		Server.server.getEventManager().registerListener(this);
	}
	
	public void clear() {
	}

	public Activity getActivity() {
		return act;
	}
	
	public void load() {
		
	}

	public void save() {
		
	}

	public void shutdown() {
		removeNpc();
	}

	public void timeHandler(){
		//每两个小时刷新一次年兽
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				npcState = 0;
				if(act!=null && act.isActive() && act.isEnabled() && act.isVisible())
					refreshNpc();
				//持续20分钟，如果还存在就消失
				Server.server.scheduExec.schedule(new Runnable(){
					public void run() {
						if(npcState == 0){
							removeNpc();
						}
					}
				}, LASTTIME, TimeUnit.MILLISECONDS);
			}
		}, 0, 2*60*60*1000L, TimeUnit.MILLISECONDS);
	}
	
	/** 刷新NPC */
	public void refreshNpc(){
		attackedCnt = 0;
		int[] npcXY = new int[2];
		if(refreshedNpc.size()>0){
			for (GameObject npc : refreshedNpc.values()) {
				if (npc != null && npc.getVMap() != null) {
					npcXY[0] = npc.x;
					npcXY[1] = npc.y;
				}
			}
		}
		removeNpc();
		ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
		GameMapObject gmo = GameMapObject.findByID(proj, npcIds[npcState]);
		VMap map = ((NoInstanceVMapManager) Server.server.getWorld()
				.getVMapManager(mapId)).getVMaps(mapId)[0];
		
		if(npcState == 0){
			npcXY[0] = gmo.x;
			npcXY[1] = gmo.y;
		}
		
		GameObject npc = VMapUtil.addCreature(map, npcXY[0], npcXY[1],
				(GameMapNPC) gmo, true, 0, null);
		
		refreshedNpc.put(mapId, npc);
		
		if(npcState == 0){
			Server.server.getServiceRegistry().getChatService().sendWorldMessage("新春年兽出现在南越，大家手里有炮仗的速去啊！");
		}
	}
	
	/** 移除NPC */
	public void removeNpc(){
		if(refreshedNpc.size()>0){
			for (GameObject npc : refreshedNpc.values()) {
				if (npc != null && npc.getVMap() != null) {
					npc.removeFromWorld();
				}
				
				if(npc.type==GameObject.TYPE_CREATURE){
					Creature c = (Creature)npc;
					c.clearThreats();
				}
			}
			refreshedNpc.clear();
		}
	}
	
	/** 对新春年兽使用炮竹 */
	public void attackedNewAnimal(){
		if(npcState == 0){
			attackedCnt++;
			if(attackedCnt >= MAX_COUNT_AttED){
				npcState = 1;	//被攻击100次变成，暴怒年兽
				refreshNpc();
			}
		}
	}
	
	public int[] getEventTypes() {
		return new int[]{
			ServiceEvent.EVENT_UNIT_DIE
		};
	}
	
	public void handleEvent(ServiceEvent event) {
		switch(event.type){
			case ServiceEvent.EVENT_UNIT_DIE:
				processUnitDie((Unit)event.param1,(Unit)event.param2);
				break;
		}
	}
	
	//暴怒年兽死亡后，发公告
	public void processUnitDie(Unit u1,Unit u2){
		if(u1.id == npcIds[1]){
			Player p = null;
			if(u2.type == GameObject.TYPE_PLAYER)
			    p = ObjectAccessor.getPlayer(u2.id);
			else if(u2.type == GameObject.TYPE_ATTENDANT){
				Attendant att =(Attendant)u2;
				p = att.owner;
			}
			if(p!=null){
				Server.server.getServiceRegistry().getChatService().sendWorldMessage(MessageFormat.format("{0}奋力一击，杀掉了正在南越肆虐的暴怒年兽。", p.name));
			}
		}
	}
}
