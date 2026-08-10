package peony.service.activity;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

import peony.game.ChatOption;
import peony.game.CommonUtil;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.nation.NationService;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

public class ExpBagActivity implements IActivityImpl, ServiceEventListener {
	
	private static Logger log = Logger.getLogger(ExpBagActivity.class);
	private static Random random = new Random();
	private Map<Integer, Long> factionChatTime = new HashMap<Integer, Long>();
	private Map<Integer, FlagBattleRecord> flagBattleRecored = new HashMap<Integer, FlagBattleRecord>();
	protected Activity activity;
	
	protected List<ExpBag> bags = new ArrayList<ExpBag>();
	
	public ExpBagActivity(Activity owner){
		this.activity = owner;
	}
	
	public void clear() {
		
	}

	public Activity getActivity() {
		return activity;
	}

	public void load() {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("expbag.xml");
		try {
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			parse(doc);
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) {
		Element root = doc.getRootElement();
		List<Element> list = root.elements();
		for (Element element : list) {
			String eventName = element.attributeValue("name");
			int itemId = Integer.parseInt(element.attributeValue("item"));
			int count = Integer.parseInt(element.attributeValue("count"));
			int probability = Integer.parseInt(element.attributeValue("probability"));
			ExpBag bag = new ExpBag(eventName, itemId, count, probability);
			bags.add(bag);
		}
	}

	public void save() {
		
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}

	public int[] getEventTypes() {
		return new int[]{
//				ServiceEvent.EVENT_ADDJEWEL_SUCCESS,
//				ServiceEvent.EVENT_UNIT_DIE,
//				ServiceEvent.EVENT_BATTLE_WIN,
				ServiceEvent.EVENT_CHAT,
//				ServiceEvent.EVENT_DIG_SUCCESS,
//				ServiceEvent.EVENT_ADDJEWEL_SUCCESS,
//				ServiceEvent.EVENT_SKILL_REFRESH,
				ServiceEvent.EVENT_USEITEM,
//				ServiceEvent.EVENT_PRODUCE,
//				ServiceEvent.EVENT_IBUY,
//				ServiceEvent.EVENT_ENHANCE,
//				ServiceEvent.EVENT_MERGEJEWEL,
//				ServiceEvent.EVENT_EXTIRPADE,
//				ServiceEvent.EVENT_CHARGE_SUCCESS,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
			case ServiceEvent.EVENT_UNIT_DIE:
				processPlayerDie((Unit)event.param1, (Unit)event.param2);
				break;
			case ServiceEvent.EVENT_ADDJEWEL_SUCCESS:
				processAddJewel((Player)event.param1, (GameItem)event.param2);
				break;
			case ServiceEvent.EVENT_BATTLE_WIN:
				processBattleWin((Integer)event.param1, (Integer)event.param2);
				break;
			case ServiceEvent.EVENT_CHAT:
				processChat((Player)event.param1,(Integer)event.param2);
				break;
			case ServiceEvent.EVENT_DIG_SUCCESS:
				processDig((Player)event.param1);
				break;
			case ServiceEvent.EVENT_SKILL_REFRESH:
				processSkillRefresh((Player)event.param1);
				break;
			case ServiceEvent.EVENT_USEITEM:
				processUseItem((Player)event.param1, (Integer)event.param2);
				break;
			case ServiceEvent.EVENT_PRODUCE:
				processProduce((Player)event.param1);
				break;
			case ServiceEvent.EVENT_IBUY:
				processIbuy((Integer)event.param1, (Integer)event.param2);
				break;
			case ServiceEvent.EVENT_ENHANCE:
				processEnhance((Player)event.param1, (Integer)event.param2);
				break;
			case ServiceEvent.EVENT_MERGEJEWEL:
				processMergeJewel((Player)event.param1);
				break;
			case ServiceEvent.EVENT_EXTIRPADE:
				processRemoveJewel((Player)event.param1);
				break;
			case ServiceEvent.EVENT_CHARGE_SUCCESS:
				processCharge((Player)event.param1, (Integer)event.param2, (Integer)event.param3);
				break;
		}
	}
	
	protected void processCharge(Player p, int type, int money){
		if(p!=null){
			addGain(p, "充值成功", "CHARGE");
		}
	}
	
	protected void processRemoveJewel(Player p){
		if(p!=null){
			addGain(p, "摘除寶石", "REMOVEJEWEL");
		}
	}
	
	protected void processMergeJewel(Player p){
		if(p!=null){
			addGain(p, "寶石合成", "MERGEJEWEL");
		}
	}
	
	protected void processEnhance(Player p, int type){
		if(p!=null){
			if(type==0){
				addGain(p, "資質鑒定", "NATUALENHANCE");
			}else if(type==1){
				addGain(p, "星級鑒定", "STARENHANCE");
			}
		}
	}
	
	protected void processIbuy(int playerId, int imoney){
		Player p = ObjectAccessor.getPlayer(playerId);
		if(p!=null){
			addGain(p, "i幣購買", "IBUY");
		}
	}
	
	protected void processProduce(Player p){
		if(p!=null && p.level>=50){
			addGain(p, "50級打造", "PRODUECE");
		}
	}
	
	protected void processUseItem(Player p, int itemId){
		if(p!=null){
			if(itemId==1183){
				addGain(p, "一盒酥", "YIHESU");
			}else if(itemId==499){
				addGain(p, "獅子吼", "SHIZIHOU");
			}
		}
	}
	
	protected void processSkillRefresh(Player p){
		if(p!=null){
			addGain(p, "洗點", "SKILLREFRESH");
		}
	}
	
	protected void processDig(Player p){
		if(p!=null){
			addGain(p, "打孔", "DIG");
		}
	}
	
	protected void processChat(Player p, int channel){
		if(p!=null){
			if(channel==ChatOption.FACTION){
				long lastTime = factionChatTime.get(p.id)==null ? 0 : factionChatTime.get(p.id);
				NationService nationService = Server.server.getServiceRegistry().getNationService();
				if(nationService.getForbidByTargetId(p.id)==null && (System.currentTimeMillis()-lastTime)>=60*60*1000L){
					addGain(p, "國家聊", "FACTIONCHAT");
					factionChatTime.put(p.id, System.currentTimeMillis());
				}
			}else if(channel==ChatOption.WORLD){
				addGain(p, "世界聊", "WORLDCHAT");
			}
		}
	}
	
	protected void processBattleWin(int playerId, int type){
		Player p = ObjectAccessor.getPlayer(playerId);
		if(p!=null){
			if(type==0)
				addGain(p, "國戰胜利", "NATIONBATTLEWIN");
			else if(type==1)
				addGain(p, "戰場胜利", "FLAGBATTLEWIN");
			else if(type==2)
				addGain(p, "城戰胜利", "TONGBATTLEWIN");
		}
	}
	
	protected void processAddJewel(Player p, GameItem item){
		if(p!=null){
			addGain(p, "鑲嵌", "ADDJEWEL");
		}
	}
	
	protected void processPlayerDie(Unit died, Unit kill){
		if(kill instanceof Player && died instanceof Player){
			Player killPlayer = (Player)kill;
			if(Server.server.getServiceRegistry().getFlagBattleFieldVMapManager().isInFlagBattle(killPlayer)==1){
				FlagBattleRecord record = flagBattleRecored.get(killPlayer.id);
				if(record==null){
					record = new FlagBattleRecord(killPlayer.getVMap().instance.getId(),0);
					flagBattleRecored.put(killPlayer.id, record);
				}
				if(record.itemCount>=5)
					return;
				if(addGain(killPlayer, "戰場殺人", "FLAGBATTLEKILLPLAYER")){
					record.itemCount++;
				}
			}
		}
	}
	
	protected ExpBag getExpBag(String name){
		for(ExpBag bag : bags){
			if(bag.eventName.equals(name))
				return bag;
		}
		return null;
	}
	
	protected boolean addGain(Player p, String eventName, String cause){
		if(p!=null){
			ExpBag bag = getExpBag(eventName);
			if(bag==null)
				return false;
			int itemId = bag.itemId;
			int count = bag.count;
			int probability = bag.probability;
			int ran = random.nextInt(100);
			if(ran<probability){
				GameItem gainItem = ObjectAccessor.createGameItem(itemId);
				PlayerTransaction tx = p.newTransaction("EXPBAG");
				try {
					p.bag.addGameItemComplete(gainItem, count, tx, true);
					tx.commit();
					log.info("[EXPBAG]"+LogUtil.getPlayerLogString(p)+LogUtil
							.getGameItemString(gainItem, count)+"CAUSE["+cause+"]");
					return true;
				} catch (NoEnoughSpaceException e) {
					tx.rollback();
				}
			}
		}
		return false;
	}

}

class ExpBag {
	
	public String eventName;
	public int itemId;
	public int count;
	public int probability;
	
	public ExpBag(String eventName, int itemId, int count, int probability){
		this.eventName = eventName;
		this.itemId = itemId;
		this.count = count;
		this.probability = probability;
	}
	
}

class FlagBattleRecord{
	
	public int instanceId;
	public int itemCount;
	
	public FlagBattleRecord(int instanceId, int itemCount){
		this.instanceId = instanceId;
		this.itemCount = itemCount;
	}
	
}
