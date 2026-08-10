package com.pip.servermgr.report.xuanyuan;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;

import shaft.horse.HorseBag;
import shaft.item.BagsEx;
import shaft.item.EquipmentsEx;
import shaft.item.ItemEnhance;
import shaft.pet.PetBag;
import shaft.pet.PetEx;
import shaft.produce.FormulaListEx;
import shaft.skill.SkillsEx;
import shaft.sprite.PlayerEx;
import shaft.util.HorseUtils;
import shaft.util.ItemUtil;
import shaft.util.PetUtils;

import com.pip.game.data.equipment.Equipment;
import com.pip.game.data.item.Formula;
import com.pip.game.data.item.Item;
import com.pip.servermgr.report.IPlayer;
import com.pip.servermgr.report.xiyou.Xiyou_VM;
import com.pip.util.ResultRow;

import cybertron.mmo.Platform;
import cybertron.mmo.data.DataService;
import cybertron.mmo.gameitem.GameItem;

public class Xuanyuan_Player implements IPlayer {
	public int id;
	public int accountid;
	public String name;
	public int mapid;
	public int x;
	public int y;
	public int level;
	public int exp;
	public int sex;
	public int faction;
	public int tongId;
	public int money;
	public int skillPoint;
	public int skillExp;
	public Date lastLoginTime;
	public Date lastLogoutTime;
	public Date createTime;
	public int tongMoney;
	public int exist;
	public HorseBag horseBag;
	public PetBag petBag;
	public int agility;
	public int strength;
	public int vitality;
	public int intellect;
	public int propertyPoint;
	public int restSkillPoint;
	public FormulaListEx formulas;
	public BagsEx bags;
	public SkillsEx skills;
	public EquipmentsEx equs;
	public Xuanyuan_VM questVm;
	
	public int consumeMoney;
	public int firstItemID;
	public int firstLevel;
	public int friendCount;
	public int consumeBoundIMoney;
	
	public static Xuanyuan_Player parse(ResultRow row) throws IOException {
		Xuanyuan_Player ret = new Xuanyuan_Player();
		ret.id = row.getInt(1);
		ret.accountid = row.getInt(2);
		ret.name = row.getString(3);
		ret.mapid = row.getInt(4);
		ret.x = row.getInt(5);
		ret.y = row.getInt(6);
		ret.level = row.getInt(7);
		ret.exp = row.getInt(8);
		ret.sex = row.getInt(9);
		ret.faction = row.getInt(10);
		ret.tongId = row.getInt(11);
		ret.money = row.getInt(12);
		ret.skillPoint = row.getInt(13);
		ret.skillExp = row.getInt(14);
		ret.lastLoginTime = row.getDate(15);
		ret.lastLogoutTime = row.getDate(16);
		ret.createTime = row.getDate(17);
		ret.tongMoney = row.getInt(18);
		ret.exist = row.getInt(19);
		ret.horseBag = HorseUtils.fromDBBytes((byte[])row.getObject(20), new PlayerEx());
		ret.petBag = PetUtils.getPetBagFromDbBytes((byte[])row.getObject(21), new PlayerEx());
		ret.agility = row.getInt(22);
		ret.strength = row.getInt(23);
		ret.vitality = row.getInt(24);
		ret.intellect = row.getInt(25);
		ret.propertyPoint = row.getInt(26);
		ret.restSkillPoint = row.getInt(27);
		ret.formulas = FormulaListEx.fromDBBytes((byte[])row.getObject(28), new PlayerEx());
		ret.bags = ItemUtil.readBagsFromDB((byte[])row.getObject(29), new PlayerEx());
		ret.skills = (SkillsEx)SkillsEx.getSkillsFromDB(new DataInputStream(new ByteArrayInputStream((byte[])row.getObject(30))), new PlayerEx());
		ret.equs = ItemUtil.readEquipmentsFromDB(new DataInputStream(new ByteArrayInputStream((byte[])row.getObject(31))), new PlayerEx());
		ret.questVm = Xuanyuan_VM.parse((byte[])row.getObject(32));
		
		return ret;
	}
	
