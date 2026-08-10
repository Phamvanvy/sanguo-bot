package peony.service.feast;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;

import peony.db.RefreshNpcCall;
import peony.game.CommonUtil;
import peony.game.CreatureDieCallback;
import peony.game.DieCallback;
import peony.game.GameMapDefinition;
import peony.game.GameObject;
import peony.game.Instance;
import peony.game.LogUtil;
import peony.game.MoveCallback;
import peony.game.NoInstanceVMapManager;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.game.asyncbattle.AsyncBattleService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.CycleInstanceMapManager;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.util.TimeUtil;

public class FeastInstanceService implements VMapManager, Service{
	
	private static Logger log = Logger.getLogger(FeastInstanceService.class);
	
	public static int MAPID = 2160;   //副本地图id
	public int[] pos = {400,380};  //传进地图坐标
	public static int NUM_PLAYER = 5;  //副本玩家
	private static int BEGIN_HOUR = 11;		//11:30	开始时间
	private static int BEGIN_MIN = 30;
	private static int END_HOUR = 12;
	public static long ONEDAY = 24 * 3600 * 1000L;
	public static int LEVEL_LIMIT = 40;  //等级限制
	
	public static int SIGN_NPCID = 3473628;  //报名NPCid
	public static int[] NPC_POS = {848,629,600};
	public static int[] OUT_POS = {848,600,600};
	public static final int DAY_START = 0; // 每天的开始时间(分钟为单位)
	public static final int DAY_END = 24 * 60; // 每天的结束时间(分钟为单位)
	public GameObject npc; //报名NPC
	public static int[] THIEF_NPCID = {8847380,8847381,8847382,8847383};
	public static int[] THIEF_POSX = {710,492,486,724};
	public static int[] THIEF_POSY = {432,436,562,560};
	public static int BASE_CARDEXP = 50;
	public static int BASE_CREDIT = 100;
	public static int BASE_EXP = 1500;
	public boolean ACTIVE_STATE = false;
	public boolean ACTIVE_PREPARE = false;
	Random rnd = new Random();
	
	public static int DAY_ENTERCOUNT = 3;
	
	public static String PROPERTY_FEAST_DAYCOUNT = "dayfeastcount";
	
	protected int lastCheckCreateTime;
	
	protected static int FEAST_SUIPIANITEM = 4368;
	
