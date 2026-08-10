package peony.service.cards;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.pip.sanguo.data.Card;

import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;

/**
 * 玩家卡片数据
 * @author dchen
 */
public class Cards {
	
	protected static final Logger log = Logger.getLogger(Cards.class);
	
	/** 卡片经验 */
	public int exp;
	
	/** 卡片最大级别 */
	public static int maxCardLevel = 12;
	
	/** 玩家身上已镶嵌的卡片信息集合 */
	public CardInfo[] equipCards = new CardInfo[10];
	
	public CardInfo[] horseEquipCards = new CardInfo[7];
	
	/** 玩家卡包未镶嵌的卡片信息集合（不包括未升级的历史卡片） */
	public Map<Integer, CardInfo> cardInfos = new HashMap<Integer, CardInfo>();
	
	/** 从卡包中获取未镶嵌的卡片信息（不包括未升级的历史卡片） */
	public CardInfo getUnEquipCardInfo(int cardId){
		return cardInfos.get(cardId);
	}
	
	public Player player;
	
	public Cards(Player player){
		this.player = player;
	}
	
	public CardInfo getEquipCardInfoByCardId(int cardId){
		for(CardInfo cardInfo : equipCards){
			if(cardInfo!=null && cardInfo.cardId==cardId)
				return cardInfo;
		}
		for(CardInfo cardInfo : horseEquipCards){
			if(cardInfo!=null && cardInfo.cardId==cardId)
				return cardInfo;
		}
		return null;
	}
	
	protected CardInfo getEquipCardInfoByIndex(int index){
		return equipCards[index];
	}
	
	protected CardInfo getHorseEquipCardInfoByIndex(int index){
		return horseEquipCards[index];
	}
	
	protected void addPlayerEquipCardInfo(CardInfo cardInfo, int index){
		equipCards[index] = cardInfo;
	}
	
	protected void addHorseEquipCardInfo(CardInfo cardInfo, int index){
		horseEquipCards[index] = cardInfo;
	}
	
	protected boolean removeEquipCardInfo(CardInfo cardInfo){
		for(int i=0;i<equipCards.length;i++){
			if(equipCards[i]!=null && equipCards[i].cardId==cardInfo.cardId){
				equipCards[i] = null;
				return true;
			}
		}
		return false;
	}
	
	protected boolean removeHorseEquipCardInfo(CardInfo cardInfo){
		for(int i=0;i<horseEquipCards.length;i++){
			if(horseEquipCards[i]!=null && horseEquipCards[i].cardId==cardInfo.cardId){
				horseEquipCards[i] = null;
				return true;
			}
		}
		return false;
	}
	
	protected void removeEquipCardInfo(int index){
		equipCards[index] = null;
	}
	
	/**
	 * 玩家镶嵌卡片
	 * @param cardId
	 * @param index
	 * @throws CardException
	 */
	public CardInfo playerEquipCard(int cardId, int index) throws CardException {
		CardInfo cardInfo = getUnEquipCardInfo(cardId);
		if(cardInfo==null)
			if(getEquipCardInfoByCardId(cardId)!=null)
				throw new CardException("此卡片已镶嵌,不能重复镶嵌");
			else
				throw new CardException("请先升级卡片再进行镶嵌");
		if(cardInfo.level<1)
            throw new CardException("请先升级卡片再进行镶嵌");
		CardInfo equipCardInfo=getEquipCardInfoByIndex(index);
		if(equipCardInfo!=null){
//			throw new CardException("此装备位已经镶嵌");
			equipCardInfo.unEquip();
			cardInfos.put(equipCardInfo.cardId, equipCardInfo);
		}
		cardInfo.equip(index);
		addPlayerEquipCardInfo(cardInfo, index);
		cardInfos.remove(cardId);
		return equipCardInfo;
	}
	
