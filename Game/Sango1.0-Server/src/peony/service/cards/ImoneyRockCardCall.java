package peony.service.cards;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import com.pip.sanguo.data.Card;
import com.pip.sanguo.data.item.Item;
import peony.common.SyncIbuyCall;
import peony.game.ErrorHandler;
import peony.game.Gain;
import peony.game.GainItem;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
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
import peony.service.shop.NoItemShopBuy;
import peony.service.shop.ShopService;

public class ImoneyRockCardCall extends SyncIbuyCall {

	protected Player player;
	protected int serial;
	public static int decImoneyAgentItem = 4220; //元宝摇卡片扣除价格代扣物品
	public static int decImoneyAgentItemOftw = 1264; //元宝摇卡片扣除价格代扣物品
	protected List<String> cardNames = new ArrayList<String>(); //本次摇树获得的卡片名字list
	protected List<Integer> cardQualitys = new ArrayList<Integer>(); //本次摇树获得的卡片品质list
	protected List<String> cardAttDesc = new ArrayList<String>(); //本次摇树获得的卡片名字list
	protected List<Integer> cardGroupIds = new ArrayList<Integer>(); //本次摇树获得的卡片群组ID
	protected int cardExps = 0; //本次摇树获得的经验
	public static int dropId = 1182;//1077; //元宝摇卡片掉落组
	
	public ImoneyRockCardCall(ClientSession session, Player player) {
		super(session, null);
		this.player = player;
	}

