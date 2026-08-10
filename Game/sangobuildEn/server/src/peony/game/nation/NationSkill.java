package peony.game.nation;

import peony.game.OrganizationSkill;
import peony.game.Player;

public abstract class NationSkill extends OrganizationSkill{
	
	
	public NationSkill(int id,String name,int level,int maxLevel,byte type){
		super(id,name,level,maxLevel,type);
	}
	public abstract String getDesc(int level);
	public abstract int getUpgradeMoney(int level);
	public abstract int getMaintainMoney(int level);
	public abstract void fire(Player p);
	public abstract NationSkill clone();
}
