package com.pip.itimes.server.stage;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;

public class MagicPosMessage {
	/** Integer为阵眼类型 value为该阵眼数据表 */
	public static LinkedHashMap<Integer, List<MagicPosMessage>> magicPositionMessageMap = new LinkedHashMap<Integer, List<MagicPosMessage>>();

	public static final int defaultMinLevel = 1;
	public static final int defaultMinFloor = 1;

	// 阵眼类型
	private static final int waterMagic = 0;
	private static final int soilMagic = 1;
	private static final int fireMagic = 2;
	private static final int windMagic = 3;
	private static final int mindMagic = 4;

	// 级别类型
	private static final int firLev = 1;
	private static final int secLev = 2;
	private static final int thiLev = 3;
	private static final int fourLev = 4;
	private static final int fifLev = 5;

	int level;
	int floor;
	int exp;
	// 阵眼带来属性提升
	int attack;
	int mattack;
	int pdef;
	int mdef;
	int hit;
	int pcri;
	int mcri;
	int flee;
	int nocri;
	int hp;
	int mp;

	public MagicPosMessage(int magiclevel, int magicfloor, int magicexp,
			int attack, int mattack, int pdef, int mdef, int hit, int pcri,
			int mcri, int flee, int nocri, int hp, int mp) {
		this.level = magiclevel;
		this.floor = magicfloor;
		this.exp = magicexp;
		this.attack = attack;
		this.mattack = mattack;
		this.pdef = pdef;
		this.mdef = mdef;
		this.hit = hit;
		this.pcri = pcri;
		this.mcri = mcri;
		this.flee = flee;
		this.nocri = nocri;
		this.hp = hp;
		this.mp = mp;
	}

	public int getMagicPoslevel() {
		return level;
	}

	public int getMagicPosFloor() {
		return floor;
	}

	public int getMagicPosExp() {
		return exp;
	}

	public int getMagicPosAttack() {
		return attack;
	}

	public int getMagicPosMattack() {
		return mattack;
	}

	public int getMagicPosPdef() {
		return pdef;
	}

	public int getMagicPosMdef() {
		return mdef;
	}

	public int getMagicPosHit() {
		return hit;
	}

	public int getMagicPosPcri() {
		return pcri;
	}

	public int getMagicPosMcri() {
		return mcri;
	}

	public int getMagicPosFlee() {
		return flee;
	}

	public int getMagicPosNocri() {
		return nocri;
	}

	public int getMagicPosHp() {
		return hp;
	}

	public int getMagicPosMp() {
		return mp;
	}

	public static void addMagicPosMessage(Integer type, MagicPosMessage mpm) {
		if (magicPositionMessageMap.get(type) == null) {
			List<MagicPosMessage> list = new ArrayList<MagicPosMessage>();
			list.add(mpm);
			magicPositionMessageMap.put(type, list);
		} else {
			magicPositionMessageMap.get(type).add(mpm);
		}
	}

	/**
	 * @param type
	 *            阵眼类型
	 * @param level
	 *            当前等级
	 * @param floor
	 *            当前阶层
	 * @return 当前等级阶层的总经验
	 */
	public static int getCurrentLevelOrFloorSumExp(int type, int level,
			int floor) {
		int currentlevelOrFloorSumExp = 0;
		int loopcount = 0;
		if (level > 1) {
			loopcount = (level - 1) * 10 + floor;
		} else {
			loopcount = floor;
		}
		List<MagicPosMessage> magicpos = new ArrayList<MagicPosMessage>();
		magicpos = magicPositionMessageMap.get(type);// 获得当前类型数据表
		for (int i = 0; i < loopcount; i++) {
			currentlevelOrFloorSumExp += magicpos.get(i).getMagicPosExp();
		}
		return currentlevelOrFloorSumExp;
	}

	public static int[] getCurrentLevelFloor(int type, int exp) {
		int level = 0;
		int floor = 0;
		List<MagicPosMessage> magicpos = new ArrayList<MagicPosMessage>();
		magicpos = magicPositionMessageMap.get(type);// 获得当前类型数据表
		int calcExp = 0;
		int i = 0;
		boolean isEnough = false;
		for (; i < magicpos.size(); i++) {
			calcExp += magicpos.get(i).getMagicPosExp();
			if (calcExp > exp) {
				isEnough = false;
				break;
			}
			if (calcExp == exp) {
				isEnough = true;
				break;
			}
		}
		if (!isEnough) {
			i--;
		}
		if (i < 0)
			i = 1;
		level = i / 10;
		floor = i % 10;
		return new int[] { level + 1, floor + 1 };
	}

