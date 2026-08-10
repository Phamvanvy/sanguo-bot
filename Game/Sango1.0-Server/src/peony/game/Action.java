package peony.game;

public class Action {
	
	public static final int START = 0; //鉴定星级
	public static final int NATURAL_ENHANCE = 1; //资质鉴定
	public static final int JEWEL = 2; //镶嵌宝石
	public static final int EQUIP = 3; //穿装备
	public static final int ADD_PROPERTY_POINT = 4; //加属性点
	public static final int SHOP_BUY = 5; //商店购买
	public static final int IMONEY_BUY = 6; //元宝购买
	public static final int CREDIT_BUY = 7; //战功购买
	public static final int RIDE = 8; //骑马
	public static final int FEED_HORSE = 9; //喂养坐骑
	public static final int UPGRADE_SKILL = 10; //升级技能
	public static final int UPGRADE_ACTIVE_SKILL = 11; //升级主动技能
	public static final int UPGRADE_PASSIVE_SKILL = 12; //升级被动技能
	public static final int EXTEND_BAG = 13; //扩展背包
	public static final int OPEN_DEPOT = 14; //开启仓库
	public static final int PRODUCE = 15; //打造成功
	public static final int MERGE_JEWEL = 16; //合成宝石成功
	public static final int REFRESH_HORSE_SKILL = 17; //洗坐骑技能
	public static final int EQUIP_TITLE = 18; //佩戴称号
	public static final int LEARN_SKILLBOOK = 19; //使用技能书
	public static final int JOIN_PARTY = 20; // 加入队伍
	public static final int EQUIP_ENHANCE = 21;//装备强化
	
	public int type;
	
	public Action(int type){
		this.type = type;
	}
}
