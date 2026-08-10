package peony.service.feast;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import peony.game.GameObject;
import peony.game.Instance;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapUtil;
import peony.game.chat.ChatService;
import peony.game.mail.MailService;
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.stat.StatService;

public class FeastInstance implements Instance,ServiceEventListener{
	
	protected final Logger log = Logger.getLogger(FeastInstance.class);

	protected FeastInstanceService manager = Server.server.getServiceRegistry().getFeastInstanceService();;
	
	protected VMap map;
	
    public static int BEGIN = 1;//开始
	
	public static int END = 2;//结束
    
	public int stat = END;
	
	protected Date endTime;
	
	public static long FEASTDURINGTIME = 10 * 60 * 1000; 
	
	public int MATERIAL_MAXSHOWNUM = 50;
	
	protected int count = 0;
	
	protected int score = 100;
	
	protected static AtomicInteger IDS = new AtomicInteger(1);
	protected List<Player> players = new ArrayList<Player>();
	public Map<Integer,PlayerPosition> player2Position = new HashMap<Integer,PlayerPosition>();
	public List<GameObject> npctemps = new ArrayList<GameObject>();
	public List<GameObject> npctemps2 = new ArrayList<GameObject>();
	List<Material> materials = new ArrayList<Material>();
	public Map<Integer,Integer> player2killCount = new HashMap<Integer,Integer>();
    Timer t = new Timer();
	
	public Menu menu;
	
	public boolean THIEF_STATE = false;
	
	public static long npcFreshPeriod = 20000; //刷NPC的时间间隔
	
	protected GameObject stolen;
	
	protected int id;
	
	protected int lastCheckCreateTime1;
	protected int lastCheckCreateTime2;
	
	public FeastInstance(VMap map){
		this.map = map;
		this.id = IDS.incrementAndGet();
		this.endTime = new Date(new Date().getTime()+FEASTDURINGTIME);
		this.stat = BEGIN;
		Server.server.getEventManager().registerListener(this);
		this.menu = getMenuRandom();
//		t.schedule(new TimerTask(){
//			public void run() {
				if(npctemps.size()>0)
				     removeNpc(npctemps);
				refreNpcs();
//			}
//		}, 0, npcFreshPeriod);
		
		t.schedule(new TimerTask(){
			public void run() {
				autoConsumption();
			}
		}, 0, 10*1000);
		
		t.schedule(new TimerTask(){
			public void run() {
				thiefAutoConsumption();
			}
		}, 0, 5*1000);
		
//		t.schedule(new TimerTask(){
//			public void run() {
				refreshThief();
//			}
//		}, 0, 2*60*1000);
	}
	
	public Menu getMenuRandom(){
		int index = manager.rnd.nextInt(manager.menuid2Menu.size());
		Menu me = manager.menuid2Menu.get(index);
		Menu mm = new Menu (me.menuId,me.menuName,me.majorMaterial);
		for(Material m : me.materials.values()){
			Material newMaterial = new Material(m.getId(),0,m.count);
			materials.add(newMaterial);
		}
		return mm;
}
	
	public void refreNpcs(){
//		removeNpc(npctemps);
		if(stat == BEGIN){
			for(Material m : materials){
				List<Npc> ns =manager.material2Npcs.get(m.getId());
				for(int i=0;i<ns.size()/2;i++){
					Npc n = ns.get(i);
					ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
					GameMapObject gmo = GameMapObject.findByID(proj, n.getId());
					GameObject npc0 = VMapUtil.addCreature(map, n.getX(), n.getY(),
							(GameMapNPC) gmo, true, 0, null);
					npctemps.add(npc0);
				}
			}
		}
	}
	
	public void refreNpcs2(){
		removeNpc(npctemps2);
		for(Material m : materials){
			List<Npc> ns =manager.material2Npcs.get(m.getId());
			for(int i=ns.size()/2;i<ns.size();i++){
				Npc n = ns.get(i);
				try {
					
					ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
					GameMapObject gmo = GameMapObject.findByID(proj, n.getId());
					GameObject npc0 = VMapUtil.addCreature(map, n.getX(), n.getY(),
							(GameMapNPC) gmo, true, 0, null);
					npctemps2.add(npc0);
				} catch (Exception e) {
					log.info("[MATERIALNAME]"+m.materialId+"[NPCID]"+n.npcId);
				}
			}
		}
	}
	
