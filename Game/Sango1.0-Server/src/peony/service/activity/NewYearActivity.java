package peony.service.activity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;

import peony.db.RefreshNpcCall;
import peony.game.DayListener;
import peony.game.GameMapDefinition;
import peony.game.GameObject;
import peony.game.NoInstanceVMapManager;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.VMap;
import peony.game.VMapUtil;

public class NewYearActivity implements IActivityImpl,DayListener{
	
	/** 场景地图ID */
	public static int[] mapIds = {848,2032,2016};
	/** 活动NPCId */
	public static int[] npcIds = {3473408,8323297,8257776};
	/** NPC刷新间隔时间(分钟) */
	public static int[] timeDis = {53,96,128};
	
	/** NPC刷新坐标*/
	public static int[][] GRID_FU = {{452,265},{746,930},{1235,844},{870,481},{380,840},{1283,389},{574,1267},{201,884},{892,112},{650,626}};
	public static int[][] GRID_LU = {{566,320},{308,647},{580,961},{955,1297},{1070,773},{1155,531},{1307,233},{876,100},{803,666},{827,474}};
	public static int[][] GRID_SHOU = {{1324,336},{966,172},{410,194},{360,514},{259,910},{555,1045},{1201,945},{907,808},{905,539},{710,342}};

	public static int LASTTIME = 5*60*1000; //怪物持续时间
	
	/** 获得奖励区间*/
	public static int[] EXP_RANGE = {1000,5000};
	public static int[] MONEY_RANGE = {10000,100000};
	
	/** 奖励物品id*/
	public static int JEWERLBAG_THREE = 1614;
	public static int FLAW_JEWERLBAG = 4664;
	
	public static int JEWERLBAG_NUM = 10;
	
	public static int LEVEL_LIMIT = 70;
	
	public static Random rnd = new Random();
	
	/** 每日领取奖励记录 */
	protected static List<Integer> getExps = new ArrayList<Integer>();
	protected static List<Integer> getMoney = new ArrayList<Integer>();
	protected static List<Integer> getJewerls = new ArrayList<Integer>();
	
	/** NPC刷新时领取宝石记录 */
	protected static List<Integer> npc2JewerlsCount = new ArrayList<Integer>();
	
	/** 刷出的NPC记录 */
	public static Map<Integer,GameObject> refreshedNpc = new HashMap<Integer,GameObject>();

	private Activity activity;
	
	public void clear() {
		
	}

	public NewYearActivity(Activity owner){
		this.activity = owner;
	}
	
	public Activity getActivity() {
		return activity;
	}

	public void load() {
		
	}

	public void save() {
		
	}

	public void shutdown() {
		removeNpc();
	}

	public void startup() throws Exception {
		Time.addDayListener(this);
		timeHandler();
	}
	
