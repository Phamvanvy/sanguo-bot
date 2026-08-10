package peony.service.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import peony.game.Creature;
import peony.game.CreatureDieCallback;
import peony.game.DieCallback;
import peony.game.ErrorHandler;
import peony.game.GameMapDefinition;
import peony.game.GameObject;
import peony.game.MoveCallback;
import peony.game.NoInstanceVMapManager;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.service.ServiceEvent;

/**
 * 中秋赏月活动(场景中杀怪,当所有怪消失后开始下经验雨,怪复活后停止下经验雨)
 * @author dchen
 */
public class CleanBossActivity implements IActivityImpl, VMapManager, Runnable {

	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(CleanBossActivity.class);
	
	protected Activity activity; // 活动实体
	
	protected int[] mapIds = {1121, 1122, 1123}; // 经验雨地图ID
	
	protected VMap[] maps = new VMap[3]; // 经验雨地图
	
	protected int[][] levels = {{0, 50}, {50, 64}, {64, 100}}; // 级别区间(前开后闭)
	
	protected Boolean[] flag = new Boolean[3]; // 经验雨开关
	
	protected Map<Integer, List<Player>> players = new HashMap<Integer, List<Player>>(); // 地图内玩家集合
	
	protected static long dis = 5 * 1000L; // 获取经验频率
	
	protected static int[] expQuotiety = {2, 4, 4}; // 经验系数
	
	protected int[][] position = {{150,250}, {370,300}, {240,420}, {480,560}}; // 真天狗位置
	
	protected int[] BOSS = {4591617, 4595713, 4599809}; // 真天狗ID
	
	protected Random random = new Random();
	
	protected Timer timer = new Timer();
	
	protected long lastUpdateTime = 0;
	
	public CleanBossActivity(Activity owner) {
		this.activity = owner;
	}

	public void clear() {

	}

	public Activity getActivity() {
		return activity;
	}

	public void load() {
		
	}

	public void save() {

	}

	/** 活动关闭 */
	public void shutdown() {
		timer.cancel();
	}

	/** 活动开启 */
	public void startup() throws Exception {
		flag[0] = false;
		flag[1] = false;
		flag[2] = false;
		for(int i=0;i<mapIds.length;i++){
			List<Player> list = new ArrayList<Player>();
			players.put(mapIds[i], list);
		}
		Server.server.getWorld().addVMapManager(this);
		for(int i=0;i<mapIds.length;i++){
			maps[i] = ((NoInstanceVMapManager) Server.server.getWorld().getVMapManager(mapIds[i])).getVMaps(mapIds[i])[0];
			Server.server.getWorld().registerVMapManager(mapIds[i], this);
			maps[i].manager = this;
		}
		new Thread(this).start();
		timer.schedule(new TimerTask(){
			public void run() {
				Server.server.syncRunner.add(new Runnable(){
					public void run() {
						for(int i=0;i<maps.length;i++){
							VMap map = maps[i];
							if(map.getCreatureById(BOSS[i])!=null && map.getCreatureById(BOSS[i]).isAlive())
								continue;
							ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
							GameMapObject gmo = GameMapObject.findByID(proj, BOSS[i]);
							int index = random.nextInt(position.length);
							int[] ps = position[index];
							VMapUtil.addCreature(map, ps[0], ps[1],(GameMapNPC) gmo, true, 0, null);
						}
					}
				});
			}
		}, 3600 * 1000, 3600 * 1000);
	}

	public VMap addToMap(Player player, int mapId, int x, int y, boolean check)
			throws VMapException {
		if (check) {
			player.removeFromMap();
			maps[getMapIndex(mapId)].addPlayer(player, x, y);
			addPlayer(player, mapId);
			return maps[getMapIndex(mapId)];
		} else {
			if(getMapIndex(mapId)!=-1){
				int maxLevel = levels[getMapIndex(mapId)][1];
				if(player.level<maxLevel){
					player.removeFromMap();
					maps[getMapIndex(mapId)].addPlayer(player, x, y);
					addPlayer(player, mapId);
					return maps[getMapIndex(mapId)];
				}else{
					ErrorHandler.sendErrorMessage(player.session, -1,
							OpCode.TELEPORT_CLIENT, "這里對您來說太沒有挑戰性了,建議您去往高級別的地圖進行探險.");
					return null;
					
				}
			}
			ErrorHandler.sendErrorMessage(player.session, -1,
					OpCode.TELEPORT_CLIENT, "您不能進入此地圖");
			return null;
		}
	}
	
	protected void addPlayer(Player p, int mapId){
		if(p!=null){
			players.get(mapId).add(p);
		}
	}
	
	protected int getMapIndex(int mapId){
		for(int i=0;i<mapIds.length;i++){
			if(mapId==mapIds[i])
				return i;
		}
		return -1;
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
		int mapId = player.map.getId();
		if(players.get(mapId)!=null){
			players.get(mapId).remove(player);
		}
	}

	public void update(int diff) {
		if(System.currentTimeMillis()-lastUpdateTime<3000)
			return;
		for(int i=0;i<maps.length;i++){
			VMap map = maps[i];
			boolean f = true;
			for(GameObject o : map.instanceid2objects){
				if(o instanceof Creature && (o.name.equals("天狗") || o.name.equals("真天狗"))){
					if(o.isAlive())
						f = false;
				}
			}
			flag[i] = f;
		}
		lastUpdateTime = System.currentTimeMillis();
	}

	public void run() {
		while(this.activity.active && this.activity.getSchedule().in()){
			try {
				Thread.sleep(5000);
				for(int i=0;i<maps.length;i++){
					boolean f = flag[i];
					if(f){
						List<Player> list = players.get(mapIds[i]);
						Iterator<Player> it = list.iterator();
						while(it.hasNext()){
							Player p = it.next();
							if(ObjectAccessor.getPlayer(p.id)!=null){
								PlayerTransaction tx = p.newTransaction("CLEANBOSS");
								p.addExp(expQuotiety[i]*p.level, tx, true);
								tx.commit();
							}
						}
					}
				}
			} catch (Exception e) {
			}
		}
	}
	
	public boolean isForbid(Player player, int mapId){
		if(getMapIndex(mapId)!=-1){
			int maxLevel = levels[getMapIndex(mapId)][1];
			if(player.level>=maxLevel){
				return true;
			}
		}
		return false;
	}

}
