package peony.game.changed;


public abstract class ChangedItem {
	
	public static final int HP = 1;
	public static final int MP = 2;
	public static final int MAXHP = 3;
	public static final int MAXMP = 4;
	public static final int SEX = 5;
	public static final int CLAZZ = 6;
	public static final int LEVEL = 7;
	public static final int MONEY = 8;
	public static final int STRENGTH = 9;
	public static final int AGILITY = 10;
	public static final int STAMINA = 11;
	public static final int INTELLECT = 12;
	public static final int SKILLPOINT = 13;
	public static final int EXP = 14;
	public static final int UPLEVELEXP = 15;
	public static final int PROPERTYPOINT = 16; //可用的属性点
	public static final int HEAD_SCORE = 17; 
	public static final int BODY_SCORE = 18;
	public static final int WEAPON_SCORE = 19;
	public static final int CREDIT = 20;
	public static final int GAINEXP = 21;
	public static final int RIDE = 22;  //最高位上马下马 高15位ImageId 低16位分数
	public static final int GRIDCOUNT = 23;
	

	public static final int ATTACKPOWERUP = 24; //攻击上限
	public static final int ATTACKPOWERDOWN = 25;//	攻击下限	
	public static final int SPELLPOWER = 26;//	法术攻击力
	public static final int DEFENSE = 27;//	防御	
	public static final int SPELLDEFENSE = 28;//	法术防御	
	public static final int CRITICAL = 29;//	暴击*100	
	public static final int SPELLCRITICAL = 30;//	法暴*100	
	public static final int HIT = 31;//	命中*100	
	public static final int SPELLHIT = 32;//	法术命中*100
	public static final int DODGE = 33;//	闪避*100	
	public static final int SPELLDODGE = 34;//	法术闪避*100	
	public static final int HEALTHRESTORE = 35;//	每五秒回血
	public static final int MANARESTORE = 36;//	每五秒回蓝
	public static final int DEFENSEPERCENT = 37; //物理减伤
	public static final int STATE = 38;
	public static final int FACTION = 39;
	public static final int SPEED = 40;
	public static final int GRIDFULL = 41;
	public static final int SPELLHEAL = 42; // 治疗效果
	public static final int REMOVE_TITLE = 43;
	public static final int HONOR = 44;
	public static final int ANTICRIT = 45; // 免暴
	
	public static final int IMONEY = 46;
	
	public static final int FLASHLEVEL = 47;
	public static final int ACTIVEPOWER = 48; // 行动力
	public static final int LOYAL = 49; 	  // 忠诚度
	public static final int MAX_HORSE_CNT = 50;//最大坐骑栏数
	public static final int MAX_ATTENDAT_CNT = 51; //最大随从栏数
	public static final int STAR_7_BUFF = 52; //全七BUFF(0为无此BUFF需要灰显，1为有此BUFF亮显)
	
	public static final int NAME = 1;
	public static final int CREDIT_STRING = 2;
	public static final int GUILD = 3;
	public static final int TITLE = 4;
	
	
	//背包改变
	public static final int BAGGRID = 1;
	//技能改变
	public static final int SKILL = 2;
	
	//物品数量变化
	public static final int ITEM_COUNT_CHANGED = 3;
	
	public static final int EQUIP = 4;
	//耐久
	public static final int DURATION = 5; //ItemInstanceId(int)+duration(short)
	//绑定状态
	public static final int BIND = 6;//ItemInstanceId(int)+bindInstance(int)
	
	public static final int ADD_TITLE = 7;
	public static final int HORSE_BAG = 8; //count(byte)如果count数量是1后面跟着horse的信息，如果为0后面跟着horse的id
	public static final int ATTENDANT_BAG = 9; //count(byte)如果count数量是1后面跟着attendant的信息，如果为0后面跟着attendant的id
	
//	public int strength;
//	public int agility;
//	public int intellect;
//	public int stamina;
//	public int speed;
//	
//	public int point;
	
	//整形类型
	public static final int TYPE_INT = 0;
	
	//字符串类型
	public static final int TYPE_STRING = 1;
	//复杂类型
	public static final int TYPE_COMPLEX = 2;
	
	public static final int TYPE_HORSE_INT = 3;
	
	public static final int HORSE_STRENGTH = 1;
	public static final int HORSE_AGILITY = 2;
	public static final int HORSE_INTELLECT = 3;
	public static final int HORSE_STAMINA = 4;
	public static final int HORSE_SPEED = 5;
	public static final int HORSE_POINT = 6;
	
	public static final int HORSE_GAINEXP = 7;
	public static final int HORSE_EXP = 8;
	public static final int HORSE_UPLEVELEXP = 9;
	
	public static final int HORSE_LEVEL = 10;
	public static final int HORSE_DEGREE = 11;
	public static final int HORSE_AGENT = 12;
	public static final int HORSE_STATE = 13;
	
	public static final int TYPE_HORSE_STRING = 4;
	public static final int HORSE_NAME = 1;
	
	public static final int TYPE_HORSE_COMPLEX = 5;
	public static final int HORSE_SKILL = 1; 
	public static final int TYPE_MAX = 8;
	
	public static final int TYPE_ATTENDANT_INT = 6;
	
	public static final int TYPE_ATTENDANT_STRING = 7;
	
	public static final int TYPE_ATTENDANT_COMPLEX = 8;
	
	protected int type;
	protected int id;
	//客户端是否需要提醒用户
	protected boolean notify;
	
	protected ChangedItem(int type,int id,boolean notify){
		this.type = type;
		this.id = id;
		this.notify = notify;
	}
	
	public int getId(){
		return id;
	}
	
	public int getType(){
		return type;
	}
	
	public boolean isNotif(){
		return notify;
	}
	
	public abstract boolean merge(ChangedItem other);
	
	public abstract void accept(ChangedItemVisitor visitor);
}
