package peony.game.instance;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.Horse;
import peony.game.HorseUtil;
import peony.game.ItemUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.Unit;
import peony.game.attendant.Attendant;
import peony.game.attendant.AttendantFixService;
import peony.game.exp.ExpService;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.shop.NoItemShopBuy;
import peony.service.shop.ShopService;
import peony.util.IntHashMap;

/**
 * 副本扫荡
 * @author mfou
 *
 */

public class InstanceSweepService implements Service,ServiceEventListener{
	
	public IntHashMap<InstanceSweep> instanceSweeps = new IntHashMap<InstanceSweep>();
	public Map<Integer,List<Integer>> bossIds = new HashMap<Integer,List<Integer>>();
	public static final short TYPE_UNSWEEP = 0;//未扫荡
	public static final short TYPE_SWEEP = 1;//扫荡中
	public static final short TYPE_SWEEPED = 2;//已扫荡
	
	public static final int PAY_DECSWEEPTIME = 15;//缩短副本扫荡费用
	public static final int[] PAY_OPENSWEEP = {0,0,2,4,8,16,0};//扫荡需要的元宝
	
//	public static final int REWARDITEM_TIANLONG = 4133; //天龙扫荡装备奖励
	public static final int REWARDITEM_TIANLONG1 = 4286; //天龙扫荡装备奖励
	public static final int REWARDITEM_LUOYANG = 4132;  //洛阳扫荡装备奖励
	
	public static int REWARDCREDIT_TIANLONG = 70; //天龙扫荡战功奖励
	public static int REWARDCREDIT_LUOYANG = 100;  //洛阳扫荡战功奖励
	public static int REWARDCREDIT_YUANSHAO = 120;//袁绍扫荡战功奖励
	public static int REWARDATTENDANTEXP_YUANSHAO = 20;//袁绍扫荡随从经验奖励
	
