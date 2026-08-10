package peony.game;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import peony.game.buff.Buff;
import peony.game.itemenhance.ItemEnhance;
import peony.game.suite.SuiteEffects;

import com.pip.sanguo.data.equipment.AttributeCalculator;
import com.pip.sanguo.data.equipment.Equipment;

public class EquipmentTemplate {

	public static final int TYPE_WEAPON = 0; // 武器
	public static final int TYPE_ARMOR = 1; // 防具
	public static final int TYPE_TRINKET = 2; // 饰品

	public static final int MINORTYPE_SPEAR = 4; // 枪
	public static final int MINORTYPE_AXE = 3; // 斧
	public static final int MINORTYPE_KNIFE = 2; // 刀
	public static final int MINORTYPE_SWORD = 1; // 剑
	public static final int MINORTYPE_BOW = 7; // 弓
	public static final int MINORTYPE_FAN = 6; // 扇
	public static final int MINORTYPE_POLEARM = 5; // 长柄刀
	public static final int MINORTYPE_HEAD = 8; // 头盔
	public static final int MINORTYPE_CHEST = 9; // 衣服
	public static final int MINORTYPE_LEG = 10; // 裤子
	public static final int MINORTYPE_FEET = 11; // 鞋子
	public static final int MINORTYPE_SHIELD = 12;// 副手
	public static final int MINORTYPE_WRIST = 13;// 护腕
	public static final int MINORTYPE_HUFU = 14;// 护符
	public static final int MINORTYPE_YUPEI = 15;// 玉佩
	public static final int MINORTYPE_PIFENG = 16;// 披风
	public static final int MINORTYPE_HORSE_HEAD = 21; // 马,面具
	public static final int MINORTYPE_HORSE_NECK = 22;// 马,颈甲
	public static final int MINORTYPE_HORSE_CHEST = 23;// 马,胸甲
	public static final int MINORTYPE_HORSE_ASS = 24;// 马,臀甲
	public static final int MINORTYPE_HORSE_BACK = 25;// 马,鞍
	public static final int MINORTYPE_HORSE_LEG = 26;// 马,蹄
	public static final int MINORTYPE_HORSE_PEDAL = 27;// 马,脚蹬

	public Equipment equ; // 原始数据

	public int useLevel; // 可装备等级
	public int type; // 类型
	public int minorType; // 小类
	public int clazz; // 职业限制 -1 所有职业
	public int strengthLimit, agilityLimit, intelligentLimit, staminaLimit; // 力量限制，敏捷限制，智力限制，耐力限制

	public int duration; // 耐久 如果耐久是0，则认为是永久不损耗的

	public boolean showRandom; // 如果为true，则隐藏实际属性，显示为“随机属性”
	public int initHole, maxHole;

	public boolean canJudgeStar; // 允许鉴定星级
	public boolean canJudgePotential; // 允许鉴定资质
	public int markCharCount; //允许刻字的数量
	public boolean canCopy; // 允许被复制

	public int level; // 物品等级
	public float value; // 装备价值

	public Buff specialEffect; // 装备特效

	public SuiteEffects suiteEffects; // 套装
	protected byte mask1, mask2, mask3;

	public static int[] weapons = {
		AttributeCalculator.ATTRIBUTE_ATTACKPOWER,
		AttributeCalculator.ATTRIBUTE_MAGICPOWER,
		AttributeCalculator.ATTRIBUTE_STR,
		AttributeCalculator.ATTRIBUTE_STA,
		AttributeCalculator.ATTRIBUTE_AGI,
		AttributeCalculator.ATTRIBUTE_INT,
		AttributeCalculator.ATTRIBUTE_HIT,
		AttributeCalculator.ATTRIBUTE_HP,
		AttributeCalculator.ATTRIBUTE_MP,
	};
	
