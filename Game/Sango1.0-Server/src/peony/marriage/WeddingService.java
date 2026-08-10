package peony.marriage;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.dom4j.Document;
import org.dom4j.Element;
import ch.javasoft.util.intcoll.IntHashMap;
import peony.game.Actor;
import peony.game.ChatOption;
import peony.game.CommonUtil;
import peony.game.CreatureDieCallback;
import peony.game.DieCallback;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.GameMapDefinition;
import peony.game.Instance;
import peony.game.LogUtil;
import peony.game.MoveCallback;
import peony.game.NoEnoughSpaceException;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.game.chat.ChatMessage;
import peony.game.chat.ChatService;
import peony.game.mail.MailService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.stat.StatService;

public class WeddingService implements VMapManager, Service , ServiceEventListener{

	protected List<WeddingInstance> instances = new ArrayList<WeddingInstance>();

	public Position[] inInfo = new Position[4];

	public Position[] outInfo = new Position[4];

	public Map<Integer, List<Npc>> marrigenpc = new HashMap<Integer, List<Npc>>();

	protected static long STAYTIME = 5 * 60 * 1000;

	public static String MANOPENWEDDINGTIME = "manopenwedding";

	public static String WOMANOPENWEDDINGTIME = "womanopenwedding";

	 public static final long ONEWEEK = 7 * 24 * 60 * 60 * 1000;

	public String[] slang = { peony.Messages.STRING_01366, peony.Messages.STRING_01367, peony.Messages.STRING_01368,peony.Messages.STRING_01369 };

	public static int[] money = { 0, 16666, 28888, 38888 };//各个等级红包金钱数 从一级开始
	
	public static int[] jewelNum = {0, 30, 50, 100};//各个等级红包宝石个数

	protected Random random = new Random();
	
	/**二期改造添加**/
	protected IntHashMap<WedQues> questions = new IntHashMap<WedQues>();

	public static String QUESTION_REFUSE_TIME = "QUESTION_REFUSE_TIME";
	
	public static long QUESTION_FORBID_TIME = 60 * 1000L;//禁止答题时间
	
	public static long QUESTION_WAIT_TIME = 60 * 1000L;//答题最长等待时间
	
	public static int ANSWER_MAX_NUM = 4;//最多答题次数
	
	public static int[] fuqiQuestID = new int[]{
		597,598,599,1446,1447,1448,2387,2388,2389,2390,2391,2392,2396,2397,2398,2399,
		2400,2401,2402,2403,2404,2405,2406,2407,2408,2409,2410,2411,2412,2413,2414,2415,
		2416,2417,2418,2419,2420,2421,2422,2423,2424,2425,2426,2427,2428,2429,2430,2431,
		2432,2433,2434
	};
	
	public static int[] ENAIDU = new int[]{10,20,30,60,90,500,800};
	
	public static String[] ENAIDU_NAME = new String[]{peony.Messages.STRING_01370,peony.Messages.STRING_01371,peony.Messages.STRING_01372,peony.Messages.STRING_01373,peony.Messages.STRING_01374,peony.Messages.STRING_01375,peony.Messages.STRING_01376};//称号
	
	public static int[] itemIds = new int[]{3680,3681,3682,3683,3684,3685,3686};//称号id
	
	public static final String PROPERTY_ENAIDU = "property_enaidu";
	
	public static long THREE_DAY = 60 * 60 * 24 * 3 * 1000L;
	
	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
				.findFile("wedding.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
		Server.server.getWorld().addVMapManager(this);
		initInstance();
	}

	protected void initInstance() {
		VMapUtil.create(this, Server.server.getWorld(), 737, Server.server.revision);
		Server.server.getWorld().registerVMapManager(737, this);
		VMapUtil.create(this, Server.server.getWorld(), 801, Server.server.revision);
		Server.server.getWorld().registerVMapManager(801, this);
		VMapUtil.create(this, Server.server.getWorld(), 785, Server.server.revision);
		Server.server.getWorld().registerVMapManager(785, this);
	}