	public static int RATE_TIANLONGBOX = 80; //获得天龙宝箱奖励的概率
	public static int RATE_TIANLONGBOX1 = 5; //获得天龙西凉首饰奖励的概率
	public static int RATE_LUOYANGBOX = 5;//获得洛阳宝箱奖励的概率
	

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
		
	}

	public void startup() throws Exception {
	   byte[] bytes = Server.server.getServiceRegistry().getDataService().data
	   .findFile("Areas/instancesweep.xml");
       Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
       parse(doc);
       Server.server.getEventManager().registerListener(this);
	}
	
	@SuppressWarnings("unchecked")
	public void parse(Document doc){
		Element root = doc.getRootElement();
		if (root != null) {
			List ins = root.elements("instance");
			for (int i = 0; i < ins.size(); i++) {
				int id = Integer.parseInt(((Element) ins.get(i))
						.attributeValue("id"));
				String name = ((Element) ins.get(i)).attributeValue("name");
				int time = Integer.parseInt(((Element) ins.get(i))
						.attributeValue("time"));
				String reward = ((Element) ins.get(i)).attributeValue("reward");
				int dayTimes = Integer.parseInt(((Element) ins.get(i))
						.attributeValue("daytimes"));
				int killBoss = Integer.parseInt(((Element) ins.get(i))
						.attributeValue("killboss"));
				int killBoss2 = Integer.parseInt(((Element) ins.get(i))
						.attributeValue("killboss2"));
				InstanceSweep insSweep = new InstanceSweep(id,name,time,reward,dayTimes);
				insSweep.killBoss.add(killBoss);
				insSweep.killBoss.add(killBoss2);
				instanceSweeps.put(id, insSweep);
				bossIds.put(id, insSweep.killBoss);
			}
		}
	}
	
    /*
     * 副本扫荡列表
     */
	public void instanceSweepList(Player p,int serial){
		Packet pt = new Packet(OpCode.INSTANCE_SWEEPLIST_SERVICE);
		pt.putInt(serial);
		pt.putInt(instanceSweeps.size());
		for(InstanceSweep is : instanceSweeps.values()){
			pt.putInt(is.id);
			pt.putString(is.getName());
			int sweepTimes = p.pool.getInt(InstanceSweepService.getPropertyOfDayTimes(is.id), 0);
			byte state = TYPE_UNSWEEP;
			int minute = is.time;
			if(sweepTimes>is.dayTimes){
				state = TYPE_SWEEPED;
			}
			if(p.sweepList.keySet().contains(is.id)){
				InstanceSweep sweep = p.sweepList.get(is.id);
				state = TYPE_SWEEP;
				minute = (int)((sweep.getEndTime() - Time.currentTimeMillis(Time.currTime))/(60*1000l));
			}
			pt.putInt(minute);
			pt.putString(is.reward);
			pt.put(state);
			pt.putInt(sweepTimes == 0?0:sweepTimes-1);
//			int itemPrice = (int)(Server.server.getServiceRegistry().getShopService().getItemPrice(NoItemShopBuy.YIYUANBAO)/36);
//			pt.putInt(InstanceSweepService.PAY_OPENSWEEP[sweepTimes]*itemPrice);
			ShopService shopService = Server.server.getServiceRegistry().getShopService();
			float price = (InstanceSweepService.PAY_OPENSWEEP[sweepTimes] * shopService.getItemPrice(NoItemShopBuy.YIYUANBAO))/36f;
			pt.putString(String.valueOf(price));
			pt.put(p.freeSweep.contains(is.id)?1:0);
		}
	    p.send(pt);
	}
	
	public InstanceSweep getSweepInstance(int instanceId){
		return instanceSweeps.get(instanceId);
	}
	
	
   public static String getPropertyOfDayTimes(int id){
	   return "SWEEPTIMES_PERDAY_OF"+String.valueOf(id);
   }

	public int[] getEventTypes() {
		return new int[] {
				ServiceEvent.EVENT_UNIT_DIE,
				ServiceEvent.EVENT_PLAYER_LOGOUTED,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_UNIT_DIE:
			unitDie((Unit) event.param1, (Unit) event.param2);
			break;
		case ServiceEvent.EVENT_PLAYER_LOGOUTED:
			playerLogout((Player) event.param1);
			break;
		}
	}
	
	public void playerLogout(Player p){
		if(p!=null && p.sweepList!=null && p.sweepList.size()>0){
			p.sweepList.clear();
		}
	}
	
	public void unitDie(Unit dieUnit,Unit killUnit){
		if(dieUnit.type==GameObject.TYPE_CREATURE &&(killUnit.type==GameObject.TYPE_PLAYER||killUnit.type==GameObject.TYPE_ATTENDANT)){
			Player player = null;
			if(killUnit.type == GameObject.TYPE_ATTENDANT){
				Attendant att = (Attendant)killUnit;
				if(att!=null){
					player = att.owner;
				}
			}else{
				player = (Player)killUnit;
			}
			Server.server.getServiceRegistry().getDbService().
	        schedule(new InstanceSweepDieCall(player==null?null:player.session, dieUnit,killUnit));
		}
	}

	public int getInstanceId(int dieBossId){
		for(int key : bossIds.keySet()){
			List<Integer> values = bossIds.get(key);
			for(Integer value : values){
				if(value == dieBossId){
					return key;
				}
			}
		}
		return -1;
	}
	
	public boolean isInstanceBoss(int bossId){
		return bossIds.values().contains(bossId);
	}
	
	public synchronized void initInstanceTimes(Player p){
	   for(InstanceSweep is : instanceSweeps.values()){
	      int sweepTimes = p.pool.getInt(InstanceSweepService.getPropertyOfDayTimes(is.id), 0);
	      if(sweepTimes!=0){
	    	  p.pool.setInt(InstanceSweepService.getPropertyOfDayTimes(is.id), 1);
	      }
	      p.freeSweep.clear();
	   }
	}
	
	public void getReward(Player p,int instanceId,String name){
		try{
			if(instanceId == 1){
				Horse h =p.horse;
				if(h == null){
					if(p.horseBag.horses!=null && p.horseBag.horses.size()>0){
						h = p.horseBag.horses.get(0);
					}
				}
				if(h!=null){
					int addHorseExp = (int)(ExpService.doubaohorseexp[h.level]*2.5);
					int horseUpLevel = HorseUtil.getUpLevel(h.level, addHorseExp+h.exp) + h.level;
					if(horseUpLevel <= Player.MAX_LEVEL && horseUpLevel<=p.level){
				       h.setExp(h.exp + addHorseExp, p, "INSTANCESWEEP");
					}
					Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id,MessageFormat.format("恭喜您已经成功扫荡了{0}，获得了{1}坐骑经验", name,addHorseExp));
				}
			}else {
				PlayerTransaction tx = p.newTransaction("INSTANCESWEEP");
				if(instanceId == 0){
					int addValue = (int)(ExpService.onlineExps[p.level]*2.5);
					p.addExp(addValue, tx, true);
					tx.commit();
					int expLock = p.pool.getInt(Player.PROPERTY_LOCK_EXP, Player.EXP_UNLOCK);  //如果玩家锁住经验，经验不增长
					if(expLock == Player.EXP_UNLOCK)
					   Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id,MessageFormat.format("恭喜您已经成功扫荡了{0}，获得了{1}经验", name,addValue));
					else
					   Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id,"由于您锁定了人物经验，本次扫荡不能获得经验。您可以前往都城找官职管理员进行修改");
						
				}else if(instanceId == 2){
					p.addCredit(REWARDCREDIT_TIANLONG, tx, true);
					tx.commit();
					int rndNum = ItemUtil.rnd.nextInt(100);
//					if(rndNum<RATE_TIANLONGBOX){
//					   itemReward(p,REWARDITEM_TIANLONG,name);
					   if(rndNum<RATE_TIANLONGBOX1){
						   itemReward(p,REWARDITEM_TIANLONG1,name);
						   Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id,"恭喜你已经成功扫荡天龙副本，获得了70战功、西凉首饰盒。");
					   }else{
						   Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id,"恭喜你已经成功扫荡天龙副本，获得了70战功。");
					   }
