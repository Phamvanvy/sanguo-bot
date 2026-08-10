package peony.service.expansionbattle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import peony.game.CreatureDieCallback;
import peony.game.DieCallback;
import peony.game.GameMapDefinition;
import peony.game.LogUtil;
import peony.game.MoveCallback;
import peony.game.NoInstanceVMapManager;
import peony.game.Player;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapManager;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.util.TimeUtil;

/**
 * 资料片战役管理器
 * @author dchen
 */
public class ExpansionService implements Service, VMapManager {

	protected List<ExpansionInstance> instances = new ArrayList<ExpansionInstance>();
	protected ExpansionConfig config;
	public VMap relationMap = null;

	public void startup() throws Exception {
		config = new ExpansionConfig();
		config.initConfig();
		ExpansionNation wei = new ExpansionNation(1);
		ExpansionNation shu = new ExpansionNation(2);
		ExpansionNation wu = new ExpansionNation(3);
		VMap map = ((NoInstanceVMapManager) Server.server.getWorld().
				getVMapManager(config.mapId)).getVMaps(config.mapId)[0];
		Server.server.getWorld().addVMapManager(this);
		Server.server.getWorld().registerVMapManager(config.mapId, this);
		map.manager = this;
		ExpansionInstance instance = new ExpansionInstance(this, map, wei, shu, wu, config);
		instances.add(instance);
		relationMap = map;
		timeHandle();
	}
	
	protected void timeHandle(){
		for(final ExpansionInstance instance : instances){
			ExpansionConfig config = instance.config;
			List<ExpansionPeriod> periods = config.periods;
			for(ExpansionPeriod p : periods){
				if(inTime(p.startHour, p.startMin, p.endHour, p.endMin)){
					instance.openInstance();
				}
				Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
					public void run() {
						Server.server.syncRunner.add(new Runnable(){
							public void run() {
								instance.openInstance();
							}
						});
					}
				}, TimeUtil.getScheduleTimeMills(new Date(), p.startHour, p.startMin), p.duration, TimeUnit.MILLISECONDS);
				Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
					public void run() {
						Server.server.syncRunner.add(new Runnable(){
							public void run() {
								instance.closeInstance();
								LogUtil.logExpansionBattleEnd(-1, "TIMEOUT");
							}
						});
					}
				}, TimeUtil.getScheduleTimeMills(new Date(), p.endHour, p.endMin), p.duration, TimeUnit.MILLISECONDS);
			}
		}
	}

	public VMap addToMap(Player player, int mapId, int x, int y, boolean check)
			throws VMapException {
		for(ExpansionInstance instance : instances){
			if(instance.map.getId()==mapId){
				instance.addPlayer(player);
				player.removeFromMap();
				instance.map.addPlayer(player, x, y);
				return instance.map;
			}
		}
		throw new VMapException(peony.Messages.STRING_00776);
	}
	
	public List<VMap> getMaps(int mapId){
    	List<VMap> maps = new ArrayList<VMap>();
    	for(ExpansionInstance instance : instances){
    		if(instance!=null && instance.getMap(mapId)!=null){
    			maps.add(instance.getMap(mapId));
    		}
    	}
    	return maps;
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
		if(p.map.map!=null){
		    int[] pos = p.map.map.mapDef.mapInfo.getPathFinder().tryOutPrison(p.x, p.y);
		    if(pos==null){
				int[] relivePoint = p.map.map.getRelivePoint(p.faction);
				try{
					int oldMapId = p.map.map.getId();
					int oldX = p.x;
					int oldY = p.y;
					p.goMap(relivePoint[0], relivePoint[1], relivePoint[2]);
					Server.server.getEventManager().fireEvent(
						new ServiceEvent(ServiceEvent.EVENT_PLAYER_OUTPRISON_RELIVEPOINT,
						p,oldMapId,oldX,oldY));
				}catch(VMapException e) {
				}
			}else{
			    try{
					p.goMap(p.map.map.getId(), pos[0], pos[1]);
				}catch (VMapException e) {
				}
			}
		}
	}

	public void removeFromMap(Player player) {
		player.getVMap().instance.removePlayer(player);
	}

	public void update(int diff) {
		for(ExpansionInstance instance : instances){
			instance.update(diff);
		}
	}
	
	public void shutdown() {
		
	}
	
	public boolean inTime(int sHour, int sMin, int eHour, int eMin){
		Calendar cal = Calendar.getInstance();
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(new Date());
		cal1.set(Calendar.HOUR_OF_DAY, sHour);
		cal1.set(Calendar.MINUTE, sMin);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(new Date());
		cal2.set(Calendar.HOUR_OF_DAY, eHour);
		cal2.set(Calendar.MINUTE, eMin);
		return cal.after(cal1) && cal.before(cal2);
	}
	
}
