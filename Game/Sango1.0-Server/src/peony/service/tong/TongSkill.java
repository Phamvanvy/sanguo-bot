package peony.service.tong;

import peony.game.OrganizationSkill;
import peony.game.Player;

public abstract class TongSkill extends OrganizationSkill {
	
	public static int SKILL_TYPE_TONG = 0;//军团技能
	public static int SKILL_TYPE_PERSONAL = 1;//专属技能
	public TongSkill(int id,String name,int level,int maxLevel,byte type){
		super(id,name,level,maxLevel,type);
	}
	public abstract String getDesc(int level);
	public abstract int getMaintainContribute(int level);
	public abstract void fire(Player p);
	public abstract TongSkill clone();
	public abstract int getUpgradeMoney(int level);
	public abstract int getMaintainMoney(int level);
	public abstract int getSkillType();
}