	/**
	 * 取得某个统计项数据。
	 * @param type 参见Xuanyuan_ReportEngine里的常量
	 * @return 可能是Boolean, Integer, Float
	 */
	public Object getValue(int type) {
		switch (type) {
		case Xuanyuan_ReportEngine.TYPE_ISALIVE:
			// 是否存活，最近7天内有登录的
			return (System.currentTimeMillis() - lastLoginTime.getTime()) / 86400000L < 7;
		case Xuanyuan_ReportEngine.TYPE_LIVETIME:
			// 存活时间（天）
			return (int)((lastLoginTime.getTime() - createTime.getTime()) / 86400000L);
		case Xuanyuan_ReportEngine.TYPE_LEVEL:
			// 等级
			return level;
		case Xuanyuan_ReportEngine.TYPE_ISPAY:
			// 是否消费
			return consumeMoney > 0;
		case Xuanyuan_ReportEngine.TYPE_PAY:
			// 消费金额（分）
			return (int)(consumeMoney / 3.6);
		case Xuanyuan_ReportEngine.TYPE_MONEY:
			// 携带游戏币
			return money;
		case Xuanyuan_ReportEngine.TYPE_FACTION:
			// 阵营
			return faction;
		case Xuanyuan_ReportEngine.TYPE_EQULEVEL:
			// 装备总等级（计算身上所有装备的总价值）	
			return getTotalValue(equs.getEqus());
		case Xuanyuan_ReportEngine.TYPE_JEWELLEVEL:
			// 宝石总等级（计算身上所有宝石的总等级）
			return getJewelLevel(equs.getEqus());
		case Xuanyuan_ReportEngine.TYPE_HOLECOUNT:
			// 装备孔数（全身装备孔数相加）
			return getHoleCount(equs.getEqus());
		case Xuanyuan_ReportEngine.TYPE_STARLEVEL:
			// 平均星级
			return getStarLevel(equs.getEqus());
		case Xuanyuan_ReportEngine.TYPE_ZIZHILEVEL:
			// 平均装备资质
			return getZiZhiLevel(equs.getEqus());
		case Xuanyuan_ReportEngine.TYPE_SKILLPOINT:
			// 技能点消耗比例
		{
			if (skillPoint > 0) {
				return (skillPoint - restSkillPoint) * 100 / skillPoint;
			} else {
				return 100;
			}
		}
		case Xuanyuan_ReportEngine.TYPE_CLAZZ:
			// 职业
			return 0;
		case Xuanyuan_ReportEngine.TYPE_ATTRPOINT:
			// 属性点消耗比例
		{
			int total = propertyPoint + strength + agility + vitality + intellect;
			return (total - propertyPoint) * 100 / total;
		}
		case Xuanyuan_ReportEngine.TYPE_PETCOUNT:
			// 宠物数量
			return petBag.getPetMapSize();
		case Xuanyuan_ReportEngine.TYPE_PETLVL:
			// 宠物最高等级
			return getMaxLevel(petBag);
		case Xuanyuan_ReportEngine.TYPE_PETEQULEVEL:
			// 宠物装备平均等级（选最高的一个宠物）
			return getEquLevel(petBag);
		case Xuanyuan_ReportEngine.TYPE_PETJEWELLEVEL:
			// 宠物宝石平均等级（选最高的一个宠物）
			return getJewelLevel(petBag);
		case Xuanyuan_ReportEngine.TYPE_PETHOLECOUNT:
			// 宠物宝石孔数（选最高的一个宠物）
			return getHoleCount(petBag);
		case Xuanyuan_ReportEngine.TYPE_PETSTARLEVEL:
			// 宠物装备平均星级（选最高的一个宠物）
			return getStarLevel(petBag);
		case Xuanyuan_ReportEngine.TYPE_PETZIZHILEVEL:
			// 宠物装备平均资质（选最高的一个宠物）
			return getZiZhiLevel(petBag);
		case Xuanyuan_ReportEngine.TYPE_FORMULA:
			// 配方等级
			return getMaxFormulaLevel();
		case Xuanyuan_ReportEngine.TYPE_FRIENDCOUNT:
			// 好友个数
			return friendCount;
		case Xuanyuan_ReportEngine.TYPE_FIRSTBUYITEM:
			// 首次购买物品
			return firstItemID;
		case Xuanyuan_ReportEngine.TYPE_FIRSTBUYLVL:
			// 首次购买等级
			return firstLevel;
		case Xuanyuan_ReportEngine.TYPE_HORSECOUNT:
			// 坐骑数
			return horseBag.horses.size();
		case Xuanyuan_ReportEngine.TYPE_FINISHQUEST:
			// 完成任务数
			return questVm.finished.size();
		case Xuanyuan_ReportEngine.TYPE_CURRENTQUEST:
			// 当前任务数
			return questVm.current.size();
		case Xuanyuan_ReportEngine.TYPE_HASTONG:
			// 是否有血盟
			return tongId != 0;
		case Xuanyuan_ReportEngine.TYPE_BOUNDIMONEY:
			// 绑定元宝
			return 0;
		case Xuanyuan_ReportEngine.TYPE_USEDBOUNDIMONEY:
			// 绑定元宝总消费
			return (int)(consumeBoundIMoney / 3.6);
		case Xuanyuan_ReportEngine.TYPE_BAGSIZE:
			// 背包格数
			return bags.getBag(0).getSize();
		}
		return 0;
	}
	
