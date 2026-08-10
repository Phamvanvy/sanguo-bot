package peony.service.activity;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;

import peony.db.RefreshNpcCall;
import peony.game.CommonUtil;
import peony.game.Creature;
import peony.game.GameItem;
import peony.game.GameMapDefinition;
import peony.game.GameObject;
import peony.game.NoInstanceVMapManager;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapUtil;
import peony.game.buff.BuffUtil;
import peony.game.mail.MailService;
import peony.util.TimeUtil;

public class MayDayActivity implements IActivityImpl{
	
	private static Logger log = Logger.getLogger(MayDayActivity.class);
   
    protected int BEGIN_HOUR1 = 11;
    protected int BEGIN_MIN1 = 0;
    protected int BEGIN_HOUR2 = 18;
    protected int BEGIN_MIN2 = 30;
    protected int ONEDAY = 24*60*60*1000;
    protected int LASTTIME = 30*60*1000;  //活动持续时间
    protected int REFRESHTIME = 9*1000;   //场景怪物刷新时间
    protected static int STATE = 0;//活动状态(0未开始，1正在进行)
    protected static Map<Integer,Config> faction2Config = new HashMap<Integer,Config>();
    protected static Map<Integer,List<Creature>> faction2Creatures = new HashMap<Integer,List<Creature>>();
    protected static Map<Integer,Creature> faction2Fire = new HashMap<Integer,Creature>();
    protected static Map<Integer,Creature> faction2ShowFire = new HashMap<Integer,Creature>();
    protected static int[] scores = new int[3]; //各个国家得分情况
    protected static Map<Integer,List<Integer>> playerHandIn = new HashMap<Integer,List<Integer>>();
    protected static int scoreLimit = 10000;   //得分上限
    protected static int LEVELLIMIT = 60;     
    protected static int ITEM_REWARD = 4731;
    protected static int BUFF_REWARD = 653;
    static Timer t = new Timer();
    
	public Activity activity;
	
	public Activity getActivity() {
		return activity;
	}
	
	public MayDayActivity(Activity owner) {
		this.activity = owner;
	}
	