	@SuppressWarnings("unchecked")
	protected void parse(Document doc) throws Exception {
		Element root = doc.getRootElement();
		Element elWei = root.element("wei");
		Element elWeiWed = elWei.element("wed");
		Element elWeiOut = elWei.element("out");
		Element npcsWei = elWei.element("npcs");
		int weiMapId = Integer.parseInt(elWeiWed.attributeValue("mapId"));
		int xWei = Integer.parseInt(elWeiWed.attributeValue("x"));
		int yWei = Integer.parseInt(elWeiWed.attributeValue("y"));
		Position weiMap = new Position(weiMapId, xWei, yWei);
		int weiOutMapId = Integer.parseInt(elWeiOut.attributeValue("mapId"));
		int weiOutX = Integer.parseInt(elWeiOut.attributeValue("x"));
		int weiOutY = Integer.parseInt(elWeiOut.attributeValue("y"));
		Position weiOutMap = new Position(weiOutMapId, weiOutX, weiOutY);
		this.inInfo[1] = weiMap;
		this.outInfo[1] = weiOutMap;
		int npcMapIdW = Integer.parseInt(npcsWei.attributeValue("mapId"));
		List<Element> npcWei = npcsWei.elements("npc");
		List<Npc> npclistWei = new ArrayList<Npc>();
		for (Element ele : npcWei) {
			int id = Integer.parseInt(ele.attributeValue("id"));
			int x = Integer.parseInt(ele.attributeValue("x"));
			int y = Integer.parseInt(ele.attributeValue("y"));
			Npc npctemp = new Npc(id, x, y);
			npclistWei.add(npctemp);
		}
		marrigenpc.put(npcMapIdW, npclistWei);

		Element elShu = root.element("shu");
		Element elShuWed = elShu.element("wed");
		Element elShuOut = elShu.element("out");
		Element npcsShu = elShu.element("npcs");
		int shuMapId = Integer.parseInt(elShuWed.attributeValue("mapId"));
		int xShu = Integer.parseInt(elShuWed.attributeValue("x"));
		int yShu = Integer.parseInt(elShuWed.attributeValue("y"));
		Position shuMap = new Position(shuMapId, xShu, yShu);
		int shuOutMapId = Integer.parseInt(elShuOut.attributeValue("mapId"));
		int shuOutX = Integer.parseInt(elShuOut.attributeValue("x"));
		int shuOutY = Integer.parseInt(elShuOut.attributeValue("y"));
		Position shuOutMap = new Position(shuOutMapId, shuOutX, shuOutY);
		this.inInfo[2] = shuMap;
		this.outInfo[2] = shuOutMap;
		int npcMapIdS = Integer.parseInt(npcsShu.attributeValue("mapId"));
		List<Element> npcShu = npcsShu.elements("npc");
		List<Npc> npclistShu = new ArrayList<Npc>();
		for (Element ele : npcShu) {
			int id = Integer.parseInt(ele.attributeValue("id"));
			int x = Integer.parseInt(ele.attributeValue("x"));
			int y = Integer.parseInt(ele.attributeValue("y"));
			Npc npctemp = new Npc(id, x, y);
			npclistShu.add(npctemp);
		}
		marrigenpc.put(npcMapIdS, npclistShu);

		Element elWu = root.element("wu");
		Element elWuWed = elWu.element("wed");
		Element elWuOut = elWu.element("out");
		Element npcsWu = elWu.element("npcs");
		int wuMapId = Integer.parseInt(elWuWed.attributeValue("mapId"));
		int xWu = Integer.parseInt(elWuWed.attributeValue("x"));
		int yWu = Integer.parseInt(elWuWed.attributeValue("y"));
		Position wuMap = new Position(wuMapId, xWu, yWu);
		int wuOutMapId = Integer.parseInt(elWuOut.attributeValue("mapId"));
		int wuOutX = Integer.parseInt(elWuOut.attributeValue("x"));
		int wuOutY = Integer.parseInt(elWuOut.attributeValue("y"));
		Position wuOutMap = new Position(wuOutMapId, wuOutX, wuOutY);
		this.inInfo[3] = wuMap;
		this.outInfo[3] = wuOutMap;
		int npcMapIdWu = Integer.parseInt(npcsWu.attributeValue("mapId"));
		List<Element> npcWu = npcsShu.elements("npc");
		List<Npc> npclistWu = new ArrayList<Npc>();
		for (Element ele : npcWu) {
			int id = Integer.parseInt(ele.attributeValue("id"));
			int x = Integer.parseInt(ele.attributeValue("x"));
			int y = Integer.parseInt(ele.attributeValue("y"));
			Npc npctemp = new Npc(id, x, y);
			npclistWu.add(npctemp);
		}
		marrigenpc.put(npcMapIdWu, npclistWu);
		//答题
		Element questionElement = root.element("questions");
		List<Element> ques = questionElement.elements("question");
		for(Element ele:ques){
			int id = Integer.parseInt(ele.attributeValue("id"));
			String que = ele.attributeValue("que");
			String an1 = ele.attributeValue("answer1");
			String an2 = ele.attributeValue("answer2");
			String an3 = ele.attributeValue("answer3");
			String an4 = ele.attributeValue("answer4");
			WedQues q = new WedQues(id,que,an1,an2,an3,an4);
			questions.put(id, q);
		}
	}

