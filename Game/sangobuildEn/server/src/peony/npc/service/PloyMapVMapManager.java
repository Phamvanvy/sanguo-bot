package peony.npc.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.dom4j.Document;
import org.dom4j.Element;

import peony.game.ChatOption;
import peony.game.CommonUtil;
import peony.game.CreatureDieCallback;
import peony.game.DieCallback;
import peony.game.GameMapDefinition;
import peony.game.GameObject;
import peony.game.MoveCallback;
import peony.game.NoInstanceVMapManager;
import peony.game.Player;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapManager;
import peony.service.Service;

public class PloyMapVMapManager implements VMapManager, Service {

	public boolean canEnter = false;
	public VMap map = null;
	private List<Enter> enters = new ArrayList<Enter>();
	private long ONEDAY = 24 * 3600 * 1000L;
	private List<Player> players = new ArrayList<Player>();
	private static int[][] out = new int[][]{
			{},
			{896, 619, 148},
			{896, 1312, 130},
			{896, 87, 253}
	};
	
	public VMap addToMap(Player player, int mapId, int x, int y, boolean check)
			throws VMapException {
		if(check){
			if(canEnter){
				players.add(player);
				player.removeFromMap();
				map.addPlayer(player, x, y);
				return map;
			}else{
				int outMapId = out[player.faction][0];
				int outX = out[player.faction][1];
				int outY = out[player.faction][2];
				return Server.server.getWorld().addPlayerToMap(player, outMapId, outX, outY, check);
			}
		}else{
			if(canEnter){
				players.add(player);
				player.removeFromMap();
				map.addPlayer(player, x, y);
				return map;
			}else{
				throw new VMapException("此時間不能進入地圖");
			}
		}
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

	public void shutdown() {
		
	}

	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
		.findFile("Areas/ployMap.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
		map = ((NoInstanceVMapManager)Server.server.getWorld().getVMapManager(1361)).getVMaps(1361)[0];
		Server.server.getWorld().addVMapManager(this);
		Server.server.getWorld().registerVMapManager(1361, this);
		for(Enter enter : enters){
			int startHour = enter.startHour;
			int startMin = enter.startMin;
			int endHour = enter.endHour;
			int endMin = enter.endMin;
			if(in(Calendar.getInstance(),startHour,startMin,endHour,endMin)){
				this.canEnter = true;
			}
			new Timer().schedule(new TimerTask(){
				public void run() {
					Server.server.getServiceRegistry().getChatService().sendSystemMessage(
							ChatOption.SYSTEM, "系統","仙桃園還有10分鐘開放,請大家准備好在江陵的相關傳送員處進入.");
				}
			}, getScheduleTime(Calendar.getInstance(), startHour, startMin-10), ONEDAY);
			new Timer().schedule(new TimerTask(){
				public void run() {
					canEnter = true;
				}
			}, getScheduleTime(Calendar.getInstance(), startHour, startMin), ONEDAY);
			new Timer().schedule(new TimerTask(){
				public void run() {
					canEnter = false;
					Server.server.syncRunner.add(new Runnable(){
						public void run() {
							transPlayers();
						}
					});
				}
			}, getScheduleTime(Calendar.getInstance(), endHour, endMin), ONEDAY);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) {
		Element root = doc.getRootElement();
		List<Element> list = root.elements();
		if(list!=null && list.size()>0){
			for(Element element : list){
				int startHour = Integer.parseInt(element.attributeValue("startHour"));
				int startMin = Integer.parseInt(element.attributeValue("startMin"));
				int endHour = Integer.parseInt(element.attributeValue("endHour"));
				int endMin = Integer.parseInt(element.attributeValue("endMin"));
				Enter enter = new Enter(startHour,startMin,endHour,endMin);
				enters.add(enter);
			}
		}
	}
	
	private Date getScheduleTime(Calendar cal, int hour, int min) {
		Calendar cal1 = Calendar.getInstance();
		cal1.set(Calendar.HOUR_OF_DAY, hour);
		cal1.set(Calendar.MINUTE, min);
		cal1.set(Calendar.SECOND, 0);
		if (cal1.before(cal)) {
			cal1.add(Calendar.DAY_OF_MONTH, 1);
			return cal1.getTime();
		}
		return cal1.getTime();
	}
	
	public boolean in(Calendar cal, int beginHour, int beginMin,
			int endHour, int endMin) {
		Calendar cal1 = Calendar.getInstance();
		cal1.set(Calendar.HOUR_OF_DAY, beginHour);
		cal1.set(Calendar.MINUTE, beginMin);
		Calendar cal2 = Calendar.getInstance();
		cal2.set(Calendar.HOUR_OF_DAY, endHour);
		cal2.set(Calendar.MINUTE, endMin);
		return cal.after(cal1) && cal.before(cal2);
	}
	
	protected void transPlayers(){
		Iterator<GameObject> itor = map.instanceid2objects.iterator2();
		while (itor.hasNext()) {
			GameObject o = itor.next();
			if(o.type==GameObject.TYPE_PLAYER){
				Player p = (Player)o;
				try {
					p.goMap(out[p.faction][0],out[p.faction][1] , out[p.faction][2]);
				} catch (VMapException e) {
					e.printStackTrace();
				}
			}
		}
		players.clear();
	}

}

class Enter{
	public int startHour;
	public int startMin;
	public int endHour;
	public int endMin;
	public Enter(int startHour,int startMin,int endHour,int endMin){
		this.startHour = startHour;
		this.startMin = startMin;
		this.endHour = endHour;
		this.endMin = endMin;
	}
}