	protected List<FeastInstance> instances = new ArrayList<FeastInstance>();
	public List<Player> players = new ArrayList<Player>();
	public List<Player> checkedPlayers = new ArrayList<Player>();
	public List<Menu> menuid2Menu = new ArrayList<Menu>();
	public Map<Integer,Material> materials = new HashMap<Integer,Material>();
    public Map<Integer,List<Npc>> material2Npcs = new HashMap<Integer,List<Npc>>();
    
	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
		.findFile("Areas/feastinstance.xml");
		try {
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			parse(doc);
		} catch (Exception e) {
			log.error(e, e);
		}
		Server.server.getWorld().addVMapManager(this);
		initInstance();
		processNotify();
	}
	
    public void shutdown() {
		
	}
    
 
    /** 
     * 检测活动状态
     */
    public void processNotify(){
		//活动前一秒发狮子否提示30秒后可报名参加活动
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				String message = "满汉全席马上就要开始啦，想做三国中独一无二的食神吗？30秒后任何大于40级的不在副本中的玩家都可以报名哦！";
				//发狮子吼
				Packet pt = new Packet(OpCode.SHOUT_SERVER);
				pt.putString(message);
				pt.putInt(0xFF0000);
				pt.putInt(10000);
				pt.put(0);
				for (Player p1 : ObjectAccessor.players.values()) {
					if(p1!=null && p1.getVMap()!=null && p1.getVMap().getId()==AsyncBattleService.battleMap)
						continue;
					p1.send(pt);
				}
				log.info("[FEASTINSTANCESHOUT]OK");
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGIN_HOUR, BEGIN_MIN-1), ONEDAY, TimeUnit.MILLISECONDS);
		
		//活动开始前30秒时在线用户自动弹出报名框
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				for (Player p1 : ObjectAccessor.players.values()) {
					if(p1.level>= LEVEL_LIMIT){
						if(p1.map.map.instance!=null ||p1.getVMap().getId() == CycleInstanceMapManager.mapId.get(new Integer(p1.clazz)))
							continue;
						if(p1!=null && p1.getVMap()!=null && p1.getVMap().getId()==AsyncBattleService.battleMap)
							continue;
						Packet pt = new Packet(OpCode.OPENUI_SERVER);
						pt.putString("ui_npc_dialog");
						pt.putString("FEAST_SIGN| ");
						p1.send(pt);                                                                                                                                                                                                                                                                                                             
					}
				}
				ACTIVE_PREPARE = true;
				log.info("[FEASTAUTOSIGN]OK");
			}
		}, TimeUtil.getScheduleTimeMills2(new Date(),BEGIN_HOUR, BEGIN_MIN-1,30), ONEDAY, TimeUnit.MILLISECONDS);
		
		//活动开始时刷出报名NPC
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				Server.server.getServiceRegistry().getChatService().sendWorldMessage("满汉全席活动已开启");
//				refreshNpc();
				RefreshNpcCall call = new RefreshNpcCall(RefreshNpcCall.FEASTINSTANCE);
				Server.server.getWorld().schedule(call);
				ACTIVE_STATE = true;
				ACTIVE_PREPARE = false;
				log.info("[FEASTINSTANCESTART]OK");
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), BEGIN_HOUR, BEGIN_MIN), ONEDAY, TimeUnit.MILLISECONDS);
		
		//活动范围内输出NPC
		Calendar cal = Calendar.getInstance();
		if (inPeriod(cal, BEGIN_HOUR, BEGIN_MIN, END_HOUR,
				0)) {
			refreshNpc();
			ACTIVE_STATE = true;
		}
		
	    //活动结束后刷掉NPC，以及传送活动地图内玩家
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				ACTIVE_STATE = false;
				npc.removeFromWorld();
				clearInstance();
				Server.server.getServiceRegistry().getChatService().sendWorldMessage("满汉全席活动已结束");
				log.info("[FEASTINSTANCEEND]");
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), END_HOUR, 0), ONEDAY, TimeUnit.MILLISECONDS);
		
	}
    
	
	/**
	 * 报名
	 * @param session
	 * @param packet
	 */
	public void signUp(Player player){
		if(player!=null){
			if(player.level<LEVEL_LIMIT){
				player.message(-1, "等级不够", -1, -1);
				return;
			}
			
			if(!ACTIVE_PREPARE){
				Calendar cal = Calendar.getInstance();
				if (!ACTIVE_STATE || !inPeriod(cal, BEGIN_HOUR, BEGIN_MIN, END_HOUR,
						0)) {
					player.message(-1, "现在不是报名时间", -1, -1);
				    return;
				}
			}
			
			if(getPlayerInstance(player)!=null){
				player.message(-1, "您已经进去过游戏，请耐心等待", -1, -1);
			    return;
			}
			
			
			int count = player.pool.getInt(PROPERTY_FEAST_DAYCOUNT,0);
			if(count>=DAY_ENTERCOUNT){
				player.message(-1, "您今天3次烹饪的次数已经用完，欢迎明天再来！", -1, -1);
			    return;
			}
			
			if(Server.server.getServiceRegistry().getFlagBattleFieldVMapManager().signups.containsKey(player.id)){
				player.message(-1, "勇士您已在战场报名队列中，因此无法报名参加满汉全席活动", -1, -1);
			    return;
			}
			if(Server.server.getServiceRegistry().getTongBattleVMapManager().isInTongBattle(player)==1){
				player.message(-1, "勇士您已在城战报名队列中，因此无法报名参加满汉全席活动", -1, -1);
			    return;
			}
			
			if(players.contains(player) || checkedPlayers.contains(player)){
				player.message(-1, "您已经报名，请耐心等待", -1, -1);
			    return;
			}
			
			if(player.getVMap().getId()==AsyncBattleService.battleMap){
			    return;
			}
			
			players.add(player);
			player.message(-1, "报名成功，请耐心等待", -1, -1);
			
			LogUtil.logFeastSign(player, players.size());
		}
	}
	
    public FeastInstance getPlayerInstance(Player player){
    	if(instances!=null && instances.size()>0){
			for(FeastInstance instance : instances){
				if(instance.players.contains(player)){
				    return instance;
				}
			}
		}
		return null;
	}
	
	public void checkEnter(Player player){
		if(player!=null){
			if(!checkedPlayers.contains(player)){
				checkedPlayers.add(player);
			}
		}
	}
	
	/**
	 * 创建副本
	 * @throws Exception
	 */
	public boolean createInstance(List<Player> players) {
		if(players.size()<NUM_PLAYER)
			return false;
		VMap map = VMapUtil.create(this, Server.server.getWorld(), MAPID, Server.server.revision);
		FeastInstance instance = new FeastInstance(map);
		instances.add(instance);
		map.manager = this;
		setMap(map, instance);
		Server.server.getWorld().registerVMapManager(MAPID, this);
		Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MAP_ADDED, map));
		int count = 0;
		for(int i=0;i<players.size();i++){
			Player p = players.get(i);
			players.remove(i);
			i--;
			count++;
			enterInstance(p,instance);
			if(count == NUM_PLAYER){
				break;
			}
		}
		return true;
	}
	
	public void enterInstance(Player player,FeastInstance instance){
		if(player!=null){
			try {
				if(player.map.map!=null){
					PlayerPosition oriPos = new PlayerPosition(player.map.map.getId(),player.x,player.y);
					if(player.map.map.instance!=null || player.map.map.getId() == CycleInstanceMapManager.mapId.get(new Integer(player.clazz))){
						oriPos = new PlayerPosition(OUT_POS[0],OUT_POS[1],OUT_POS[2]);
					}
					instance.player2Position.put(player.id, oriPos);
					player.goMap(instance.map.getId(), pos[0], pos[1]);
					instance.addPlayer(player);
					int count = player.pool.getInt(PROPERTY_FEAST_DAYCOUNT,0);
					count++;
					player.pool.setInt(PROPERTY_FEAST_DAYCOUNT, count);
					LogUtil.logFeastEnter(player, instance.getId(), count);
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}
	
	/**
	 * 传进副本地图
	 * @param players
	 */
	protected void arrange2(){
		if(!createInstance(checkedPlayers)){
			return;
		}else{
			arrange2();
		}
	}
	
	/**
	 * 处理已报名玩家
	 */
	protected void arrange1(){
		if(!arrangeSign(players)){
			return;
		}else{
			arrange1();
		}
	}
	
	public boolean arrangeSign(List<Player> players){
		if(players.size()<=0)
			return false;
		for(int i=0;i<players.size();i++){
			Player p = players.get(i);
		    
			Packet pt = new Packet(OpCode.OPENUI_SERVER);
			pt.putString("ui_npc_dialog");
			pt.putString("FEAST_ENTER| ");
			p.send(pt);
			
			players.remove(i);
			i--;

		}
		return true;
		//添加日志
	}
	
	/**
	 * 定期检测报名
	 */
	protected void checkCreate() {
		if(Time.currTime-lastCheckCreateTime<10*1000)
			return;	
			Iterator<Player> it = checkedPlayers.iterator();
			if(it.hasNext()){
				Player p = it.next();
				if(ObjectAccessor.getPlayer(p.id)==null || (p.getVMap()!=null && (p.getVMap().instance!=null||p.getVMap().getId() == CycleInstanceMapManager.mapId.get(new Integer(p.clazz))) 
					|| p.map.id==AsyncBattleService.battleMap)){
					it.remove();
				}
			}
			if(checkedPlayers.size()>=NUM_PLAYER){
			      arrange2();
			}
		
		
			Iterator<Player> it2 = players.iterator();
			if(it2.hasNext()){
				Player p = it2.next();
				if(ObjectAccessor.getPlayer(p.id)==null || (p.getVMap()!=null && (p.getVMap().instance!=null || p.getVMap().getId() == CycleInstanceMapManager.mapId.get(new Integer(p.clazz))) 
						|| p.map.id==AsyncBattleService.battleMap)){
					it2.remove();
				}
			}
//			if(players.size()>=NUM_PLAYER){
			   arrange1();
//		    }
		
		lastCheckCreateTime = Time.currTime;
		
	}
	
	/**
	 * 显示菜谱
	 * @param session
	 * @param packet
	 */
	public void showMenu(ClientSession session,Packet packet){
	    int serial = packet.getInt();
		Player p = (Player)session.getClient();
		if(p!=null){
			Packet pt = new Packet(OpCode.FEAST_VIEWMENU_SERVICE);
			FeastInstance instance = (FeastInstance) p.map.map.instance;
			pt.putInt(serial);
			if(instance!=null){
			   pt.putInt(instance.materials.size());
			   int count = 0;
			   for(Material key : instance.materials){
				    pt.put(count);
				    count++;
					pt.putInt(key.materialId);
					Material m = getMaterial(key.materialId);
					pt.putString(m.getMaterialName());
					pt.putInt(key.count);
					pt.putInt(key.killCount);
				}
				  pt.putString(instance.menu.menuName);
			}else{
				pt.putInt(0);
			}
			p.send(pt);
		}
	}
	
	public Material getMaterial(int materialId){
		return materials.get(materialId);
	}
	
	protected void setMap(VMap map, Instance instance) {
		map.instance = instance;
	}
	
	public void clearInstance(){
		players.clear();
		checkedPlayers.clear();
//		instances.clear();
	}
	
	
	
	/**
	 * 刷出报名Npc
	 */
	public void refreshNpc(){
		if(npc!=null)
		    npc.removeFromWorld();
		ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
		GameMapObject gmo = GameMapObject.findByID(proj, SIGN_NPCID);
		VMap map = ((NoInstanceVMapManager) Server.server.getWorld()
				.getVMapManager(NPC_POS[0])).getVMaps(NPC_POS[0])[0];
		GameObject npc0 = VMapUtil.addCreature(map, NPC_POS[1], NPC_POS[2],
				(GameMapNPC) gmo, true, 0, null);
	    npc = npc0;
	}
	
	
	
	protected void initInstance() {
		VMapUtil.create(this, Server.server.getWorld(), MAPID, Server.server.revision);
		Server.server.getWorld().registerVMapManager(MAPID, this);
	}
	
	@SuppressWarnings("unchecked")
	public void parse(Document doc){
		Element root = doc.getRootElement();
		if (root != null) {
			List<Element> mats = root.elements("material");
			for(Element e : mats){
				int id = Integer.parseInt(e.attributeValue("id"));
				String name = e.attributeValue("name");
				int decCount = Integer.parseInt(e.attributeValue("deccount"));
				Material material = new Material(id,name,decCount);
				List<Element> npcs = e.elements("npc");
				List<Npc> ns = new ArrayList<Npc>();
				for(Element ne : npcs){
					int npcId = Integer.parseInt(ne.attributeValue("npcid"));
					int x = Integer.parseInt(ne.attributeValue("x"));
					int y = Integer.parseInt(ne.attributeValue("y"));
					Npc npc = new Npc(npcId,x,y);
					ns.add(npc);
				}
				materials.put(id, material);
				material2Npcs.put(id, ns);
			}
			List<Element> mns = root.elements("menu");
			for(Element e : mns){
				int id = Integer.parseInt(e.attributeValue("id"));
				String name = e.attributeValue("name");
				String materials = e.attributeValue("materials");
				String[] strs = materials.split(",");
				String c = e.attributeValue("count");
				String[] count = c.split(",");
				int majorMaterial = Integer.parseInt(e.attributeValue("majormaterial"));
				Menu menu = new Menu(id,name,majorMaterial);
				for(int i=0;i<strs.length;i++){
					int mId = Integer.parseInt(strs[i]);
					int cnt = Integer.parseInt(count[i]);
					Material material = new Material(mId,0,cnt);
					menu.addMaterial(material);
				}
				menuid2Menu.add(menu);
			}
		}
	}
	
	protected boolean inPeriod(Calendar cal, int startHour, int startMinute,
			int endHour, int endMinute) {
		int start = startHour * 60 + startMinute;
		int end = endHour * 60 + endMinute;
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minu = cal.get(Calendar.MINUTE);
		int v = hour * 60 + minu;
		if (start <= end) {
			return v >= start && v <= end;
		} else {
			return (v >= start && v <= DAY_END) || (v >= DAY_START && v <= end);
		}

	}
	
	public VMap addToMap(Player player, int mapId, int x, int y, boolean check)
			throws VMapException {
		if (check) {
			FeastInstance instance = getPlayerInstance(player);
			if (instance==null) {
				return Server.server.getWorld().addPlayerToMap(player, OUT_POS[0], OUT_POS[1],
						OUT_POS[2], true);
			} else {
				return in(player, instance);
			}
		} else {
			FeastInstance instance = getPlayerInstance(player);
			if (instance == null) {
				throw new VMapException("活动已结束");
			} else {
				return in(player, instance);
			}
		}
	}
	
	protected VMap in(Player player, FeastInstance instance) throws VMapException {
		VMap map = instance.map;
		player.removeFromMap();
		map.addPlayer(player, pos[0],pos[1]);
		instance.addPlayer(player);
		return map;
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
		FeastInstance currentInstance = (FeastInstance) player.map.map.instance;
        for (FeastInstance instance : instances) {
			if(instance.id!=currentInstance.id){
				instance.removePlayer(player);
			}
		}
	}

	public void update(int diff) {
		if(ACTIVE_STATE){
		    checkCreate();
		}
		if(instances!=null && instances.size()>0){
		    for(int i=0;i<instances.size();i++){
		    	FeastInstance instance = instances.get(i);
		    	if(instance != null){
		    		instance.update(diff);
		    	}
		    }
	    }
	}
}

class Material{
	int materialId;
	String name;
	int killCount;
	int count;
	int decCount;
	
	
	public Material(int materialId,String name,int decCount){
		this.materialId = materialId;
		this.name = name;
		this.decCount = decCount;
	}
	public Material(int materialId,int killCount,int count){
		this.materialId = materialId;
		this.count = count;
		this.killCount = killCount;
	}
	public int getId(){
		return materialId;
	}
	public String getMaterialName(){
		return name;
	}
	public int getKillCount(){
		return killCount;
	}
	public void setKillCount(int killCount){
		this.killCount = killCount;
	}
	public void setCount(int count){
		this.count = count;
	}
	public int getCount(){
		return count;
	}
	public int getDeccount(){
		return decCount;
	}
	public boolean isMajorMaterial(int majorMaterial){
		if(materialId == majorMaterial)
			return true;
		return false;
	}
}

class PlayerPosition{
	int mapId;
	int x;
	int y;
	public PlayerPosition(int mapId,int x ,int y){
		this.mapId = mapId;
		this.x = x;
		this.y = y;
	}
}

