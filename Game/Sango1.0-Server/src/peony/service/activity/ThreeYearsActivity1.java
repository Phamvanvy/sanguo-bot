package peony.service.activity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.joda.time.MutableDateTime;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import peony.game.ChatOption;
import peony.game.CycleListener;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.NoInstanceVMapManager;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.VMap;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.game.chat.ChatService;
import peony.game.mail.MailService;
import peony.service.ServiceEvent;

/**
 * 明珠三周年活动PVE版
 * @author dchen
 */
public class ThreeYearsActivity1 implements IActivityImpl, CycleListener {

	protected Activity act; //活动实体
	
	public static int mapId; //地图ID
	public static int battleStartH,battleStartM,battleEndH,battleEndM;
	public static int rainStartH,rainStartM,rainEndH,rainEndM;
	protected boolean battleOpened;
	protected boolean rainOpened;
	protected static int battleDropItem; //战争阶段掉落组
	protected static int flag = 8323258; //和平旗帜
	protected static int flagWei = 8323259; //魏国胜利旗帜
	protected static int flagWei1 = 8323262; //魏国胜利旗帜
	protected static int flagShu = 8323260; //蜀国胜利旗帜
	protected static int flagShu1 = 8323263; //蜀国胜利旗帜
	protected static int flagWu = 8323261; //吴国胜利旗帜
	protected static int flagWu1 = 8323264; //吴国胜利旗帜
	public static int seedId = 4366; //火炬ID
	public static int seedMin = 10; //单次上缴火种最少数量
	public static int gift = 4367; //兑换的礼包ID
	
	protected List<Player> weiPlayers = new ArrayList<Player>();
	protected List<Player> shuPlayers = new ArrayList<Player>();
	protected List<Player> wuPlayers = new ArrayList<Player>();
	protected List<GameObject> battleNpcs = new ArrayList<GameObject>();
	protected List<GameObject> rainNpcs = new ArrayList<GameObject>();
	
	public int[] factionRecord = new int[3];
	public int[] seedsRecord = new int[3];
	public int[] reputes = new int[3];
	
	protected int lastRain;
	
	public static int typeOfBattle = 1;
	public static int typeOfRain = 2;
	
	public static int dropRatio = 50;
	public static Random random = new Random();
	
	public static int firstCredit = 9;
	public static int secondCredit = 6;
	public static int thirdCredit = 3;
	
	protected MutableDateTime cachedCal = new MutableDateTime();
	
	public ThreeYearsActivity1(Activity act){
		this.act = act;
	}
	
	public Activity getActivity() {
		return act;
	}
	
