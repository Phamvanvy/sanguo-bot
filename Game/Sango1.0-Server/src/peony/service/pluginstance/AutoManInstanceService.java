package peony.service.pluginstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import peony.game.Creature;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.VMap;
import peony.game.instance.NormalInstance;
import peony.game.instance.NormalVMapManager;
import peony.game.mail.MailService;
import peony.service.Service;

/**
 * "Ð¡¹ÖÊÞ´óÕ½°¼Í¹Âü"
 * @author dchen
 */
public class AutoManInstanceService implements Service {

	public static int[] maps = {1712, 1776, 1792};
	public static int[] bosses = {7012352, 7274496, 7340032};
	public static int winnerItem = 3626;
	public static int loseItem = 3627;
	protected long lastUpdateTime;
	protected static long gameTime = 5 * 60 * 1000L;
	protected Map<Integer, Boolean> instanceStat = new HashMap<Integer, Boolean>();
	
	public void startup() throws Exception {
		
	}
	
	public void update(){
		try {
			if(System.currentTimeMillis()-lastUpdateTime>5000){
				for(int i=0;i<maps.length;i++){
					int mapId = maps[i];
					NormalVMapManager manager = (NormalVMapManager) Server.server.getWorld().getVMapManager(mapId);
					List<VMap> maps = manager.getMaps(mapId);
					for(VMap map : maps){
						if(map!=null && map.instance!=null){
							NormalInstance instance = (NormalInstance) map.instance;
							if(instanceStat.get(instance.id)==null || !instanceStat.get(instance.id)){
								int bossId = bosses[i];
								int instanceCreateTime = instance.createTime;
								Creature creature = map.getCreatureById(bossId);
								if(Time.currTime-instanceCreateTime<=gameTime){
									if(creature==null || !creature.isAlive()){
										result(map, false);
										instanceStat.put(instance.id, true);
										NormalInstance.out(map);
									}
								}else if(creature!=null && creature.isAlive()){
									result(map, true);
									instanceStat.put(instance.id, true);
									NormalInstance.out(map);
								}
							}
							if(Time.currTime-instance.createTime>gameTime || (instanceStat!=null 
									&& instanceStat.get(instance.id)!=null && instanceStat.get(instance.id))){
								NormalInstance.out(map);
							}
							if(Time.currTime-instance.createTime>instance.definition.refreshSecond*1000L){
								instance.timeOut = true;
							}
						}
					}
				}
				lastUpdateTime = System.currentTimeMillis();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void result(VMap map, boolean winner){
		if(map!=null){
			for(int faction=GameObject.FACTION_WEI;faction<=GameObject.FACTION_WU;faction++){
				List<Player> players = map.getPlayersByFaction(faction);
				for(Player player : players){
					if(player!=null){
						PlayerTransaction tx = player.newTransaction("AUTOMANINSTANCE");
						GameItem item = ObjectAccessor.createGameItem(winnerItem);
						if(!winner)
							item = ObjectAccessor.createGameItem(loseItem);
						try {
							player.bag.addGameItemComplete(item, 1, tx, false);
							tx.commit();
						} catch (NoEnoughSpaceException e) {
							tx.rollback();
							MailService service = Server.server.getServiceRegistry().getMailService();
							service.sendSystemMail(player.id, peony.Messages.STRING_00004, peony.Messages.STRING_01195, "", 0, item, 1, "MOUSEINSTANCE");
						}
						if(winner){
							Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, 
							peony.Messages.STRING_01196);
						}else{
							Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, 
							peony.Messages.STRING_01197);
						}
					}
				}
			}
		}
	}

	public void shutdown() {
		
	}

}
