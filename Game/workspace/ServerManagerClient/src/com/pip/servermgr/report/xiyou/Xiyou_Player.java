package com.pip.servermgr.report.xiyou;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;

import optimus.designation.XyDesignations;
import optimus.equipbarstrengthen.EquipBar;
import optimus.gameobject.Const;
import optimus.item.BagsEx;
import optimus.item.EquipmentsEx;
import optimus.item.ItemUtil;
import optimus.item.PetEquipmentEx;
import optimus.item.itemenhance.ItemEnhance;
import optimus.item.itemenhance.NaturalEnhance;
import optimus.magicweapon.MagicBag;
import optimus.pet.PetBag;
import optimus.pet.PetEx;
import optimus.player.PlayerEx;
import optimus.player.PlayerExUtil;
import optimus.player.SkillsEx;
import optimus.player.interal.PlayerInteral;
import optimus.produce.FormulaList;
import optimus.talent.TalentService;

import com.pip.game.data.equipment.Equipment;
import com.pip.game.data.item.Formula;
import com.pip.game.data.item.Item;
import com.pip.servermgr.report.IPlayer;
import com.pip.util.ResultRow;

import cybertron.core.Bag;
import cybertron.core.BagGrid;
import cybertron.core.Bags;
import cybertron.core.GameItem;
import cybertron.core.Platform;
import cybertron.core.PropertyPool;
import cybertron.core.data.DataService;
import cybertron.core.skill.Skill;
import cybertron.core.skill.Skills;

public class Xiyou_Player implements IPlayer {
	public int id;
	public int accountid;
	public String name;
	public int mapid;
	public int x;
	public int y;
	public int level;
	public int exp;
	public int sex;
	public int propertypoint;
	public int money;
	public BagsEx bags;
	public int faction;
	public EquipmentsEx equs;
	public int exist;
	public MagicBag magicBag;
	public Skills skills;
	public PetBag petBag;
	public byte[] opBarConf;
	public int skillPoint;
	public Xiyou_VM questVm;
	public String holeName;
	public int honour;
	public int actionPoint;
	public int ability;
	public int reputation;
	public FormulaList formulaList;
	public PropertyPool pool;
	public Date lastLoginTime;
	public Date createTime;
	public int boundIMoney;
	public GameItem flyingItem;
	public TIntObjectHashMap<EquipBar> equipBar;
	public XyDesignations designations;
	public Bags storeHouses;
	public PlayerInteral interalCount;
	public int integral;
	public int yuanyinglevel;
	
	public int consumeMoney;
	public int firstItemID;
	public int firstLevel;
	public int friendCount;
	public int consumeBoundIMoney;
	
	public static Xiyou_Player parse(ResultRow row) throws IOException {
		Xiyou_Player ret = new Xiyou_Player();
		ret.id = row.getInt(1);
		ret.accountid = row.getInt(2);
		ret.name = row.getString(3);
		ret.mapid = row.getInt(4);
		ret.x = row.getInt(5);
		ret.y = row.getInt(6);
		ret.level = row.getInt(7);
		ret.exp = row.getInt(8);
		ret.sex = row.getInt(9);
		ret.propertypoint = row.getInt(10);
		ret.money = row.getInt(11);
		ret.bags = ItemUtil.getBagsFromDB((byte[])row.getObject(12), new PlayerEx());
		ret.faction = row.getInt(13);
		ret.equs = ItemUtil.getEquipmentsFromDB((byte[])row.getObject(14), new PlayerEx());
		ret.exist = row.getInt(15);
		if (row.getObject(16) != null) {
			ret.magicBag = MagicBag.getMagicBagFromDB((byte[])row.getObject(16), new PlayerEx());
		} else {
			ret.magicBag = new MagicBag(new PlayerEx());
		}
		ret.skills = SkillsEx.getSkillsFromDB((byte[])row.getObject(17), new PlayerEx());
		if (row.getObject(18) != null) {
			ret.petBag = PetBag.getPetBagFromDB((byte[])row.getObject(18), new PlayerEx());
		} else {
			ret.petBag = new PetBag(new PlayerEx());
		}
		ret.opBarConf = (byte[])row.getObject(19);
		ret.skillPoint = row.getInt(20);
		ret.questVm = Xiyou_VM.parse((byte[])row.getObject(21));
		ret.holeName = row.getString(22);
		ret.honour = row.getInt(23);
		ret.actionPoint = row.getInt(24);
		ret.ability = row.getInt(25);
		ret.reputation = row.getInt(26);
		ret.formulaList = FormulaList.fromDBBytes((byte[])row.getObject(27), new PlayerEx());
		ret.pool = new PropertyPool();
		if (row.getString(28) != null) {
			byte[] jj = (byte[])row.getObject(28);
			String s = new String(jj);
			ret.pool.parse(s);
		}
		ret.lastLoginTime = row.getDate(29);
		ret.createTime = row.getDate(30);
		ret.boundIMoney = row.getInt(31);
		if (row.getObject(32) != null) {
			ret.flyingItem = ItemUtil.getGameItemFromDB(new DataInputStream(new ByteArrayInputStream((byte[])row.getObject(32))));
		}
		if (row.getObject(33) != null) {
			ret.equipBar = EquipBar.getEquipBarFromDB((byte[])row.getObject(33), new PlayerEx());
		}
		if (row.getObject(34) != null) {
			ret.designations = XyDesignations.fromDBBytes((byte[])row.getObject(34), new PlayerEx());
		}
		if (row.getObject(35) != null) {
			ret.storeHouses = (Bags)ItemUtil.getStoreHousesFromDB((byte[])row.getObject(35), new PlayerEx());
		}
		if (row.getObject(36) != null) {
			ret.interalCount = PlayerInteral.getPlayerInteralFromDB((byte[])row.getObject(36), new PlayerEx());
		}
		ret.integral = row.getInt(37);
		ret.yuanyinglevel = row.getInt(38);
		
		return ret;
	}
	
