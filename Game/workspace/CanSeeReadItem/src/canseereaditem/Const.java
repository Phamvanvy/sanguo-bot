package canseereaditem;

public interface Const {
	byte[] digits = { '0', '1', '2', '3', '4', '5', '6','7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	String[] menuName={"基本内容","宠物相关","装备与道具","佣兵相关","庄园相关"};
	
	int MENU_NORMAL=0;
	int MENU_PET=1;
	int MENU_EQUIP=2;
	int MENU_MERCENARY=3;
	int MENU_FARM = 4;
	
	String defaultEnd="]";//默认的提取字符串时用到的结尾
	//normal
	byte TYPE_MAIL = 1;  //解析邮件
	byte TYPE_I_SHOP_BUY = 2;	//i币卖场购买记录
	byte TYPE_CHAT = 3;	//聊天记录
	byte TYPE_J_SHOP_BUY_SELL = 4;	//j币卖场买卖记录
	byte TYPE_CREDIT_AUCTION = 5;	//荣誉拍卖竞拍记录
	byte TYPE_J_AUCTION = 6;	//普通拍卖行竞拍和上架记录
	byte TYPE_LEVELUP=7; //角色升级
	byte TYPE_TASK = 8; // 完成任务
	byte TYPE_ARENASHOP_BUY = 9; //竞技场商店购买
	byte TYPE_HOME_ADDITEM = 10;//家园仓库放入物品
	byte TYPE_BOSSRUSH = 11;//百层挑战
	byte TYPE_IMONEYCARD = 12;//10元i币卡
	byte TYPE_AWARD_BOX = 13;//奥德赛之旅
	byte TYPE_RECHARGE = 14;//充值相关
	
	//pet
	byte TYPE_SELL_BUY_PET = 1;	//买卖宠物记录
	byte TYPE_PET_LOST = 2;	//宠物丢失记录
	byte TYPE_PET_SYNTHETIZE=3;//宠物合成
	byte TYPE_PET_RECASTINGPROPERTY=4;//宠物重铸
	byte TYPE_PET_PRACTICE=5;//宠物修炼
	byte TYPE_PET_SPIRITUALITY=6;//宠物灵性
	byte TYPE_PET_PERCEPTION=7;//宠物悟性
	byte TYPE_PET_SKILLREBIRTH=8;//技能重生
	byte TYPE_PET_SKILLLOCK=9;//技能锁定
	byte TYPE_PET_THROWPET=10;//宠物放生
	byte TYPE_PET_FEEDERROR=11; //喂宠时没有找到宠物
	byte TYPE_PET_CHANGECOLOR=12;//宠物变色
	byte TYPE_PET_DEBLOCK=13;//宠物解锁
	byte TYPE_PET_RACE=14;//宠物奥运会
	byte TYPE_PET_EVOLUTION=15;//宠物进化系统
	
	//equipment&item
	byte TYPE_BREAK_NOMAL_EQU = 1;  //解析分解非周年装备
	byte TYPE_BREAK_ZHOUNIAN_EQU = 2;	//解析分解周年装备
	byte TYPE_USE_LOST_ITEM = 3;	//丢弃物品和使用物品记录
	byte TYPE_DIAMOND = 4;	//装备鉴定
	byte TYPE_GEM = 5;	//宝石记录（合成、镶嵌、摘除）
	byte TYPE_REMOVE_BATH = 6;	//扣除澡票记录
	byte TYPE_DROP_ITEM = 7;	//打怪掉落物品记录
	byte TYPE_PRODUCE=8;//打造物品	
	byte TYPE_EQU_DRILLING=9;//装备打孔
	byte TYPE_EQU_MOSAIC=10;//镶嵌
	byte TYPE_EQU_EXCISE=11;//摘除
	byte TYPE_GIFT=12;//获得礼品
	byte TYPE_REMOVE_EQU = 13;//移除装备
	byte TYPE_EQU_EXCHANGECREDIT = 14;//装备换荣誉
	byte TYPE_GETITEM_DROPGROUP = 15; //使用定向包
	byte TYPE_AUTOMIX = 16;//宝石升级
	byte TYPE_ADDATTRIBUTE=17;//永久增加属性
	byte TYPE_EGG_GETITEM=18;//砸蛋获得物品
	byte TYPE_DIAMOND_REPLACE = 19;//宝石置换
	byte TYPE_DIAMOND_DEVELOP = 20;//宝石融合
	
	//MercenaryMenu
	byte TYPE_MERCENARY_SELLSELF = 1;//卖身查询
	byte TYPE_MERCENARY_BUYSELF = 2; //赎身
	byte TYPE_MERCENARY_BUYSYSTEM = 3; //雇佣系统佣兵
	byte TYPE_MERCENARY_BUYPLAYER = 4; //雇佣玩家佣兵
	byte TYPE_MERCENARY_SETJOINTEAM = 5;//启用佣兵
	byte TYPE_MERCENARY_SETSLEEP = 6;//休息佣兵
	byte TYPE_MERCENARY_LAYOFF = 7;//解雇佣兵
	
	//farmMenu
	byte TYPE_FARM_DROPITEM = 1;//缴获战利品
	byte TYPE_FARM_LOSTITEM = 2;//损失的物品
	byte TYPE_FARM_LANDLEVELUP = 3;//土地升级
	byte TYPE_FARM_LANDOPEN=4;//土地开放
	byte TYPE_FARM_STEAL=5;//偷取物品
	
	
	
	byte MAIL_POST = 24;				//发送邮件
	byte MAIL_GET_ATTACHMENT = 27;		//提取邮件附件
	byte MAIL_DELETE = 28;				//删除邮件
	byte USE_ITEM = 33;					//使用&丢弃物品
	byte BATTLE_RESULT = 34;            //本地战斗结束
	byte BATTLE_ROUND_END = 52;			//服务器战斗结束
	byte USE_PET = (byte)138;			//装备、卸下、丢弃宠物
	byte AUCTION_PRICE = 120;			//竞拍物品
	byte AUCTION_ITEM = 121;			//发布拍卖
	
    int WORLD = -1;
    int MAP = -2;
    int GUILD = -3;
    int GROUP = -4;
    int TEAM = -5;
    int FAVORITE = -6;
    int SYSTEM = -7;
    int GM = -8;
    
    int	CAMP = -9;		 //阵营
    int NEW = -10;
    int ROAR = -12;	 // 狮子吼
	
    String[] produceTypeName={"武器", "防具", "首饰", "材料", "召唤符"};
}
