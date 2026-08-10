package peony.game.nation;

import peony.game.GameItem;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Time;

public class NationSkill4 extends NationSkill {
	
	static final String NAME = "珍宝获取";
	static final int MAXLEVEL = 1;
	
	static int[] UPGRADE_MONEY = {0,2660000};
	
	static int[] MAINTAIN_MONEY = {0,266000};
	
	static int[] GET_ITEMS = {0,1311};
	
	static int GET_ITEM_MAXLEVEL = 50;
	
	static String[] DESC = { 
		"珍宝获取0级,无任何效果。",
		"珍宝获取1级，50级以上的用户每日可领取珍珠1颗",
	};
	
	public NationSkill4(int level) {
		super(4, NAME, level, MAXLEVEL, NationSkill.TYPE_ITEM);
	}

	public NationSkill clone() {
		return new NationSkill4(level);
	}

	public void fire(Player p) {
		GameItem item = null;
		try {
			item = ObjectAccessor.createGameItem(GET_ITEMS[level]);
		} catch (Exception e1) {
			p.message(-1, "没有可领取的物品", -1, -1);
			return;
		}
		if(p.level<GET_ITEM_MAXLEVEL){
			p.message(-1, "您的级别不可领取", -1, -1);
			return;
		}
		PlayerTransaction tx = p.newTransaction("NSL");
		try {
			if(p.pool.getInt(Player.PROPERTY_GETNATIONSKILL_ITEM_DAY,0)!=Time.day){
				p.bag.addGameItemComplete(item, 1, tx, true);
				tx.commit();
				p.pool.setInt(Player.PROPERTY_GETNATIONSKILL_ITEM_DAY, Time.day);
			}else{
				tx.rollback();
				p.message(-1, "每天只能领取一次", -1, -1);
			}
		} catch (NoEnoughSpaceException e) {
			tx.rollback();
		}

	}

	public String getDesc(int level) {
		return DESC[level];
	}

	public int getMaintainMoney(int level) {
		return MAINTAIN_MONEY[level];
	}

	public int getUpgradeMoney(int level) {
		return UPGRADE_MONEY[level];
	}

}
