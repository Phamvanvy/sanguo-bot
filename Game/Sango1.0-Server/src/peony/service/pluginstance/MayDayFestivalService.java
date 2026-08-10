package peony.service.pluginstance;


import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.NoInstanceVMapManager;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.ReliveOption;
import peony.game.ReliveOptions;
import peony.game.Server;
import peony.game.Time;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.VMapException;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

public class MayDayFestivalService implements Service, ServiceEventListener{
	
	public static int MAYDAY_MAP = 2144;//五一活动地图
	protected static int DURATION = 5 * 60 * 1000;//活动持续时间
	public static int MAYDAY_OUTMAP = 2032;//传出地图
	public static int[] MAYDAY_OUTPOINT = {654,824};//传出点坐标
	public static int MAYDAY_DIEREWARD = 2766; //角色死亡10次后被传出地图给次物品
	public static int DIE_COUNT = 10;//死亡次数
	public static int ENTER_COUNT = 5;//单日场景进入次数
	public static int MAY_ENTERITEM = 4193;
	public static int[] MAYDAY_OUTPOINT_WEI = {136,173};//魏国传出点坐标
	public static int[] MAYDAY_OUTPOINT_SHU = {401,291};//蜀国传出点坐标
	public static int[] MAYDAY_OUTPOINT_WU = {195,388};//吴国传出点坐标
	
	public static boolean canEnter = true; //进入场景开关
	
	public static final String PROPERTY_MAYDAY_OUT = "maydayout";//五一提示信息标志
	public static final String PROPERTY_DIECOUNTIN_MAYDAY = "maydaydiecount";//死亡次数
	public static final String PROPERTY_MAYDAY_ENTERTIME = "maydayentertime";//地图进入时间
	public static final String PROPERTY_MAYDAY_ENTERTIMES = "maydayentertimes";//单日进入地图次数
	
	public int lastCheckTime = 0;
	
	public void initState(Player p){
		try{
			if(p.map.map.getId() == MAYDAY_MAP){
				p.pool.remove(PROPERTY_MAYDAY_OUT);
				p.pool.remove(PROPERTY_MAYDAY_ENTERTIME);
				p.pool.remove(PROPERTY_DIECOUNTIN_MAYDAY);
			}
		}catch(Exception e){
			
		}
	}
	
