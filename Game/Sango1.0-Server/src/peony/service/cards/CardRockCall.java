package peony.service.cards;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.pip.sanguo.data.Card;
import com.pip.sanguo.data.item.Item;
import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.Gain;
import peony.game.GainItem;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.NoEnoughValueException;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.buff.Buff;
import peony.game.buff.BuffUtil;
import peony.game.drop.DropGroupUtil;
import peony.game.drop.GroupDrop;
import peony.game.salary.SalaryService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.VIP.VipPrivilegeService;
import peony.service.ranking.CardExpRankingCall;
import peony.util.IntHashMap;

/**
 * 摇卡片
 * @author dchen
 */
public class CardRockCall extends ClientSessionAsyncCall {

	public static Random random = new Random();
	
	/** 摇卡片扣除战功系数 */
	public static int decCreditOfRockCard = 65;
	
	/** 免费摇卡片掉落组 */
	public static int freeRockDropId = 1074;
	/** 战功摇卡片掉落组(1~3次) */
	public static int creditRockDropId1 = 1074;
	/** 战功摇卡片掉落组(4~6次) */
	public static int creditRockDropId2 = 1075;
	/** 战功摇卡片掉落组(7~9次) */
	public static int creditRockDropId3 = 1076;
	/** 战功摇卡片掉落组(10次以上) */
	public static int creditRockDropId4 = 1077;
	
	/** 免费摇卡片方式 */
	public static final int ROCK_TYPE_FREE = 0;
	/** 战功摇卡片方式 */
	public static final int ROCK_TYPE_CREDIT = 1;
	/** 元宝摇卡片方式 */
	public static final int ROCK_TYPE_IMONEY = 2;
	
	/** 每棵树免费摇卡片的每天免费次数 */
	public static int freeLimit = 3;
	
	/** 所有树战功摇卡片的每天限制次数 */
	public static int creditLimit = 50;
	
	/** 摇到白色品质卡片获得经验 */
	public static int QUALITY_WHITE_EXP = 2;
	/** 摇到绿色品质卡片获得经验 */
	public static int QUALITY_GREEN_EXP = 4;
	/** 摇到蓝色品质卡片获得经验 */
	public static int QUALITY_BLUE_EXP = 8;
	/** 摇到紫色品质卡片获得经验 */
	public static int QUALITY_PURPLE_EXP = 16;
	/** 摇到橙色品质卡片获得经验 */
	public static int QUALITY_ORANGE_EXP = 32;
	
	/** 获得卡片经验系数 */
	public static float GAINCARDEXPRATIO = 1f;
	
	/**免费摇卡次数*/
	public static int FREEROCKCARDCOUNT=6;
	
	//四棵祈福树对应的玩家免费摇卡片次数  key:playerId,value:摇卡片次数
	public static IntHashMap<Integer> freeCountOfPlayer1 = new IntHashMap<Integer>();
	public static IntHashMap<Integer> freeCountOfPlayer2 = new IntHashMap<Integer>();
	public static IntHashMap<Integer> freeCountOfPlayer3 = new IntHashMap<Integer>();
	public static IntHashMap<Integer> freeCountOfPlayer4 = new IntHashMap<Integer>();
	
	//四棵祈福树对应的玩家战功摇卡片次数  key:playerId,value:摇卡片次数
	public static IntHashMap<Integer> creditCountOfPlayer1 = new IntHashMap<Integer>();
	public static IntHashMap<Integer> creditCountOfPlayer2 = new IntHashMap<Integer>();
	public static IntHashMap<Integer> creditCountOfPlayer3 = new IntHashMap<Integer>();
	public static IntHashMap<Integer> creditCountOfPlayer4 = new IntHashMap<Integer>();
	
	//四棵祈福树对应的玩家元宝摇卡片次数  key:playerId,value:摇卡片次数
	public static IntHashMap<Integer> iMoneyCountOfPlayer1 = new IntHashMap<Integer>();
	public static IntHashMap<Integer> iMoneyCountOfPlayer2 = new IntHashMap<Integer>();
	public static IntHashMap<Integer> iMoneyCountOfPlayer3 = new IntHashMap<Integer>();
	public static IntHashMap<Integer> iMoneyCountOfPlayer4 = new IntHashMap<Integer>();
	
	/** 每颗树对应的免费摇树集合 */
	public static IntHashMap<IntHashMap<Integer>> npcToFreeMap = new IntHashMap<IntHashMap<Integer>>();
	
	/** 每颗树对应的战功摇树集合 */
	public static IntHashMap<IntHashMap<Integer>> npcToCreditMap = new IntHashMap<IntHashMap<Integer>>();
	