	/**
	 * 玩家给坐骑镶嵌卡片
	 * @param cardId
	 * @param index
	 * @throws CardException
	 */
	public CardInfo horseEquipCard(int cardId, int index) throws CardException {
		CardInfo cardInfo = getUnEquipCardInfo(cardId);
		if(cardInfo==null)
			if(getEquipCardInfoByCardId(cardId)!=null)
				throw new CardException("此卡片已镶嵌,不能重复镶嵌");
			else
				throw new CardException("请先升级卡片再进行镶嵌");
		if(cardInfo.level<1)
		    throw new CardException("请先升级卡片再进行镶嵌");
		CardInfo equipCardInfo=getHorseEquipCardInfoByIndex(index);
		if(equipCardInfo!=null){
//			throw new CardException("此装备位已经镶嵌");
			equipCardInfo.unEquip();
			cardInfos.put(equipCardInfo.cardId, equipCardInfo);
		}
		cardInfo.equip(index);
		addHorseEquipCardInfo(cardInfo, index);
		cardInfos.remove(cardId);
		return equipCardInfo;
	}
	
	/**
	 * 玩家摘除镶嵌卡片
	 * @param index
	 * @throws CardException
	 */
	public CardInfo unequpPlayerCard(int index) throws CardException {
		CardInfo cardInfo = getEquipCardInfoByIndex(index);
		if(cardInfo==null)
			throw new CardException("未找到指定卡片");
		cardInfo.unEquip();
		removeEquipCardInfo(cardInfo);
		cardInfos.put(cardInfo.cardId, cardInfo);
		return cardInfo;
	}
	
	/**
	 * 玩家给坐骑摘除镶嵌卡片
	 * @param horse
	 * @param index
	 * @throws CardException
	 */
	public CardInfo unequpHorseCard(int index) throws CardException {
		CardInfo cardInfo = getCardInfoByIndex(1, index);
		if(cardInfo==null)
			throw new CardException("未镶嵌卡片");
		cardInfo.unEquip();
		removeHorseEquipCardInfo(cardInfo);
		cardInfos.put(cardInfo.cardId, cardInfo);
		return cardInfo;
	}
	