	/**
	 * 取得某个统计项数据。
	 * @param type 参见Xiyou_ReportEngine里的常量
	 * @return 可能是Boolean, Integer, Float
	 */
	public Object getValue(int type) {
		switch (type) {
		case Xiyou_ReportEngine.TYPE_ISALIVE:
			// 是否存活，最近7天内有登录的
			return (System.currentTimeMillis() - lastLoginTime.getTime()) / 86400000L < 7;
		case Xiyou_ReportEngine.TYPE_LIVETIME:
			// 存活时间（天）
			return (int)((lastLoginTime.getTime() - createTime.getTime()) / 86400000L);
		case Xiyou_ReportEngine.TYPE_LEVEL:
			// 等级
			return level;
		case Xiyou_ReportEngine.TYPE_ISPAY:
			// 是否消费
			return consumeMoney > 0;
		case Xiyou_ReportEngine.TYPE_PAY:
			// 消费金额（分）
			return (int)(consumeMoney / 3.6);
		case Xiyou_ReportEngine.TYPE_MONEY:
			// 携带游戏币
			return money;
		case Xiyou_ReportEngine.TYPE_FACTION:
			// 阵营
			return faction;
		case Xiyou_ReportEngine.TYPE_EQULEVEL:
			// 装备总等级（计算身上所有装备的总价值）	
			return getTotalValue(equs.getEquips());
		case Xiyou_ReportEngine.TYPE_JEWELLEVEL:
			// 宝石总等级（计算身上所有宝石的总等级）
			return getJewelLevel(equs.getEquips());
		case Xiyou_ReportEngine.TYPE_HOLECOUNT:
			// 装备孔数（全身装备孔数相加）
			return getHoleCount(equs.getEquips());
		case Xiyou_ReportEngine.TYPE_STARLEVEL:
			// 平均星级
			return getStarLevel(equs.getEquips());
		case Xiyou_ReportEngine.TYPE_ZIZHILEVEL:
			// 平均装备资质
			return getZiZhiLevel(equs.getEquips());
		case Xiyou_ReportEngine.TYPE_SKILLPOINT:
			// 技能点消耗比例
		{
			int total = PlayerExUtil.getGrowSkillPoint(1, level);
			if (total > 0) {
				return (total - skillPoint) * 100 / total;
			} else {
				return 100;
			}
		}
		case Xiyou_ReportEngine.TYPE_CLAZZ:
			// 职业
			return getMainTalent();		
		case Xiyou_ReportEngine.TYPE_ATTRPOINT:
			// 属性点消耗比例
			return (level * 2 - propertypoint) * 100 / (level * 2);
		case Xiyou_ReportEngine.TYPE_PETLVL:
			// 宠物最高等级
			return getMaxLevel(petBag);
		case Xiyou_ReportEngine.TYPE_PETQUALITY:
			// 宠物最高价值
			return getMaxQuality(petBag);
		case Xiyou_ReportEngine.TYPE_PETEQULEVEL:
			// 宠物装备平均等级（选最高的一个宠物）
			return getEquLevel(petBag);
		case Xiyou_ReportEngine.TYPE_PETJEWELLEVEL:
			// 宠物宝石平均等级（选最高的一个宠物）
			return getJewelLevel(petBag);
		case Xiyou_ReportEngine.TYPE_PETHOLECOUNT:
			// 宠物宝石孔数（选最高的一个宠物）
			return getHoleCount(petBag);
		case Xiyou_ReportEngine.TYPE_PETSTARLEVEL:
			// 宠物装备平均星级（选最高的一个宠物）
			return getStarLevel(petBag);
		case Xiyou_ReportEngine.TYPE_PETZIZHILEVEL:
			// 宠物装备平均资质（选最高的一个宠物）
			return getZiZhiLevel(petBag);
		case Xiyou_ReportEngine.TYPE_FORMULA:
			// 配方等级
			return getMaxFormulaLevel();
		case Xiyou_ReportEngine.TYPE_FRIENDCOUNT:
			// 好友个数
			return friendCount;
		case Xiyou_ReportEngine.TYPE_FIRSTBUYITEM:
			// 首次购买物品
			return firstItemID;
		case Xiyou_ReportEngine.TYPE_FIRSTBUYLVL:
			// 首次购买等级
			return firstLevel;
		case Xiyou_ReportEngine.TYPE_MAGICCOUNT:
			// 法宝数
			return magicBag.getMagicweaponCount();
		case Xiyou_ReportEngine.TYPE_MAGICLEVEL:
			// 法宝等级
			// TODO Integer 计算法宝价值
			return 0;
		case Xiyou_ReportEngine.TYPE_FINISHQUEST:
			// 完成任务数
			return questVm.finished.size();
		case Xiyou_ReportEngine.TYPE_CURRENTQUEST:
			// 当前任务数
			return questVm.current.size();
		case Xiyou_ReportEngine.TYPE_HASTONG:
			// 是否有洞府
			return holeName != null && holeName.length() > 0;
		case Xiyou_ReportEngine.TYPE_HONOR:
			// 荣誉
			return honour;
		case Xiyou_ReportEngine.TYPE_REPUTATION:
			// 声望
			return reputation;
		case Xiyou_ReportEngine.TYPE_BOUNDIMONEY:
			// 绑定元宝
			return boundIMoney / 360;
		case Xiyou_ReportEngine.TYPE_USEDBOUNDIMONEY:
			// 绑定元宝总消费
			return (int)(consumeBoundIMoney / 3.6);
		case Xiyou_ReportEngine.TYPE_FLY:
			// 是否飞行
			return flyingItem != null;
		case Xiyou_ReportEngine.TYPE_EQUIPBAR:
			// 装备栏强化总等级
			return getTotalEquipBar();
		case Xiyou_ReportEngine.TYPE_BAGSIZE:
			// 背包格数
			return bags.getBag(0).getSize();
		case Xiyou_ReportEngine.TYPE_YUANYING_LEVEL:
			return yuanyinglevel;
		case Xiyou_ReportEngine.TYPE_HAS_SW:
			return hasTopEquip();
		}
		return 0;
	}
	