	public static int[] armors = {
			AttributeCalculator.ATTRIBUTE_ARMOR,
			AttributeCalculator.ATTRIBUTE_MAGICARMOR,
			AttributeCalculator.ATTRIBUTE_STR,
			AttributeCalculator.ATTRIBUTE_STA,
			AttributeCalculator.ATTRIBUTE_AGI,
			AttributeCalculator.ATTRIBUTE_INT,
			AttributeCalculator.ATTRIBUTE_DODGE,
			AttributeCalculator.ATTRIBUTE_MAGICDODGE,
			AttributeCalculator.ATTRIBUTE_ANTICRIT,
			AttributeCalculator.ATTRIBUTE_HP,
			AttributeCalculator.ATTRIBUTE_MP,
	};
	
	public static int[] trinkets = {
			AttributeCalculator.ATTRIBUTE_CRIT,
			AttributeCalculator.ATTRIBUTE_HIT,
			AttributeCalculator.ATTRIBUTE_DODGE,
			AttributeCalculator.ATTRIBUTE_MAGICDODGE,
			AttributeCalculator.ATTRIBUTE_ANTICRIT,
			AttributeCalculator.ATTRIBUTE_MAGICARMOR,
	};
	public int[] getNaturalEnhanceAtts() {
		// 武器：物攻，法攻，力量，体力，敏捷，智力，命中（物理，法术），生命值，气力值（精力值）
		// 防具：护甲，法防，力量，体力，敏捷，智力，闪避（物理，法术），免暴，生命值，气力值（精力值）
		// 饰品：暴击率，命中率（物理，法术），闪避（物理，法术），免暴,法防
		if (!canJudgePotential)
			throw new IllegalStateException();
		if(type==EquipmentTemplate.TYPE_WEAPON){
			//武器
			return weapons;
		}else if(type==EquipmentTemplate.TYPE_ARMOR){
			//防具
			return armors;
		}else if(type==EquipmentTemplate.TYPE_TRINKET){
			//饰品
			return trinkets;
		}else if(isHorseEquipment()){
			return armors;
		}
		return null;
	}
	
	public int[] getNaturalEnhanceAtts0() {
		Set<Integer> l = new HashSet<Integer>();
		if (getSTR(null) > 0) {
			l.add(AttributeCalculator.ATTRIBUTE_STR);
		}
		if (getAGI(null) > 0) {
			l.add(AttributeCalculator.ATTRIBUTE_AGI);
		}
		if (getSTA(null) > 0) {
			l.add(AttributeCalculator.ATTRIBUTE_STA);
		}
		if (getINT(null) > 0) {
			l.add(AttributeCalculator.ATTRIBUTE_INT);
		}
		if (getMaxHP(null) > 0) {
			l.add(AttributeCalculator.ATTRIBUTE_HP);
		}
		if (getMaxMP(null) > 0) {
			l.add(AttributeCalculator.ATTRIBUTE_MP);
		}
		if (getCrit(null) > 0) {
			l.add(AttributeCalculator.ATTRIBUTE_CRIT);
		}
		if (getHit(null) > 0) {
			l.add(AttributeCalculator.ATTRIBUTE_HIT);
		}
		if (getDodge(null) > 0) {
			l.add(AttributeCalculator.ATTRIBUTE_DODGE);
		}
		if (getMagicDodge(null) > 0) {
			l.add(AttributeCalculator.ATTRIBUTE_MAGICDODGE);
		}
		if (getAttackPower(null) > 0) {
			l.add(AttributeCalculator.ATTRIBUTE_ATTACKPOWER);
		}
		if (getMagicPower(null) > 0) {
			l.add(AttributeCalculator.ATTRIBUTE_MAGICPOWER);
		}
		if (getArmor(null) > 0) {
			l.add(AttributeCalculator.ATTRIBUTE_ARMOR);
		}
		if (getMagicArmor(null) > 0) {
			l.add(AttributeCalculator.ATTRIBUTE_MAGICARMOR);
		}
		if (getHpRenew(null) > 0) {
			l.add(AttributeCalculator.ATTRIBUTE_HPRENEW);
		}
		if (getMpRenew(null) > 0) {
			l.add(AttributeCalculator.ATTRIBUTE_MPRENEW);
		}
		if (getSpeed(null) > 0) {
			l.add(AttributeCalculator.ATTRIBUTE_SPEED);
		}
		if (getAnticrit(null) > 0) {
			l.add(AttributeCalculator.ATTRIBUTE_ANTICRIT);
		}
		if(type==EquipmentTemplate.TYPE_WEAPON){
			//武器
			for(int i : weapons){
				l.add(i);
			}
		}else if(type==EquipmentTemplate.TYPE_ARMOR){
			//防具
			for(int i : armors){
				l.add(i);
			}
		}else if(type==EquipmentTemplate.TYPE_TRINKET){
			//饰品
			for(int i : trinkets){
				l.add(i);
			}
		}else if(isHorseEquipment()){
			for(int i : armors){
				l.add(i);
			}
		}
		int[] ret = new int[l.size()];
		Iterator<Integer> it = l.iterator();
		int i = 0;
		while(it.hasNext()){
			ret[i] = it.next();
			i++;
		}
		return ret;
	}

