package peony.service.pluginstance;

import peony.game.CreatureDieCallback;
import peony.game.DieCallback;
import peony.game.GameItem;
import peony.game.GameMapDefinition;
import peony.game.GameObject;
import peony.game.MoveCallback;
import peony.game.NoInstanceVMapManager;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapManager;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

/**
 * 元宵节活动
 * @author mfou
 *
 */

public class LanternFestivalService implements Service, ServiceEventListener,VMapManager{
	
	public static int LANTERN_MAP = 2112;//元宵活动地图
	protected static int DURATION = 15 * 60 * 1000;//活动持续时间
	public static int LANTERN_OUTMAP = 2016;//传出地图
	public static int[] LANTERN_OUTPOINT = {750,480};//传出点坐标
	public static int[] LANTERN_OUTPOINT_WEI = {29,374};//魏国传出点坐标
	public static int[] LANTERN_OUTPOINT_SHU = {148,168};//蜀国传出点坐标
	public static int[] LANTERN_OUTPOINT_WU = {569,292};//吴国传出点坐标
	public static int LANTERN_DIEREWARD = 4084; //角色死亡10次后被传出地图给次物品
	public static int DIE_COUNT = 10;//死亡次数
	public int lastCheckTime = 0;
	

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
		
	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
		
	}
	
	public void processPlayerGoMap(VMap map,Player p){
		if(map!=null && p!=null && map.getId() == LANTERN_MAP){
		    int type = 0;
			int enterTime = p.pool.getInt(Player.PROPERTY_LANTERN_ENTERTIME, 0);
		    if(enterTime==0){
		    	type = 1;
		    	enterTime = p.pool.setInt(Player.PROPERTY_LANTERN_ENTERTIME, Time.currTime);
		    }
			int dur = Time.currTime - enterTime;
			if(dur <=DURATION && type==1){
				int count = p.pool.getInt(Player.PROPERTY_DIECOUNTIN_LANTERN, 0);
				if(count != 0){
					p.pool.setInt(Player.PROPERTY_DIECOUNTIN_LANTERN, 0);
				}
				Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, 
						"您在闹元宵喜乐场可以享受到每15秒一次的战功+经验雨，在该场景杀敌更可获得花灯，不过一次只能呆15分钟哦。");
			}
		}
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
	
	public void processUnitDie(Unit u1,Unit u2){
		if(u1 instanceof Player && u1.map.map.getId() == LANTERN_MAP){
		   Player player = (Player)u1;
		   if(player!=null){
			   int count = player.pool.getInt(Player.PROPERTY_DIECOUNTIN_LANTERN, 0);
			   count++;
			   player.pool.setInt(Player.PROPERTY_DIECOUNTIN_LANTERN, count);
		   }
		}
	}
	
	public int[] relivePoint(Player p){
		int count = p.pool.getInt(Player.PROPERTY_DIECOUNTIN_LANTERN, 0);
		int[] ret = new int[3];
		if(count >= DIE_COUNT){
			ret[0] = LANTERN_OUTMAP;
			ret[1] = LANTERN_OUTPOINT[0];
			ret[2] = LANTERN_OUTPOINT[1];
			p.pool.remove(Player.PROPERTY_LANTERN_OUT);
			p.pool.remove(Player.PROPERTY_LANTERN_ENTERTIME);
			p.pool.remove(Player.PROPERTY_DIECOUNTIN_LANTERN);
		    if(count >= DIE_COUNT){
				PlayerTransaction tx = p.newTransaction("LANTERN");
				GameItem rewardItem = ObjectAccessor.createGameItem(LANTERN_DIEREWARD);
				try {
					p.bag.addGameItemComplete(rewardItem, 1, tx, true);
					tx.commit();
				} catch (Exception e) {
					tx.rollback();
					Server.server.getServiceRegistry().getMailService()
							.sendSystemMail(p.id, peony.Messages.STRING_00004, "元宵奖励", "闹元宵奖励", 0,
									rewardItem, 1, "LANTERN");
				}	
		   }
		} else if(p.faction ==1){
			ret[0] = LANTERN_MAP;
			ret[1] = LANTERN_OUTPOINT_WEI[0];
			ret[2] = LANTERN_OUTPOINT_WEI[1];
		} else if(p.faction ==2){
			ret[0] = LANTERN_MAP;
			ret[1] = LANTERN_OUTPOINT_SHU[0];
			ret[2] = LANTERN_OUTPOINT_SHU[1];
		}else if(p.faction ==3){
			ret[0] = LANTERN_MAP;
			ret[1] = LANTERN_OUTPOINT_WU[0];
			ret[2] = LANTERN_OUTPOINT_WU[1];
		}
		return ret;
	}
	
	public void initState(Player p){
		try{
			if(p.map.map.getId() == LANTERN_MAP){
				p.pool.remove(Player.PROPERTY_LANTERN_OUT);
				p.pool.remove(Player.PROPERTY_LANTERN_ENTERTIME);
				p.pool.remove(Player.PROPERTY_DIECOUNTIN_LANTERN);
			}
		}catch(Exception e){
			
		}
	}
	
	public void update(){
		if(Time.currTime - lastCheckTime > 1000 * 30){
			lastCheckTime = Time.currTime;
			NoInstanceVMapManager manager = (NoInstanceVMapManager) Server.server.getWorld().getVMapManager(LANTERN_MAP);
			VMap[] maps = manager.getVMaps(LANTERN_MAP);
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
		int enterTime = player.pool.getInt(Player.PROPERTY_LANTERN_ENTERTIME, 0);
		if(Time.currTime-enterTime>=DURATION-60000 && player.pool.getInt(Player.PROPERTY_LANTERN_OUT, 0)==0){
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, 
					"您还有1分钟享受战功经验雨的时间，不过杀敌就可以获得花灯换取大奖，快快杀敌吧。");
			player.pool.setInt(Player.PROPERTY_LANTERN_OUT, 1);
		} else if(Time.currTime - enterTime>=DURATION){
			try {
				player.goMap(LANTERN_OUTMAP,  LANTERN_OUTPOINT[0], LANTERN_OUTPOINT[1]);
				player.pool.remove(Player.PROPERTY_LANTERN_OUT);
				player.pool.remove(Player.PROPERTY_LANTERN_ENTERTIME);
				player.pool.remove(Player.PROPERTY_DIECOUNTIN_LANTERN);
			} catch (VMapException e) {
				e.printStackTrace();
			}
		}
	}

	public VMap addToMap(Player player, int mapId, int x, int y, boolean check)
			throws VMapException {
		if(check){
			int[] outPoint =relivePoint(player);
			if(outPoint!=null){
			    return Server.server.getWorld().addPlayerToMap(player, LANTERN_OUTMAP, LANTERN_OUTPOINT[0], LANTERN_OUTPOINT[1], true);
			} else if(player.faction == 1){
				return Server.server.getWorld().addPlayerToMap(player, LANTERN_MAP, LANTERN_OUTPOINT_WEI[0], LANTERN_OUTPOINT_WEI[1], true);
			}else if(player.faction == 2){
				return Server.server.getWorld().addPlayerToMap(player, LANTERN_MAP, LANTERN_OUTPOINT_SHU[0], LANTERN_OUTPOINT_SHU[1], true);
			}else if(player.faction == 3){
				return Server.server.getWorld().addPlayerToMap(player, LANTERN_MAP, LANTERN_OUTPOINT_WU[0], LANTERN_OUTPOINT_WU[1], true);
			}
		}
		return null;
	}

	public CreatureDieCallback creatureDieCallback() {
		return null;
	}

	public DieCallback dieCallback() {
		return null;
	}

	public void mapChanged(GameMapDefinition mapDef) {
		
	}

	public MoveCallback moveCallback() {
		return null;
	}

	public void outPrison(Player p) {
		
	}

	public void removeFromMap(Player player) {
		
	}

	public void update(int diff) {
		
	}
}