//					   GameItem item = ObjectAccessor.createGameItem(REWARDITEM_TIANLONG);
//						if(item != null){
//						    Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id,"恭喜你已经成功扫荡天龙副本，获得了70战功、无畏装备箱和西凉首饰盒。");
//						}
//					}else{
//						Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id,MessageFormat.format("恭喜您已经成功扫荡了{0}，获得了{1}战功的奖励", name,REWARDCREDIT_TIANLONG));
//					}
				}else if(instanceId == 3){
					p.addCredit(REWARDCREDIT_LUOYANG, tx, true);
					tx.commit();
					int rndNum = ItemUtil.rnd.nextInt(100);
					if(rndNum < RATE_LUOYANGBOX){
						itemReward(p,REWARDITEM_LUOYANG,name);
						GameItem item = ObjectAccessor.createGameItem(REWARDITEM_LUOYANG);
						if(item != null){
						    Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id,MessageFormat.format("恭喜您已经成功扫荡了{0}，获得了{1}战功和{2}的奖励", name,REWARDCREDIT_LUOYANG,item.template.name));
						}
					}else{
						Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id,MessageFormat.format("恭喜您已经成功扫荡了{0}，获得了{1}战功的奖励", name,REWARDCREDIT_LUOYANG));
					}    
				}else if(instanceId == 4){
					p.addCredit(REWARDCREDIT_YUANSHAO, tx, true);
					AttendantFixService.addAttExp(p, REWARDATTENDANTEXP_YUANSHAO);
					tx.commit();
					Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id,MessageFormat.format("恭喜您已经成功扫荡了{0}，获得了{1}战功和{2}随从经验的奖励", name,REWARDCREDIT_YUANSHAO,REWARDATTENDANTEXP_YUANSHAO));
				}else{
				    tx.rollback();
				}
			}
		}catch(Exception e){
			
		}
	}
	
	public void itemReward(Player p,int itemId,String name){
		PlayerTransaction tx = p.newTransaction("INSTANCESWEEP");
		GameItem item = ObjectAccessor.createGameItem(itemId);
		if(item != null){
			try {
				p.bag.addGameItemComplete(item, 1, tx, true);
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				Server.server.getServiceRegistry().getMailService()
				.sendSystemMail(p.id, peony.Messages.STRING_00004,"扫荡奖励", MessageFormat.format("扫荡{0}后的奖励", name), 0,
						item, 1, "INSTANCESWEEP");
			}
		}
	}
   
}