	public boolean hasEffect() {
		return specialEffect != null || suiteEffects != null;
	}

	public boolean isHorseEquipment() {
		return minorType >= MINORTYPE_HORSE_HEAD
				&& minorType <= MINORTYPE_HORSE_PEDAL;
	}

	public void mask() {
		if (getMaxHP(null) > 0)
			mask1 |= 1;
		if (getMaxMP(null) > 0)
			mask1 |= 1 << 1;
		if (getSTR(null) > 0)
			mask1 |= 1 << 2;
		if (getAGI(null) > 0)
			mask1 |= 1 << 3;
		if (getSTA(null) > 0)
			mask1 |= 1 << 4;
		if (getINT(null) > 0)
			mask1 |= 1 << 5;
		if (getAttackPower(null) > 0)
			mask1 |= 1 << 6;
		if (getMagicPower(null) > 0)
			mask1 |= 1 << 7;

		if (getMagicArmor(null) > 0)
			mask2 |= 1 << 1;
		if (getHit(null) > 0)
			mask2 |= 1 << 2;
		if (getDodge(null) > 0)
			mask2 |= 1 << 3;
		if (getCrit(null) > 0)
			mask2 |= 1 << 4;
		if (getMagicDodge(null) > 0)
			mask2 |= 1 << 5;
		if (getHpRenew(null) > 0)
			mask2 |= 1 << 6;
		if (getMpRenew(null) > 0)
			mask2 |= 1 << 7;

		if (getArmor(null) > 0)
			mask3 |= 1;
		if (getMaxAttack(null) > 0) {
			mask3 |= 1 << 1;
			mask3 |= 1 << 2;
		}
		if (duration > 0)
			mask3 |= 1 << 3;
		if (getAnticrit(null) > 0)
			mask3 |= 1 << 4;
		if (showRandom)
			mask3 |= 1 << 5;
		if (getSpeed(null) > 0)
			mask3 |= 1 << 6;
	}

