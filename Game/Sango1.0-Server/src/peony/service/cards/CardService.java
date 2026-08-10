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

import org.apache.log4j.Logger;

import peony.game.DataService;
import peony.game.DayListener;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.PropertyCalculator;
import peony.game.Server;
import peony.game.Time;
import peony.game.TransactionException;
import peony.game.buff.Buff;
import peony.game.buff.BuffUtil;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.fame.Fame;
import peony.service.ranking.RankingService;
import com.pip.sanguo.data.Card;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.DataObjectCategory;
import com.pip.sanguo.data.Card.Material;
import com.pip.sanguo.data.item.Item;

public class CardService implements Service, DayListener, ServiceEventListener {

	private static final Logger log = Logger.getLogger(CardService.class);
	
	public Map<Integer, CardGroup> cardGroups = new ConcurrentHashMap<Integer, CardGroup>();
	public List<CardGroup> cardGroupList = new ArrayList<CardGroup>();
	public Map<Integer, Card> allCards = new ConcurrentHashMap<Integer, Card>();
	protected int totalcount = 0;
	public static final String PROPERTY_HAVECARD = "HAVECARDS";
	protected static int ITEMID_CARD_SHOWNAMES = 971;
	private Map<String, Card> formulas = new HashMap<String, Card>(); // 配方
	protected Map<Integer, String> formulaDesc = new HashMap<Integer, String>();// 配方描述
	public static Map<Integer, Integer> item2Card = new HashMap<Integer, Integer>(); // itemId
	private Map<Integer,boolean[]> randCache;	
	protected Map<String, Buff> effectCardBuffs = new HashMap<String, Buff>(); //存放所有卡片BUFF，key为BUFFID_LEVEL
	protected static Random rnd;
	public static int MAX_GENERALCARD_ENERGY = 100; //普通附魔卡片最大能量值
	public static int MAX_FLASHCARD_ENERGY = 150; //闪卡附魔卡片最大能量值
	protected Map<Integer, Material[]> materials = new HashMap<Integer, Material[]>();
	protected List<Integer> allCardBuffs = new ArrayList<Integer>();
	public static final String[] PROPERTY_TYPE_NAMES = {"力量", "敏捷", "智力", "命中","暴击","物攻","法攻","体力","技能"};
	
	public static String PROPERTY_ADDCARDEXP_EVERYDAY = "addcardexpday";  //玩家每日获取的摇卡经验
	public static String PROPERTY_ROCKCARDCOUNT_EVERYDAY = "rockcountday";  //玩家每日的摇卡次数
	public static String PROPERTY_CLEAROLDCARDDATA = "clearoldcarddata";  //清理旧的卡片数据
	
	
	private Map<Integer,Integer> allCardInfo=new HashMap<Integer, Integer>();//已收藏卡片信息
	
	public static final int TYPE_PLAYER = 0;
	public static final int TYPE_HORSE = 1;
	public static final int TYPE_ATTENDANT = 2;
	
	/**职业技能卡片ID*/
	public static final int[] clazzCardId={
		151,152,153,
		154,155,156,
		157,158,159,
		160,161,162
	};
	