	public void shutdown() {

	}

	public int[] getEventTypes() {
		return new int[]{
			ServiceEvent.EVENT_PLAYER_LOADED,
			ServiceEvent.EVENT_FINISH_QUEST,
			ServiceEvent.EVENT_DIVORCE
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
			case ServiceEvent.EVENT_PLAYER_LOADED:
				processPlayerLoad((Player)event.param1);
				break;
			case ServiceEvent.EVENT_FINISH_QUEST:
				playerFinishQuest((Player)event.param1, (Integer)event.param2,(Integer)event.param3);
				break;
			case ServiceEvent.EVENT_DIVORCE:
				processDivorce((Player)event.param1,(Integer)event.param2);
				break;
		}
	}
	
	private void processDivorce(Player p1,int p2){
		if(p1.pool.getInt(WeddingService.PROPERTY_ENAIDU,0) > 0){
			p1.pool.setInt(WeddingService.PROPERTY_ENAIDU,0);
		}
		if(ObjectAccessor.getPlayer(p2) != null && ObjectAccessor.getPlayer(p2).pool.getInt(WeddingService.PROPERTY_ENAIDU,0) > 0){
			ObjectAccessor.getPlayer(p2).pool.setInt(WeddingService.PROPERTY_ENAIDU,0);
		}
		try{
			MarriageService mService = Server.server.getServiceRegistry().getMarriageService();
			mService.refreshSkill(p1, 0);
			p1.mateenaidu = 0;
			Player mate = ObjectAccessor.getPlayer(p2);
			if(mate!=null){
				mService.refreshSkill(mate, 0);
				mate.mateenaidu = 0;
			}
		}catch(Exception e){
			
		}
	}
	
	private void processPlayerLoad(Player p){
		if(p!=null&&p.pool.getInt(WeddingService.PROPERTY_ENAIDU)>0){
			if(System.currentTimeMillis() - p.lastLogoutTime.getTime() > THREE_DAY){
				int value = p.pool.getInt(WeddingService.PROPERTY_ENAIDU);
				value --;
				p.pool.setInt(WeddingService.PROPERTY_ENAIDU,value);
			}
		}
		if(p!=null){
			try{
				if(p.relations.mateId!=-1){
					Player mate = ObjectAccessor.getPlayer(p.relations.mateId);
					if(mate == null){
						mate = Server.server.getServiceRegistry().getDbService().playerDAO.getPlayerById(p.relations.mateId);
					}
					if(mate!=null){
						p.mateenaidu = mate.pool.getInt(WeddingService.PROPERTY_ENAIDU, 0);
					}
				}
			}catch(Exception e){
				
			}
		}
	}
	
