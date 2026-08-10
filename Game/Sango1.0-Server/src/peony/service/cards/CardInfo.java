package peony.service.cards;

public class CardInfo {

	/**
	 * 装备部位
	 * 头HEAD = 1;   脖子NECK = 2;   胸CHEST = 0;   护腕WRIST = 7;   手HAND = 5
	 * 手指FINGER = 6;   腿LEG = 8;   脚FEET = 9;   副手HAND2 = 4;   背BACK = 3
	 * 
	 * 坐骑
	 * 头HEAD = 0;   脖子NECK = 1;   胸CHEST = 2;    臀ASS = 3;    鞍BACK = 4;    
	 * 蹄LEG = 5;    脚蹬PEDAL = 6;
	 */
	public int index = -1;
	
	/** 卡片ID */
	public int cardId; 
	
	/** 卡片级别 */
	public int level=1; 

	public CardInfo(int cardId){
		this.cardId = cardId;
	}
	
	public CardInfo(int index, int cardId, int level) {
		super();
		this.index = index;
		this.cardId = cardId;
		this.level = level;
	}
	
	public void unEquip(){
		index = -1;
	}
	
	public void equip(int index){
		this.index = index;
	}
	
}
