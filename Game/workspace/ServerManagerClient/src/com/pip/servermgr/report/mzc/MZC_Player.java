package com.pip.servermgr.report.mzc;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.pip.mzcity.world.entity.Army;
import com.pip.mzcity.world.entity.Armys;
import com.pip.mzcity.world.entity.Building;
import com.pip.mzcity.world.entity.Buildings;
import com.pip.mzcity.world.entity.City;
import com.pip.mzcity.world.entity.Friends;
import com.pip.mzcity.world.entity.Hero;
import com.pip.mzcity.world.entity.Heros;
import com.pip.mzcity.world.entity.Lord;
import com.pip.mzcity.world.gameitem.Bag;
import com.pip.mzcity.world.gameitem.GameItem;
import com.pip.mzcity.world.model.city.CityModel;
import com.pip.mzcity.world.model.city.tavern.HeroTemplate.TalentType;
import com.pip.servermgr.report.IPlayer;
import com.pip.util.ResultRow;

public class MZC_Player implements IPlayer {
	public int id;
	public int accountid;
	public String name;
	public Date lastLoginTime;
	public Date createTime;
	public int level;
	public int chargeAmount;
	public int vipLevel;
	public int money;
	public int wood;
	public int stone;
	public int food;
	public int crystal;
	public int idle;
	public int heroBlood;
	public int exploits;
	public Heros heros;
	public Heros leaveHeros;
	public Bag bag;
	public Armys armys;
	public Friends friends;
	public Buildings buildings;

