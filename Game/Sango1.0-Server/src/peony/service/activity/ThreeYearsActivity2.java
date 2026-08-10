package peony.service.activity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.joda.time.MutableDateTime;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import peony.game.ChatOption;
import peony.game.CycleListener;
import peony.game.GameObject;
import peony.game.NoInstanceVMapManager;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

/**
 * 明珠三周年活动PVP版
 * @author dchen
 */
public class ThreeYearsActivity2 implements IActivityImpl, ServiceEventListener, CycleListener {

	protected Activity act; //活动实体
	
	protected static int mapId;
	
	protected static int flag = 8257696;
	protected static int flagWei = 8257697;
	protected static int flagWei1 = 8257698;
	protected static int flagShu = 8257714;
	protected static int flagShu1 = 8257699;
	protected static int flagWu = 8257715;
	protected static int flagWu1 = 8257700;
	
	protected static int npc10 = 8257707;
	protected static int npc20 = 8257708;
	protected static int npc30 = 8257709;
	protected static int npc40 = 8257710;
	protected static int npc50 = 8257711;
	protected static int npc60 = 8257712;
	protected static int npc70 = 8257713;
	
	public static int typeOfBattle = 1;
	public static int typeOfRain = 2;
	
	protected List<GameObject> battleNpcs = new ArrayList<GameObject>();
	protected List<GameObject> rainNpcs = new ArrayList<GameObject>();
	
	public static int battleStartH,battleStartM,battleEndH,battleEndM;
	protected int[] score = new int[3];
	protected MutableDateTime cachedCal = new MutableDateTime();
	
	protected boolean end = true;
	protected boolean sendMessage = false;
	
	protected List<Player> weiPlayers = new ArrayList<Player>();
	protected List<Player> shuPlayers = new ArrayList<Player>();
	protected List<Player> wuPlayers = new ArrayList<Player>();
	
	protected int killTotal;
	