	public void callFinish() throws Exception {
		if(success){
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
			int freeCount = 0;
			try {
				int count=0;
				for(int i=0;i<CardRockCall.npcIds.length;i++){
					peony.util.IntHashMap<Integer> prayCountOfPlayer1=CardRockCall.npcToFreeMap.get(CardRockCall.npcIds[i]);
					count+=prayCountOfPlayer1.get(player.id)==null?0:prayCountOfPlayer1.get(player.id);
				}
				freeCount=CardRockCall.FREEROCKCARDCOUNT-count;
				if(freeCount<=0){
					freeCount=0;
				}
//				freeCount = CardRockCall.npcToFreeMap.get(npcId).get(player.id);
			} catch (Exception e) {
				e.printStackTrace();
			}
			pt.put(freeCount);
			int total = CardRockCall.getTotalCreditRockCount(CardRockCall.ROCK_TYPE_CREDIT, player.id);
			int upLimit = CardRockCall.creditLimit;
			if(player.vipLevel>=5){
				upLimit = VipPrivilegeService.ROCKCARD_UPLIMIT;
			}
			int count=upLimit-total;
			if(count<=0){
				count=0;
			}
			pt.putShort(count);
			session.send(pt);
			player.cardExpAdd += cardExps;
			player.rockCardCount+=cardNames.size();
			Server.server.getServiceRegistry().getSyncExecutorService().schedule(new CardExpRankingCall(session,player));
			SalaryService salaryService = Server.server.getServiceRegistry().getSalaryService();
			salaryService.processRockCardSalary(player);//摇卡工资
			Server.server.getServiceRegistry().getRankingService().rockCountReward(player);
		}else{
			if(!errorMessage.equals(peony.Messages.STRING_00405))
				ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_PRAY_CLIENT, errorMessage);
		}
	}

	public void run() {
		ShopService service = Server.server.getServiceRegistry().getShopService();
		int decItemId = decImoneyAgentItem;
		if(Server.server.revision.equalsIgnoreCase(Server.REVISION_TYPE_TW))
			decItemId = decImoneyAgentItemOftw;
		int shopId = service.getShopByItemId(decItemId).id;
		try {
			CardService cs = Server.server.getServiceRegistry().getCardService();
			waitBuy(player, serial, shopId, decItemId, 1, this);
			Player p = ObjectAccessor.getPlayer(player.id);
			for(int i=0;i<10;i++){
				GroupDrop drop = DropGroupUtil.getGroupDrop(dropId);
				Gain gain = new Gain(player);
				drop.calc(CardRockCall.random, gain);
				PlayerTransaction tx1 = player.newTransaction("ROCKCARD");
				GainItem[] gainItems = gain.getGainItems();
				getGainItems(gainItems, p, tx1, cs);
			}
			addToClientSession();
		} catch (Exception e) {
			error(peony.Messages.STRING_00405);
			addToClientSession();
			return;
		}
	}
	
	
	
	public void getGainItems(GainItem[] gainItems,Player p,PlayerTransaction tx1,CardService cs){
		for(GainItem gainItem : gainItems){
			if(gainItem==null)
				continue;
			GameItem item = gainItem.getItem();
			try {
				if(item!=null){
					if(item.template.itemType==Item.TYPE_CARD){
						int itemId = item.template.id;
						Card card = cs.getCardByItemId(itemId);
						if(card == null){
							continue;
						}
						if(card.buff2Id!=-1){//技能卡片
							CardInfo getEquipCardInfo=player.cards.getEquipCardInfoByCardId(card.id);
							CardInfo getUnEquipCardInfo=player.cards.getUnEquipCardInfo(card.id);
							if(getEquipCardInfo!=null&&getEquipCardInfo.level==12 || getUnEquipCardInfo!=null&&getUnEquipCardInfo.level==12 ){
								GroupDrop drop = DropGroupUtil.getGroupDrop(1047);//送紫卡
								Gain gain = new Gain(player);
								drop.calc(CardRockCall.random, gain);
								GainItem[] gainItems1 = gain.getGainItems();
								for(GainItem gainItemTemp : gainItems1){
									if(gainItemTemp==null)
										continue;
									item = gainItemTemp.getItem();
								}
							}
							if(item==null){
								continue;
							}
						}
					}
					if(p!=null){
					    player.bag.addGameItemComplete(item, gainItem.getCount(), tx1, true);
					    tx1.commit();
					}else{
						tx1.rollback();
						int count = gainItem.getCount();
						Server.server.getServiceRegistry().getMailService().sendSystemMailAsync(player.id, 
								peony.Messages.STRING_00004, "摇卡奖励", "", 0, item, count, "VOWROCK");
					}
					//sch add
					Server.server.getServiceRegistry().getStatService().playerCollectCard(player, cs.getCardByItemId(item.template.id).suiteId);
					
					//增加经验
					int quality = item.template.quality;
					int cardExp = 0;
					if(quality==Item.QUALITY_WHITE)
						cardExp = CardRockCall.QUALITY_WHITE_EXP;
					else if(quality==Item.QUALITY_GREEN)
						cardExp = CardRockCall.QUALITY_GREEN_EXP;
					else if(quality==Item.QUALITY_BLUE)
						cardExp = CardRockCall.QUALITY_BLUE_EXP;
					else if(quality==Item.QUALITY_PURPLE)
						cardExp = CardRockCall.QUALITY_PURPLE_EXP;
					else if(quality==Item.QUALITY_ORANGE)
						cardExp = CardRockCall.QUALITY_ORANGE_EXP;
					cardExp *= CardRockCall.GAINCARDEXPRATIO;
					player.cards.addExp(cardExp);
					if(quality==Item.QUALITY_ORANGE){
						Server.server.getServiceRegistry().getChatService().sendWorldMessage(MessageFormat.
								format("{0}摇树意外得到一张{1}，赢得众人羡慕的目光，默默收入囊中，心里早已乐开了花。", player.name, item.template.name));
					}
					LogUtil.logVowCard(player, item.template.id, 2, quality, cardExp);
					Card card = cs.getCardByItemId(item.template.id);
					if(card!=null){
						cardNames.add(card.title);
						cardGroupIds.add(card.prorertyType);
					}else{
						cardNames.add("无名卡片");
						cardGroupIds.add(0);
					}
					cardQualitys.add(item.template.quality);
					CardInfo info = cs.getEquipCardInfo(player, card.id);
					if(info==null){
						info = player.cards.getUnEquipCardInfo(card.id);
					}
					if(card.prorertyType == Card.PROPERTY_TYPE_SKILL){
						Buff skillBuff=null;
						String desc="";
						if(info!=null){
							skillBuff=BuffUtil.createBuff(card.buff2Id, info.level, player, player, 0);
						}else{
							skillBuff=BuffUtil.createBuff(card.buff2Id, 1, player, player, 0);
						}
						if(skillBuff!=null){
							desc=MessageFormat.format("等级：{0}\n当前属性：{1}", info.level,skillBuff.getDesc());
						}
						cardAttDesc.add(desc);
					}else{
						if(info!=null){
							String desc1=MessageFormat.format("等级：{0}\n当前属性：{1}", info.level,cs.getEnhanceDesc(info.cardId, info.level));
							cardAttDesc.add(desc1);
						}else{
							String desc1=MessageFormat.format("等级：{0}\n当前属性：{1}", 1,cs.getEnhanceDesc(card.id, 0));
							cardAttDesc.add(desc1);
						}
					}
//					if(info!=null){
//						cardAttDesc.add(cs.getEnhanceDesc(info.cardId, info.level));
//					}else{
//						cardAttDesc.add(cs.getEnhanceDesc(card.id, 0));
//					}
					cardExps += cardExp;
				}
			} catch (Exception e) {
				tx1.rollback();
				error(e.getMessage());
			}
		}
	}
	
	
	public static int getItemPrice(){
		ShopService shopService = Server.server.getServiceRegistry().getShopService();
		int decItemId = decImoneyAgentItem;
		if(Server.server.revision.equalsIgnoreCase(Server.REVISION_TYPE_TW))
			decItemId = decImoneyAgentItemOftw;
		int itemPrice = (int)(shopService.getItemPrice(decItemId)/36);
		return itemPrice;
	}

}