	public void startup() throws Exception {
		
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
							}else if(key.equalsIgnoreCase("time1")){
								String[] str4 = value.split(";");
								String start = str4[0];
								String end = str4[1];
								String[] start1 = start.split(":");
								String[] end1 = end.split(":");
								battleStartH = Integer.parseInt(start1[0]);
								battleStartM = Integer.parseInt(start1[1]);
								battleEndH = Integer.parseInt(end1[0]);
								battleEndM = Integer.parseInt(end1[1]);
							}else if(key.equalsIgnoreCase("time2")){
								String[] str4 = value.split(";");
								String start = str4[0];
								String end = str4[1];
								String[] start1 = start.split(":");
								String[] end1 = end.split(":");
								rainStartH = Integer.parseInt(start1[0]);
								rainStartM = Integer.parseInt(start1[1]);
								rainEndH = Integer.parseInt(end1[0]);
								rainEndM = Integer.parseInt(end1[1]);
							}else if(key.equalsIgnoreCase("dropitem")){
								battleDropItem = Integer.parseInt(value);
							}else if(key.equalsIgnoreCase("flag")){
								flag = Integer.parseInt(value);
							}else if(key.equalsIgnoreCase("flagwei")){
								flagWei = Integer.parseInt(value);
							}else if(key.equalsIgnoreCase("flagshu")){
								flagShu = Integer.parseInt(value);
							}else if(key.equalsIgnoreCase("flagwu")){
								flagWu = Integer.parseInt(value);
							}else if(key.equalsIgnoreCase("seedid")){
								seedId = Integer.parseInt(value);
							}else if(key.equalsIgnoreCase("seedmin")){
								seedMin = Integer.parseInt(value);
							}else if(key.equalsIgnoreCase("gift")){
								gift = Integer.parseInt(value);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void handinSeed(Player player) throws Exception {
		if(player!=null){
			if(!battleOpened)
				throw new Exception("活动暂未开启");
			int faction = player.faction;
			int seedCount = player.bag.getGameItemCount(seedId);
			if(seedCount<seedMin){
				GameItem temp = ObjectAccessor.createGameItem(seedId);
				String itemName = "点燃的火种";
				try{itemName = temp.template.name;}catch(Exception e){}
				throw new Exception(MessageFormat.format("您拥有的{0}还不足{1}个,攒齐后再向我兑换大礼包吧", itemName, seedMin));
			}
			PlayerTransaction tx0 = player.newTransaction("THREEYEAR");
			GameItem item0 = player.bag.removeGameItemIngoreInstanceId(seedId, seedMin, tx0, false);
			if(item0!=null){
				tx0.commit();
			}else{
				tx0.rollback();
				return;
			}
			int index = getIndex(faction);
			int totalCount = seedsRecord[index];
			totalCount += seedMin;
			seedsRecord[index] = totalCount;
			
			PlayerTransaction tx = player.newTransaction("THREEYEAR");
			GameItem item = ObjectAccessor.createGameItem(gift);
			try {
				player.bag.addGameItemComplete(item, 1, tx, true);
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				MailService mailService = Server.server.getServiceRegistry().getMailService();
				mailService.sendSystemMailAsync(player.id, "系统", "三周年活动奖励", 
						"义士于三国三周年庆期间上交未点燃的火种若干，为国效力，特此奖励。", 
						0, item, 1, "THREEYEAR");
			}
			bubble();
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
	
	public boolean isInRainTime(){
		long now = System.currentTimeMillis();
		cachedCal.setMillis(now);
		int hour = cachedCal.getHourOfDay();
		int min = cachedCal.getMinuteOfHour();
		boolean afterStartTime = false;
		if(hour>rainStartH || hour==rainStartH && min>=rainStartM)
			afterStartTime = true;
		boolean beforeEndTime = false;
		if(hour<rainEndH || hour==rainEndH && min<rainEndM)
			beforeEndTime = true;
		if(afterStartTime && beforeEndTime)
			return true;
		return false;
	}
	
	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_UNIT_DIE
		};
	}

	public boolean update(int diff) {
		if(isInBattleTime() && !battleOpened){
			factionRecord[0] = GameObject.FACTION_WEI;
			factionRecord[1] = GameObject.FACTION_SHU;
			factionRecord[2] = GameObject.FACTION_WU;
			battleOpened = true;
			refreshBattleNpcs();
			Server.server.getServiceRegistry().getChatService().sendSystemMessage(ChatOption.SYSTEM, 
					"系统", MessageFormat.format("三周年活动开启！速到南海缴获火种吧！截止到{0}点整点燃火炬，按国别名次享受战功雨!", rainStartH));
		}
		if(!isInBattleTime() && battleOpened){
			battleOpened = false;
			battleEnd();
		}
		if(isInRainTime() && !rainOpened){
			rainOpened = true;
			try {
				StringBuffer sb = new StringBuffer();
				for(int i=0;i<factionRecord.length;i++){
					int faction = factionRecord[i];
					if(reputes[i]==1){
						sb.append(GameObject.FACTION_NAME[faction]);
						sb.append("和");
					}
				}
				String factionName = sb.toString().substring(0, sb.length()-1);
				Server.server.getServiceRegistry().getChatService().sendWorldMessage(MessageFormat
						.format("火种收集完成！现在开启战功雨，快来南海享受吧！{0}勇士们上交火种数量最多，将享受3倍战功雨，恭喜他们吧！", factionName));
			} catch (Exception e) {
			}
		}
		if(!isInRainTime() && rainOpened){
			rainOpened = false;
			removeNpc(typeOfBattle);
			removeNpc(typeOfRain);
			clearAllData();
		}
		if(rainOpened){
			if(Time.currTime-lastRain>60000){
				checkPlayers();
				for(int faction=GameObject.FACTION_WEI;faction<=GameObject.FACTION_WU;faction++){
					int index = getIndex(faction);
					int repute = reputes[index];
					if(repute==1){
						//第一名
						rain(faction, firstCredit);
					}else if(repute==2){
						//第二名
						rain(faction, secondCredit);
					}else if(repute==3){
						//第三名
						rain(faction, thirdCredit);
					}
				}
				lastRain = Time.currTime;
			}
		}
		return true;
	}
	
	protected void clearAllData(){
		weiPlayers.clear();
		shuPlayers.clear();
		wuPlayers.clear();
		battleNpcs.clear();
		rainNpcs.clear();
		
		factionRecord = new int[3];
		seedsRecord = new int[3];
		reputes = new int[3];
	}
	
	protected void refreshBattleNpcs(){
		removeNpc(typeOfRain);
		refreshNpc(flag, typeOfBattle);
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
	
	protected void battleEnd(){
		ChatService chatService = Server.server.getServiceRegistry().getChatService();
		int winSeeds = seedsRecord[0];
		if(winSeeds==0)
			return;
		if(seedsRecord[getIndex(GameObject.FACTION_WEI)]>=winSeeds){
			refreshNpc(flagWei, typeOfRain);
			refreshNpc(flagWei1, typeOfRain);
			chatService.sendFactionShout(GameObject.FACTION_WEI, 
					"恭喜魏国强壮的勇士们点燃了火炬，现在，大魏子民们，来到南海尽情享受战功雨的恩泽吧！", 
					0x0000ff, 6000);
		}
		if(seedsRecord[getIndex(GameObject.FACTION_SHU)]>=winSeeds){
			refreshNpc(flagShu, typeOfRain);
			refreshNpc(flagShu1, typeOfRain);
			chatService.sendFactionShout(GameObject.FACTION_SHU, 
					"恭喜蜀国强壮的勇士们点燃了火炬，现在，大蜀子民们，来到南海尽情享受战功雨的恩泽吧！", 
					0x0000ff, 6000);
		}
		if(seedsRecord[getIndex(GameObject.FACTION_WU)]>=winSeeds){
			refreshNpc(flagWu, typeOfRain);
			refreshNpc(flagWu1, typeOfRain);
			chatService.sendFactionShout(GameObject.FACTION_WU, 
					"恭喜吴国强壮的勇士们点燃了火炬，现在，大吴子民们，来到南海尽情享受战功雨的恩泽吧！", 
					0x0000ff, 6000);
		}
		removeNpc(typeOfBattle);
	}
	
	protected void bubble(){
		for(int i=0;i<3;i++){
			for(int j=i+1;j<3;j++){
				if(seedsRecord[j]>=seedsRecord[i]){
					int temp = seedsRecord[j];
					seedsRecord[j] = seedsRecord[i];
					seedsRecord[i] = temp;
					
					temp = factionRecord[j];
					factionRecord[j] = factionRecord[i];
					factionRecord[i] = temp;
				}
			}
		}
		reputes[0] = 1;
		if(seedsRecord[1]==seedsRecord[0]){
			reputes[1] = 1;
			if(seedsRecord[2]==seedsRecord[1])
				reputes[2] = 1;
			else
				reputes[2] = 2;
		}else{
			reputes[1] = 2;
			if(seedsRecord[2]==seedsRecord[1])
				reputes[2] = 2;
			else 
				reputes[2] = 3;
		}
	}
	
	protected int getIndex(int faction){
		for(int i=0;i<3;i++){
			if(faction==factionRecord[i])
				return i;
		}
		return 0;
	}
	
	protected void rain(int faction, int credit){
		List<Player> players = null;
		if(faction==GameObject.FACTION_WEI){
			players = weiPlayers;
		}else if(faction==GameObject.FACTION_SHU){
			players = shuPlayers;
		}else if(faction==GameObject.FACTION_WU){
			players = wuPlayers;
		}
		Iterator<Player> it = players.iterator();
		while(it.hasNext()){
			Player player = it.next();
			if(player!=null){
				PlayerTransaction tx = player.newTransaction("THREEYEAR");
				player.addCredit(credit, tx, true);
				tx.commit();
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

	public void save() {
		
	}

	public void shutdown() {
		removeNpc(typeOfRain);
		removeNpc(typeOfBattle);
		clearAllData();
	}
	
	public void clear() {
		
	}

}
