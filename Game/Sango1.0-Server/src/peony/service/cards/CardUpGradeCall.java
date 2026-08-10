package peony.service.cards;

import java.text.MessageFormat;
import java.util.Map;

import com.pip.sanguo.data.Card;
import com.pip.sanguo.data.item.Item;
import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.buff.Buff;
import peony.game.buff.BuffUtil;
import peony.game.chat.ChatService;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * 卡片升级
 * @author dchen
 */
public class CardUpGradeCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int cardId;
	protected Player player;

	public static int[] CARD_UPGRADE_WHITE_EXP_OLD = { 8, 16, 32, 64, 128, 256,
			512, 1024, 2048, 4096, 8192, 16384 }; //白卡片升级所需经验(已过期)
	
	public static int[] CARD_UPGRADE_WHITE_EXP = { 8,
		16,
		32,
		64,
		128,
		256,
		512,
		1024,
		1673,
		2129,
		2517,
		2904 }; //白卡片升级所需经验

	public static int[] CARD_UPGRADE_GREEN_EXP_OLD = { 16, 32, 64, 128, 256, 512,
			1024, 2048, 4096, 8192, 16384, 32768 }; //绿卡片升级所需经验(已过期)
	
	public static int[] CARD_UPGRADE_GREEN_EXP = { 
		16,
		32,
		64,
		128,
		256,
		512,
		1024,
		2048,
		3346,
		4259,
		5034,
		5809 }; //绿卡片升级所需经验

	public static int[] CARD_UPGRADE_BLUE_EXP_OLD = { 32, 64, 128, 256, 512, 1024,
			2048, 4096, 8192, 16384, 32768, 65536 }; //蓝卡片升级所需经验(已过期)
	
	public static int[] CARD_UPGRADE_BLUE_EXP = { 
		32,
		64,
		128,
		256,
		512,
		1024,
		2048,
		4096,
		6692,
		8518,
		10068,
		11618 }; //蓝卡片升级所需经验

	public static int[] CARD_UPGRADE_PURPLE_EXP_OLD = { 64, 128, 256, 512, 1024,
			2048, 4096, 8192, 16384, 32768, 65536, 131072 }; //紫卡片升级所需经验(已过期)
	
	public static int[] CARD_UPGRADE_PURPLE_EXP = { 
		64,
		128,
		256,
		512,
		1024,
		2048,
		4096,
		8192,
		13384,
		17036,
		20136,
		23236 }; //紫卡片升级所需经验

	public static int[] CARD_UPGRADE_ORANGE_EXP_OLD = { 96, 192, 384, 768, 1536, 3072, 
		6144, 12288, 24576, 49152, 98304, 196608 }; //橙卡片升级所需经验(已过期)

	public static int[] CARD_UPGRADE_ORANGE_EXP = { 
		128,
		256,
		512,
		1024,
		2048,
		4096,
		6144,
		12288,
		20076,
		25554,
		30204,
		34854 }; //橙卡片升级所需经验
	