	// 计算装备价值
	public int getValue(GameItem item) {
		Equipment equ = Platform.getAppContext().get(DataService.class).data.findEquipment(item.getTemplate().getId());
		if (equ == null) {
			return 0;
		} else {
			return (int)equ.getValue();
		}
	}
	
	// 计算宝石的总等级
	public int getJewelLevel(GameItem item) {
		int ret = 0;
		if (item.getGameItemObject() != null && item.getGameItemObject() instanceof ItemEnhance) {
			ItemEnhance enh = (ItemEnhance)item.getGameItemObject();
			for (int i = 0; i < enh.jewels.size(); i++) {
				Item jewelItem = Platform.getAppContext().get(DataService.class).data.findItem(enh.jewels.get(i));
				ret += jewelItem.playerLevel;
			}
		}
		return ret;
	}
	
	// 计算孔数
	public int getHoleCount(GameItem item) {
		int ret = 0;
		if (item.getGameItemObject() != null && item.getGameItemObject() instanceof ItemEnhance) {
			ItemEnhance enh = (ItemEnhance)item.getGameItemObject();
			return enh.getJewels().size();
		}
		return ret;
	}
	
	// 计算星级
	public int getStarLevel(GameItem item) {
		int ret = 0;
		if (item.getGameItemObject() != null && item.getGameItemObject() instanceof ItemEnhance) {
			ItemEnhance enh = (ItemEnhance)item.getGameItemObject();
			ret = enh.getStar();
		}
		return ret;
	}
	
	// 计算资质等级
	public int getZiZhiLevel(GameItem item) {
		int ret = 0;
		if (item.getGameItemObject() != null && item.getGameItemObject() instanceof ItemEnhance) {
			ItemEnhance enh = (ItemEnhance)item.getGameItemObject();
			ret = enh.judgeLevelFactor[0] + enh.judgeLevelFactor[1];
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
	
	// 计算宠物背包里最高宠物等级
	public int getMaxLevel(PetBag pets) {
		int ret = 0;
		for (PetEx pet : pets.getPetMap().valueCollection()) {
			if (pet.getLevel() > ret) {
				ret = pet.getLevel();
			}
		}
		return ret;
	}
	
	// 计算宠物背包里最高装备分值
	public int getEquLevel(PetBag pets) {
		int ret = 0;
		for (PetEx pet : pets.getPetMap().valueCollection()) {
			int v = getTotalValue(pet.getEquipments().getEqus());
			if (v > ret) {
				ret = v;
			}
		}
		return ret;
	}
	
	// 计算宠物背包里最高宝石总等级
	public int getJewelLevel(PetBag pets) {
		int ret = 0;
		for (PetEx pet : pets.getPetMap().valueCollection()) {
			int v = getJewelLevel(pet.getEquipments().getEqus());
			if (v > ret) {
				ret = v;
			}
		}
		return ret;
	}
	
	// 计算宠物背包里最高宝石数量
	public int getHoleCount(PetBag pets) {
		int ret = 0;
		for (PetEx pet : pets.getPetMap().valueCollection()) {
			int v = getHoleCount(pet.getEquipments().getEqus());
			if (v > ret) {
				ret = v;
			}
		}
		return ret;
	}
	
	// 计算宠物背包里最高总星级
	public int getStarLevel(PetBag pets) {
		int ret = 0;
		for (PetEx pet : pets.getPetMap().valueCollection()) {
			int v = getStarLevel(pet.getEquipments().getEqus());
			if (v > ret) {
				ret = v;
			}
		}
		return ret;
	}
	
	// 计算宠物背包里最高总资质等级
	public int getZiZhiLevel(PetBag pets) {
		int ret = 0;
		for (PetEx pet : pets.getPetMap().valueCollection()) {
			int v = getZiZhiLevel(pet.getEquipments().getEqus());
			if (v > ret) {
				ret = v;
			}
		}
		return ret;
	}
	
	// 计算配方最高等级
	public int getMaxFormulaLevel() {
		int ret = 0;
		for (int id : formulas.getFormulaIds()) {
			Formula f = (Formula)Platform.getAppContext().get(DataService.class).data.findObject(Formula.class, id);
			if (f.level > ret) {
				ret = f.level;
			}
		}
		return ret;
	}
}