	public static int[] WHITE_ENHANCE_VALUES = {
		1,
		2,
		3,
		4,
		5,
		6,
		7,
		8,
		9,
		11,
		13,
		16};
	public static int[] GREEN_ENHANCE_VALUES = {
		2,
		4,
		6,
		8,
		10,
		12,
		14,
		17,
		20,
		24,
		29,
		35};
	public static int[] BLUE_ENHANCE_VALUES = {
		4,
		8,
		12,
		16,
		20,
		24,
		28,
		34,
		41,
		50,
		61,
		74};
	public static int[] PURPLE_ENHANCE_VALUES = {
		8,
		14,
		20,
		26,
		32,
		38,
		47,
		59,
		74,
		93,
		116,
		143};
	public static int[] ORANGE_ENHANCE_VALUES = {
		16,
		28,
		40,
		52,
		64,
		76,
		91,
		109,
		130,
		154,
		181,
		211};
	
	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}


	public void startup() throws Exception {
		loadServiceData();
		Time.addDayListener(this);
		Server.server.getEventManager().registerListener(this);
	}

	/**
	 * 用卡片配方查找结果
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
//				formulaDesc.put(cd.id, cd.getFormulaDesc());
				formulaDesc.put(cd.id, getFormulaDesc(cd));
				materials.put(cd.id, cd.materials);
				// 物品映射卡片
				item2Card.put(cd.itemId, cd.id);
				// 载入分组信息
				totalcount++;
				//载入卡片效果BUFF
				try {
					int buffId = cd.buffId;
					int buffLevel1 = cd.buffLevel1;
					int buffLevel2 = cd.buffLevel2;
					Buff buff1 = BuffUtil.createSuiteBuff(buffId, buffLevel1);
					Buff buff2 = BuffUtil.createSuiteBuff(buffId, buffLevel2);
					effectCardBuffs.put(cd.id+"_"+buffLevel1, buff1);
					effectCardBuffs.put(cd.id+"_"+buffLevel2, buff2);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		rnd = new Random();
		randCache = Collections.synchronizedMap(new ConcurrentHashMap<Integer, boolean[]>());
	}
	
	public String getFormulaDesc(Card card){
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < card.materials.length; i++) {
            if(card.materials[i].type == Card.MATERIAL_TYPE_CARD){
                sb.append(MessageFormat.format(peony.Messages.STRING_01061, card.materials[i].name,card.materials[i].value));
                sb.append(" + ");
            }
        }
        String ret = sb.toString();
        if(ret.length() > 0){
            ret = ret.substring(0, ret.length() - 3);
        }
        return ret;
	}
	
	public void clearData(){
		cardGroups.clear();
		cardGroupList.clear();
		allCards.clear();
		formulas.clear();
		formulaDesc.clear();
		item2Card.clear();
		effectCardBuffs.clear();
		materials.clear();
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
						OpCode.CARD_SHOWNAME_CLIENT, peony.Messages.STRING_01062);
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
	/**记录玩家收藏卡片属性的新字段*/
	public static String CARD_NEW_PROPERTY="CARD_NEW_PROP";

	/** 记录玩家是否收藏卡片的属性 0未收藏 1收藏 */
	public static String getPropertyOfPlayerCard(int cardId) {
		return "PROPERTY_HAVECARD" + cardId;
	}
	
	/** 记录玩家收藏的卡片的品质 0普通品质 1闪卡 */
	public static String getPropertyOfCardQuality(int cardId) {
		return "PROPERTY_CARD_QUALITY" + cardId;
	}
	
	/**
	 * 记录玩家镶嵌的卡片的品质 0普通品质 1闪卡
	 * @param cardId,卡片ID
	 * @param equItemId,卡片所属装备ID
	 * @param equInstanceId,卡片所属装备instanceId
	 * @return
	 */
	public static String getPropertyOfAddCardQuality(int cardId, int equItemId, int equInstanceId) {
		return "PROPERTY_CARD_QUALITY" + cardId + "_" + equItemId + "_" + equInstanceId;
	}

	/** 记录玩家收藏特定套装张数属性 */
	public static String getPropertyOfPlayerSuit(int groupId) {
		return "PROPERTY_HAVECARD_OFSUIT" + groupId;
	}

	/** 记录玩家使用集卡名录的属性 0不显示卡片名称 >=1显示卡片名称 */
	public static String getPropertyOfShowCardName(int groupId) {
		return "PROPERTY_SHOWCARDNAME" + groupId;
	}
	
	/** 记录卡片能量值的pool属性名称 */
	public static String getPropertyOfCardEnergy(int cardId){
		return "PROPERTY_CARDENERGY" + cardId;
	}
	
	/**
	 * 卡片附魔时间，效果BUFF生效时间的pool属性名称
	 * @param cardId,卡片ID
	 * @param equItemId,卡片所属装备ID
	 * @param equInstanceId,卡片所属装备instanceId
	 * @return
	 */
	public static String getPropertyOfCardAddToEquTime(int cardId, int equItemId, int equInstanceId){
		return "PROPERTY_CARDCOLLECT_TIME" + cardId + "_" +  equItemId + "_" + equInstanceId;
	}
	
	/** 随机生成卡片能量值 */
	public int generateCardEnergy(Player player, Card card){
		int minEnergy = card.energy_min;
		int maxEnergy = card.energy_max;
		int energy = rnd.nextInt(maxEnergy-minEnergy+1) + minEnergy;
		return energy;
	}
	
	/** 卡片充能 */
	public void addCardEnergy(Player p, Card card, int energy, boolean isFlash){
		if(p!=null){
			int currentEnergy = getCardEnergy(p, card.id, false);
			int maxEnergy = getCardEnergy(p, card.id, true);
			if(isFlash)
				maxEnergy = MAX_FLASHCARD_ENERGY;
			else
				maxEnergy = MAX_GENERALCARD_ENERGY;
			if(energy + currentEnergy >= maxEnergy)
				setCardEnergy(p, card.id, maxEnergy, maxEnergy);
			else
				setCardEnergy(p, card.id, energy + currentEnergy, maxEnergy);
		}
	}
	
	/** 高16位为当前卡片能量，低16位为最大卡片能量值 */
	public void setCardEnergy(Player player, int cardId, int currentEnergy, int maxEnergy){
		if(player!=null){
			String poolProperty = getPropertyOfCardEnergy(cardId);
			int value = (currentEnergy<<16) | (maxEnergy);
			player.pool.setInt(poolProperty, value);
		}
	}
	
	/**
	 * 读取卡片能量
	 * @param player,玩家角色ID
	 * @param cardId,卡片ID
	 * @param max,true为最大值,false为当前值
	 * @return
	 */
	public int getCardEnergy(Player player, int cardId,boolean max){
		if(player!=null){
			String poolProperty = getPropertyOfCardEnergy(cardId);
			int value = player.pool.getInt(poolProperty, 0);
			if(max)
				value = value & 0xFF;
			else
				value = value>>16;
			return value<0 ? 0 : value;
		}
		return 0;
	}

	/** 根据卡片id得到卡片名字 */
	public Card getCardByCardId(int cardId) {
		return allCards.get(cardId);
	}
	
	/** 对输入的cardId进行排序并以字符串形式输出 */
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
	 * 得到物品对应的卡片id
	 * @param itemId,物品ID
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
	 * @param groupId,卡片系列ID
	 * @return
	 */
	public CardGroup getCardGroup(int groupId) {
		return cardGroups.get(groupId);
	}

	/**
	 * 物品id查找卡片
	 * @param itemId,物品ID
	 * @return,卡品信息
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
	
	/**
	 * 判断是有图卡片还是无图卡片
	 * @param card
	 * @return true有图,false无图
	 */
	public boolean isPictureCard(Card card){
		return card.res.equals("") ? false : true;
	}
	
	/** 获取所有卡片BUFF的ID集合 */
	public List<Integer> getCardBuffs(){
		if(allCardBuffs!=null && allCardBuffs.size()>0)
			return allCardBuffs;
		for(Card c : allCards.values()){
			if(c!=null)
				allCardBuffs.add(c.buffId);
		}
		return allCardBuffs;
	}
	
	public boolean isCardBuff(int buffId){
		for(int buff : getCardBuffs()){
			if(buff==buffId)
				return true;
		}
		return false;
	}
	
	/**
	 * 添加卡片BUFF效果
	 * @param player,玩家
	 * @param cardId,卡片ID
	 * @param ownerType,主人类型
	 * @param ownerInstanceId,主人instanceId
	 * @param equItemId,装备ID
	 */
	public void addCardBuff(Player player, int cardId, int ownerType, int ownerInstanceId, int equItemId, int equInstanceId){
		if(player!=null){
			Card card = getCardByCardId(cardId);
			long buffDuration = card.buffDuration * 3600 * 1000L;
			long buffAddToEquTime = player.pool.getLong(getPropertyOfCardAddToEquTime(cardId,equItemId,equInstanceId), 0);
			if(buffAddToEquTime>0 && System.currentTimeMillis()-buffAddToEquTime<buffDuration){
				int buffId = card.buffId;
				int buffLevel1 = card.buffLevel1;
				int buffLevel2 = card.buffLevel2;
				boolean isFlash = player.pool.getInt(getPropertyOfAddCardQuality(cardId,equItemId,equInstanceId), 1)==1;
				int buffLevel = isFlash ? buffLevel2 : buffLevel1;
				Buff buff = BuffUtil.createSuiteBuff(buffId, buffLevel);
				if(ownerType==PropertyCalculator.TYPE_ATTENDANT && player.attendant!=null && player.attendant.getInstanceId()==ownerInstanceId){
					player.attendant.buffs.addBuff(buff);
				}else{
					player.buffs.addBuff(buff);
				}
			}
		}
	}
	
	/** 获取已生效的卡片BUFF描述 */
	public String getBuffDesc(int cardId, int buffLevel){
		String key = cardId + "_" + buffLevel;
		Buff buff = effectCardBuffs.get(key);
		if(buff!=null){
			return buff.getDesc();
		}
		return peony.Messages.STRING_00064;
	}
	
	public int getFormulaCount(int itemId, int cardId){
		itemId = getCardId(itemId);
		Material[] ms = materials.get(cardId);
		if(ms!=null && ms.length>0){
			for(Material m : ms){
				if(m.itemId==itemId)
					return m.value;
			}
		}
		return -1;
	}
	
	/**
	 * 卡片是否已收集
	 * @param player
	 * @param cardId
	 * @return
	 */
	public boolean hasMatch(Player player, int cardId){
//		boolean hasMatch = player.pool.getInt(getPropertyOfPlayerCard(cardId),0) >= 1;
//	    return hasMatch;
		return getCardCount(player, cardId)>=1;
	}
	
	public boolean hasMatchOld(Player player,int cardId){
		boolean hasMatch = player.pool.getInt(getPropertyOfPlayerCard(cardId),0) >= 1;
	    return hasMatch;
	}
	
	/***
	 * 获取玩家卡片列表数量
	 * @param player
	 * @return
	 */
	public static Map<Integer,Integer> getAllCardsInfo(Player player){
		Map<Integer,Integer> allCardInfo=new HashMap<Integer, Integer>();
		String cardInfos=player.pool.getString(CardService.CARD_NEW_PROPERTY);
		if(cardInfos!=null){
			String[] allCards=cardInfos.split("\\|");
			for(String cardInfo:allCards){
				if(cardInfo==null){
					continue;
				}
				try{
					String[] card=cardInfo.split(",");
					int cardIdTemp=Integer.parseInt(card[0]);
					int cardCount=Integer.parseInt(card[1]);
					allCardInfo.put(cardIdTemp, cardCount);
				}catch(Exception e){}
			}
		}
		return allCardInfo;
	}
	
	/***
	 * 获取卡片数量的新方法
	 * @param player
	 * @param cardId
	 * @return
	 */
	public static int getCardCount(Player player,int cardId){
		String cardInfos=player.pool.getString(CardService.CARD_NEW_PROPERTY);
		if(cardInfos!=null){
			String[] allCards=cardInfos.split("\\|");
			for(String cardInfo:allCards){
				if(cardInfo==null){
					continue;
				}
				try{
					String[] card=cardInfo.split(",");
					int cardIdTemp=Integer.parseInt(card[0]);
					int cardCount=Integer.parseInt(card[1]);
					if(cardIdTemp==cardId){
						return cardCount;
					}
				}catch(Exception e){}
			}
		}
		return 0;
	}
	
	/**
	 * 玩家增加卡片
	 * @param player
	 * @param cardId
	 * @param count
	 * @return    返回当前卡片数量
	 */
	public static int addCard(Player player,int cardId,int count){
		String cardInfos=player.pool.getString(CardService.CARD_NEW_PROPERTY);
		int cardCountReturn=0;
		StringBuffer sb=new StringBuffer();
		if(cardInfos!=null){
			String[] allCards=cardInfos.split("\\|");
			boolean hasMatch=false;
			for(String cardInfo:allCards){
				if(cardInfo==null){
					continue;
				}
				String spliteStr="";
				if(sb.toString().length()>0){
					spliteStr="|";
				}
				try{
					String[] card=cardInfo.split(",");
					int cardIdTemp=Integer.parseInt(card[0]);
					int cardCount=Integer.parseInt(card[1]);
					if(cardIdTemp==cardId){
						card[1]=(cardCount+count)+"";
						hasMatch=true;
						cardCountReturn=cardCount+count;
					}
					sb.append(spliteStr+card[0]+","+card[1]);
				}catch(Exception e){}
			}
			if(!hasMatch){//此卡片没有拥有时添加到记录里
				String spliteStr="";
				if(sb.toString().length()>0){
					spliteStr="|";
				}
				sb.append(spliteStr+cardId+","+count);
				cardCountReturn=count;
			}
			player.pool.setString(CardService.CARD_NEW_PROPERTY,sb.toString());
		}
		return cardCountReturn;
	}
	public static int addCard_Fame(Fame player,int cardId,int count){
		String cardInfos=player.pool.getString(CardService.CARD_NEW_PROPERTY);
		int cardCountReturn=0;
		StringBuffer sb=new StringBuffer();
		if(cardInfos!=null){
			String[] allCards=cardInfos.split("\\|");
			boolean hasMatch=false;
			for(String cardInfo:allCards){
				if(cardInfo==null){
					continue;
				}
				String spliteStr="";
				if(sb.toString().length()>0){
					spliteStr="|";
				}
				try{
					String[] card=cardInfo.split(",");
					int cardIdTemp=Integer.parseInt(card[0]);
					int cardCount=Integer.parseInt(card[1]);
					if(cardIdTemp==cardId){
						card[1]=(cardCount+count)+"";
						hasMatch=true;
						cardCountReturn=cardCount+count;
					}
					sb.append(spliteStr+card[0]+","+card[1]);
				}catch(Exception e){}
			}
			if(!hasMatch){//此卡片没有拥有时添加到记录里
				String spliteStr="";
				if(sb.toString().length()>0){
					spliteStr="|";
				}
				sb.append(spliteStr+cardId+","+count);
				cardCountReturn=count;
			}
			player.pool.setString(CardService.CARD_NEW_PROPERTY,sb.toString());
		}
		return cardCountReturn;
	}
	
	
	/**
	 * 移除旧卡片系统存放在pool中的数据
	 * @param player
	 * @param propertyPrefix
	 */
	public void removePoolOfCard(Player player, String propertyPrefix){
		if(player!=null){
			player.pool.deleteByPrefix(propertyPrefix);
		}
	}
	
	/**
	 * 经验重置
	 * @param player
	 * @param cardInfo
	 */
	public void removeCardExp(Player player, CardInfo cardInfo) throws CardException {
		Card card = getCardByCardId(cardInfo.cardId);
		int cardLevel = cardInfo.level;
		if(cardLevel<=1)
			throw new CardException("一级卡片无法重置");
		GameItem cardItem = ObjectAccessor.createGameItem(card.itemId);
		int quality = cardItem.template.quality;
		int totalExp = CardUpGradeCall.getTotalExp(cardLevel, quality);
//		int decCardExp = 0;
//		try {decCardExp = CardUpGradeCall.CARD_REMOVEEXP_EXP[quality];} catch (Exception e1) {}
		if(totalExp==0 /*|| decCardExp>totalExp*/)
			throw new CardException("该张卡片经验不足以进行重置");
		cardInfo.level = 1;
		try {player.cards.addExp(totalExp/*-decCardExp*/);} catch (Exception e) {}
		LogUtil.logRemoveCardExp(player, card.id);
	}
	
	public void reservedRemoveCardExp(Player player, CardInfo cardInfo) throws CardException {
		Card card = getCardByCardId(cardInfo.cardId);
		int cardLevel = cardInfo.level;
		if(cardLevel<2)
			throw new CardException("该卡片无法重置");
		GameItem cardItem = ObjectAccessor.createGameItem(card.itemId);
		int quality = cardItem.template.quality;
		int totalExp = CardUpGradeCall.getTotalExpOfOld(cardLevel, quality);
		if(totalExp==0)
			throw new CardException("该张卡片经验不足以进行重置");
		cardInfo.level = 0;
		try {player.cards.addExp(totalExp);} catch (Exception e) {}
	}
	
	public void processRemoveCardExp(Player player){
		if(player.cards!=null && player.pool.getInt(PROPERTY_CLEAROLDCARDDATA, 0)==0){
			//清理旧的卡片数据
			for(int index=0;index<player.cards.equipCards.length;index++){
				try {
					player.cards.unequpPlayerCard(index);
				} catch (CardException e) {
				}
			}
			for(int index=0;index<player.cards.horseEquipCards.length;index++){
				try {
					player.cards.unequpHorseCard(index);
				} catch (CardException e) {
				}
			}
			for(CardInfo info : player.cards.cardInfos.values()){
				try {
					reservedRemoveCardExp(player, info);
				} catch (CardException e) {
				}
			}
			player.pool.setInt(PROPERTY_CLEAROLDCARDDATA, 1);
		}
	}
	
	public void enhanceCardValue(PropertyCalculator pc, int cardLevel, int cardPropertyType, int baseValue, int upLevelValue,int quality){
//		int value = baseValue + upLevelValue * (cardLevel-1);
//		value = cardLevel<1 ? 0 : value;
		int value = 0;
		if(quality==Item.QUALITY_WHITE)
			value = WHITE_ENHANCE_VALUES[cardLevel-1];
		else if(quality==Item.QUALITY_GREEN)
			value = GREEN_ENHANCE_VALUES[cardLevel-1];
		else if(quality==Item.QUALITY_BLUE)
			value = BLUE_ENHANCE_VALUES[cardLevel-1];
		else if(quality==Item.QUALITY_PURPLE)
			value = PURPLE_ENHANCE_VALUES[cardLevel-1];
		else if(quality==Item.QUALITY_ORANGE)
			value = ORANGE_ENHANCE_VALUES[cardLevel-1];
		if(cardPropertyType==0){
			pc.strength += value;
		}else if(cardPropertyType==1){
			pc.agility += value;
		}else if(cardPropertyType==2){
			pc.intellect += value;
		}else if(cardPropertyType==7){
			pc.stamina += value;
		}
	}
	
	/**
	 * 计算卡片升级所需经验
	 * @param cardId
	 * @param cardLevel
	 * @return
	 */
	public int getEnhanceValue(int cardId, int cardLevel, int quality){
		Card card = getCardByCardId(cardId);
		if(card!=null){
			if(cardLevel<1 || quality==-1)
				return 0;
//			return card.propertyBaseValue + card.propertyUpLevelValue * (cardLevel-1);
			int value = 0;
			if(quality==Item.QUALITY_WHITE)
				value = WHITE_ENHANCE_VALUES[cardLevel-1];
			else if(quality==Item.QUALITY_GREEN)
				value = GREEN_ENHANCE_VALUES[cardLevel-1];
			else if(quality==Item.QUALITY_BLUE)
				value = BLUE_ENHANCE_VALUES[cardLevel-1];
			else if(quality==Item.QUALITY_PURPLE)
				value = PURPLE_ENHANCE_VALUES[cardLevel-1];
			else if(quality==Item.QUALITY_ORANGE)
				value = ORANGE_ENHANCE_VALUES[cardLevel-1];
			return value;
		}
		return 0;
	}
	
	/**
	 * 获取卡片属性增强描述
	 * @param cardId
	 * @param cardLevel
	 * @return
	 */
	public String getEnhanceDesc(int cardId, int cardLevel){
		try {
			Card card = getCardByCardId(cardId);
			int quality = -1;
			try {
				quality = ObjectAccessor.createGameItem(card.itemId).template.quality;
			} catch (Exception e) {
			}
			return MessageFormat.format("{0} +{1}", Card.PROPERTY_TYPE_NAMES[card.prorertyType], 
					getEnhanceValue(cardId, cardLevel, quality));
		} catch (Exception e) {
			return "暂无";
		}
	}

	/**
	 * 从玩家已镶嵌的卡片中获取卡片信息
	 * @param player
	 * @param cardId
	 * @return
	 */
	public CardInfo getEquipCardInfo(Player player, int cardId){
		if(player!=null){
			CardInfo cardInfo = player.cards.getEquipCardInfoByCardId(cardId);
			if(cardInfo!=null)
				return cardInfo;
		}
		return null;
	}
	
	public void dayChanged() {
		CardRockCall.clearPrayRecord();
		Server.server.getServiceRegistry().getRankingService().resetRanking();
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_PLAYER_FIRSTLOAD,
				ServiceEvent.EVENT_PLAYER_LOGOUTED
		};
	}


	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
			processPlayerLoad((Player)event.param1);
		case ServiceEvent.EVENT_PLAYER_LOGOUTED:
			processPlayerLogOuted((Player)event.param1);
		}
	}
	
	protected void processPlayerLoad(Player player){
		if(player!=null){
			removePoolOfCard(player, "PROPERTY_CARDENERGY"); //移除卡片能量pool
			removePoolOfCard(player, "PROPERTY_CARD_QUALITY"); //移除卡片品质pool
		}
	}
	
	/**
	 * 处理历史遗留卡片
	 * @param player
	 * @param item
	 * @param count
	 */
	public void processHistoryCards(Player player, GameItem item, int count){
		try {
			if(player!=null){
				if(item!=null && item.template.itemType==Item.TYPE_CARD){
					int qulity = item.template.quality;
					PlayerTransaction tx = player.newTransaction("CARDCREDIT");
					try {
						if(qulity==0)
							player.addCredit(1*count, tx, true);
						else if(qulity==1)
							player.addCredit(5*count, tx, true);
						else if(qulity==2)
							player.addCredit(20*count, tx, true);
						else if(qulity==3)
							player.addCredit(50*count, tx, true);
						tx.commit();
					} catch (TransactionException e) {
						tx.rollback();
					}
				}
			}
		} catch (TransactionException e) {
			
		}
	}
	
	/**
	 * 摇卡获得经验排行榜
	 * @param player
	 */
	
	public void processPlayerLogOuted(Player player){
		if(player!=null){
			if(player.cardExpAdd!=0){
				player.pool.setInt(PROPERTY_ADDCARDEXP_EVERYDAY, player.cardExpAdd);
			}
			if(player.rockCardCount!=0){
				player.pool.setInt(PROPERTY_ROCKCARDCOUNT_EVERYDAY, player.rockCardCount);
			}
			if(player.prayCount!=0){
				player.pool.setInt(RankingService.PROPERTY_PRAYCOUNT_EVERYDAY, player.prayCount);
			}
		}
	}
	
	/**
	 * 初始化玩家当天的摇卡经验
	 * @param p
	 */
	public synchronized void initExpAdded(Player p){
		int oldExp = p.pool.getInt(PROPERTY_ADDCARDEXP_EVERYDAY,0);
		if(oldExp == 0){
			p.pool.remove(PROPERTY_ADDCARDEXP_EVERYDAY);
		}else{
			p.pool.setInt(PROPERTY_ADDCARDEXP_EVERYDAY, 0);
			p.cardExpAdd = 0;
		}
		int oldCount = p.pool.getInt(PROPERTY_ROCKCARDCOUNT_EVERYDAY,0);
		if(oldCount == 0){
			p.pool.remove(PROPERTY_ROCKCARDCOUNT_EVERYDAY);
		}else{
			p.pool.setInt(PROPERTY_ROCKCARDCOUNT_EVERYDAY, 0);
			p.rockCardCount = 0;
		}
		p.pool.remove(RankingService.PROPERTY_ADDPRAY_EVERYDAY);
		p.payForPray=0;
		p.pool.remove(RankingService.PROPERTY_PRAYCOUNT_EVERYDAY);
		p.prayCount = 0;
	}
	
	public void processRanking(Player player){
		player.cardExpAdd = player.pool.getInt(PROPERTY_ADDCARDEXP_EVERYDAY, 0);
		player.rockCardCount = player.pool.getInt(PROPERTY_ROCKCARDCOUNT_EVERYDAY, 0);
//        Server.server.getServiceRegistry().getRankingService().playerLoadRockCount(player);
        player.payForPray = player.pool.getInt(RankingService.PROPERTY_ADDPRAY_EVERYDAY, 0);
        player.prayCount = player.pool.getInt(RankingService.PROPERTY_PRAYCOUNT_EVERYDAY, 0);
//        Server.server.getServiceRegistry().getRankingService().playerLoadPrayCount(player);
	}

}