	public static MZC_Player parse(ResultRow row) throws IOException {
		try {
			MZC_Player ret = new MZC_Player();
			ret.id = row.getInt(1);
			ret.accountid = row.getInt(2);
			ret.name = row.getString(3);
			ret.lastLoginTime = new Date(row.getInt(4) * 1000L);
			ret.createTime = new Date(row.getInt(5) * 1000L);
			if (ret.lastLoginTime.getTime() == 0) {
				ret.lastLoginTime = ret.createTime;
			}
			ret.level = row.getInt(6);
			ret.chargeAmount = row.getInt(7);
			ret.vipLevel = row.getInt(8);
			ret.money = row.getInt(9);
			ret.wood = row.getInt(10);
			ret.stone = row.getInt(11);
			ret.food = row.getInt(12);
			ret.crystal = row.getInt(13);
			ret.idle = row.getInt(14);
			ret.heroBlood = row.getInt(15);
			ret.exploits = row.getInt(16);
			byte[] data = (byte[])row.getObject(17);
			ret.heros = Heros.parse(data);
			data = (byte[])row.getObject(18);
			ret.leaveHeros = Heros.parse(data);
			for (Hero hero : ret.leaveHeros.getHeros()) {
				ret.heros.addHero(hero);
			}
			data = (byte[])row.getObject(19);
			ret.bag = (Bag)Bag.parse(data, new Lord());
			data = (byte[])row.getObject(20);
			ret.armys = Armys.parse(data);
			data = (byte[])row.getObject(21);
			ret.friends = Friends.parse(data);
			data = (byte[])row.getObject(22);
			ret.buildings = Buildings.parse(data, new City());
			return ret;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * 取得某个统计项数据。
	 * @param type 参见Xuanyuan_ReportEngine里的常量
	 * @return 可能是Boolean, Integer, Float
	 */
	public Object getValue(int type) {
		switch (type) {
		case MZC_ReportEngine.TYPE_ISALIVE:
			// 是否存活，最近7天内有登录的
			return (System.currentTimeMillis() - lastLoginTime.getTime()) / 86400000L < 7;
		case MZC_ReportEngine.TYPE_LIVETIME:
			// 存活时间（天）
			return (int)((lastLoginTime.getTime() - createTime.getTime()) / 86400000L);
		case MZC_ReportEngine.TYPE_LEVEL:
			// 等级
			return level;
		case MZC_ReportEngine.TYPE_ISPAY:
			// 是否充值过
			return chargeAmount > 0;
		case MZC_ReportEngine.TYPE_PAY:
			// 累计充值金额（元宝）
			return chargeAmount;
		case MZC_ReportEngine.TYPE_VIPLEVEL:
			// VIP等级
			return vipLevel;
		case MZC_ReportEngine.TYPE_MONEY:
			// 角色当前持有金币
			return money;
		case MZC_ReportEngine.TYPE_WOOD:
			// 角色当前持有木材
			return wood;
		case MZC_ReportEngine.TYPE_STONE:
			// 角色当前持有石头
			return stone;
		case MZC_ReportEngine.TYPE_FOOD:
			// 角色当前持有粮食
			return food;
		case MZC_ReportEngine.TYPE_CRYSTAL:
			// 角色当前持有水晶
			return crystal;
		case MZC_ReportEngine.TYPE_IDLE:
			// 角色当前剩余人口数量
			return idle;
		case MZC_ReportEngine.TYPE_HEROBLOOD:
			// 角色当前持有英雄血
			return heroBlood;
		case MZC_ReportEngine.TYPE_EXPLOITS:
			// 角色当前持有战功
			return exploits;
		case MZC_ReportEngine.TYPE_GOLDHEROCOUNT:
			// 角色拥有的金色英雄数量
			return getGoldHeroCount();
		case MZC_ReportEngine.TYPE_PURPLEHEROCOUNT:
			// 角色拥有的紫色或金色英雄数量
			return getPurpleHeroCount();
		case MZC_ReportEngine.TYPE_BLUEHEROCOUNT:
			// 角色拥有的蓝色、紫色或金色英雄数量
			return getBlueHeroCount();
		case MZC_ReportEngine.TYPE_AVGHEROLEVEL:
			// 角色最高等级的5个英雄的平均等级
			return getAvgHeroLevel();
		case MZC_ReportEngine.TYPE_AVGBUILDLEVEL:
			// 角色所有建筑的平均等级
			return getAvgBuildLevel();
		case MZC_ReportEngine.TYPE_AVGJEWELLEVEL:
			// 角色装备符文等级最高的5个英雄的平均符文等级（符文总等级除以60）
			return getAvgJewelLevel();
		case MZC_ReportEngine.TYPE_AVGENHLEVEL:
			// 角色强化等级最高的最多10件装备的平均强化等级
			return getAvgEnhLevel();
		case MZC_ReportEngine.TYPE_AVGHEROENHLEVEL:
			// 角色培养等级最高的5个英雄的5个属性平均培养等级
			return getAvgHeroEnhLevel();
		case MZC_ReportEngine.TYPE_AVGHEROTRAINLEVEL:
			// 角色训练等级最高的5个英雄的平均训练等级（训练等级=5个训练属性之和/5个训练属性最大值之和）
			return getAvgHeroTrainLevel();
		case MZC_ReportEngine.TYPE_AVGHEROSTAR:
			// 角色最高等级的5个英雄的平均刀盾数
			return getAvgHeroStar();
		case MZC_ReportEngine.TYPE_FRIENDCOUNT:
			// 好友个数
			return friends.getFriendList().size();
		case MZC_ReportEngine.TYPE_MAXARMYLEVEL:
			// 最高士兵进阶等级
			return getMaxArmyLevel();
		case MZC_ReportEngine.TYPE_MAXARMYENHLEVEL:
			// 最高士兵强化等级
			return getMaxArmyEnhLevel();
		}
		return 0;
	}
	
	// 角色拥有的金色英雄数量
	private int getGoldHeroCount() {
		int ret = 0;
		for (Hero hero : heros.getHeros()) {
			if (hero.getType().ordinal() >= TalentType.DARKGLOD.ordinal()) {
				ret++;
			}
		}
		return ret;
	}

	// 角色拥有的紫色或金色英雄数量
	private int getPurpleHeroCount() {
		int ret = 0;
		for (Hero hero : heros.getHeros()) {
			if (hero.getType().ordinal() >= TalentType.GLOD.ordinal()) {
				ret++;
			}
		}
		return ret;
	}

	// 角色拥有的蓝色、紫色或金色英雄数量
	private int getBlueHeroCount() {
		int ret = 0;
		for (Hero hero : heros.getHeros()) {
			if (hero.getType().ordinal() >= TalentType.BLUE.ordinal()) {
				ret++;
			}
		}
		return ret;
	}
	
	private int getSum(int[] arr, int start, int len) {
		int ret = 0;
		for (int i = start; i < start + len; i++) {
			ret += arr[i];
		}
		return ret;
	}

	// 角色最高等级的5个英雄的平均等级
	private float getAvgHeroLevel() {
		List<Hero> heroList = heros.getHeros();
		int[] levels = new int[heroList.size()];
		for (int i = 0; i < heroList.size(); i++) {
			levels[i] = heroList.get(i).getLevel();
		}
		if (levels.length <= 5) {
			return getSum(levels, 0, levels.length) / (float)levels.length;
		} else {
			Arrays.sort(levels);
			return getSum(levels, levels.length - 5, 5) / 5.0f;
		}
	}
	
	// 角色所有建筑的平均等级
	private float getAvgBuildLevel() {
		Collection<Building> buildings = this.buildings.getBuildings();
		int count = 0;
		int totalLevel = 0;
		for (Building building : buildings){
			//过滤掉传送门等无等级建筑
			if(building.getBuildingtemplate().getId()!=CityModel.DEFAULT_PORTAL_TEMPLATE_ID&&building.getBuildingtemplate().getId()!=CityModel.DEFAULT_ARENA_TEMPLATE_ID){
				if (building.getBuildingtemplate().getName() != null) {
					count++;
					totalLevel += building.getLevel();
				}
			}
		}
		if (count == 0) {
			return 0;
		}
		return totalLevel / (float)count;
	}
	
	// 角色装备符文等级最高的5个英雄的平均符文等级（符文总等级除以60）
	private float getAvgJewelLevel() {
		List<Hero> heroList = heros.getHeros();
		int[] levels = new int[heroList.size()];
		for (int i = 0; i < heroList.size(); i++) {
			Hero hero = heroList.get(i);
			for (GameItem item : hero.getGameItems().values(new GameItem[0])) {
				for (GameItem item2 : item.getOwnGameItems()) {
					if (item2.getTemplate().runeTemplate != null) {
						levels[i] += item2.getTemplate().runeTemplate.runeLevel;
					}
				}
			}
		}
		if (levels.length <= 5) {
			return getSum(levels, 0, levels.length) / (levels.length * 12.0f);
		} else {
			Arrays.sort(levels);
			return getSum(levels, levels.length - 5, 5) / 60.0f;
		}
	}
	
	// 角色强化等级最高的最多5个装备强化英雄的平均强化等级
	private float getAvgEnhLevel() {
		List<Hero> heroList = heros.getHeros();
		int[] levels = new int[heroList.size()];
		for (int i = 0; i < heroList.size(); i++) {
			Hero hero = heroList.get(i);
			for (GameItem item : hero.getGameItems().values(new GameItem[0])) {
				levels[i] += item.getTreasureLevel();
			}
		}
		if (levels.length <= 5) {
			return getSum(levels, 0, levels.length) / (levels.length * 2.0f);
		} else {
			Arrays.sort(levels);
			return getSum(levels, levels.length - 5, 5) / 10.0f;
		}
	}
	
	// 角色培养等级最高的5个英雄的5个属性平均培养等级
	private float getAvgHeroEnhLevel() {
		List<Hero> heroList = heros.getHeros();
		int[] levels = new int[heroList.size()];
		for (int i = 0; i < heroList.size(); i++) {
			Hero hero = heroList.get(i);
			levels[i] += hero.getAtkPoint();
			levels[i] += hero.getCommandPoint();
			levels[i] += hero.getDefPoint();
			levels[i] += hero.getHpPoint();
			levels[i] += hero.getDamagePoint();
		}
		if (levels.length <= 5) {
			return getSum(levels, 0, levels.length) / (levels.length * 5.0f);
		} else {
			Arrays.sort(levels);
			return getSum(levels, levels.length - 5, 5) / 25.0f;
		}
	}
	
	// 角色训练等级最高的5个英雄的平均训练等级（训练等级=5个训练属性之和/5个训练属性最大值之和）
	private float getAvgHeroTrainLevel() {
		List<Hero> heroList = heros.getHeros();
		int[] levels = new int[heroList.size()];
		for (int i = 0; i < heroList.size(); i++) {
			Hero hero = heroList.get(i);
			levels[i] += hero.getAtk_talent_Add() * 100 / hero.getTemplate().atkAddLimit;
			levels[i] += hero.getDef_talent_Add() * 100 / hero.getTemplate().defAddLimit;
			levels[i] += hero.getHp_talent_Add() * 100 / hero.getTemplate().hpAddLimit;
			levels[i] += hero.getDamage_talent_Add() * 100 / hero.getTemplate().damageAddLimit;
		}
		if (levels.length <= 5) {
			return getSum(levels, 0, levels.length) / (levels.length * 4.0f);
		} else {
			Arrays.sort(levels);
			return getSum(levels, levels.length - 5, 5) / 20.0f;
		}
	}
	
	// 角色最高等级的5个英雄的平均刀盾数
	private float getAvgHeroStar() {
		List<Hero> heroList = heros.getHeros();
		int[] levels = new int[heroList.size()];
		for (int i = 0; i < heroList.size(); i++) {
			levels[i] = (int)(heroList.get(i).getAtk_2GrowStar() + heroList.get(i).getDef_2GrowStar());
		}
		if (levels.length <= 5) {
			return getSum(levels, 0, levels.length) / (float)levels.length;
		} else {
			Arrays.sort(levels);
			return getSum(levels, levels.length - 5, 5) / 5.0f;
		}
	}
	
	// 最高士兵进阶等级
	private int getMaxArmyLevel() {
		int max = 0;
		for (Army army : armys.getArmys()) {
			if (army.getLevel() > max) {
				max = army.getLevel();
			}
		}
		return max;
	}
	
	// 最高士兵强化等级
	private int getMaxArmyEnhLevel() {
		int max = 0;
		for (Army army : armys.getArmys()) {
			if (army.getAtkLevel() > max) {
				max = army.getAtkLevel();
			}
			if (army.getDefLevel() > max) {
				max = army.getDefLevel();
			}
		}
		return max;
	}
}
