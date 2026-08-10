package com.pip.servermgr.report.sanguo;

import java.io.IOException;
import java.util.Date;

import peony.game.Equipments;
import peony.game.FormulaList;
import peony.game.GameItem;
import peony.game.Horse;
import peony.game.HorseBag;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerUtil;
import peony.game.Server;
import peony.game.Skills;
import peony.game.Titles;
import peony.game.TransactionBag;
import peony.game.itemenhance.ItemEnhance;
import peony.game.itemenhance.NaturalEnhance;

import com.pip.sanguo.data.equipment.Equipment;
import com.pip.sanguo.data.item.Formula;
import com.pip.sanguo.data.item.Item;
import com.pip.servermgr.report.IPlayer;
import com.pip.util.ResultRow;

public class Sanguo_Player implements IPlayer {
	public int id;
	public String name;
	public int level;
	public int sex;
	public int money;
	public int x;
	public int y;
	public int mapid;
	public int faction;
	public TransactionBag bag;
	public Sanguo_VM vm;
	public Skills skills;
	public int skillpoint;
	public int accountid;
	public int clazz;
	public int propertypoint;
	public Equipments equipments;
	public byte[] actionbar;
	public int credit;
	public int exist;
	public Date createtime;
	public int honor;
	public Date lastlogin;
	public HorseBag horses;
	public int weekcredit;
	public int rank;
	public Titles titles;
	public FormulaList formulas;
	public TransactionBag depot;
	public int killCount;
	public int dieCount;
	public int consumeMoney;
	public int friendCount;
	
	public static Sanguo_Player parse(ResultRow row) throws IOException {
		Sanguo_Player ret = new Sanguo_Player();
		ret.id = row.getInt(1);
		ret.name = row.getString(2);
		ret.level = row.getInt(3);
		ret.sex = row.getInt(4);
		ret.money = row.getInt(5);
		ret.x = row.getInt(6);
		ret.y = row.getInt(7);
		ret.mapid = row.getInt(8);
		ret.faction = row.getInt(9);
		ret.bag = ItemUtil.getTransactionBagFromDB((byte[])row.getObject(10), new Player());
		ret.vm = Sanguo_VM.parse((byte[])row.getObject(11));
		ret.skills = Skills.getSkillsFromDB((byte[])row.getObject(12), new Player());
		ret.skillpoint = row.getInt(13);
		ret.accountid = row.getInt(14);
		ret.clazz = row.getInt(15);
		ret.propertypoint = row.getInt(16);
		ret.equipments = ItemUtil.getEquipmentsFromDB((byte[])row.getObject(17), new Player());
		ret.actionbar = (byte[])row.getObject(18);
		ret.credit = row.getInt(19);
		ret.exist = row.getInt(20);
		ret.createtime = row.getDate(21);
		ret.honor = row.getInt(22);
		ret.lastlogin = row.getDate(23);
		try {
			ret.horses = HorseBag.fromDBBytes((byte[])row.getObject(24), new Player());
		} catch (Exception e) {
		}
		ret.weekcredit = row.getInt(25);
		ret.rank = row.getInt(26);
		ret.titles = Titles.fromDBBytes((byte[])row.getObject(27), new Player());
		ret.formulas = FormulaList.fromDBBytes((byte[])row.getObject(28), new Player());
		ret.depot = ItemUtil.getTransactionBagFromDB((byte[])row.getObject(29), new Player());
		return ret;
	}
	