	/**
	 * @param type
	 *            阵眼类型
	 * @param level
	 *            当前等级
	 * @param floor
	 *            当前阶层
	 * @return 升级阶层所需经验
	 */
	public static int getFloorSumExp(int type, int level, int floor) {
		int sumExp = 0;
		int startfloor = 1;
		int nextfloor = 0;
		if (floor < 10) {
			nextfloor = floor + 1;
		} else if (floor == 10) {
			nextfloor = 10;
		}
		List<MagicPosMessage> magicpos = new ArrayList<MagicPosMessage>();
		magicpos = magicPositionMessageMap.get(type);// 获得当前类型数据表
		for (MagicPosMessage tmp : magicpos) {
			if (tmp.getMagicPoslevel() == level
					&& tmp.getMagicPosFloor() == startfloor) {
				sumExp += tmp.getMagicPosExp();
				startfloor++;
				if (startfloor > nextfloor) {
					return sumExp;
				}
			}
		}
		return sumExp;
	}

	/**
	 * @param type
	 *            阵眼类型
	 * @param floor
	 *            当前等级
	 * @return 升级等级所需经验(一个等级总经验)
	 */
	public static int getLevelSumExp(int type, int level) {
		int sumExp = 0;
		if (level > 5) {
			return 0;
		}
		List<MagicPosMessage> magicpos = new ArrayList<MagicPosMessage>();
		magicpos = magicPositionMessageMap.get(type);// 获得当前类型数据表
		for (MagicPosMessage tmp : magicpos) {
			if (tmp.getMagicPoslevel() == level) {
				sumExp += tmp.getMagicPosExp();
			}
		}
		return sumExp;
	}

	/**
	 * 
	 * @param type
	 *            类型
	 * @param level
	 *            当前等级
	 * @return 当前等级之前的总经验值
	 */

	public static int getBeforeLevelSumExp(int type, int level) {
		int sumExp = 0;
		if (level > 5) {
			return 0;
		}
		int startlevel = 1;
		List<MagicPosMessage> magicpos = new ArrayList<MagicPosMessage>();
		magicpos = magicPositionMessageMap.get(type);// 获得当前类型数据表
		int floor = level * 10;
		// for(int i = startlevel;i<=level;i++){
		// for(MagicPosMessage tmp : magicpos){
		// if(tmp.getMagicPoslevel() == i){
		// sumExp += tmp.getMagicPosExp();
		// }
		// }
		// }
		for (int i = 0; i < floor; i++) {
			sumExp += magicpos.get(i).getMagicPosExp();
		}

		return sumExp;
	}

	/**
	 * @param type
	 *            类型
	 * @param level
	 *            当前等级
	 * @param floor
	 *            阶层
	 * @return 对应的属性值
	 */
	public static int[] getMagicPosAttr(int type, int level, int floor) {
		int[] attrpoint = new int[3];
		if (level > 5 && floor > 10) {
			return attrpoint;
		}
		List<MagicPosMessage> magicpos = new ArrayList<MagicPosMessage>();
		magicpos = magicPositionMessageMap.get(type);// 通过阵眼类型得到该阵眼数值表
		for (MagicPosMessage tmp : magicpos) {
			if (tmp.getMagicPoslevel() == level
					&& tmp.getMagicPosFloor() == floor) {// 根据玩家阵眼等级和阶层查找对应的属性值
				if (type == waterMagic || type == soilMagic
						|| type == windMagic || type == mindMagic) {// 除去火元素均为两种属性(火元素为三种)
					if (type == waterMagic) {// 水元素
						attrpoint[0] = tmp.getMagicPosAttack();
						attrpoint[1] = tmp.getMagicPosMattack();
					} else if (type == soilMagic) {// 土
						attrpoint[0] = tmp.getMagicPosPdef();
						attrpoint[1] = tmp.getMagicPosMdef();
					} else if (type == windMagic) {// 风
						attrpoint[0] = tmp.getMagicPosFlee();
						attrpoint[1] = tmp.getMagicPosNocri();
					} else if (type == mindMagic) {// 精神
						attrpoint[0] = tmp.getMagicPosHp();
						attrpoint[1] = tmp.getMagicPosMp();
					}
				} else if (type == fireMagic) {
					attrpoint[0] = tmp.getMagicPosHit();
					attrpoint[1] = tmp.getMagicPosPcri();
					attrpoint[2] = tmp.getMagicPosMcri();
				}
				break;
			}
		}
		return attrpoint;
	}

	/**
	 * @param type
	 *            类型
	 * @param level
	 *            当前等级
	 * @param floor
	 *            阶层
	 * @return 对应的属性值
	 */
	public static String getLevStr(int level) {
		String levStr;
		switch (level){ 
		case firLev:
			levStr = "普通";
			break;
		case secLev:
			levStr = "优秀";
			break;
		case thiLev:
			levStr = "精良";
			break;
		case fourLev:
			levStr = "史诗";
			break;
		case fifLev:
			levStr = "传说";
			break;
		default:
			levStr = "传说";
		}
		return levStr;
	}

}