	public byte[] toClientBytes(ItemEnhance ie) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(useLevel);
			dos.write(clazz);
			dos.write(minorType);
			dos.writeShort(strengthLimit);
			dos.writeShort(agilityLimit);
			dos.writeByte(initHole);
			dos.writeByte(maxHole);
			dos.write(markCharCount);
			dos.write(mask1);
			dos.write(mask2);
			dos.write(mask3);
			if (getMaxHP(ie) > 0) {
				dos.writeShort(getMaxHP(ie));
			}
			if (getMaxMP(ie) > 0) {
				dos.writeShort(getMaxMP(ie));
			}
			if (getSTR(ie) > 0) {
				dos.writeShort(getSTR(ie));
			}
			if (getAGI(ie) > 0) {
				dos.writeShort(getAGI(ie));
			}
			if (getSTA(ie) > 0) {
				dos.writeShort(getSTA(ie));
			}
			if (getINT(ie) > 0) {
				dos.writeShort(getINT(ie));
			}
			if (getAttackPower(ie) > 0) {
				dos.writeShort(getAttackPower(ie));
			}
			if (getMagicPower(ie) > 0) {
				dos.writeShort(getMagicPower(ie));
			}
			if (getMagicArmor(ie) > 0) {
				dos.writeShort(getMagicArmor(ie));
			}
			if (getHit(ie) > 0) {
				dos.writeShort(getHit(ie));
			}
			if (getDodge(ie) > 0) {
				dos.writeShort(getDodge(ie));
			}
			if (getCrit(ie) > 0) {
				dos.writeShort(getCrit(ie));
			}
			if (getMagicDodge(ie) > 0) {
				dos.writeShort(getMagicDodge(ie));
			}
			if (getHpRenew(ie) > 0) {
				dos.writeShort(getHpRenew(ie));
			}
			if (getMpRenew(ie) > 0) {
				dos.writeShort(getMpRenew(ie));
			}
			if (getArmor(ie) > 0) {
				dos.writeShort(getArmor(ie));
			}
			if (getMaxAttack(ie) > 0) {
				dos.writeShort(getMinAttack(ie));
				dos.writeShort(getMaxAttack(ie));
			}
			if (duration > 0) {
				dos.writeShort(duration);
			}
			if (getAnticrit(ie) > 0) {
				dos.writeShort(getAnticrit(ie));
			}
			if (getSpeed(ie) > 0) {
				dos.writeShort(getSpeed(ie));
			}
		} catch (IOException e) {
		}
		return baos.toByteArray();
	}

	public int getMaxHP(ItemEnhance ie) {
		if (ie != null) {
			float f = equ.getAttributeImpl(AttributeCalculator.ATTRIBUTE_HP);
			int v = Math.round(f);
			if (v != 0) {
				if(ie.getPrimaryEnhance() != 0){
					if(ie.getStar()==0)
						return v;
					else {
						float sv = f * (level + ie.getStar()*7)/level;
						float fv = f + (sv - f) *(1f+ie.getStarEnhance()/1000f);
						return Math.round(fv);
					}
				} else {
					if(ie.getStar() == 0){
						return v;
					} else {
						return Math.round(f * (level + ie.getStar()*7)/level);
					}
				}
			} else {
				return 0;
			}
		} else {
			return equ.getAttribute(AttributeCalculator.ATTRIBUTE_HP);
		}
	}

	public int getMaxMP(ItemEnhance ie) {
		if (ie != null) {
			float f = equ.getAttributeImpl(AttributeCalculator.ATTRIBUTE_MP);
			int v = Math.round(f);
			if (v != 0) {
				if(ie.getPrimaryEnhance() != 0){
					if(ie.getStar()==0)
						return v;
					else {
						float sv = f * (level + ie.getStar()*7)/level;
						float fv = f + (sv - f) *(1f+ie.getStarEnhance()/1000f);
						return Math.round(fv);
					}
				} else {
					if(ie.getStar()==0)
						return v;
					else 
						return  Math.round(f * (level + ie.getStar()*7)/level);	
				}
			} else {
				return 0;
			}
		} else {
			return equ.getAttribute(AttributeCalculator.ATTRIBUTE_MP);
		}
	}

	public int getSTR(ItemEnhance ie) {
		if (ie != null) {
			float f = equ.getAttributeImpl(AttributeCalculator.ATTRIBUTE_STR);
			int v = Math.round(f);
			if (v != 0) {
				if(ie.getPrimaryEnhance() != 0){
					if(ie.getStar()==0)
						return v;
					else {
						float sv = f * (level + ie.getStar()*7)/level;
						float fv = f + (sv - f) *(1f+ie.getStarEnhance()/1000f);
						return Math.round(fv);
					}
				} else {
					if(ie.getStar()==0)
						return v;
					else 
						return Math.round(f * (level + ie.getStar()*7)/level);
				}
			} else {
				return 0;
			}
		} else {
			return equ.getAttribute(AttributeCalculator.ATTRIBUTE_STR);
		}
	}

	public int getAGI(ItemEnhance ie) {
		if (ie != null) {
			float f = equ.getAttributeImpl(AttributeCalculator.ATTRIBUTE_AGI);
			int v = Math.round(f);
			if (v != 0) {
				if(ie.getPrimaryEnhance() != 0){
					if(ie.getStar()==0)
						return v;
					else {
						float sv = f * (level + ie.getStar()*7)/level;
						float fv = f + (sv - f) *(1f+ie.getStarEnhance()/1000f);
						return Math.round(fv);
					}
				} else {
					if(ie.getStar()==0)
						return v;
					else 
						return Math.round(f * (level + ie.getStar()*7)/level);
				}
			} else {
				return 0;
			}
		} else {
			return equ.getAttribute(AttributeCalculator.ATTRIBUTE_AGI);
		}
	}

	public int getSTA(ItemEnhance ie) {
		if (ie != null) {
			float f = equ.getAttributeImpl(AttributeCalculator.ATTRIBUTE_STA);
			int v = Math.round(f);
			if (v != 0) {
				if(ie.getPrimaryEnhance() != 0){
					if(ie.getStar()==0)
						return v;
					else {
						float sv = f * (level + ie.getStar()*7)/level;
						float fv = f + (sv - f) *(1f+ie.getStarEnhance()/1000f);
						return Math.round(fv);
					}
				} else {
					if(ie.getStar()==0)
						return v;
					else 
						return Math.round(f * (level + ie.getStar()*7)/level);
				}
			} else {
				return 0;
			}
		} else {
			return equ.getAttribute(AttributeCalculator.ATTRIBUTE_STA);
		}
	}

	public int getINT(ItemEnhance ie) {
		if (ie != null) {
			float f = equ.getAttributeImpl(AttributeCalculator.ATTRIBUTE_INT);
			int v = Math.round(f);
			if (v != 0) {
				if(ie.getPrimaryEnhance() != 0){
					if(ie.getStar()==0)
						return v;
					else {
						float sv = f * (level + ie.getStar()*7)/level;
						float fv = f + (sv - f) *(1f+ie.getStarEnhance()/1000f);
						return Math.round(fv);
					}
				} else {
					if(ie.getStar()==0)
						return v;
					else 
						return Math.round(f * (level + ie.getStar()*7)/level);
				}
			} else {
				return 0;
			}
		} else {
			return equ.getAttribute(AttributeCalculator.ATTRIBUTE_INT);
		}
	}

	public int getAttackPower(ItemEnhance ie) {
		if (ie != null) {
			float f = equ
					.getAttributeImpl(AttributeCalculator.ATTRIBUTE_ATTACKPOWER);
			int v = Math.round(f);
			if (v != 0) {
				if(ie.getPrimaryEnhance() != 0){
					if(ie.getStar()==0){
						float fv = v*(1f+ie.getPrimaryEnhance()/1000f);
						v = Math.round(fv);
						return v;
					} else {
						float fs = f * (level + ie.getStar()*7)/level;
						float fv =f *(1f+ie.getPrimaryEnhance()/1000f)+(fs-f)*(1f+ie.getStarEnhance()/1000f);
						return Math.round(fv);
					}
				} else {
					if(ie.getStar() == 0){
						return v;
					} else {
						return Math.round(f * (level + ie.getStar()*7)/level);
					}
				}
			} else {
				return 0;
			}
		} else {
			return equ.getAttribute(AttributeCalculator.ATTRIBUTE_ATTACKPOWER);
		}
	}

	public int getMagicPower(ItemEnhance ie) {
		if (ie != null) {
			float f = equ
					.getAttributeImpl(AttributeCalculator.ATTRIBUTE_MAGICPOWER);
			int v = Math.round(f);
			if (v != 0) {
				if(ie.getPrimaryEnhance() != 0){
					if(ie.getStar()==0){
						float fv = v*(1f+ie.getPrimaryEnhance()/1000f);
						v = Math.round(fv);
						return v;
					} else {
						float fs = f * (level + ie.getStar()*7)/level;
						float fv = f*(1f+ie.getPrimaryEnhance()/1000f)+(fs - f)*(1f+ ie.getStarEnhance()/1000f);
						return Math.round(fv);
					}
				} else {
					if(ie.getStar() == 0){
						return v;
					} else {
						return Math.round(f * (level + ie.getStar()*7)/level);
					}
				}
			} else {
				return 0;
			}
		} else {
			return equ.getAttribute(AttributeCalculator.ATTRIBUTE_MAGICPOWER);
		}
	}

	public int getMagicArmor(ItemEnhance ie) {
		if (ie != null) {
			float f = equ
					.getAttributeImpl(AttributeCalculator.ATTRIBUTE_MAGICARMOR);
			int v = Math.round(f);
			if (v != 0) {
				if(ie.getPrimaryEnhance() != 0){
					if(ie.getStar()==0){
						float fv = v*(1f+ie.getPrimaryEnhance()/1000f);
						v = Math.round(fv);
						return v;
					}
					else {
						float fs = f * (level + ie.getStar()*7 + 9)/(level+9);
						float fv = f*(1f+ie.getPrimaryEnhance()/1000f)+(fs-f)*(1f+ie.getStarEnhance()/1000f);
						return Math.round(fv);
					}
				} else {
					if(ie.getStar() == 0)
						return v;
					else 
						return Math.round(f * (level + ie.getStar()*7 + 9)/(level+9));
					
				}
			} else {
				return 0;
			}
		} else {
			return equ.getAttribute(AttributeCalculator.ATTRIBUTE_MAGICARMOR);
		}
	}

	public int getHit(ItemEnhance ie) {
		if (ie != null) {
			float f = equ.getAttributeImpl(AttributeCalculator.ATTRIBUTE_HIT);
			int v = Math.round(f);
			if (v != 0) {
				if(ie.getPrimaryEnhance() != 0){
					if(ie.getStar()==0)
						return v;
					else {
						float sv = f * (level + ie.getStar()*7)/level;
						float fv = f + (sv - f) * (1f+ie.getStarEnhance()/1000f);
						return Math.round(fv);
					}
				} else {
					if(ie.getStar()==0)
						return v;
					else 
						return Math.round(f * (level + ie.getStar()*7)/level);
				}
			} else {
				return 0;
			}
		} else {
			return equ.getAttribute(AttributeCalculator.ATTRIBUTE_HIT);
		}
	}

	public int getDodge(ItemEnhance ie) {
		if (ie != null) {
			float f = equ.getAttributeImpl(AttributeCalculator.ATTRIBUTE_DODGE);
			int v = Math.round(f);
			if (v != 0) {
				if(ie.getPrimaryEnhance() != 0){
					if(ie.getStar()==0)
						return v;
					else {
						float sv = f * (level + ie.getStar()*7)/level;
						float fv = f + (sv - f) *(1f+ie.getStarEnhance()/1000f);
						return Math.round(fv);
					}
				} else {
					if(ie.getStar()==0)
						return v;
					else 
						return Math.round(f * (level + ie.getStar()*7)/level);
				}
			} else {
				return 0;
			}
		} else {
			return equ.getAttribute(AttributeCalculator.ATTRIBUTE_DODGE);
		}
	}

	public int getCrit(ItemEnhance ie) {
		if (ie != null) {
			float f = equ.getAttributeImpl(AttributeCalculator.ATTRIBUTE_CRIT);
			int v = Math.round(f);
			if (v != 0) {
				if(ie.getPrimaryEnhance() != 0){
					if(ie.getStar()==0)
						return v;
					else {
						float sv = f * (level + ie.getStar()*7)/level;
						float fv = f + (sv - f) * (1f+ie.getStarEnhance()/1000f);
						return Math.round(fv);
					}
				} else {
					if(ie.getStar()==0)
						return v;
					else 
						return Math.round(f * (level + ie.getStar()*7)/level);
				}
			} else {
				return 0;
			}
		} else {
			return equ.getAttribute(AttributeCalculator.ATTRIBUTE_CRIT);
		}
	}

	public int getMagicDodge(ItemEnhance ie) {
		if (ie != null) {
			float f = equ
					.getAttributeImpl(AttributeCalculator.ATTRIBUTE_MAGICDODGE);
			int v = Math.round(f);
			if (v != 0) {
				if(ie.getPrimaryEnhance() != 0){
					if(ie.getStar()==0)
						return v;
					else {
						float sv = f * (level + ie.getStar()*7)/level;
						float fv = f + (sv - f) * (1f+ie.getStarEnhance()/1000f);
						return Math.round(fv);
					}
				} else {
					if(ie.getStar()==0)
						return v;
					else 
						return Math.round(f * (level + ie.getStar()*7)/level);
				}
			} else {
				return 0;
			}
		} else {
			return equ.getAttribute(AttributeCalculator.ATTRIBUTE_MAGICDODGE);
		}
	}

	public int getHpRenew(ItemEnhance ie) {
		if (ie != null) {
			float f = equ
					.getAttributeImpl(AttributeCalculator.ATTRIBUTE_HPRENEW);
			int v = Math.round(f);
			if (v != 0) {
				if(ie.getPrimaryEnhance() != 0){
					if(ie.getStar()==0)
						return v;
					else {
						float sv = f * (level + ie.getStar()*7)/level;
						float fv = f + (sv - f) * (1f+ie.getStarEnhance()/1000f);
						return Math.round(fv);
					}
				} else {
					if(ie.getStar()==0)
						return v;
					else 
						return Math.round(f * (level + ie.getStar()*7)/level);	
				}
			} else {
				return 0;
			}
		} else {
			return equ.getAttribute(AttributeCalculator.ATTRIBUTE_HPRENEW);
		}
	}

	public int getMpRenew(ItemEnhance ie) {
		if (ie != null) {
			float f = equ
					.getAttributeImpl(AttributeCalculator.ATTRIBUTE_MPRENEW);
			int v = Math.round(f);
			if (v != 0) {
				if(ie.getPrimaryEnhance() != 0){
					if(ie.getStar()==0)
						return v;
					else {
						float sv = f * (level + ie.getStar()*7)/level;
						float fv = f + (sv - f) * (1f+ie.getStarEnhance()/1000f);
						return Math.round(fv);
					}
				} else {
					if(ie.getStar()==0)
						return v;
					else 
						return Math.round(f * (level + ie.getStar()*7)/level);
				}
			} else {
				return 0;
			}
		} else {
			return equ.getAttribute(AttributeCalculator.ATTRIBUTE_MPRENEW);
		}
	}

	public int getArmor(ItemEnhance ie) {
		if (ie != null) {
			float f = equ.getAttributeImpl(AttributeCalculator.ATTRIBUTE_ARMOR);
			int v = Math.round(f);
			if (v != 0) {
				if(ie.getPrimaryEnhance() != 0){
					if(ie.getStar()==0){
						float fv = v*(1f+ie.getPrimaryEnhance()/1000f);
						v = Math.round(fv);
						return v;
					}
					else {
						float sv = f * (level + ie.getStar()*7 + 9)/(level+9);
						float fv = v*(1+ie.getPrimaryEnhance()/1000f)+(sv-v)*(1f+ie.getStarEnhance()/1000f);
						return Math.round(fv);
					}
				} else {
					if(ie.getStar() == 0)
						return v;
					else 
						return Math.round(f * (level + ie.getStar()*7 + 9)/(level+9));
				}
			} else {
				return 0;
			}
		} else {
			return equ.getAttribute(AttributeCalculator.ATTRIBUTE_ARMOR);
		}
	}

	public int getMaxAttack(ItemEnhance ie) {
		if (ie != null) {
			float f = equ
					.getAttributeImpl(AttributeCalculator.ATTRIBUTE_MAXATTACK);
			int v = Math.round(f);
			if (v != 0) {
				if(ie.getPrimaryEnhance() != 0){
					if(ie.getStar()==0){
						float fv = v*(1f+ie.getPrimaryEnhance()/1000f);
						v = Math.round(fv);
						return v;
					}
					else {
						float sv = f * (level + ie.getStar()*7)/level;
						float fv = f*(1f+ie.getPrimaryEnhance()/1000f)+(sv-f)*(1f+ie.getStarEnhance()/1000f);
						return Math.round(fv);
					}
				} else {
					if(ie.getStar() == 0)
						return v;
					else 
						return Math.round(f * (level + ie.getStar()*7)/level);
				}
			} else {
				return 0;
			}
		} else {
			return equ.getAttribute(AttributeCalculator.ATTRIBUTE_MAXATTACK);
		}
	}

	public int getMinAttack(ItemEnhance ie) {
		if (ie != null) {
			float f = equ
					.getAttributeImpl(AttributeCalculator.ATTRIBUTE_MINATTACK);
			int v = Math.round(f);
			if (v != 0) {
				if(ie.getPrimaryEnhance() != 0){
					if(ie.getStar()==0){
						float fv = v*(1f+ie.getPrimaryEnhance()/1000f);
						v = Math.round(fv);
						return v;
					}
					else {
						float sv = f * (level + ie.getStar()*7)/level;
						float fv = f*(1f+ie.getPrimaryEnhance()/1000f)+(sv-f)*(1f+ie.getStarEnhance()/1000f);
						return Math.round(fv);
					}
				} else {
					if(ie.getStar() == 0)
						return v;
					else 
						return Math.round(f * (level + ie.getStar()*7)/level);
				}
			} else {
				return 0;
			}
		} else {
			return equ.getAttribute(AttributeCalculator.ATTRIBUTE_MINATTACK);
		}
	}

	public int getAnticrit(ItemEnhance ie) {
		if (ie != null) {
			float f = equ
					.getAttributeImpl(AttributeCalculator.ATTRIBUTE_ANTICRIT);
			int v = Math.round(f);
			if (v != 0) {
				if(ie.getPrimaryEnhance() != 0){
					if(ie.getStar()==0)
						return v;
					else {
						float sv = f * (level + ie.getStar()*7)/level;
						float fv = f + (sv - f) * (1f+ie.getStarEnhance()/1000f);
						return Math.round(fv);
					} 
				} else {
					if(ie.getStar()==0)
						return v;
					else 
						return Math.round(f * (level + ie.getStar()*7)/level);
				}
			} else {
				return 0;
			}
		} else {
			return equ.getAttribute(AttributeCalculator.ATTRIBUTE_ANTICRIT);
		}
	}

	public int getSpeed(ItemEnhance ie) {
		if (ie != null) {
			float f = equ.getAttributeImpl(AttributeCalculator.ATTRIBUTE_SPEED);
			int v = Math.round(f);
			if (v != 0) {
				if(ie.getPrimaryEnhance() != 0){
					if(ie.getStar()==0)
						return v;
					else {
						float sv = f * (level + ie.getStar()*7)/level;
						float fv = f + (sv - f) * (1f+ie.getStarEnhance()/1000f);
						return Math.round(fv);
					}
				} else {
					if(ie.getStar() == 0)
						return v;
					else 
						return Math.round(f * (level + ie.getStar()*7)/level);
				}
			} else {
				return 0;
			}
		} else {
			return equ.getAttribute(AttributeCalculator.ATTRIBUTE_SPEED);
		}
	}
}
