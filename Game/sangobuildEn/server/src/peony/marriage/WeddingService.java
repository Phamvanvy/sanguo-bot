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
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;

public class WeddingService implements VMapManager, Service {

	protected List<WeddingInstance> instances = new ArrayList<WeddingInstance>();

	public Position[] inInfo = new Position[4];

	public Position[] outInfo = new Position[4];

	public Map<Integer, List<Npc>> marrigenpc = new HashMap<Integer, List<Npc>>();

	protected static long STAYTIME = 5 * 60 * 1000;

	public static String MANOPENWEDDINGTIME = "manopenwedding";

	public static String WOMANOPENWEDDINGTIME = "womanopenwedding";

	 public static final long ONEWEEK = 7 * 24 * 60 * 60 * 1000;

	public String[] slang = { "白頭偕老,永結同心.", "早生貴子,百年好合.", "天作之合,鸞鳳和鳴.","比翼齊飛,連理共生." };

	public int[] money = { 0, 16666, 28888, 38888 };

	protected Random random = new Random();

	public void startup() throws Exception {
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
	}

	public void shutdown() {

	}

	/**
	 * 开启婚礼
	 * 
	 * @throws Exception
	 */
	public void createInstance(Player man, Player woman, int level,
			int guestLevel) throws Exception {
		if (man != null && woman != null) {
			int faction = man.faction;
			int mapId = inInfo[faction].mapId;
			WeddingInstance instance = new WeddingInstance(man, woman, new Date());
			VMap map = VMapUtil.create(this, Server.server.getWorld(), mapId, Server.server.revision);
//			Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MAP_ADDED, map));
			instance.map = map;
			instance.level = level;
			instance.guestLevel = guestLevel;
			map.manager = this;
			setMap(map, instance);
			Server.server.getWorld().registerVMapManager(mapId, this);
			Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MAP_ADDED, map));
			instance.addPlayer(man);
			instance.addPlayer(woman);
			PlayerTransaction tx = man.newTransaction("WEDDING");
			try {
				int money = (2 * level - 1) * 1000000;
				man.decMoney(money, tx, true);
			} catch (NoEnoughValueException ex) {
				tx.rollback();
				throw new Exception("您的金錢不足,不能開啟婚禮");
			}
			if (man.bag.removeGameItem(1311, -1, 20, tx, false) == null) {
				tx.rollback();
				throw new Exception("您的珍珠不足,不能開啟婚禮");
			}
			tx.commit();
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
				throw new VMapException("沒有指定的婚禮");
			if (instance.stat == WeddingInstance.END)
				throw new VMapException("沒有指定的婚禮");
			if (p.id!=instance.man.id && p.id!=instance.woman.id && p.level < instance.guestLevel) {
				throw new VMapException("您的等級不夠,不能參加");
			}
			if (instance.kicked.contains(p.id)) {
				throw new VMapException("您已被踢出婚禮殿堂,不能再參加此次婚禮");
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
	public void signIn(Packet packet, ClientSession session) throws VMapException {
		Player p = (Player) session.getClient();
		int serial = packet.getInt();
		if (p != null) {
			if (p.map.map.instance != null && p.map.map.instance instanceof WeddingInstance) {
				WeddingInstance instance = (WeddingInstance) p.map.map.instance;
				if (instance == null)
					throw new VMapException("沒有指定的婚禮");
				if (instance.stat == WeddingInstance.END)
					throw new VMapException("沒有指定的婚禮");
				Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(p.id);
				if (p.id == instance.man.id || p.id == instance.woman.id) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_SIGNIN_CLIENT, "新郎和新娘無需簽到");
					return;
				}
				if (instance.signIns.contains(actor)) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_SIGNIN_CLIENT, "您已經簽過到了");
					return;
				}
				instance.signIns.add(actor);
				instance.playerentertime.put(p.id, System.currentTimeMillis());
				int index = random.nextInt(slang.length);
				String message = "祝你們" + slang[index];
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
	public List<Actor> getSignIns(Player p) {
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
					throw new MarriageException("沒有物品或者物品數量不足");
				}
				PlayerTransaction tx1 = target.newTransaction("WEDDING");
				try {
					target.bag.addGameItemComplete(item, count, tx1, true);
					instance.getgift.add(new Integer(targetId));
					tx.commit();
					tx1.commit();
					target.message(-1, MessageFormat.format("你收到了{0}的贈送", p.name), -1, -1);
				} catch (NoEnoughSpaceException e) {
					tx.rollback();
					tx1.rollback();
					throw new MarriageException("對方背包已滿");
				}
			}
		}
	}

	/** 踢除宾客 */
	public void kickGuest(Packet packet, ClientSession session) {
		Player p = (Player) session.getClient();
		int serial = packet.getInt();
		int targetId = packet.getInt();
		if (p != null) {
			WeddingInstance instance = (WeddingInstance) p.map.map.instance;
			if (p.id != instance.man.id) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_KICK_CLINT, "只有新郎才可以踢除賓客");
				return;
			}
			Player targetPlayer = ObjectAccessor.getPlayer(targetId);
			if(targetPlayer==null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_KICK_CLINT, "對方不在線");
				return;
			}
			if (targetPlayer.map.map.instance==null || (targetPlayer.map.map.instance!=null && targetPlayer.map.map.instance.getId()!=instance.id)) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_KICK_CLINT, "對方不在同個地圖上");
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
				p.message(-1, MessageFormat.format("逐出{0}成功", targetPlayer.name), -1, -1);
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
					ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_GETEXP_CLIENT, "新郎和新娘不能領取經驗");
					return;
				}
				Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(p.id);
				if (!instance.signIns.contains(actor)) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_GETEXP_CLIENT, "請您先到婚禮司儀處簽到");
					return;
				}
				if (instance.deskgift.contains(p.id)) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_GETEXP_CLIENT, "您已經吃過酒席,別太貪心哦");
					return;
				}
				if (instance.fetchCount >= instance.MAXCOUNT[instance.level]) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_GETEXP_CLIENT, "對不起,酒席已空");
					return;
				}
				if (instance.playerentertime.get(p.id) > (System.currentTimeMillis()-STAYTIME)) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_GETEXP_CLIENT, "簽到后五分鐘才可以領取經驗");
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
				throw new VMapException("婚禮已經結束");
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