	/** 每颗树对应的元宝摇树集合 */
	public static IntHashMap<IntHashMap<Integer>> npcToImoneyMap = new IntHashMap<IntHashMap<Integer>>();
	
	/**每天面板摇树对应的战功摇树集合*/
	public static IntHashMap<Integer> freeCountOfOwner = new IntHashMap<Integer>();
	public static IntHashMap<Integer> creditCountOfOwner = new IntHashMap<Integer>();
	
	static{
		npcToFreeMap.put(1114209, freeCountOfPlayer1);
		npcToFreeMap.put(983138, freeCountOfPlayer2);
		npcToFreeMap.put(1441882, freeCountOfPlayer3);
		npcToFreeMap.put(8257662, freeCountOfPlayer4);
		
		npcToCreditMap.put(1114209, creditCountOfPlayer1);
		npcToCreditMap.put(983138, creditCountOfPlayer2);
		npcToCreditMap.put(1441882, creditCountOfPlayer3);
		npcToCreditMap.put(8257662, creditCountOfPlayer4);
		
		npcToImoneyMap.put(1114209, iMoneyCountOfPlayer1);
		npcToImoneyMap.put(983138, iMoneyCountOfPlayer2);
		npcToImoneyMap.put(1441882, iMoneyCountOfPlayer3);
		npcToImoneyMap.put(8257662, iMoneyCountOfPlayer4);
		
		npcToFreeMap.put(-1, freeCountOfOwner);
		npcToCreditMap.put(-1, creditCountOfOwner);
		
	}
	public static int[] npcIds={1114209,983138,1441882,8257662,-1};
	public int getFreeRockCardCount(){
		int count=0;
		for(int i=0;i<npcIds.length;i++){
			IntHashMap<Integer> prayCountOfPlayer1=npcToFreeMap.get(npcIds[i]);
			count+=(prayCountOfPlayer1.get(player.id)==null?0:prayCountOfPlayer1.get(player.id));
		}
		return count;
	}
	
	protected List<String> cardNames = new ArrayList<String>(); //本次摇树获得的卡片名字list
	protected List<Integer> cardQualitys = new ArrayList<Integer>(); //本次摇树获得的卡片品质list
	protected List<String> cardAttDesc = new ArrayList<String>(); //本次摇树获得的卡片属性增强描述list
	protected List<Integer> cardGroupIds = new ArrayList<Integer>(); //本次摇树获得的卡片群组(区分卡片类型)
	
	protected int cardExps = 0; //本次摇树获得的经验
	
	protected Player player;
	protected int serial;
	protected int rockType;
	protected int npcId;
	protected int limit;
	
	public CardRockCall(ClientSession session, Packet packet) {
		super(session);
		this.player = (Player)session.getClient();
		this.serial = packet.getInt();
		this.npcId = packet.getInt();
		this.rockType = packet.getByte();
		this.limit = packet.getByte();
	}
	
	public void run() {
		addToClientSession();
	}
	