	private void processNotify(){
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				Server.server.getServiceRegistry().getChatService()
				           .sendWorldMessage("5分钟后南海、南越、江陵各国的烽火台需要添加木材，请勇士们做好准备。");
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGIN_HOUR1-1, 55), ONEDAY, TimeUnit.MILLISECONDS);
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				Server.server.getServiceRegistry().getChatService()
		           .sendWorldMessage("南海、南越、江陵各国的烽火台急需添加木材，勇士们迅速集合吧！");
				scores = new int[3];
				refreshNpc();
				STATE = 1;
				t = new Timer();
				t.schedule(new TimerTask(){
					public void run() {
//						refreshCreature();
						RefreshNpcCall call = new RefreshNpcCall(RefreshNpcCall.MAYDAYACTIVITY);
						Server.server.getWorld().schedule(call);
					}
				}, 0, REFRESHTIME);
				//持续30分钟，如果还存在就消失
				Server.server.scheduExec.schedule(new Runnable(){
					public void run() {
						if(STATE == 1){
							int winFaction = getWinFaction();
							gameOver(winFaction);
						}
					}
				}, LASTTIME, TimeUnit.MILLISECONDS);
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGIN_HOUR1, BEGIN_MIN1), ONEDAY, TimeUnit.MILLISECONDS);
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				if(STATE == 1){
					Server.server.getServiceRegistry().getChatService()
					           .sendWorldMessage("5分钟后将统计本次三座烽火台积攒的木材。评分最多的国家获得本次胜利，勇士们赶快上交已经得到的木材啊！");
				}
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGIN_HOUR1, 25), ONEDAY, TimeUnit.MILLISECONDS);
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				Server.server.getServiceRegistry().getChatService()
		           .sendWorldMessage("南海、南越、江陵各国的烽火台急需添加木材，勇士们迅速集合吧！");
				scores = new int[3];
				refreshNpc();
				STATE = 1;
				t = new Timer();
				t.schedule(new TimerTask(){
					public void run() {
//						refreshCreature();
						RefreshNpcCall call = new RefreshNpcCall(RefreshNpcCall.MAYDAYACTIVITY);
						Server.server.getWorld().schedule(call);
					}
				}, 0, REFRESHTIME);
				//持续30分钟，如果还存在就消失
				Server.server.scheduExec.schedule(new Runnable(){
					public void run() {
						if(STATE == 1){
							int winFaction = getWinFaction();
							gameOver(winFaction);
						}
					}
				}, LASTTIME, TimeUnit.MILLISECONDS);
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGIN_HOUR2, BEGIN_MIN2), ONEDAY, TimeUnit.MILLISECONDS);
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				if(STATE == 1){
					Server.server.getServiceRegistry().getChatService()
					           .sendWorldMessage("5分钟后将统计本次三座烽火台积攒的木材。评分最多的国家获得本次胜利，勇士们赶快上交已经得到的木材啊！");
				}
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGIN_HOUR2, 55), ONEDAY, TimeUnit.MILLISECONDS);
	}
	
	public static void handInMatieral(Player player) throws Exception{
		if(player!=null){
			if(player.level<LEVELLIMIT){
				throw new Exception("60级方可参加活动");
			}
			if(STATE == 0){
				throw new Exception("现在不是活动时间");
			}
			int score = 0;
			for(int faction : faction2Config.keySet()){
				Config config = faction2Config.get(faction);
				int baseScore = faction == player.faction?1:2;
				PlayerTransaction tx = player.newTransaction("MAYDAY");
				try{
					int count = player.bag.getGameItemCount(config.materialId);
					if(count > 0){
						player.bag.removeGameItemIngoreInstanceId(config.materialId, count, tx, true);
						score += baseScore*count;
						tx.commit();
					}else{
						tx.rollback();
					}
				}catch(Exception e){
					tx.rollback();
				}
			}
			if(score>0){
				PlayerTransaction tx = player.newTransaction("MAYDAY");
				player.addExp(score*1000, tx, true);
				player.addCredit(score, tx, true);
				tx.commit();
				int value = scores[player.faction-1];
				value+=score;
				scores[player.faction-1]=value;
				List<Integer> l = playerHandIn.get(player.faction);
				if(!l.contains(player.id)){
					l.add(player.id);
					playerHandIn.put(player.faction, l);
				}
				if(value>=scoreLimit){
					gameOver(player.faction);
				}
			}else{
				player.message(-1, "勇士当前没有需要上交的木材，还请取得木材之后再来上交吧！", -1, -1);
			}
		}
	}
	
	public int getWinFaction(){
		int ret = 0;
		int tempRet = scores[0];
		if(tempRet!=0)
			ret = 1;
		for(int i=1;i<scores.length;i++){
			if(scores[i]>tempRet){
				ret = i+1;
				tempRet = scores[i];
			}
		}
		return ret;
	}
	

	public void refreshNpc(){
		for(int faction : faction2Config.keySet()){
			List<Integer> players = new ArrayList<Integer>();
			playerHandIn.put(faction, players);
			if(faction2Fire.containsKey(faction)){
				Creature fire = faction2Fire.get(faction);
				if(fire!=null && fire.isVisibleAndAlive()){
					fire.removeFromWorld();
				}
			}
			Config config = faction2Config.get(faction);
			ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
			int[] fireCreatures = config.fireCreature;
			GameMapObject gmo = GameMapObject.findByID(proj, fireCreatures[faction-1]);
			VMap map = ((NoInstanceVMapManager) Server.server.getWorld()
					.getVMapManager(config.mapId)).getVMaps(config.mapId)[0];
			GameObject npc0 = VMapUtil.addCreature(map, config.towerX, config.towerY,
					(GameMapNPC) gmo, true, 0, null);
			faction2Fire.put(faction, (Creature)npc0);
			if(faction2ShowFire.containsKey(faction)){
				Creature showFire = faction2ShowFire.get(faction);
				if(showFire!=null && showFire.isVisibleAndAlive()){
					showFire.removeFromWorld();
				}
			}
			int showTower = config.showTower;
			GameMapObject gmo1 = GameMapObject.findByID(proj, showTower);
			GameObject npc1 = VMapUtil.addCreature(map, config.towerX, config.towerY,
					(GameMapNPC) gmo1, true, 0, null);
			faction2ShowFire.put(faction, (Creature)npc1);
		}
	}
	
	public synchronized static void gameOver(int winFaction){
		STATE = 0;
		t.cancel();
		for(int faction : faction2Config.keySet()){
            if(winFaction!=0 && faction!=winFaction){
				Config config = faction2Config.get(faction);
				ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
				int[] fireCreatures = config.fireCreature;
				Creature creature = faction2Fire.get(faction);
				if(creature!=null && creature.isVisibleAndAlive()){
					creature.removeFromWorld();
				}
				GameMapObject gmo = GameMapObject.findByID(proj, fireCreatures[winFaction-1]);
				VMap map = ((NoInstanceVMapManager) Server.server.getWorld()
						.getVMapManager(config.mapId)).getVMaps(config.mapId)[0];
				GameObject npc0 = VMapUtil.addCreature(map, config.towerX, config.towerY,
						(GameMapNPC) gmo, true, 0, null);
				faction2Fire.put(faction, (Creature)npc0);
				Creature showTower = faction2ShowFire.get(faction);
				if(showTower!=null && showTower.isVisibleAndAlive()){
					showTower.removeFromWorld();
				}
				Config winConfig = faction2Config.get(winFaction);
				GameMapObject gmo1 = GameMapObject.findByID(proj, winConfig.showTower);
				GameObject npc1 = VMapUtil.addCreature(map, config.towerX, config.towerY,
						(GameMapNPC) gmo1, true, 0, null);
				faction2ShowFire.put(faction, (Creature)npc1);
            }
			List<Creature> creatures = faction2Creatures.get(faction);
			if(creatures!=null && creatures.size()>0){
				for(Creature c : creatures){
					if(c!=null && c.isVisibleAndAlive()){
						c.clearThreats();
						c.removeFromWorld();
					}
				}
			}
		}
		if(winFaction>0){
			List<Integer> winPlayers = playerHandIn.get(winFaction);
			if(winPlayers.size()>0){
		        MailService mailService = Server.server.getServiceRegistry().getMailService();
		        GameItem rewardItem = ObjectAccessor.createGameItem(ITEM_REWARD);
				for(int playerId : winPlayers){
					mailService.sendSystemMail(playerId, "系统", "五一劳动节胜利奖励", 
							     "五一劳动节胜利奖励", 0, rewardItem, 1, "MAYDAY");
				}
			}
			for(Player p:ObjectAccessor.players.values()){
				if(p.faction == winFaction)
				    p.buffs.addBuff(BuffUtil.createBuff(BUFF_REWARD, 1, p, p, 0));
			}
			String factionName = GameObject.getFactionName(winFaction);
			String winMessage = MessageFormat.format("{0}烽火更胜一筹，迅速占领了其他两国的烽火台。{1}国君欣喜不已，特赐全国人民3倍经验1个小时。", factionName,factionName);
			Server.server.getServiceRegistry().getChatService()
	           .sendWorldMessage(winMessage);
		}else{
			Server.server.getServiceRegistry().getChatService()
	           .sendWorldMessage("因无人上交木材，各国烽火台保持原状。各国国君大为不喜，收回了准备发放的奖励。");
		}
		faction2Creatures.clear();
		playerHandIn.clear();
	}
	
	public static int getScore(int faction){
		return scores[faction-1];
	}
	
	public synchronized static void refreshCreature(){
		if(STATE==0)
			return;
		for(int faction : faction2Config.keySet()){
		   Config config = faction2Config.get(faction);
		   VMap map = ((NoInstanceVMapManager) Server.server.getWorld().
					getVMapManager(config.mapId)).getVMaps(config.mapId)[0];
		   List<Creature> list = faction2Creatures.get(faction);
		   if(list!=null && list.size()>0){
			   for(int i=0;i<list.size();i++){
				   Creature c = list.get(i);
				   if(c!=null && c.isVisibleAndAlive())
					  continue;
					  boolean find = false;
					   for (int j = 0; j < 5; j++) {
							int numX = map.findPoint()[0];
							int numY = map.findPoint()[1];
							GameMapDefinition mapDef = VMapUtil.getDefinition(config.mapId);
							if (mapDef.mapInfo.getPathFinder().canReach(numX, numY)) {
								find = true;
							} 
							if (find) {
								ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
								GameMapObject gmo = GameMapObject.findByID(proj,c.id);
								GameObject npc0 = VMapUtil.addCreature(map,numX, numY,(GameMapNPC) gmo, true, 0, null);
								list.remove(c);
								i--;
								list.add((Creature)npc0);
								break;
							}
//						}
					}
			   } 
		   }else {
			   list = new ArrayList<Creature>();
			   for(int creatureId : config.creatureList){
				   boolean find = false;
				   for (int j = 0; j < 5; j++) {
						int numX = map.findPoint()[0];
						int numY = map.findPoint()[1];
						GameMapDefinition mapDef = VMapUtil.getDefinition(config.mapId);
						if (mapDef.mapInfo.getPathFinder().canReach(numX, numY)) {
							find = true;
						} 
						if (find) {
							ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
							GameMapObject gmo = GameMapObject.findByID(proj,creatureId);
							GameObject npc0 = VMapUtil.addCreature(map,numX, numY,(GameMapNPC) gmo, true, 0, null);
							list.add((Creature)npc0);
							break;
						}
					}
				}
			}
		   faction2Creatures.put(faction, list);
	    }
	}
	
	
	@SuppressWarnings("unchecked")
	public void parse(Document doc){
		Element root = doc.getRootElement();
		if (root != null) {
			List<Element> list = root.elements();
			for (Element l : list) {
				int faction = Integer.parseInt(l.attributeValue("factionid"));
				int mapId = Integer.parseInt(l.attributeValue("mapid"));
				int towerWei = Integer.parseInt(l.attributeValue("towerwei"));
				int towerShu = Integer.parseInt(l.attributeValue("towershu"));
				int towerWu = Integer.parseInt(l.attributeValue("towerwu"));
				int towerX = Integer.parseInt(l.attributeValue("towerx"));
				int towerY = Integer.parseInt(l.attributeValue("towery"));
				int material = Integer.parseInt(l.attributeValue("material"));
				int showTower = Integer.parseInt(l.attributeValue("showtower"));
				Config config = new Config(faction,mapId,towerX,towerY,material,showTower);
				config.fireCreature = new int[3];
				config.fireCreature[0] = towerWei;
				config.fireCreature[1] = towerShu;
				config.fireCreature[2] = towerWu;
				List<Element> creatures = l.elements("creature");
				for(Element c : creatures){
					int creatureId = Integer.parseInt(c.attributeValue("id"));
					config.addCreature(creatureId);
				}
				
				faction2Config.put(faction, config);
			}
		}
	}
	
	public void removeCreature(){
		for(int faction : faction2Config.keySet()){
			List<Creature> list = faction2Creatures.get(faction);
		    if(list!=null && list.size()>0){
			    for(Creature c : list){
				    if(c!=null && c.isVisibleAndAlive()){
					    c.clearThreats();
					    c.removeFromWorld();
				    }
			    }
		    }
		    Creature c = faction2Fire.get(faction);
		    if(c!=null && c.isVisibleAndAlive()){
			    c.removeFromWorld();
		    }
		    Creature showFire = faction2ShowFire.get(faction);
		    if(showFire!=null && showFire.isVisibleAndAlive()){
		    	showFire.removeFromWorld();
		    }
		    
		}
		faction2Creatures.clear();
		faction2Fire.clear();
		faction2Config.clear();
		faction2ShowFire.clear();
	}
	
	public void clear() {
			
	}

	public void load() {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
		.findFile("beaconActivity.xml");
		try {
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			parse(doc);
		} catch (Exception e) {
			log.error(e, e);
		}
	}

	public void save() {
		
		
	}

	public void shutdown() {
		removeCreature();
	}

	public void startup() throws Exception {
		processNotify();
	}

}
class Config{
	int faciton;
	int mapId;
	int towerX;
	int towerY;
	int materialId;
	int showTower;
	int[] fireCreature = new int[3];
	List<Integer> creatureList = new ArrayList<Integer>();
	public Config(int faction,int mapId,int towerX,int towerY,int materialId,int showTower){
		this.faciton = faction;
		this.mapId = mapId;
		this.towerX = towerX;
		this.towerY = towerY;
		this.materialId = materialId;
		this.showTower = showTower;
	}
	public void addCreature(int creatureId){
		creatureList.add(creatureId);
	}
	
}