	public void addPlayer(Player player) throws VMapException {
		for(FeastInstance instance : manager.instances){
			Iterator<Player> it = instance.players.iterator();
			while(it.hasNext()){
				Player p = it.next();
				if(p.id==player.id){
					it.remove();
				}
			}
		}
		players.add(player);
		
	}
	
	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_UNIT_DIE,
				ServiceEvent.EVENT_PLAYER_LOGOUTED
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_UNIT_DIE:
			processDie((Unit)event.param1, (Unit)event.param2);
			break;
		case ServiceEvent.EVENT_PLAYER_LOGOUTED:
			playerLogouted((Player)event.param1);
			break;
		}
	}
	
	public void playerLogouted(Player player){
		
	}
	
	public void processDie(Unit u1,Unit u2){
		if(u1.type==GameObject.TYPE_CREATURE&&u2.type==GameObject.TYPE_PLAYER){
			Player player = ObjectAccessor.getPlayer(u2.id);
			if(player!=null){
				if(player.getVMap().instance!=null && player.getVMap().instance.getId()== id){
					boolean quit = false;
					for(int key : manager.material2Npcs.keySet()){
						List<Npc> npcs = manager.material2Npcs.get(key);
						if(npcs!=null && npcs.size()>0){
							for(Npc n:npcs){
								if(n.getId() == u1.id){
									Material material = getMaterialById(key);
									if(material!=null){
										int killCount = material.killCount;
										if(killCount<MATERIAL_MAXSHOWNUM){
										   material.killCount = killCount+1;
										}
										quit = true;
										break;
									}
								}
							}
						}
						if(quit)
							break;
					}
					if(StatService.isInArray(FeastInstanceService.THIEF_NPCID, u1.id)!=-1){
						THIEF_STATE = false;
						score += 3;
					}
					int count = 0;
					if(player2killCount.containsKey(player.id)){
						count = player2killCount.get(player.id);
					}
					count++;
					player2killCount.put(player.id, count);
				}
			}
		}
	}
	
	public Material getMaterialById(int materialId){
		for(Material m : materials){
			if(m.materialId == materialId){
				return m;
			}
		}
		return null;
	}
	
	/** 评分*/
	public int getScore(){
		int lose = 0;
	     for(Material material : materials){
	    	  int obtain = material.getKillCount();
	    	  int need = material.count;
	    	  if(need>=obtain){
	    		  lose += need - obtain;
	    	  }else{
	    		  int tempLost = obtain - need;
	    		  if(tempLost>=need){
	    			  tempLost = need;
	    		  }
	    		  lose += tempLost;
	    	  }
	    	  try{
	    	      log.info("[SCORERESULT]INS["+getId()+"]MATERIALID["+material.materialId+"]OBTAIN["+obtain+"]NEED["+need+"]");
	    	  }catch(Exception e){
	    		  
	    	  }
	     }
	     score = score - lose;
	     return score;
	}
	
	/** 移除NPC */
	protected void removeNpc(List<GameObject> npctemp) {
		for (GameObject npc : npctemp) {
			if (npc != null && npc.getVMap() != null) {
				npc.removeFromWorld();
			}
		}
	}
	
	/** 副本结束时移除场景内npc */
	protected void removeNpcs(){
		Iterator<GameObject> itor = map.instanceid2objects.iterator2();
		while (itor.hasNext()) {
			GameObject o = itor.next();
			if (o != null && o.getVMap() != null && o.type!=GameObject.TYPE_PLAYER) {
				o.removeFromWorld();
			}
		}
	}
	
	public int getId() {
		return id;
	}

	public VMap getMap(int mapId) {
		if(map!=null&&map.getId()==mapId){
			return map;
		}
		return null;
	}

	public String getName() {
		return map.mapDef.mapInfo.name;
	}

	public void loadingFinished(Player player) {
		
		
	}

	public void removePlayer(Player player) {
		Iterator<Player> it = players.iterator();
		while(it.hasNext()){
			Player p = it.next();
			if(p.id==player.id){
				it.remove();
			}
		}
	}
	
	
	/**
	 * 副本移除时传出玩家
	 */
	public void transPlayers(int score){
		Iterator<GameObject> itor = map.instanceid2objects.iterator2();
		MailService mailService = Server.server.getServiceRegistry().getMailService();
		while (itor.hasNext()) {
			GameObject o = itor.next();
			if(o.type==GameObject.TYPE_PLAYER){
				Player p = (Player)o;
				PlayerPosition po = player2Position.get(p.id);
				if(po!=null){
					try {
						int killCount = 0;
						if(player2killCount.containsKey(p.id)){
							killCount = player2killCount.get(p.id);
						}
						if(killCount>=1){
							PlayerTransaction tx = p.newTransaction("FEASTRESULT");
							p.addCredit(score, tx, true);
							int exp = Math.round(p.level*15*score*15/10);
						    p.addExp(exp, tx, true);
						    int addExp = Math.round(50*score/100);
						    if(p.cards!=null){
							    try {
									p.cards.addExp(addExp);
								} catch (Exception e) {
									
								}
						    }
						    tx.commit();
						    PlayerTransaction tx2 = p.newTransaction("FEASTRESULT");
						    if(!p.bag.addGameItem(ObjectAccessor.createGameItem(1311), 1, tx2, true)){
						    	tx2.rollback();
								mailService.sendSystemMailAsync(p.id, "满汉全席大赛组委会", "满汉全席珍珠奖励", "由于您的背包已满，满汉全席的珍珠奖励通过飞鸽发放", 0, 
										ObjectAccessor.createGameItem(1311), 1, "FEASTRESULT");
						    }else{
						    	tx2.commit();
						    }
						    String msg = MessageFormat.format("恭喜您完成了满汉全席的菜肴,您的菜品获得了{0}分，奖励您{1}经验,{2}战功,{3}卡片经验,请下次再接再厉", score,exp,score,addExp);
						    int count = sendSuipiancount();
						    if(count>0){
						    	PlayerTransaction tx3 = p.newTransaction("FEASTRESULT");
							    if(!p.bag.addGameItem(ObjectAccessor.createGameItem(FeastInstanceService.FEAST_SUIPIANITEM), count, tx3, true)){
							    	tx3.rollback();
									Server.server.getServiceRegistry().getMailService().sendSystemMailAsync(p.id, "满汉全席大赛组委会", "满汉全席碎片奖励", "由于您的背包已满，满汉全席的碎片奖励通过飞鸽发放", 0, 
											ObjectAccessor.createGameItem(FeastInstanceService.FEAST_SUIPIANITEM), count, "FEASTRESULT");
							    }else{
							    	tx3.commit();
							    }
							    msg = MessageFormat.format("恭喜您完成了满汉全席的菜肴,您的菜品获得了{0}分，奖励您{1}经验,{2}战功,{3}卡片经验,{4}个随从碎片,请下次再接再厉", score,exp,score,addExp,count);
						    }
							String message = MessageFormat.format("恭喜您完成了满汉全席菜肴，详情已通过飞鸽发放，请注意查收！",menu.menuName,score);
						    Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, message);
							mailService.sendSystemMailAsync(p.id, "满汉全席大赛组委会", "满汉全席奖励", msg, 0, 
									null, 0, "FEASTRESULT");
						}else{
							String message = MessageFormat.format("恭喜您完成了满汉全席菜肴，详情已通过飞鸽发放，请注意查收！",menu.menuName,score);
						    Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, message);
							mailService.sendSystemMailAsync(p.id, "满汉全席大赛组委会", "满汉全席奖励", "很遗憾,由于您在满汉全席活动期间没有达到足够的活跃度,所以您无法获得奖励,请下次再接再厉,满汉全席是需要大家共同努力才可以获得好的成绩哦.", 0, 
									null, 0, "FEASTRESULT");
						}
						p.goMap(po.mapId, po.x, po.y);
					} catch (VMapException e) {
						
					}
				}
			}
		}

	}
	
	public int sendSuipiancount(){
		if(score>=50 && score<=79){
			return 1;
		}else if(score>=80 && score<=99){
			return 2;
		}else if(score>=100){
			return 3;
		}
		return 0;
	}

	public void update(int diff) {
		if(Time.currTime-lastCheckCreateTime1>=npcFreshPeriod){
			if(npctemps.size()>0)
			     removeNpc(npctemps);
			refreNpcs();
			lastCheckCreateTime1=Time.currTime;
		}
		
		if(Time.currTime-lastCheckCreateTime2>=2*60*1000){
			refreshThief();
			lastCheckCreateTime2=Time.currTime;
		}
		
		if(endTime.getTime() - System.currentTimeMillis() <= 5 * 60 * 1000 && count == 0){
			count = 1;
			//发狮子吼
			Iterator<GameObject> itor = map.instanceid2objects.iterator2();
			while (itor.hasNext()) {
				GameObject o = itor.next();
				if(o.type==GameObject.TYPE_PLAYER){
				    if(o!=null){
				    	Player p = (Player)o;
				    	ChatService service= Server.server.getServiceRegistry().getChatService();
						String msg = "已经烹饪5分钟啦，大家努力啊！灶边的人注意看烹饪的火候啊！";
						service.sendPrivateShout(p.id, 0x0000ff, 6000, p.faction, msg);
				    }
				}
			}
		}
		if(endTime.getTime() - System.currentTimeMillis() <= 60*1000 && count == 1){
			count = 2;
			//发狮子吼
			Iterator<GameObject> itor = map.instanceid2objects.iterator2();
			while (itor.hasNext()) {
				GameObject o = itor.next();
				if(o.type==GameObject.TYPE_PLAYER){
				    if(o!=null){
				    	Player p = (Player)o;
				    	ChatService service= Server.server.getServiceRegistry().getChatService();
						String msg = "您还有一分钟来烹饪，请速速下锅啦！时间到了可是会传送出去的。";
						service.sendPrivateShout(p.id, 0x0000ff, 6000, p.faction, msg);
				    }
				}
			}
		}
		
		Date date = new Date();
		if(date.after(endTime) && stat==BEGIN){
			stat = END;
			if(menu!=null){
				score = getScore();
			}
			transPlayers(score);
			removeNpcs();
			npctemps.clear();
			npctemps2.clear();
			player2Position.clear();
			materials.clear();
			player2killCount.clear();
			t.cancel();
			t.purge();
			t = null;
			Server.server.getEventManager().unregisterListener(this);
			for(int i=0;i<manager.instances.size();i++){
				if(id == manager.instances.get(i).id){
					manager.instances.remove(i);
					i--;
				}
			}
		}

		if(map!=null){
			map.update(diff);
		}
	}
	
	/** 刷新盗贼Npc*/
	public void refreshThief(){
		 if(stolen!=null){
			 stolen.removeFromWorld();
			 THIEF_STATE=false;
		 }
		 if(stat == BEGIN){
			 int npcIndex = manager.rnd.nextInt(FeastInstanceService.THIEF_NPCID.length);
			 int npcId = FeastInstanceService.THIEF_NPCID[npcIndex];
			 int x = FeastInstanceService.THIEF_POSX[npcIndex];
			 int y = FeastInstanceService.THIEF_POSY[npcIndex];
			 ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
			 GameMapObject gmo = GameMapObject.findByID(proj, npcId);
			 GameObject npc0 = VMapUtil.addCreature(map, x, y,
			 (GameMapNPC) gmo, true, 0, null);
			 stolen = npc0;
			 THIEF_STATE = true;
		 }
	}
	
	
	 public void removeInstance(){
    	Iterator<FeastInstance> ite = manager.instances.iterator();
		while (ite.hasNext()) {
			FeastInstance instance = ite.next();
			if(id == instance.getId())
			     ite.remove();
		}
    }
	 
	 /** 自动消耗材料*/
	 public void autoConsumption(){
		if(materials!=null){
			for(Material m : materials){
				Material m2 = manager.getMaterial(m.getId());
				if(m2!=null){
				    int killCount = m.getKillCount();
				    int decCount = m2.getDeccount();
				    if(killCount>=decCount){
				    	m.setKillCount(killCount -decCount);
				    }
				}
			}
		}
	 }
	 
	 /** 贼偷去材料*/
	 public void thiefAutoConsumption(){
		 if(THIEF_STATE){
			 if(materials!=null&&materials.size()>0){
				List<Material> tempList = new ArrayList<Material>();
				for(Material l : materials){
					if(l.getKillCount()>0){
						tempList.add(l);
					}
				}
				if(tempList.size()>0){
					int index = manager.rnd.nextInt(tempList.size());
					Material ma = tempList.get(index);
					ma.setKillCount(ma.getKillCount()-1);
				}
			 }
		 }
	 }
}