	/**
	 * 根据index获取卡片信息
	 * @param type
	 * @param index
	 * @return
	 */
	public CardInfo getCardInfoByIndex(int type, int index){
		try {
			if(type==0){
				return equipCards[index];
			}else if(type==1){
				return horseEquipCards[index];
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}
	
	/**
	 * 添加经验
	 * @param exp
	 * @throws Exception
	 */
	public void addExp(int exp) throws Exception {
		int oldExp = this.exp;
		if(this.exp+exp<0)
			throw new Exception("经验已满");
		this.exp += exp;
		LogUtil.logCardExpChange(player, oldExp, this.exp);
	}
	
	/**
	 * 扣除经验
	 * @param exp
	 * @throws Exception
	 */
	public void decExp(int exp) throws Exception {
		int oldExp = this.exp;
		if(this.exp<exp)
			throw new Exception("经验不足");
		this.exp -= exp;
		LogUtil.logCardExpChange(player, oldExp, this.exp);
	}
	
	public byte[] toDBBytes(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
			dos.writeByte(1); //version
			dos.writeInt(exp);
			dos.writeByte(equipCards.length);
			for(CardInfo info : equipCards){
				if(info==null)
					dos.writeByte(0);
				else{
					dos.writeByte(1);
					dos.writeByte(info.index);
					dos.writeInt(info.cardId);
					dos.writeByte(info.level);
				}
			}
			dos.writeByte(horseEquipCards.length);
			for(CardInfo info : horseEquipCards){
				if(info==null)
					dos.writeByte(0);
				else{
					dos.writeByte(1);
					dos.writeByte(info.index);
					dos.writeInt(info.cardId);
					dos.writeByte(info.level);
				}
			}
			dos.writeInt(cardInfos.size());
			for(CardInfo info : cardInfos.values()){
				if(info==null)
					dos.writeByte(0);
				else{
					dos.writeByte(1);
					dos.writeByte(info.index);
					dos.writeInt(info.cardId);
					dos.writeByte(info.level);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return baos.toByteArray();
	}
	
	public static Cards getFromDBBytes(DataInputStream dis, Player player){
		try{
			dis.readByte(); //version
			Cards cards = new Cards(player);
			int exp = dis.readInt();
			cards.exp = exp;
			int length1 = dis.readByte();
			for(int i=0;i<length1;i++){
				int flag = dis.readByte();
				if(flag==1){
					int index = dis.readByte();
					int cardId = dis.readInt();
					int level = dis.readByte();
					CardInfo info = new CardInfo(index, cardId, level);
					cards.equipCards[i] = info;
				}else{
					cards.equipCards[i] = null;
				}
			}
			int length2 = dis.readByte();
			for(int i=0;i<length2;i++){
				int flag = dis.readByte();
				if(flag==1){
					int index = dis.readByte();
					int cardId = dis.readInt();
					int level = dis.readByte();
					CardInfo info = new CardInfo(index, cardId, level);
					cards.horseEquipCards[i] = info;
				}else{
					cards.horseEquipCards[i] = null;
				}
			}
			int length3 = dis.readInt();
			for(int i=0;i<length3;i++){
				int flag = dis.readByte();
				if(flag==1){
					int index = dis.readByte();
					int cardId = dis.readInt();
					int level = dis.readByte();
					if(level==0){
						level=1;
					}
					CardInfo info = new CardInfo(index, cardId, level);
					cards.cardInfos.put(cardId, info);
				}
			}
			return cards;
		}catch(Exception e){
			e.printStackTrace();
			return new Cards(null);
		}
	}
	
	public static Cards getFromDBBytes(byte[] bytes, Player player){
		try{
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			DataInputStream dis = new DataInputStream(bais);
			dis.readByte(); //version
			Cards cards = new Cards(player);
			int exp = dis.readInt();
			cards.exp = exp;
			int length1 = dis.readByte();
			for(int i=0;i<length1;i++){
				int flag = dis.readByte();
				if(flag==1){
					int index = dis.readByte();
					int cardId = dis.readInt();
					int level = dis.readByte();
					if(level==0){
						level=1;
					}
					CardInfo info = new CardInfo(index, cardId, level);
					cards.equipCards[i] = info;
				}else{
					cards.equipCards[i] = null;
				}
			}
			int length2 = dis.readByte();
			for(int i=0;i<length2;i++){
				int flag = dis.readByte();
				if(flag==1){
					int index = dis.readByte();
					int cardId = dis.readInt();
					int level = dis.readByte();
					if(level==0){
						level=1;
					}
					CardInfo info = new CardInfo(index, cardId, level);
					cards.horseEquipCards[i] = info;
				}else{
					cards.horseEquipCards[i] = null;
				}
			}
			int length3 = dis.readInt();
			for(int i=0;i<length3;i++){
				int flag = dis.readByte();
				if(flag==1){
					int index = dis.readByte();
					int cardId = dis.readInt();
					int level = dis.readByte();
					if(level==0){
						level=1;
					}
					CardInfo info = new CardInfo(index, cardId, level);
					cards.cardInfos.put(cardId, info);
				}
			}
			return cards;
		}catch(Exception e){
			e.printStackTrace();
			return new Cards(null);
		}
	}

	public Object clone() {
		Cards cards = new Cards(player);
		cards.cardInfos = cardInfos;
		cards.equipCards = equipCards;
		cards.horseEquipCards = horseEquipCards;
		return cards;
	}
	
	public byte[] toClientBytes(){
		CardService service = Server.server.getServiceRegistry().getCardService();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.write(equipCards.length);
			for(CardInfo info : equipCards){
				if(info!=null){
					dos.writeByte(1);
					dos.writeInt(info.cardId);
					dos.writeUTF(service.getCardByCardId(info.cardId).title);
					dos.writeByte(info.level);
					dos.writeUTF(service.getEnhanceDesc(info.cardId, info.level));
					int quality =  ObjectAccessor.createGameItem(service.getCardByCardId(info.cardId).itemId).template.quality;
					dos.writeByte(quality);
				}else{
					dos.writeByte(0);
				}
			}
			dos.write(horseEquipCards.length);
			for(CardInfo info : horseEquipCards){
				if(info!=null){
					dos.writeByte(1);
					dos.writeInt(info.cardId);
					dos.writeUTF(service.getCardByCardId(info.cardId).title);
					dos.writeByte(info.level);
					dos.writeUTF(service.getEnhanceDesc(info.cardId, info.level));
					int quality =  ObjectAccessor.createGameItem(service.getCardByCardId(info.cardId).itemId).template.quality;
					dos.writeByte(quality);
				}else{
					dos.writeByte(0);
				}
			}
		} catch (IOException e) {
			//TODO LOG
		}
		return bos.toByteArray();
	}
	
}