	/**
	 * 取得某个统计项数据。
	 * @param type 参见Sanguo_ReportEngine里的常量
	 * @return 可能是Boolean, Integer, Float
	 */
	public Object getValue(int type) {
		switch (type) {
		case Sanguo_ReportEngine.TYPE_ISALIVE:
			// 是否存活，最近7天内有登录的
			return (System.currentTimeMillis() - lastlogin.getTime()) / 86400000L < 7;
		case Sanguo_ReportEngine.TYPE_LIVETIME:
			// 存活时间（天）
			return (int)((lastlogin.getTime() - createtime.getTime()) / 86400000L);
		case Sanguo_ReportEngine.TYPE_LEVEL:
			// 等级
			return level;
		case Sanguo_ReportEngine.TYPE_ISPAY:
			// 是否消费
			return consumeMoney > 0;
		case Sanguo_ReportEngine.TYPE_PAY:
			// 消费金额（分）
			return consumeMoney;
		case Sanguo_ReportEngine.TYPE_MONEY:
			// 携带游戏币
			return money;
		case Sanguo_ReportEngine.TYPE_FACTION:
			// 阵营
			return faction;
		case Sanguo_ReportEngine.TYPE_EQULEVEL:
			// 装备平均等级（计算身上所有装备的平均价值）	
			return getTotalValue(equipments.equs);
		case Sanguo_ReportEngine.TYPE_JEWELLEVEL:
			// 宝石平均等级（计算身上所有宝石的平均等级）
			return getJewelLevel(equipments.equs);
		case Sanguo_ReportEngine.TYPE_HOLECOUNT:
			// 装备孔数（全身装备孔数相加）
			return getHoleCount(equipments.equs);
		case Sanguo_ReportEngine.TYPE_STARLEVEL:
			// 平均星级
			return getStarLevel(equipments.equs);
		case Sanguo_ReportEngine.TYPE_ZIZHILEVEL:
			// 平均装备资质
			return getZiZhiLevel(equipments.equs);
		case Sanguo_ReportEngine.TYPE_ENHLEVEL:
			// 平均装备强化等级
			return getEnhLevel(equipments.equs);
		case Sanguo_ReportEngine.TYPE_SKILLPOINT:
			// 技能点消耗比例
			if (PlayerUtil.SKILL_POINT[level] > 0) {
				return (PlayerUtil.SKILL_POINT[level] - skillpoint) * 100 / PlayerUtil.SKILL_POINT[level];
			} else {
				return 100;
			}
		case Sanguo_ReportEngine.TYPE_CLAZZ:
			// 职业
			return clazz;
		case Sanguo_ReportEngine.TYPE_ATTRPOINT:
			// 属性点消耗比例
			return (level * 2 - propertypoint) * 100 / (level * 2);
		case Sanguo_ReportEngine.TYPE_HORSELVL:
			// 坐骑最高等级
			return getMaxLevel(horses);
		case Sanguo_ReportEngine.TYPE_HORSEEQULEVEL:
			// 坐骑装备平均等级（选最高的一个坐骑）
			return getEquLevel(horses);
		case Sanguo_ReportEngine.TYPE_HORSEJEWELLEVEL:
			// 坐骑宝石平均等级（选最高的一个坐骑）
			return getJewelLevel(horses);
		case Sanguo_ReportEngine.TYPE_HORSEHOLECOUNT:
			// 坐骑宝石孔数（选最高的一个坐骑）
			return getHoleCount(horses);
		case Sanguo_ReportEngine.TYPE_HORSESTARLEVEL:
			// 坐骑装备平均星级（选最高的一个坐骑）
			return getStarLevel(horses);
		case Sanguo_ReportEngine.TYPE_HORSEZIZHILEVEL:
			// 坐骑装备平均资质（选最高的一个坐骑）
			return getZiZhiLevel(horses);
		case Sanguo_ReportEngine.TYPE_HORSEENHLEVEL:
			// 坐骑装备平均强化等级（选最高的一个坐骑）
			return getEnhLevel(horses);
		case Sanguo_ReportEngine.TYPE_FORMULA:
			// 配方等级
			return getMaxFormulaLevel();
		case Sanguo_ReportEngine.TYPE_KILLCOUNT:
			// 杀人数
			return killCount;
		case Sanguo_ReportEngine.TYPE_DIECOUNT:
			// 被杀数
			return dieCount;
		case Sanguo_ReportEngine.TYPE_WINLEVEL:
			// 胜率
			if (killCount + dieCount == 0) {
				return 0;
			}
			return killCount * 100 / (killCount + dieCount);
		case Sanguo_ReportEngine.TYPE_FRIENDCOUNT:
			// 好友个数
			return friendCount;
		case Sanguo_ReportEngine.TYPE_FIRSTQUEST:
			// 第一个任务ID
			return vm.current.size() == 0 ? 0 : vm.current.iterator().next();
		case Sanguo_ReportEngine.TYPE_LASTQUEST:
			// 最后完成的任务ID
			return getLastQuest();
		}
		return 0;
	}
	
	// 计算装备价值
	public int getValue(GameItem item) {
		Equipment equ = Server.server.getServiceRegistry().getDataService().data.findEquipment(item.template.id);
		if (equ == null) {
			return 0;
		} else {
			return (int)equ.getValue();
		}
	}
	
	// 计算宝石的总等级
	public int getJewelLevel(GameItem item) {
		int ret = 0;
		if (item.object != null && item.object instanceof ItemEnhance) {
			ItemEnhance enh = (ItemEnhance)item.object;
			for (int id : enh.getJewelIDs()) {
				Item jewelItem = Server.server.getServiceRegistry().getDataService().data.findItem(id);
				ret += jewelItem.playerLevel;
			}
		}
		return ret;
	}
	
	// 计算孔数
	public int getHoleCount(GameItem item) {
		int ret = 0;
		if (item.object != null && item.object instanceof ItemEnhance) {
			ItemEnhance enh = (ItemEnhance)item.object;
			return enh.getJewelCount();
		}
		return ret;
	}
	
	// 计算星级
	public int getStarLevel(GameItem item) {
		int ret = 0;
		if (item.object != null && item.object instanceof ItemEnhance) {
			ItemEnhance enh = (ItemEnhance)item.object;
			ret = enh.getStar();
		}
		return ret;
	}
	
	// 计算资质等级
	public int getZiZhiLevel(GameItem item) {
		int ret = 0;
		if (item.object != null && item.object instanceof ItemEnhance) {
			ItemEnhance enh = (ItemEnhance)item.object;
			for (NaturalEnhance n : enh.getNaturals()) {
				if (n.level > ret) {
					ret = n.level;
				}
			}
		}
		return ret;
	}
	