	public static int prefix = 2;
	
	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}
	
	public Activity getActivity() {
		return act;
	}
	
	public ThreeYearsActivity2(Activity act){
		this.act = act;
	}

	public void load() {
		String configData = act.configData;
		String[] str1 = configData.split("#");
		for(String str2 : str1){
			try {
				if(str2!=null){
					String[] str3 = str2.split("@");
					if(str3!=null){
						String key = str3[0];
						String value = str3[1];
						if(key!=null && value!=null){
							if(key.equalsIgnoreCase("mapid")){
								mapId = Integer.parseInt(value);
							}else if(key.equalsIgnoreCase("time")){
								String[] str4 = value.split(";");
								String start = str4[0];
								String end = str4[1];
								String[] start1 = start.split(":");
								String[] end1 = end.split(":");
								battleStartH = Integer.parseInt(start1[0]);
								battleStartM = Integer.parseInt(start1[1]);
								battleEndH = Integer.parseInt(end1[0]);
								battleEndM = Integer.parseInt(end1[1]);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static boolean isSpecialNpc(int npcId){
		if(npcId==npc10 || npcId==npc20 || npcId==npc30 || npcId==npc40 || npcId==npc50 || npcId==npc60 || npcId==npc70){
			return true;
		}
		return false;
	}
	
	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_UNIT_DIE
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_UNIT_DIE:
			processUnitDie((Unit)event.param1, (Unit)event.param2, event.param3);
			break;
		}
	}
	
	protected void processUnitDie(Unit dier, Unit killer, Object faction){
		if(dier!=null && killer!=null && (killer.type==GameObject.TYPE_PLAYER || killer.type==GameObject.TYPE_ATTENDANT) 
				&& dier.type!=GameObject.TYPE_PLAYER){
			if(dier.getVMap()!=null && dier.getVMap().getId()==mapId && isSpecialNpc(dier.id)){
				if(faction!=null){
					int winFaction = (Integer)faction;
					score[winFaction-1] = score[winFaction-1] + 1;
				}
			}
		}
		if(dier!=null && dier.getVMap()!=null && dier.getVMap().getId()==mapId && isSpecialNpc(dier.id)){
			killTotal++;
		}
	}
	
	public boolean update(int diff) {
		if(isInBattleTime() && end){
			end = false;
			score = new int[3];
			clearAllData();
			refreshBattleNpcs();
			Server.server.getServiceRegistry().getChatService().sendSystemMessage(ChatOption.SYSTEM, 
					"系统", "三周年活动开启！速到南越占领小火盆吧！哪个国家占领小火盆越多，则该国享受的战功雨越丰厚！");
		}
		if(!isInBattleTime() && !end){
			end = true;
			removeNpc(typeOfBattle);
			removeNpc(typeOfRain);
			killTotal = 0;
			sendMessage = false;
		}
		if(!end && killTotal>=7){
			removeNpc(typeOfBattle);
			sendOneMessages();
			checkPlayers();
			for(Player player : weiPlayers){
				int value = score[0];
				if(player!=null){
					PlayerTransaction tx = player.newTransaction("THREEYEAR");
					player.addCredit((value+1)*prefix, tx, true);
					tx.commit();
				}
			}
			for(Player player : shuPlayers){
				int value = score[1];
				if(player!=null){
					PlayerTransaction tx = player.newTransaction("THREEYEAR");
					player.addCredit((value+1)*prefix, tx, true);
					tx.commit();
				}
			}
			for(Player player : wuPlayers){
				int value = score[2];
				if(player!=null){
					PlayerTransaction tx = player.newTransaction("THREEYEAR");
					player.addCredit((value+1)*prefix, tx, true);
					tx.commit();
				}
			}
		}
		return true;
	}
	
	protected void sendOneMessages(){
		if(!sendMessage){
			boolean weiWin = false,shuWin=false,wuWin=false;
			int max = score[0]>score[1] ? score[0] : score[1];
			max = max>score[2] ? max : score[2];
			if(score[0]>=max)
				weiWin = true;
			if(score[1]>=max)
				shuWin = true;
			if(score[2]>=max)
				wuWin = true;
			if(weiWin){
				refreshNpc(flagWei, typeOfRain);
				refreshNpc(flagWei1, typeOfRain);
				Server.server.getServiceRegistry().getChatService().sendFactionShout(GameObject.FACTION_WEI, 
						MessageFormat.format("恭喜魏国强壮的勇士们以第一的成绩打掉了{0}个小火盆，点燃了主火炬，你们可以获得每小时{1}点的战功雨，快来南越尽情享受吧！", 
								max, (max+1)*60), 0x0000ff, 6000);
			}
			if(shuWin){
				refreshNpc(flagShu, typeOfRain);
				refreshNpc(flagShu1, typeOfRain);
				Server.server.getServiceRegistry().getChatService().sendFactionShout(GameObject.FACTION_SHU, 
						MessageFormat.format("恭喜蜀国强壮的勇士们以第一的成绩打掉了{0}个小火盆，点燃了主火炬，你们可以获得每小时{1}点的战功雨，快来南越尽情享受吧！", 
								max, (max+1)*60), 0x0000ff, 6000);
			}
			if(wuWin){
				refreshNpc(flagWu, typeOfRain);
				refreshNpc(flagWu1, typeOfRain);
				Server.server.getServiceRegistry().getChatService().sendFactionShout(GameObject.FACTION_WU, 
						MessageFormat.format("恭喜吴国强壮的勇士们以第一的成绩打掉了{0}个小火盆，点燃了主火炬，你们可以获得每小时{1}点的战功雨，快来南越尽情享受吧！", 
								max, (max+1)*60), 0x0000ff, 6000);
			}
			sendMessage = true;
		}
	}
	
	protected void removeNpc(int type){
		if(type==1){
			Iterator<GameObject> it = battleNpcs.iterator();
			while(it.hasNext()){
				GameObject o = it.next();
				o.removeFromWorld();
				it.remove();
			}
		}else if(type==2){
			Iterator<GameObject> it = rainNpcs.iterator();
			while(it.hasNext()){
				GameObject o = it.next();
				o.removeFromWorld();
				it.remove();
			}
		}
	}
	
	protected void refreshRainNpcs(){
		removeNpc(typeOfBattle);
	}
	
	protected void refreshBattleNpcs(){
		removeNpc(typeOfRain);
		refreshNpc(flag, typeOfBattle);
		refreshNpc(npc10, typeOfBattle);
		refreshNpc(npc20, typeOfBattle);
		refreshNpc(npc30, typeOfBattle);
		refreshNpc(npc40, typeOfBattle);
		refreshNpc(npc50, typeOfBattle);
		refreshNpc(npc60, typeOfBattle);
		refreshNpc(npc70, typeOfBattle);
	}
	
	protected void refreshNpc(int npcId, int type){
		GameMapObject gmo = GameMapObject.findByID(Server.server.getServiceRegistry().getDataService().data, npcId);
		VMapManager manager = Server.server.getWorld().getVMapManager(mapId);
		VMap[] maps = ((NoInstanceVMapManager) manager).getVMaps(mapId);
		for (VMap map : maps) {
			if(map!=null){
				GameObject o = VMapUtil.addCreature(map, gmo.x,  gmo.y, (GameMapNPC) gmo, true, 0, null);
				if(type==typeOfBattle){
					battleNpcs.add(o);
				}else if(type==typeOfRain){
					rainNpcs.add(o);
				}
			}
		}
	}
	
	protected void checkPlayers(){
		VMapManager manager = Server.server.getWorld().getVMapManager(mapId);
		VMap[] maps = ((NoInstanceVMapManager) manager).getVMaps(mapId);
		for (VMap map : maps) {
			if(map!=null){
				weiPlayers = map.getPlayersByFaction(GameObject.FACTION_WEI);
				shuPlayers = map.getPlayersByFaction(GameObject.FACTION_SHU);
				wuPlayers = map.getPlayersByFaction(GameObject.FACTION_WU);
			}
		}
	}
	
	public boolean isInBattleTime(){
		long now = System.currentTimeMillis();
		cachedCal.setMillis(now);
		int hour = cachedCal.getHourOfDay();
		int min = cachedCal.getMinuteOfHour();
		boolean afterStartTime = false;
		if(hour>battleStartH || hour==battleStartH && min>=battleStartM)
			afterStartTime = true;
		boolean beforeEndTime = false;
		if(hour<battleEndH || hour==battleEndH && min<battleEndM)
			beforeEndTime = true;
		if(afterStartTime && beforeEndTime)
			return true;
		return false;
	}
	
	public void clear() {
		
	}

	public void save() {
		
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
		removeNpc(typeOfBattle);
		removeNpc(typeOfRain);
		clearAllData();
	}
	
	protected void clearAllData(){
		weiPlayers.clear();
		shuPlayers.clear();
		wuPlayers.clear();
		battleNpcs.clear();
		rainNpcs.clear();
		score = new int[3];
		killTotal = 0;
	}

}