	public synchronized void initEnterTime(Player p){
		int times = p.pool.getInt(PROPERTY_MAYDAY_ENTERTIMES, 0);
		if(times==0){
			p.pool.remove(PROPERTY_MAYDAY_ENTERTIMES);
		}else{
			p.pool.setInt(PROPERTY_MAYDAY_ENTERTIMES, 0);
		}
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
		
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_MAP_PLAYER_ADDED,
				ServiceEvent.EVENT_MAP_PLAYER_REMOVED,
				ServiceEvent.EVENT_UNIT_DIE
				
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_MAP_PLAYER_ADDED:
			processPlayerGoMap((VMap)event.param1, (Player)event.param2);
			break;
		case ServiceEvent.EVENT_UNIT_DIE:
			processUnitDie((Unit)event.param1,(Unit)event.param2);
			break;
		}
	}
	
	public void processPlayerGoMap(VMap map,Player p){
		if(map!=null && p!=null && map.getId() == MAYDAY_MAP){
		    int type = 0;
			int enterTime = p.pool.getInt(PROPERTY_MAYDAY_ENTERTIME, 0);
		    if(enterTime==0){
		    	type = 1;
		    	enterTime = p.pool.setInt(PROPERTY_MAYDAY_ENTERTIME, Time.currTime);
		    	int times = p.pool.getInt(PROPERTY_MAYDAY_ENTERTIMES, 0);
		    	if(times<ENTER_COUNT){
		    		times++;
		    		p.pool.setInt(PROPERTY_MAYDAY_ENTERTIMES, times);
		    	}
		    }
			int dur = Time.currTime - enterTime;
//			if(type == 0 && (dur>=DURATION ||dur<0)){
//				try {
//					p.goMap(MAYDAY_OUTMAP,  MAYDAY_OUTPOINT[0], MAYDAY_OUTPOINT[1]);
//					p.pool.remove(PROPERTY_MAYDAY_OUT);
//					p.pool.remove(PROPERTY_MAYDAY_ENTERTIME);
//					p.pool.remove(PROPERTY_DIECOUNTIN_MAYDAY);
//				} catch (VMapException e) {
//				
//				}
//			}
			if(dur <=DURATION && type==1){
				int count = p.pool.getInt(PROPERTY_DIECOUNTIN_MAYDAY, 0);
				if(count != 0){
					p.pool.setInt(PROPERTY_DIECOUNTIN_MAYDAY, 0);
				}
				Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, 
						"欢迎勇士来到发财者的仙园，赶快开启您的寻宝旅程吧，要记住寻宝只有5分钟哦！");
			}
		}
	}
	
	public static void isTimeOut(Player p){
		int enterTime = p.pool.getInt(PROPERTY_MAYDAY_ENTERTIME, 0);
		int dur = Time.currTime - enterTime;
		if(enterTime!=0 && (dur>=DURATION ||dur<0)){
			try {
				Server.server.getWorld().addPlayerToMap(p, MAYDAY_OUTMAP, MAYDAY_OUTPOINT[0], MAYDAY_OUTPOINT[1],true);
				p.pool.remove(PROPERTY_MAYDAY_OUT);
				p.pool.remove(PROPERTY_MAYDAY_ENTERTIME);
				p.pool.remove(PROPERTY_DIECOUNTIN_MAYDAY);
			} catch (VMapException e) {
			
			}
		}else{
			try {
				Server.server.getWorld().addPlayerToMap(p, p.map.id, p.x, p.y,true);
			} catch (VMapException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void processUnitDie(Unit u1,Unit u2){
		if(u1 instanceof Player && u1.map.map.getId() == MAYDAY_MAP){
		   Player player = (Player)u1;
		   if(player!=null){
			   int count = player.pool.getInt(PROPERTY_DIECOUNTIN_MAYDAY, 0);
			   count++;
			   player.pool.setInt(PROPERTY_DIECOUNTIN_MAYDAY, count);
			   if(count >= DIE_COUNT){
				   try {
						player.goMap(MAYDAY_OUTMAP,  MAYDAY_OUTPOINT[0], MAYDAY_OUTPOINT[1]);
						player.pool.remove(PROPERTY_MAYDAY_OUT);
						player.pool.remove(PROPERTY_MAYDAY_ENTERTIME);
						player.pool.remove(PROPERTY_DIECOUNTIN_MAYDAY);
						PlayerTransaction tx = player.newTransaction("MAYDAY");
						GameItem rewardItem = ObjectAccessor.createGameItem(MAYDAY_DIEREWARD);
						try {
							player.bag.addGameItemComplete(rewardItem, 1, tx, true);
							tx.commit();
						} catch (Exception e) {
							tx.rollback();
							Server.server.getServiceRegistry().getMailService()
									.sendSystemMail(player.id, peony.Messages.STRING_00004, "元宵奖励", "闹元宵奖励", 0,rewardItem, 1, "MAYDAY");
						}
						Server.server.getServiceRegistry().getChatService()
						                    .sendPrivateMessage(player.id, "您由于死亡次数太多送出活动场景。有道是富贵险中求，祝您下次寻宝财运亨通。");
					} catch (VMapException e) {
						e.printStackTrace();
					}
			   }
		   }
		}
	}
	
	public static boolean gameOver(Player player){
		if(player.getVMap().getId() == MAYDAY_MAP){
			int count = player.pool.getInt(PROPERTY_DIECOUNTIN_MAYDAY, 0);
			if(count >= DIE_COUNT){
				return true;
			}
		}
		return false;
	}
	
	public void update(){
		if(Time.currTime - lastCheckTime>=30*1000){
			lastCheckTime = Time.currTime;
			NoInstanceVMapManager manager = (NoInstanceVMapManager) Server.server.getWorld().getVMapManager(MAYDAY_MAP);
			VMap[] maps = manager.getVMaps(MAYDAY_MAP);
			for(int i=0;i<maps.length;i++){
				VMap map = maps[i];
				if(map!=null){
					for(GameObject go : map.instanceid2objects.values()){
						if(go!=null && go.type==GameObject.TYPE_PLAYER){
							Player player = (Player)go;
							checkTimeout(player);
						}
					}
				}
			}
		}
	}
	
	/**
	 * 检测时间过期
	 * @param player
	 */
	public void checkTimeout(Player player){
		int enterTime = player.pool.getInt(PROPERTY_MAYDAY_ENTERTIME, 0);
		if(enterTime>0 && Time.currTime-enterTime>=DURATION-60000 && player.pool.getInt(PROPERTY_MAYDAY_OUT, 0)==0){
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, 
					"您还有1分钟开宝箱的时间，到时候您是会被无情滴传送到南海的哦。");
			player.pool.setInt(PROPERTY_MAYDAY_OUT, 1);
		} else if(enterTime == 0 || Time.currTime - enterTime>=DURATION){
			try {
				player.goMap(MAYDAY_OUTMAP,  MAYDAY_OUTPOINT[0], MAYDAY_OUTPOINT[1]);
				player.pool.remove(PROPERTY_MAYDAY_OUT);
				player.pool.remove(PROPERTY_MAYDAY_ENTERTIME);
				player.pool.remove(PROPERTY_DIECOUNTIN_MAYDAY);
			} catch (VMapException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean checkMayDayEntertime(Player player){
		if(player!=null){
			int times = player.pool.getInt(PROPERTY_MAYDAY_ENTERTIMES, 0);
	    	if(times<ENTER_COUNT){
	    		return true;
	    	}
		}
		return false;
	}
	
	public void checkTimes(Player player,int count){
		if(count>=DIE_COUNT){
			player.reliveOptions = new ReliveOptions(Time.currTime + 60 * 1000);
			ReliveOption option = new ReliveOption(ReliveOption.NORMAL, peony.Messages.STRING_00962, 14,
					MAYDAY_OUTMAP, MAYDAY_OUTPOINT[0], MAYDAY_OUTPOINT[1]);
			player.reliveOptions.addOption(option, false);
			player.send(player.reliveOptions.getRelivePacket());
			PlayerTransaction tx = player.newTransaction("MAYDAY");
			GameItem rewardItem = ObjectAccessor.createGameItem(MAYDAY_DIEREWARD);
			try {
				player.bag.addGameItemComplete(rewardItem, 1, tx, true);
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				Server.server.getServiceRegistry().getMailService()
						.sendSystemMail(player.id, peony.Messages.STRING_00004, "劳动节活动奖励", "劳动节活动奖励", 0,
								rewardItem, 1, "MADAY");
			}
			player.pool.remove(PROPERTY_MAYDAY_OUT);
			player.pool.remove(PROPERTY_MAYDAY_ENTERTIME);
			player.pool.remove(PROPERTY_DIECOUNTIN_MAYDAY);
		}
	}
	
	public int[] relivePoint(Player p){
		int count = p.pool.getInt(PROPERTY_DIECOUNTIN_MAYDAY, 0);
		int[] ret = new int[3];
		if(count >= DIE_COUNT){
			ret[0] = MAYDAY_OUTMAP;
			ret[1] = MAYDAY_OUTPOINT[0];
			ret[2] = MAYDAY_OUTPOINT[1];
			p.pool.remove(PROPERTY_MAYDAY_OUT);
			p.pool.remove(PROPERTY_MAYDAY_ENTERTIME);
			p.pool.remove(PROPERTY_DIECOUNTIN_MAYDAY);
		} else if(p.faction ==1){
			ret[0] = MAYDAY_MAP;
			ret[1] = MAYDAY_OUTPOINT_WEI[0];
			ret[2] = MAYDAY_OUTPOINT_WEI[1];
		} else if(p.faction ==2){
			ret[0] = MAYDAY_MAP;
			ret[1] = MAYDAY_OUTPOINT_SHU[0];
			ret[2] = MAYDAY_OUTPOINT_SHU[1];
		}else if(p.faction ==3){
			ret[0] = MAYDAY_MAP;
			ret[1] = MAYDAY_OUTPOINT_WU[0];
			ret[2] = MAYDAY_OUTPOINT_WU[1];
		}
		return ret;
	}
	
}