	public void callFinish() throws Exception {
		if(player!=null){
			int rockCount = 0;
			int dropId = 0;
			IntHashMap<Integer> prayCountOfPlayer = null;
			if(rockType==ROCK_TYPE_FREE){
				prayCountOfPlayer = npcToFreeMap.get(npcId);
				if(prayCountOfPlayer==null){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_PRAY_CLIENT, "不合法的请求");
					return;
				}
				int allRockCount=0;
				try{
					allRockCount = getFreeRockCardCount();//prayCountOfPlayer.get(player.id);
					rockCount = prayCountOfPlayer.get(player.id);
				}catch(Exception e){}
				if(allRockCount>=freeLimit*2){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_PRAY_CLIENT, "您今日免费摇卡次数已用光，请改日再来");
					return;
				}
				dropId = freeRockDropId;
				try {
					processGain(dropId, rockCount, ROCK_TYPE_FREE, prayCountOfPlayer);
				} catch (CardException e) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_PRAY_CLIENT, e.getMessage());
					if(cardNames.size()==0)
						return;
				}
			}else if(rockType==ROCK_TYPE_CREDIT){
				int total = getTotalCreditRockCount(rockType, player.id);
				int count = total+1;
				int upLimit = creditLimit;
				if(player.vipLevel>=5){
					upLimit = VipPrivilegeService.ROCKCARD_UPLIMIT;
				}
				if(total>=upLimit){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_PRAY_CLIENT, "已达到本日战功摇卡上限");
					return;
				}
				for(int x=0;x<limit;x++){
					prayCountOfPlayer = npcToCreditMap.get(npcId);
					if(prayCountOfPlayer==null){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_PRAY_CLIENT, "不合法的请求");
						return;
					}
					try{rockCount = prayCountOfPlayer.get(player.id);}catch(Exception e){}
					if(rockCount<3)
						dropId = creditRockDropId1;
					else if(rockCount<6)
						dropId = creditRockDropId2;
					else if(rockCount<9)
						dropId = creditRockDropId3;
					else
						dropId = creditRockDropId4;
					try {
						processGain(dropId, rockCount, ROCK_TYPE_CREDIT, prayCountOfPlayer);
					} catch (Exception e) {
						ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_PRAY_CLIENT, e.getMessage());
						if(cardNames.size()==0)
							return;
					}
					count++;
					if(count>upLimit){
//						ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_PRAY_CLIENT, "已达到本日战功摇卡上限");
						break;
					}
				}
			}else if(rockType==ROCK_TYPE_IMONEY){
				Server.server.getServiceRegistry().getSyncExecutorService().schedule(new ImoneyRockCardCall(session, player));
				return;
			}
			Packet pt = new Packet(OpCode.CARD_PRAY_SERVER);
			pt.putInt(serial);
			pt.put(cardNames.size());
			for(int i=0;i<cardNames.size();i++){
				pt.putUTF(cardNames.get(i));
				pt.put(cardQualitys.get(i));
				pt.putUTF(cardAttDesc.get(i));
				pt.put(cardGroupIds.get(i));
			}
			pt.putInt(cardExps);
			try{
				rockCount = getFreeRockCardCount();
			}catch(Exception e){}
			int freeCount=FREEROCKCARDCOUNT-rockCount;
			if(freeCount<=0){
				freeCount=0;
			}
			pt.put(freeCount);
			int total = getTotalCreditRockCount(ROCK_TYPE_CREDIT, player.id);
			int upLimit = creditLimit;
			if(player.vipLevel>=5){
				upLimit = VipPrivilegeService.ROCKCARD_UPLIMIT;
			}
			int count=upLimit-total;
			if(count<=0){
				count=0;
			}
			pt.putShort(count);
			session.send(pt);
			if(rockType==ROCK_TYPE_CREDIT){
				player.cardExpAdd += cardExps;
				Server.server.getServiceRegistry().getSyncExecutorService().schedule(new CardExpRankingCall(session,player));
			}
			SalaryService salaryService = Server.server.getServiceRegistry().getSalaryService();
			salaryService.processRockCardSalary(player);//摇卡工资
			Server.server.getServiceRegistry().getRankingService().rockCountReward(player);
		}
	}
	
	public static int getTotalCreditRockCount(int type, int playerId){
		int count = 0;
		if(type==ROCK_TYPE_CREDIT){
			IntHashMap<Integer> prayCountOfPlayer = npcToCreditMap.get(1114209);
			if(prayCountOfPlayer!=null){
				try { count += prayCountOfPlayer.get(playerId); } catch (Exception e) {}
			}
			prayCountOfPlayer = npcToCreditMap.get(983138);
			if(prayCountOfPlayer!=null){
				try { count += prayCountOfPlayer.get(playerId); } catch (Exception e) {}
			}
			prayCountOfPlayer = npcToCreditMap.get(1441882);
			if(prayCountOfPlayer!=null){
				try { count += prayCountOfPlayer.get(playerId); } catch (Exception e) {}
			}
			prayCountOfPlayer = npcToCreditMap.get(8257662);
			if(prayCountOfPlayer!=null){
				try { count += prayCountOfPlayer.get(playerId); } catch (Exception e) {}
			}
			prayCountOfPlayer = npcToCreditMap.get(-1);
			if(prayCountOfPlayer!=null){
				try { count += prayCountOfPlayer.get(playerId); } catch (Exception e) {}
			}
		}
		return count;
	}
	
	/** 处理卡片掉落 */
	protected void processGain(int dropId, int rockCount, int type, IntHashMap<Integer> prayCountOfPlayer) throws CardException {
		CardService service = Server.server.getServiceRegistry().getCardService();
		if(type==ROCK_TYPE_CREDIT){
			PlayerTransaction tx = player.newTransaction("ROCKCARD");
			try {
//				int count = rockCount + 1;
//				count = count >=10 ? 10 : count;
//				player.decCredit(decCreditOfRockCard * count, tx, true);
				player.decCredit(decCreditOfRockCard, tx, true);
				tx.commit();
			} catch (NoEnoughValueException e) {
				tx.rollback();
				throw new CardException("您的战功不足，您可以通过任务或消费元宝获得战功。");
			}
		}
		GroupDrop drop = DropGroupUtil.getGroupDrop(dropId);
		Gain gain = new Gain(player);
		drop.calc(random, gain);
		CardService cs = Server.server.getServiceRegistry().getCardService();
		GainItem[] gainItems = gain.getGainItems();
		for(GainItem gainItem : gainItems){
			if(gainItem==null)
				continue;
			GameItem item = gainItem.getItem();
			PlayerTransaction tx1 = player.newTransaction("ROCKCARD");
			try {
				if(item!=null){
					player.bag.addGameItemComplete(item, gainItem.getCount(), tx1, true);
					//sch add
					Server.server.getServiceRegistry().getStatService().playerCollectCard(player, cs.getCardByItemId(item.template.id).suiteId);
					tx1.commit();
					CardService cardService = Server.server.getServiceRegistry().getCardService();
					Card card = cardService.getCardByItemId(item.template.id);
					//增加经验
					int quality = item.template.quality;
					int cardExp = 0;
					if(quality==Item.QUALITY_WHITE)
						cardExp = QUALITY_WHITE_EXP;
					else if(quality==Item.QUALITY_GREEN)
						cardExp = QUALITY_GREEN_EXP;
					else if(quality==Item.QUALITY_BLUE)
						cardExp = QUALITY_BLUE_EXP;
					else if(quality==Item.QUALITY_PURPLE)
						cardExp = QUALITY_PURPLE_EXP;
					else if(quality==Item.QUALITY_ORANGE)
						cardExp = QUALITY_ORANGE_EXP;
					if(card.prorertyType==Card.PROPERTY_TYPE_SKILL){//如果是技能卡片默认给蓝色品质的卡片经验
						cardExp = QUALITY_BLUE_EXP;
					}
					cardExp *= GAINCARDEXPRATIO;
					player.cards.addExp(cardExp);
					//测试用聊天
//					Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, 
//							MessageFormat.format("本次摇得{0},品质{1},获得经验{2}", item.template.name, quality, cardExp));
					if(quality==Item.QUALITY_ORANGE){
						Server.server.getServiceRegistry().getChatService().sendWorldMessage(MessageFormat.
								format("{0}摇树意外得到一张{1}，赢得众人羡慕的目光，默默收入囊中，心里早已乐开了花。", player.name, item.template.name));
					}
					LogUtil.logVowCard(player, item.template.id, type, quality, cardExp);
//					Card card = service.getCardByItemId(item.template.id);
					if(card!=null)
						cardNames.add(card.title);
					else
						cardNames.add("无名卡片");
					cardQualitys.add(item.template.quality);
					if(card!=null){
						cardGroupIds.add(card.prorertyType);
					}else{
						cardGroupIds.add(0);
					}
					
					CardInfo info = cs.getEquipCardInfo(player, card.id);
					if(info==null){
						info = player.cards.getUnEquipCardInfo(card.id);
					}
//					if(info!=null){
//						cardAttDesc.add(cs.getEnhanceDesc(info.cardId, info.level));
//					}else{
//						cardAttDesc.add(cs.getEnhanceDesc(card.id, 0));
//					}
					if(info!=null){
						String desc1=MessageFormat.format("等级：{0}\n当前属性：{1}", info.level,cs.getEnhanceDesc(info.cardId, info.level));
						cardAttDesc.add(desc1);
					}else{
						String desc1=MessageFormat.format("等级：{0}\n当前属性：{1}", 1,cs.getEnhanceDesc(card.id, 0));
						cardAttDesc.add(desc1);
					}
					cardExps += cardExp;
					player.rockCardCount++;
				}
			} catch (Exception e) {
				tx1.rollback();
				error(e.getMessage());
			}
		}
		try {
			prayCountOfPlayer.put(player.id, rockCount+1);
		} catch (Exception e) {
			
		}
	}
	
	/** 清除摇卡片的次数记录 */
	public static void clearPrayRecord(){
		freeCountOfPlayer1.clear();
		freeCountOfPlayer2.clear();
		freeCountOfPlayer3.clear();
		freeCountOfPlayer4.clear();
		
		creditCountOfPlayer1.clear();
		creditCountOfPlayer2.clear();
		creditCountOfPlayer3.clear();
		creditCountOfPlayer4.clear();
		
		iMoneyCountOfPlayer1.clear();
		iMoneyCountOfPlayer2.clear();
		iMoneyCountOfPlayer3.clear();
		iMoneyCountOfPlayer4.clear();
		
		freeCountOfOwner.clear();
		creditCountOfOwner.clear();
	}

}