	public void timeHandler(){
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
//				refreshNpc(0);
				if(activity!=null && activity.isActive() && activity.isEnabled() && activity.isVisible()){
					RefreshNpcCall call = new RefreshNpcCall(RefreshNpcCall.NEWYEARACTIVITY);
					call.index = 0;
					Server.server.getWorld().schedule(call);
				}
				Server.server.scheduExec.schedule(new Runnable(){
					public void run() {
						removeNpc(0);
					}
				}, LASTTIME, TimeUnit.MILLISECONDS);
			}
		}, 60*1000, timeDis[0]*60*1000, TimeUnit.MILLISECONDS); 
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
//				refreshNpc(1);
				if(activity!=null && activity.isActive() && activity.isEnabled() && activity.isVisible()){
					RefreshNpcCall call = new RefreshNpcCall(RefreshNpcCall.NEWYEARACTIVITY);
					call.index = 1;
					Server.server.getWorld().schedule(call);
				}
				Server.server.scheduExec.schedule(new Runnable(){
				    public void run() {
				    	removeNpc(1);
					}
				}, LASTTIME, TimeUnit.MILLISECONDS);
			}
		}, 60*1000, timeDis[1]*60*1000, TimeUnit.MILLISECONDS);
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
//				refreshNpc(2);
				if(activity!=null && activity.isActive() && activity.isEnabled() && activity.isVisible()){
					RefreshNpcCall call = new RefreshNpcCall(RefreshNpcCall.NEWYEARACTIVITY);
					call.index = 2;
					Server.server.getWorld().schedule(call);
				}
				Server.server.scheduExec.schedule(new Runnable(){
					public void run() {
						removeNpc(2);
					}
				}, LASTTIME, TimeUnit.MILLISECONDS);
			}
		}, 60*1000, timeDis[2]*60*1000, TimeUnit.MILLISECONDS);
	}
	
	/** 刷新NPC */
	public  synchronized static void refreshNpc(int index){
		removeNpc(index);
		ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
		GameMapObject gmo = GameMapObject.findByID(proj, npcIds[index]);
		VMap map = ((NoInstanceVMapManager) Server.server.getWorld()
				.getVMapManager(mapIds[index])).getVMaps(mapIds[index])[0];
		int[] grid = getGrid(index);
		GameObject npc0 = VMapUtil.addCreature(map, grid[0], grid[1],
				(GameMapNPC) gmo, true, 0, null);
		GameMapDefinition def = VMapUtil.getDefinition(map.getId());
		String message = MessageFormat.format("{0}出现在{1}的某个角落，他只会存在5分钟，与他对话即可免费获得春节神秘大礼！", npc0.name,def.mapInfo.name);
		Server.server.getServiceRegistry().getChatService().sendWorldMessage(message);
		refreshedNpc.put(mapIds[index], npc0);
	}
	
	/** 活动关闭时移除NPC */
	public void removeNpc(){
		if(refreshedNpc.size()>0){
			for (GameObject npc : refreshedNpc.values()) {
				if (npc != null && npc.getVMap() != null) {
					npc.removeFromWorld();
				}
			}
			refreshedNpc.clear();
			getJewerls.clear();
			getExps.clear();
			getMoney.clear();
			npc2JewerlsCount.clear();
		}
	}
	
	/** 移除NPC */
	public synchronized static  void removeNpc(int index){
		if(refreshedNpc.size()>0 && refreshedNpc.containsKey(mapIds[index])){
			for (GameObject npc : refreshedNpc.values()) {
				if (npc != null && npc.getVMap() != null && npc.getVMap().getId() == mapIds[index]) {
					npc.removeFromWorld();
					refreshedNpc.remove(mapIds[index]);
					if(index == 2){
					    npc2JewerlsCount.clear();
					}
					GameMapDefinition def = VMapUtil.getDefinition(mapIds[index]);
					String message = MessageFormat.format("{0}老人已经悄悄地离开了{1}。找他要春节大礼的勇士们记得下次及时赶到啊。", npc.name,def.mapInfo.name);
					Server.server.getServiceRegistry().getChatService().sendWorldMessage(message);
					
				}
			}
		}
	}
	
	/** 已经领取宝石记录*/
	public synchronized static void getReward(Player p,int type) throws Exception{
		if(type == 0){
			if(!getExps.contains(p.id)) {
				int tempNum = NewYearActivity.EXP_RANGE[1]-NewYearActivity.EXP_RANGE[0];
				int num = NewYearActivity.EXP_RANGE[0]+rnd.nextInt(tempNum);
				PlayerTransaction tx = p.newTransaction("NEWYEARACTIVITY");
				p.addExp(p.level*num, tx, true);
				tx.commit();
				getExps.add(p.id);
				String message = MessageFormat.format("恭喜您得到{0}经验，祝您春节快乐", p.level*num);
				Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, message);
			}else{
				throw new Exception("每天只能领取一次");
			}
		}else if(type == 1){
            if(!getMoney.contains(p.id)) {
            	int tempNum = NewYearActivity.MONEY_RANGE[1]-NewYearActivity.MONEY_RANGE[0];
				int num = NewYearActivity.MONEY_RANGE[0]+rnd.nextInt(tempNum);
				PlayerTransaction tx = p.newTransaction("NEWYEARACTIVITY");
				p.addMoney(num, tx, true);
				tx.commit();
				getMoney.add(p.id);
				String message = MessageFormat.format("恭喜您得到{0}金钱，祝您春节快乐", num);
				Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, message);
			}else{
				throw new Exception("每天只能领取一次");
			}
		}
	}
	
	
	public synchronized static int getJewerlCount(Player p)throws Exception{
		if(getJewerls!=null){
			if(getJewerls.contains(p.id)){
				throw new Exception("每天只能领取一次");
			}else{
				return npc2JewerlsCount.size();
			}		
		}
		return 0;
	}
	
	public synchronized static void recordJewerlReward(Player p){
		getJewerls.add(p.id);
		npc2JewerlsCount.add(p.id);
	}
	
	/** 获取NPC刷新坐标 */
	public static int[] getGrid(int index){
		int[][] grid = GRID_FU;
		if(index == 1){
			grid = GRID_LU;
		}else if(index == 2){
			grid = GRID_SHOU;
		}
		int rndIndex = rnd.nextInt(grid.length);
		return grid[rndIndex];
	}

	public void dayChanged() {
		synchronized (this) {
			getExps.clear();
			getMoney.clear();
			getJewerls.clear();
			npc2JewerlsCount.clear();
		}
	}
}