	public boolean hasTopEquip() {
		for(GameItem equ : equs.getEquips()) {
			if(equ != null && equ.getName().startsWith("尚武之王")) {
				return true;
			}
		}
		
		Bag bag = bags.getBag(0);
		for(BagGrid grid : bag.getGrids()) {
			GameItem equ = grid.getItem();
			if(equ != null && equ.getName().startsWith("尚武之王")) {
				return true;
			}
		}
		
		return false;
	}
	
	//获取主天赋id
	public int getMainTalent() {
		int mainTalent = pool.getInt(Const.PROPERTY_MAIN_TALENT, 0);
		if(mainTalent == -1) {
			if(this.faction == 1 || this.faction == 4) {
				Skill skill = skills.getSkillByGroupId(14); //修罗的终极技能，七杀
				if(skill != null) {
					return 1;
				}
				skill = skills.getSkillByGroupId(26); //灵璧的终极技能，灵脉
				if(skill != null) {
					return 2;
				}
				skill = skills.getSkillByGroupId(46); //魂缚的终极技能，燃魂
				if(skill != null) {
					return 3;
				}
				skill = skills.getSkillByGroupId(50); //食脱的终极技能，七杀
				if(skill != null) {
					return 4;
				}
			} else {
				Skill skill = skills.getSkillByGroupId(61); //仙剑的终极技能，龙吻
				if(skill != null) {
					return 5;
				}
				skill = skills.getSkillByGroupId(73); //霸体的终极技能，喋血
				if(skill != null) {
					return 6;
				}
				skill = skills.getSkillByGroupId(76); //灵羁的终极技能，燃灵
				if(skill != null) {
					return 7;
				}
				skill = skills.getSkillByGroupId(97); //杏林的终极技能，灵种
				if(skill != null) {
					return 8;
				}
			}
		} else {
			return mainTalent;
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
			for (GameItem jitem : enh.getJewels()) {
				Item jewelItem = Platform.getAppContext().get(DataService.class).data.findItem(jitem.getTemplate().getId());
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
			for (NaturalEnhance n : enh.getNaturals()) {
				if (n.level > ret) {
					ret = n.level;
				}
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
	
	// 计算宠物背包里最高宠物等级
	public int getMaxLevel(PetBag pets) {
		int ret = 0;
		for (PetEx pet : pets.getPets()) {
			if (pet.getLevel() > ret) {
				ret = pet.getLevel();
			}
		}
		return ret;
	}
	
	// 计算宠物背包里最高宠物价值
	public int getMaxQuality(PetBag pets) {
		int ret = 0;
		for (PetEx pet : pets.getPets()) {
			if (getQuality(pet) > ret) {
				ret = getQuality(pet);
			}
		}
		return ret;
	}
	
	// 计算一只宠物的价值
	public int getQuality(PetEx pet) {
		// TODO 如何评估一个宠物的价值？
		return 0;
	}
	
	// 计算宠物背包里最高装备分值
	public int getEquLevel(PetBag pets) {
		int ret = 0;
		for (PetEx pet : pets.getPets()) {
			int v = getTotalValue(((PetEquipmentEx)pet.getEquipments()).getEquips());
			if (v > ret) {
				ret = v;
			}
		}
		return ret;
	}
	
	// 计算宠物背包里最高宝石总等级
	public int getJewelLevel(PetBag pets) {
		int ret = 0;
		for (PetEx pet : pets.getPets()) {
			int v = getJewelLevel(((PetEquipmentEx)pet.getEquipments()).getEquips());
			if (v > ret) {
				ret = v;
			}
		}
		return ret;
	}
	
	// 计算宠物背包里最高宝石数量
	public int getHoleCount(PetBag pets) {
		int ret = 0;
		for (PetEx pet : pets.getPets()) {
			int v = getHoleCount(((PetEquipmentEx)pet.getEquipments()).getEquips());
			if (v > ret) {
				ret = v;
			}
		}
		return ret;
	}
	
	// 计算宠物背包里最高总星级
	public int getStarLevel(PetBag pets) {
		int ret = 0;
		for (PetEx pet : pets.getPets()) {
			int v = getStarLevel(((PetEquipmentEx)pet.getEquipments()).getEquips());
			if (v > ret) {
				ret = v;
			}
		}
		return ret;
	}
	
	// 计算宠物背包里最高总资质等级
	public int getZiZhiLevel(PetBag pets) {
		int ret = 0;
		for (PetEx pet : pets.getPets()) {
			int v = getZiZhiLevel(((PetEquipmentEx)pet.getEquipments()).getEquips());
			if (v > ret) {
				ret = v;
			}
		}
		return ret;
	}
	
	// 计算配方最高等级
	public int getMaxFormulaLevel() {
		int ret = 0;
		for (int id : formulaList.ids) {
			Formula f = (Formula)Platform.getAppContext().get(DataService.class).data.findObject(Formula.class, id);
			if (f.level > ret) {
				ret = f.level;
			}
		}
		return ret;
	}
	
	// 计算装备栏强化等级之和
	public int getTotalEquipBar() {
		if (equipBar == null) {
			return 0;
		}
		int ret = 0;
		for (EquipBar bar : equipBar.valueCollection()) {
			ret += bar.effectLevel;
		}
		return ret;
	}
}