	public void playerFinishQuest(Player player,int questId,int branch){
		if(isFuqiQuest(questId)){
			int value = player.pool.getInt(PROPERTY_ENAIDU,0);
			value++;
			player.pool.setInt(PROPERTY_ENAIDU, value);
			
			try{
				int v = Math.min(value, player.mateenaidu);
				MarriageService mService = Server.server.getServiceRegistry().getMarriageService();
				mService.refreshSkill(player, v);
				if(player.relations.mateId!=-1){
					Player mate = ObjectAccessor.getPlayer(player.relations.mateId);
					if(mate!=null){
						mService.refreshSkill(mate, v);
						mate.mateenaidu = value;
					}
				}
				
			}catch(Exception e){
				
			}
			//统计拥有恩爱度成就
			StatService statService = Server.server.getServiceRegistry().getStatService();
			statService.enaiduAchieve(player);
			int index = -1;
			if(value == ENAIDU[0]){
				index =0;
			}else if(value == ENAIDU[1]){
				index =1;
			}else if(value == ENAIDU[2]){
				index =2;
			}else if(value == ENAIDU[3]){
				index =3;
			}else if(value == ENAIDU[4]){
				index =4;
			}else if(value == ENAIDU[5]){
				index =5;
			}else if(value == ENAIDU[6]){
				index =6;
			}
			if(index != -1){
				GameItem item = ObjectAccessor.createGameItem(itemIds[index]);
				if(item != null){
//					PlayerTransaction tx = player.newTransaction("MARRYDEGREE");
//					try{
//						player.bag.addGameItemComplete(item, 1, tx, true);
//						tx.commit();
//					}catch(Exception e){
//						tx.rollback();
					    Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, "恭喜您获得了夫妻恩爱度奖励，奖励已通过飞鸽发送，请查收。");
						MailService mailService = Server.server.getServiceRegistry().getMailService();
						mailService.sendSystemMailAsync(player.id, peony.Messages.STRING_00004, peony.Messages.STRING_01377, "", 0, item, 1, "MARRYDEGREE");
//					}
				}
			}
		}
	}
	
	private boolean isFuqiQuest(int id){
		for(int i = 0;i < fuqiQuestID.length;i++){
			if(fuqiQuestID[i] == id){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 开启婚礼
	 * 
	 * @throws Exception
	 */
	public void createInstance(Player player,Player man, Player woman, int level,
			int guestLevel,int jewelTotleNum,List<Integer> jewels) throws Exception {
		if (man != null && woman != null) {
			int faction = player.faction;
			int mapId = inInfo[faction].mapId;
			PlayerTransaction tx = player.newTransaction("WEDDING");
			try {
//				int money = (2 * level - 1) * 1000000;
				int money = level * 1200000; //婚礼费用打六折
				player.decMoney(money, tx, true);
			} catch (NoEnoughValueException ex) {
				tx.rollback();
				throw new Exception(peony.Messages.STRING_01378);
			}
			if (player.bag.removeGameItem(1311, -1, 20, tx, false) == null) {
				tx.rollback();
				throw new Exception(peony.Messages.STRING_01379);
			}
			if(jewels.size() > 0){
				for(int i = 0;i < jewels.size();i++){
					int id = jewels.get(i);
					if(player.bag.removeGameItem(id, -1, 1, tx, false) == null){
						tx.rollback();
						throw new Exception(peony.Messages.STRING_01380);
					}
				}
			}
			tx.commit();
			VMap map = VMapUtil.create(this, Server.server.getWorld(), mapId, Server.server.revision);
			WeddingInstance instance = new WeddingInstance(map,man, woman, new Date(),jewelTotleNum,jewels);
//			Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MAP_ADDED, map));
			instance.level = level;
			instance.guestLevel = guestLevel;
			map.manager = this;
			setMap(map, instance);
			Server.server.getWorld().registerVMapManager(mapId, this);
			Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MAP_ADDED, map));
			instance.addPlayer(man);
			instance.addPlayer(woman);
			instances.add(instance);
			man.goMap(map.getId(), inInfo[man.faction].x,inInfo[man.faction].y);
			woman.goMap(map.getId(), inInfo[man.faction].x,inInfo[man.faction].y);
			man.pool.setLong(MANOPENWEDDINGTIME, System.currentTimeMillis());
			woman.pool.setLong(WOMANOPENWEDDINGTIME, System.currentTimeMillis());
			LogUtil.logOpenWedding(man, woman.id,woman.name);
		}
	}

	protected void setMap(VMap map, Instance instance) {
		map.instance = instance;
	}

	/** 获取当前进行的婚礼列表 */
	public List<Actor[]> getWeddingList(Player p) {
		List<Actor[]> list = new ArrayList<Actor[]>();
		for (WeddingInstance instance : instances) {
			if (instance.man.faction == p.faction) {
				Actor[] refs = new Actor[2];
				refs[0] = instance.man;
				refs[1] = instance.woman;
				list.add(refs);
			}
		}
		return list;
	}

	/** 参加婚礼 */
	public void joinWedding(Player p, int manId) throws VMapException {
		if (p != null) {
			WeddingInstance instance = getInstanceByManId(manId);
			if (instance == null)
				throw new VMapException(peony.Messages.STRING_01381);
			if (instance.stat == WeddingInstance.END)
				throw new VMapException(peony.Messages.STRING_01381);
			if (p.id!=instance.man.id && p.id!=instance.woman.id && p.level < instance.guestLevel) {
				throw new VMapException(peony.Messages.STRING_01382);
			}
			if (instance.kicked.contains(p.id)) {
				throw new VMapException(peony.Messages.STRING_01383);
			}
			if ((p.id != instance.man.id && p.id != instance.woman.id && p.id != instance.banLangId && p.id != instance.banNiangId)&&instance.players2.size() >= WeddingInstance.MAX_PEOPLE_NUM){
				throw new VMapException(peony.Messages.STRING_01384);
			}
			VMap map = instance.map;
			instance.addPlayer(p);
			p.goMap(map.getId(), inInfo[p.faction].x, inInfo[p.faction].y);
			LogUtil.logEnterWedding(p, manId, instance.man.name);
		}
	}

	/**
	 * 处理地图数据变化，尽可能地更新已有对象的属性。
	 */
	public void mapChanged(GameMapDefinition mapDef) {
		for (WeddingInstance instance : instances) {
			VMap map = instance.map;
			if (map.mapDef.mapInfo.getGlobalID() == mapDef.mapInfo.getGlobalID()) {
				map.mapDef = mapDef;
				map.mapChanged();
			}
		}
	}

	protected VMap in(Player player, WeddingInstance instance) throws VMapException {
		VMap map = instance.map;
		player.removeFromMap();
		map.addPlayer(player, inInfo[player.faction].x,inInfo[player.faction].y);
		instance.addPlayer(player);
		return map;
	}

	/**
	 * 婚礼签到
	 * @param msg
	 * @throws VMapException
	 */
	public synchronized void signIn(Packet packet, ClientSession session) throws VMapException {
		Player p = (Player) session.getClient();
		int serial = packet.getInt();
		if (p != null) {
			if (p.map.map.instance != null && p.map.map.instance instanceof WeddingInstance) {
				WeddingInstance instance = (WeddingInstance) p.map.map.instance;
				if (instance == null)
					throw new VMapException(peony.Messages.STRING_01381);
				if (instance.stat == WeddingInstance.END)
					throw new VMapException(peony.Messages.STRING_01381);
				Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(p.id);
				if (p.id == instance.man.id || p.id == instance.woman.id) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_SIGNIN_CLIENT, peony.Messages.STRING_01385);
					return;
				}
				if (instance.signIns.contains(actor)) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_SIGNIN_CLIENT, peony.Messages.STRING_01386);
					return;
				}
				instance.signIns.add(actor);
				instance.playerentertime.put(p.id, System.currentTimeMillis());
				int index = random.nextInt(slang.length);
				String message = MessageFormat.format(peony.Messages.STRING_01387, slang[index]);
				ChatService service = Server.server.getServiceRegistry().getChatService();
				ChatMessage cm = new ChatMessage(ChatOption.AREA, -1, -1,p.name, message, null);
				cm.destId = Integer.parseInt(instance.map.getId()+""+instance.getId());
				service.addChatMessage(cm);
				LogUtil.logSignInWedding(p, instance.man.id, instance.man.name);
			}
			Packet pt = new Packet(OpCode.WEDDING_SIGNIN_SERVER);
			pt.putInt(serial);
			p.send(pt);
		}
	}

	/** 获取签到人的列表 */
	public synchronized List<Actor> getSignIns(Player p) {
		if (p != null) {
			if (p.map.map.instance != null && p.map.map.instance instanceof WeddingInstance) {
				WeddingInstance instance = (WeddingInstance) p.map.map.instance;
				return instance.signIns;
			}
		}
		return null;
	}

	/** 发放红包 */
	public void sendGift(Player p, int targetId, int itemId, int count)
			throws MarriageException {
		if (p != null) {
			Player target = ObjectAccessor.getPlayer(targetId);
			if (target != null) {
				PlayerTransaction tx = p.newTransaction("WEDDING");
				GameItem item = p.bag.removeGameItemIngoreInstanceId(itemId, count, tx, true);
				WeddingInstance instance = (WeddingInstance) p.map.map.instance;
				if (item == null) {
					tx.rollback();
					throw new MarriageException(peony.Messages.STRING_01388);
				}
				PlayerTransaction tx1 = target.newTransaction("WEDDING");
				try {
					target.bag.addGameItemComplete(item, count, tx1, true);
					instance.getgift.add(new Integer(targetId));
					tx.commit();
					tx1.commit();
					target.message(-1, MessageFormat.format(peony.Messages.STRING_01389, p.name), -1, -1);
				} catch (NoEnoughSpaceException e) {
					tx.rollback();
					tx1.rollback();
					throw new MarriageException(peony.Messages.STRING_01390);
				}
			}
		}
	}

	/** 踢除宾客 */
	public synchronized void kickGuest(Packet packet, ClientSession session) {
		Player p = (Player) session.getClient();
		int serial = packet.getInt();
		int targetId = packet.getInt();
		if (p != null) {
			WeddingInstance instance = (WeddingInstance) p.map.map.instance;
			if (p.id != instance.man.id) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_KICK_CLINT, peony.Messages.STRING_01391);
				return;
			}
			Player targetPlayer = ObjectAccessor.getPlayer(targetId);
			if(targetPlayer==null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_KICK_CLINT, peony.Messages.STRING_00609);
				return;
			}
			if (targetPlayer.map.map.instance==null || (targetPlayer.map.map.instance!=null && targetPlayer.map.map.instance.getId()!=instance.id)) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_KICK_CLINT, peony.Messages.STRING_01392);
				return;
			}
			if (targetPlayer != null) {
				try {
					targetPlayer.goMap(outInfo[targetPlayer.faction].mapId,outInfo[targetPlayer.faction].x,
							outInfo[targetPlayer.faction].y);
					Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(targetId);
					instance.signIns.remove(actor);
					instance.kicked.add(targetId);
				} catch (VMapException e) {

				}
				Packet pt = new Packet(OpCode.WEDDING_KICK_SERVER);
				pt.putInt(serial);
				targetPlayer.send(pt);
				p.message(-1, MessageFormat.format(peony.Messages.STRING_01393, targetPlayer.name), -1, -1);
				LogUtil.logKickWedding(p, targetId, targetPlayer.name);
			}
		}
	}

	/** 宾客在酒桌处领取经验 */
	public void getWeddingExp(Packet packet, ClientSession session) {
		Player p = (Player) session.getClient();
		int serial = packet.getInt();
		if (p != null) {
			WeddingInstance instance = (WeddingInstance) p.map.map.instance;
			if (instance != null) {
				if (p.id == instance.man.id || p.id == instance.woman.id) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_GETEXP_CLIENT, peony.Messages.STRING_01394);
					return;
				}
				Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(p.id);
				if(instance.stat != WeddingInstance.BEGIN){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_GETEXP_CLIENT, peony.Messages.STRING_01395);
					return;
				}
				if (instance.deskgift.contains(p.id)) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_GETEXP_CLIENT, peony.Messages.STRING_01396);
					return;
				}
				if (instance.fetchCount >= instance.MAXCOUNT[instance.level]) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_GETEXP_CLIENT, peony.Messages.STRING_01397);
					return;
				}
				PlayerTransaction tx = p.newTransaction("WEDDING");
				p.addMoney(money[instance.level], tx, true);
				p.addExp(p.level * 100, tx, true);
				instance.deskgift.add(p.id);
				instance.fetchCount++;
				tx.commit();
			}
			Packet pt = new Packet(OpCode.WEDDING_GETEXP_SERVER);
			pt.putInt(serial);
			p.send(pt);
		}
	}

	public WeddingInstance getInstanceByManId(int manId) {
		for (WeddingInstance instance : instances) {
			if (instance.man.id == manId) {
				return instance;
			}
		}
		return null;
	}

	public WeddingInstance getInstance(int playerId) {
		for (WeddingInstance instance : instances) {
			if(instance.stat!=WeddingInstance.END){
				for (Player p : instance.players) {
					if (p.id == playerId) {
						return instance;
					}
				}
			}
		}
		return null;
	}

	public VMap addToMap(Player player, int mapId, int x, int y, boolean check)
			throws VMapException {
		if (check) {
			WeddingInstance instance = getInstance(player.id);
			if (instance == null) {
				return Server.server.getWorld().addPlayerToMap(player, outInfo[player.faction].mapId,
						outInfo[player.faction].x, outInfo[player.faction].y, true);
			} else {
				return in(player, instance);
			}
		} else {
			WeddingInstance instance = getInstance(player.id);
			if (instance == null) {
				throw new VMapException(peony.Messages.STRING_01398);
			} else {
				return in(player, instance);
			}
		}
	}

	public CreatureDieCallback creatureDieCallback() {
		return null;
	}

	public DieCallback dieCallback() {
		return null;
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
		WeddingInstance currentInstance = (WeddingInstance) player.map.map.instance;
		for (WeddingInstance instance : instances) {
			if(instance.id!=currentInstance.id)
				instance.removePlayer(player);
		}
	}

	public void update(int diff) {
		Iterator<WeddingInstance> ite = instances.iterator();
		while (ite.hasNext()) {
			WeddingInstance instance = ite.next();
			if (instance.stat == WeddingInstance.END) {
				ite.remove();
			} else {
				instance.update(diff);
			}
		}
	}
	
	/**根据id得到答题对象**/
	public AnswerQue getAnswerQueByPlayerId(int playerId){
		WeddingInstance instance = getInstance(playerId);
		if(instance != null){
			for(AnswerQue answerQue:instance.answerQues){
				if(answerQue.isContainPlayer(playerId)){
					return answerQue;
				}
			}
		}
		return null;
	}
	
	/**玩家答题**/
	public void answerQuestion(int playerId,int questionId,int answer) throws MarriageException{
		AnswerQue answerQue = getAnswerQueByPlayerId(playerId);
		if(answerQue == null)
			throw new MarriageException(peony.Messages.STRING_01399);
		answerQue.answerQuestion(questionId,answer);
	}
	
	/**答题过程中玩家拒绝答题**/
	public void refuseQuestion(int playerId) throws MarriageException{
		AnswerQue answerQue = getAnswerQueByPlayerId(playerId);
		if(answerQue == null)
			throw new MarriageException(peony.Messages.STRING_01399);
		answerQue.refuseQuestion(playerId);
	}

}


class Position {

	public int mapId;
	public int x;
	public int y;

	public Position(int mapId, int x, int y) {
		super();
		this.mapId = mapId;
		this.x = x;
		this.y = y;
	}

}

class Npc {
	public int id;
	public int x;
	public int y;

	public Npc(int id, int x, int y) {
		this.id = id;
		this.x = x;
		this.y = y;
	}
}

class WedQues{
	public int id;
	public String question;
	public String answer1;
	public String answer2;
	public String answer3;
	public String answer4;
	public WedQues(int id,String question,String answer1,String answer2,String answer3,String answer4){
		this.id = id;
		this.question = question;
		this.answer1 = answer1;
		this.answer2 = answer2;
		this.answer3 = answer3;
		this.answer4 = answer4;
	}
	
	public String getAnswerById(int id){
		if(id == 0){
			return answer1;
		}else if(id == 1){
			return answer2;
		}else if(id == 2){
			return answer3;
		}else if(id == 3){
			return answer4;
		}
		return "";
	}
}





