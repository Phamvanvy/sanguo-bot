package peony.service.cards;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import peony.game.DataService;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;

import com.pip.sanguo.data.Card;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.DataObjectCategory;

public class CardService implements Service {

	protected Map<Integer, CardGroup> cardGroups = new ConcurrentHashMap<Integer, CardGroup>();
	protected List<CardGroup> cardGroupList = new ArrayList<CardGroup>();
	protected Map<Integer, Card> allCards = new ConcurrentHashMap<Integer, Card>();
	protected int totalcount = 0;
	public static final String PROPERTY_HAVECARD = "HAVECARDS";
	protected static final int ITEMID_CARD_SHOWNAMES = 971;
	private Map<String, Card> formulas = new HashMap<String, Card>(); // 配方
	protected Map<Integer, String> formulaDesc = new HashMap<Integer, String>();// 配方描述
	public static Map<Integer, Integer> item2Card = new HashMap<Integer, Integer>(); // itemId
	private Map<Integer,boolean[]> randCache;
	protected Random rnd;			
	
	public void shutdown() {
		
	}


	public void startup() throws Exception {
		loadServiceData();
	}

	/**
	 * 用卡片配方查找结果
	 * 
	 * @param key
	 * @return
	 */
	public Card findCardFormula(String key) {
		if (formulas.containsKey(key)) {
			return formulas.get(key);
		} else {
			return null;
		}
	}

	/**
	 * 载入模板数据
	 */
	public void loadServiceData() {
		DataService ds = Server.server.getServiceRegistry().getDataService();
		CardGroup cg = null;
		List<DataObjectCategory> groups = ds.data.getCategoryListByType(Card.class);
		for (DataObjectCategory grp : groups) {
			if(grp.objects.size() == 0){
				continue;
			}
			Card cd0 = (Card)grp.objects.get(0);
			cg = new CardGroup(cd0.suiteId,cd0.categoryName);
			addGroup(cg);
			cardGroups.put(cd0.suiteId, cg);
			for (DataObject obj : grp.objects) {
				Card cd = (Card)obj;
				cg.addCard(cd);
				// 载入模板查找
				allCards.put(cd.id, cd);
				// 载入配方映射
				String fmlStr = cd.getFormulaStr();
				if (!"".equals(fmlStr)) {
					formulas.put(fmlStr, cd);
				}
				// 载入配方描述
				formulaDesc.put(cd.id, getFormulaDesc(cd));
				// 物品映射卡片
				item2Card.put(cd.itemId, cd.id);
				// 载入分组信息
				totalcount++;
			}
		}
		rnd = new Random();
		randCache = Collections.synchronizedMap(new ConcurrentHashMap<Integer, boolean[]>());
	}
	
	/**
	 * 加入到卡片组列表中
	 * @param cg
	 */
	private void addGroup(CardGroup cg){
		int start = -1;
		int end = cardGroupList.size();
		int mid = end;
		while(end - start > 1){
			mid = (start + end) / 2;
			CardGroup g = (CardGroup)cardGroupList.get(mid);
			if(cg.groupId < g.groupId){
				end = mid;
			} else {
				start = mid;
			}
		}
		cardGroupList.add(end, cg);
	}
	
	/** 使用集卡名录 */
	public void showCardName(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int groupId = packet.getInt();
		Player p = (Player) session.getClient();
		if (p != null) {
			if (p.pool.getInt(getPropertyOfShowCardName(groupId), 0) != 0) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.CARD_SHOWNAME_CLIENT, "该卡片系列名称已开启");
				return;
			}
			p.pool.setInt(getPropertyOfShowCardName(groupId), 1);
			Packet pt = new Packet(OpCode.CARD_SHOWNAME_SERVER);
			pt.putInt(serial);
			p.send(pt);
			PlayerTransaction tx = p.newTransaction("CARD");
			GameItem item = p.bag.removeGameItemIngoreInstanceId(
					ITEMID_CARD_SHOWNAMES, 1, tx, true);
			if (item != null) {
				tx.commit();
			} else {
				tx.rollback();
			}
		}
	}

	/** 记录玩家是否收藏卡片的属性 */
	public String getPropertyOfPlayerCard(int cardId) {
		return "PROPERTY_HAVECARD" + cardId;
	}
	
	/** 记录玩家收藏的卡片的品质 */
	public String getPropertyOfCardQuality(int cardId) {
		return "PROPERTY_CARD_QUALITY" + cardId;
	}

	/** 记录玩家收藏特定套装张数属性 */
	public String getPropertyOfPlayerSuit(int groupId) {
		return "PROPERTY_HAVECARD_OFSUIT" + groupId;
	}

	/** 记录玩家使用集卡名录的属性 */
	public String getPropertyOfShowCardName(int groupId) {
		return "PROPERTY_SHOWCARDNAME" + groupId;
	}

	/** 根据卡片id得到卡片名字 */
	public Card getCardByCardId(int cardId) {
		return allCards.get(cardId);
	}
	
	/** 对输入的cardid进行排序并以字符串形式输出 */
	public String getUserFormulaKey(List<Integer> cardIds) {
		Collections.<Integer> sort(cardIds);
		StringBuilder sb = new StringBuilder();
		int len = cardIds.size();
		for (int i = 0; i < len; i++) {
			sb.append(cardIds.get(i));
			if (i < len - 1) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	/**
	 * 得到物品的卡片id
	 * 
	 * @param itemId
	 * @return
	 */
	public static int getCardId(int itemId) {
		Integer obj = item2Card.get(itemId);
		if (obj != null) {
			return obj.intValue();
		} else {
			return -1;
		}
	}

	/**
	 * 获得卡片组
	 * 
	 * @param groupId
	 * @return
	 */
	public CardGroup getCardGroup(int groupId) {
		return cardGroups.get(groupId);
	}

	/**
	 * 物品id查找卡片
	 * 
	 * @return
	 */
	public Card getCardByItemId(int itemId) {
		Card ret = null;
		Integer cid = item2Card.get(itemId);
		if (cid != null) {
			ret = getCardByCardId(cid.intValue());
		}
		return ret;
	}
	
	/**
	 * 随机生成闪卡
	 * @param cd
	 * @return
	 */
	public boolean generateFlashCard(Card cd){
		if(cd.rate >= 10000){
			return true;
		} else if(cd.rate <= 0){
			return false;
		} else {
			boolean[] r = randCache.get(cd.rate);
			if(r == null){
				r = new boolean[10000];
				Arrays.fill(r, 0, cd.rate - 1, true);
				synchronized(randCache){
					randCache.put(cd.rate, r);
				}
			}
			int x = rnd.nextInt(10000);
			return r[x];
		}
	}
	
	public String getFormulaDesc(Card card){
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < card.materials.length; i++) {
            if(card.materials[i].type == Card.MATERIAL_TYPE_CARD){
                sb.append(MessageFormat.format("{0}卡{1}张", card.materials[i].name,card.materials[i].value));
                sb.append(" + ");
            }
        }
        String ret = sb.toString();
        if(ret.length() > 0){
            ret = ret.substring(0, ret.length() - 3);
        }
        return ret;
	}
}