	// 计算强化等级
	public int getEnhLevel(GameItem item) {
		int ret = 0;
		if (item.object != null && item.object instanceof ItemEnhance) {
			ItemEnhance enh = (ItemEnhance)item.object;
			for (int i = 0; enh.equipEnhanceData != null && i < enh.equipEnhanceData.length; i++) {
				ret += enh.equipEnhanceData[i];
			}
		}
		return ret;
	}
	
	// 计算一组装备的总价值
	public int getTotalValue(GameItem[] equs) {
		int total = 0;
		for (int i = 0; i < equs.length; i++) {
			if (equs[i] != null) {
				total += getValue(equs[i]);
			}
		}
		return total;
	}
	
	// 计算一组装备宝石的总等级
	public int getJewelLevel(GameItem[] equs) {
		int total = 0;
		for (int i = 0; i < equs.length; i++) {
			if (equs[i] != null) {
				total += getJewelLevel(equs[i]);
			}
		}
		return total;
	}
	
	// 计算一组装备宝石总数
	public int getHoleCount(GameItem[] equs) {
		int total = 0;
		for (int i = 0; i < equs.length; i++) {
			if (equs[i] != null) {
				total += getHoleCount(equs[i]);
			}
		}
		return total;
	}
	
	// 计算一组装备总星级
	public int getStarLevel(GameItem[] equs) {
		int total = 0;
		for (int i = 0; i < equs.length; i++) {
			if (equs[i] != null) {
				total += getStarLevel(equs[i]);
			}
		}
		return total;
	}
	
	// 计算一组装备总资质等级
	public int getZiZhiLevel(GameItem[] equs) {
		int total = 0;
		for (int i = 0; i < equs.length; i++) {
			if (equs[i] != null) {
				total += getZiZhiLevel(equs[i]);
			}
		}
		return total;
	}
	
	// 计算一组装备总强化等级
	public int getEnhLevel(GameItem[] equs) {
		int total = 0;
		for (int i = 0; i < equs.length; i++) {
			if (equs[i] != null) {
				total += getEnhLevel(equs[i]);
			}
		}
		return total;
	}
	
	// 计算坐骑背包里最高坐骑等级
	public int getMaxLevel(HorseBag horses) {
		int ret = 0;
		for (Horse h : horses.horses) {
			if (h.level > ret) {
				ret = h.level;
			}
		}
		return ret;
	}
	
	// 计算坐骑背包里最高装备分值
	public int getEquLevel(HorseBag horses) {
		int ret = 0;
		for (Horse h : horses.horses) {
			int v = getTotalValue(h.equs.equs);
			if (v > ret) {
				ret = v;
			}
		}
		return ret;
	}
	
	// 计算坐骑背包里最高宝石总等级
	public int getJewelLevel(HorseBag horses) {
		int ret = 0;
		for (Horse h : horses.horses) {
			int v = getJewelLevel(h.equs.equs);
			if (v > ret) {
				ret = v;
			}
		}
		return ret;
	}
	
	// 计算坐骑背包里最高宝石数量
	public int getHoleCount(HorseBag horses) {
		int ret = 0;
		for (Horse h : horses.horses) {
			int v = getHoleCount(h.equs.equs);
			if (v > ret) {
				ret = v;
			}
		}
		return ret;
	}
	
	// 计算坐骑背包里最高总星级
	public int getStarLevel(HorseBag horses) {
		int ret = 0;
		for (Horse h : horses.horses) {
			int v = getStarLevel(h.equs.equs);
			if (v > ret) {
				ret = v;
			}
		}
		return ret;
	}
	
	// 计算坐骑背包里最高总资质等级
	public int getZiZhiLevel(HorseBag horses) {
		int ret = 0;
		for (Horse h : horses.horses) {
			int v = getZiZhiLevel(h.equs.equs);
			if (v > ret) {
				ret = v;
			}
		}
		return ret;
	}
	
	// 计算坐骑背包里最高总强化等级
	public int getEnhLevel(HorseBag horses) {
		int ret = 0;
		for (Horse h : horses.horses) {
			int v = getEnhLevel(h.equs.equs);
			if (v > ret) {
				ret = v;
			}
		}
		return ret;
	}
	
	// 计算配方最高等级
	public int getMaxFormulaLevel() {
		int ret = 0;
		for (int id : formulas.ids) {
			Formula f = (Formula)Server.server.getServiceRegistry().getDataService().data.findObject(Formula.class, id);
			if (f.level > ret) {
				ret = f.level;
			}
		}
		return ret;
	}
	
	// 最后完成的任务ID
	public int getLastQuest() {
		if (vm.finishedTime.size() == 0) {
			return 0;
		}
		long latestTime = 0;
		int ret = 0;
		for (int i = 0; i < vm.finishedTime.size(); i++) {
			if (vm.finishedTime.get(i) > latestTime) {
				ret = vm.finished.get(i);
				latestTime = vm.finishedTime.get(i);
			}
		}
		return ret;
	}
}
