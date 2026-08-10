package peony.game;


/**
 * 组织技能类的基类，比如军团技能以及国家技能
 * @author Jeffrey
 *
 */
public abstract class OrganizationSkill {
	public static final byte TYPE_ITEM = 1; //可以领取物品
	public static final byte TYPE_NONE = 0; 
	
	public int id;
	public String name;
	public int level;
	public int maxLevel;
	public byte type;
	public int upgradeDay;
	public int maintainDay;
	
	public OrganizationSkill(int id,String name,int level,int maxLevel,byte type){
		this.id = id;
		this.name = name;
		this.level = level;
		this.maxLevel = maxLevel;
		this.type = type;
	}
	public abstract String getDesc(int level);
	public abstract int getUpgradeMoney(int level);
	public abstract int getMaintainMoney(int level);
	public abstract void fire(Player p);
	public abstract OrganizationSkill clone();
}