//	public static int[] CARD_REMOVEEXP_EXP = {20, 40, 80, 160, 320}; //重置经验扣除经验

	public CardUpGradeCall(ClientSession session, Packet packet) {
		super(session);
		this.player = (Player) session.getClient();
		this.serial = packet.getInt();
		this.cardId = packet.getInt();
	}

	public void run() {
		addToClientSession();
	}
	
	public static int getUpGradeExp(int quality, int currentLevel) throws Exception {
		int[] arr = null;
		if(quality==Item.QUALITY_WHITE)
			arr = CARD_UPGRADE_WHITE_EXP;
		else if(quality==Item.QUALITY_GREEN)
			arr = CARD_UPGRADE_GREEN_EXP;
		else if(quality==Item.QUALITY_BLUE)
			arr = CARD_UPGRADE_BLUE_EXP;
		else if(quality==Item.QUALITY_PURPLE)
			arr = CARD_UPGRADE_PURPLE_EXP;
		else if(quality==Item.QUALITY_ORANGE)
			arr = CARD_UPGRADE_ORANGE_EXP;
		if(arr!=null){
			try {
				return arr[currentLevel];
			} catch (Exception e) {
				return 0;
			}
		}
		return 0;
	}

	public static int getTotalExp(int cardLevel, int quality){
		int totalExp = 0;
		int[] arr = null;
		if(quality==Item.QUALITY_WHITE)
			arr = CARD_UPGRADE_WHITE_EXP;
		else if(quality==Item.QUALITY_GREEN)
			arr = CARD_UPGRADE_GREEN_EXP;
		else if(quality==Item.QUALITY_BLUE)
			arr = CARD_UPGRADE_BLUE_EXP;
		else if(quality==Item.QUALITY_PURPLE)
			arr = CARD_UPGRADE_PURPLE_EXP;
		else if(quality==Item.QUALITY_ORANGE)
			arr = CARD_UPGRADE_ORANGE_EXP;
		if(arr!=null){
			for(int i=1;i<cardLevel;i++){
				totalExp += arr[i];
			}
		}
		return totalExp;
	}
	
	public static int getTotalExpOfOld(int cardLevel, int quality){
		int totalExp = 0;
		int[] arr = null;
		if(quality==Item.QUALITY_WHITE)
			arr = CARD_UPGRADE_WHITE_EXP_OLD;
		else if(quality==Item.QUALITY_GREEN)
			arr = CARD_UPGRADE_GREEN_EXP_OLD;
		else if(quality==Item.QUALITY_BLUE)
			arr = CARD_UPGRADE_BLUE_EXP_OLD;
		else if(quality==Item.QUALITY_PURPLE)
			arr = CARD_UPGRADE_PURPLE_EXP_OLD;
		else if(quality==Item.QUALITY_ORANGE)
			arr = CARD_UPGRADE_ORANGE_EXP_OLD;
		if(arr!=null){
			for(int i=0;i<cardLevel;i++){
				totalExp += arr[i];
			}
		}
		return totalExp;
	}
	
	public void callFinish() throws Exception {
		if (player != null) {
			CardService service = Server.server.getServiceRegistry().getCardService();
			CardInfo cardInfo = service.getEquipCardInfo(player, cardId);
			if(cardInfo==null)
				cardInfo = player.cards.getUnEquipCardInfo(cardId);
			Card card = service.getCardByCardId(cardId);
			GameItem cardItem = null;
			Map<Integer,Integer> allCardInfo=CardService.getAllCardsInfo(player);
			boolean hasMatch = false;//cs.hasMatch(this, cd.id);
			if(allCardInfo.get(cardId)!=null&&allCardInfo.get(cardId)>0){
				hasMatch=true;
			}
			if((cardInfo!=null || /*service.hasMatch(player, cardId)*/hasMatch) && card!=null){
				cardItem = ObjectAccessor.createGameItem(card.itemId);
				if(cardItem!=null){
					int currentLevel = 0;
					try{currentLevel = cardInfo.level;}catch(Exception e){}
					int quality = cardItem.template.quality;
					if(currentLevel>=Cards.maxCardLevel){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_UPGRADE_CLIENT, "已满级");
						return;
					}else{
						try {
							boolean isSkillCard=false;
							int decExp = getUpGradeExp(quality, currentLevel);
							if(card.prorertyType!=Card.PROPERTY_TYPE_SKILL){
								if(decExp<=0){
									ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_UPGRADE_CLIENT, "尚未开放，敬请期待");
									return;
								}
								player.cards.decExp(decExp);
							}else{//技能卡片
//								int cardCount=player.pool.getInt(CardService.getPropertyOfPlayerCard(cardId),0);
								int cardCount=CardService.getCardCount(player, cardId);
								int needCount=getCardUpGradeNeedCount(cardInfo.level);
								int needCardExp=getSkillCardNeedExp(needCount);
								if((cardInfo.level==1&&cardCount<needCount)||cardCount==0 || (cardCount-1<needCount&&cardInfo.level>1)){
									ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_UPGRADE_CLIENT, "卡片数量不足");
									return;
								}
								if(needCardExp>player.cards.exp){
									ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_UPGRADE_CLIENT, "经验不足");
									return;
								}
								player.cards.decExp(needCardExp);
								int fixLevel=0;
								if(cardInfo.level==1){
									fixLevel=1;
								}
//								player.pool.setInt(CardService.getPropertyOfPlayerCard(cardId),player.pool.getInt(CardService.getPropertyOfPlayerCard(cardId),0)-needCount+fixLevel);
								CardService.addCard(player, cardId, -needCount+fixLevel);
								isSkillCard=true;
							}
							if(cardInfo==null){
								cardInfo = new CardInfo(cardId);
								player.cards.cardInfos.put(cardId, cardInfo);
							}
							cardInfo.level++;
							Packet pt = new Packet(OpCode.CARD_UPGRADE_SERVER);
							pt.putInt(serial);
							pt.putInt(player.cards.exp);
							int type=card.buff2Id==-1?0:1;
							pt.put(type);
							if(type==0){
								pt.putInt(getUpGradeExp(quality, currentLevel+1));
								pt.putUTF(service.getEnhanceDesc(cardId, cardInfo.level));
								pt.putUTF(/*"下级属性:"+*/service.getEnhanceDesc(cardId, cardInfo.level+1));
							}else{
								CardInfo card_BuffLevelup=player.cards.getEquipCardInfoByCardId(cardId);
								if(card_BuffLevelup!=null){
									Card cardSkill=service.getCardByCardId(cardId);
									if(cardSkill!=null){
										int buffId=cardSkill.buff2Id;
										player.buffs.removeBuff(buffId);
										if(buffId!=-1&&player.buffs.getBuffByID(buffId)==null){
											Buff buff=BuffUtil.createBuff(buffId,cardInfo.level , player, player, 0);
											player.buffs.addBuff(buff);
										}
									}
								}
								pt.putInt(getCardUpGradeNeedCount(cardInfo.level));
//								int totalCount=player.pool.getInt(CardService.getPropertyOfPlayerCard(card.id),0);
								int totalCount=CardService.getCardCount(player, card.id);
								if(totalCount>=1&&cardInfo.level>1){
									totalCount-=1;
									if(totalCount<=0){
										totalCount=0;
									}
								}
								pt.putInt(totalCount);
								Buff skillBuff=BuffUtil.createBuff(card.buff2Id, cardInfo.level, player, player, 0);
								String desc=service.getEnhanceDesc(cardId, cardInfo.level);
								if(skillBuff!=null){
									desc=skillBuff.getDesc();
								}
								pt.putUTF(desc);
								Buff skillBuffNext=BuffUtil.createBuff(card.buff2Id, cardInfo.level+1, player, player, 0);
								if(skillBuffNext!=null){
									desc=skillBuffNext.getDesc();
								}
								else if(cardInfo.level>=12){
									desc="";
								}
								pt.putUTF(desc);
								int needExp=CardUpGradeCall.getSkillCardNeedExp(getCardUpGradeNeedCount(cardInfo.level));
								pt.putInt(needExp);
								if(isSkillCard&&cardInfo.level%3==0){
									String hint="";
									Buff skillBuffT=BuffUtil.createBuff(card.buff2Id, cardInfo.level, player, player, 0);
									if(skillBuffT!=null){
										desc=skillBuffT.getName();
									}
									ChatService cs = Server.server.getServiceRegistry().getChatService();
									switch(cardInfo.level){
									case 3:
										hint=MessageFormat.format("恭喜{0}将{1}升到了3级，{2}技能获得提升，武力更进一步，从而雄霸一方。", player.name,cardItem.template.name,desc);
										cs.sendFactionSystemMessage(player.faction, hint);
										break;
									case 6:
										hint=MessageFormat.format("恭喜{0}将{1}升级到6级，{2}技能精进，从而称霸一域，逐鹿中原。", player.name,cardItem.template.name,desc);
										cs.sendFactionSystemMessage(player.faction, hint);
										break;
									case 9:
										hint=MessageFormat.format("恭喜{0}将{1}升级到9级，{2}技能向巅峰更近一步，从而称霸一国，难逢敌手。", player.name,cardItem.template.name,desc);
										cs.sendFactionSystemMessage(player.faction, hint);
										break;
									case 12:
										hint=MessageFormat.format("{0}的12级{1}片横空出世，{2}技能达到巅峰，从此称霸天下再无敌手，群雄莫不臣服。", player.name,cardItem.template.name,desc);
										cs.sendWorldMessage(hint);
										break;
									}
								}
								int preLevel=1;
								if(cardInfo.level<3){
									preLevel=3;
								}else if(cardInfo.level<6){
									preLevel=6;
								}else if(cardInfo.level<9){
									preLevel=9;
								}else if(cardInfo.level<12){
									preLevel=12;
								}
								Buff skillBuffPre=BuffUtil.createBuff(card.buff2Id, preLevel, player, player, 0);
								if(skillBuffPre!=null&&cardInfo.level<12){
									desc=skillBuffPre.getDesc();
									pt.putUTF(desc);
								}else{
									pt.putUTF("");
								}
							}
							pt.put(card.prorertyType);
							session.send(pt);
							if(player.horse!=null)
								player.horse.refreshProperties(false, player);
							player.refreshProperties(false);
							LogUtil.logUpGradeCard(player, decExp, cardInfo.level);
						} catch (Exception e) {
							ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_UPGRADE_CLIENT, "经验不足,暂不能升级");
						}
					}
				}else{
					ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_UPGRADE_CLIENT, "卡片物品失效");
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_UPGRADE_CLIENT, "未找到指定卡片");
			}
		}
	}
	
	/**获取技能卡片升级所需卡片数量*/
	public static int getCardUpGradeNeedCount(int level){
		return (int)(level*level+3);
	}
	
	/**技能卡片升级所需卡片经验*/
	public static int getSkillCardNeedExp(int count){
		return  count*125;
	}

}
