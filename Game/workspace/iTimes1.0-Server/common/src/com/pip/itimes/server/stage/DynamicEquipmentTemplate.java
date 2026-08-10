package com.pip.itimes.server.stage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.util.IDGenerator;



/**
 * @author Jeffrey
 * @version 1.0
 */
public class DynamicEquipmentTemplate extends EquipmentTemplate{

    public static final int[] WEAPON_RADIO = {80,100,90,60};

    private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    public DynamicEquipmentTemplate() {
        setCreateType(IEquipment.CREATE_DYNAMIC);
    }

    public IItem newInstance(){
        int id = IDGenerator.getEquipmentId();
        int seed = RandomSeed.getSeed();
        return newInstance(id,seed);
    }

	public IEquipment newInstance(int id, int seed) {
		DynamicEquipment ret = new DynamicEquipment(this, id, seed);
		Random rnd = new Random(seed);
		byte part = getPart();
		if (part == IEquipment.PART_WEAPON) {
			IntRange minRange = getMinAttack();
			IntRange maxRange = getMaxAttack();
			int min = Utils.getCount(rnd, minRange.getMin(), minRange.getMax());
			int max = Utils.getCount(rnd, maxRange.getMin(), maxRange.getMax());
			ret.addProperty(IEquipment.EQUIP_ADD_ATTACK_MIN, (short) min);
			ret.addProperty(IEquipment.EQUIP_ADD_ATTACK_MAX, (short) max);
		} else if (part == IEquipment.PART_CHEST
				|| part == IEquipment.PART_SHIELD
				|| part == IEquipment.PART_HEAD || part == IEquipment.PART_FEET
				|| part == IEquipment.PART_WAIST) {
			IntRange defRange = getDefence();
			int def = Utils.getCount(rnd, defRange.getMin(), defRange.getMax());
			ret.addProperty(IEquipment.EQUIP_ADD_DEFENCE, (short) def);
		}
		int point = getAddedPoint();
		if (point > 0) {
			int start = rnd.nextInt(4);
			int count = 0;
			while (point > 0) {
				if (count < 3) {
					int pro = (start + count) % 4;
					int value = rnd.nextInt(point + 1);
					ret.addProperty(pro + 1, (short) value);
					point -= value;
					count++;
				} else {
					int pro = (start + count) % 4;
					ret.addProperty(pro + 1, (short) point);
					break;
				}
			}
		}
		ret.setCurrentDurability(getDurability());
		if (getBindType() == IEquipment.BIND_GET) {
			ret.setBinded(true);
		}
		// mengjie add 过期日期
		if (getProperty(IEquipment.EQUIP_FAILURE_DATE) == 0) {
			if (getProperty(IEquipment.EQUIP_FAILURE_TIME) > 0) {
				// 到期日
				long long_date = (new Date()).getTime() + 24 * 3600 * 1000
						* getProperty(IEquipment.EQUIP_FAILURE_TIME);
				ret.setFAILURE_TIME(long_date);
			} else {
				ret.setFAILURE_TIME(-1);
			}
		} else {
			String str_date = Integer.valueOf(
					getProperty(IEquipment.EQUIP_FAILURE_DATE)).toString();
			Date date_tmp = new Date();
			try {
				date_tmp = format.parse(str_date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// 优先级，到期日期
			ret.setFAILURE_TIME(date_tmp.getTime());
		}
		// 新生成的话则需要设置字符串
		// if(ret.canLettering()){//没有可过可以刻则从服务器配置表读入
		String letteringTemplateString = Enhance.getLettering(ret.getItemId());
		if (letteringTemplateString != null) {
			int extendFlag = ret.getExtendFlag();
			try {
				extendFlag = Utils.SetIntN(extendFlag,
						DynamicEquipment.CAN_LETTERING);
				ret.setExtendFlag(extendFlag);
				ret.setLetteringString(letteringTemplateString);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		ret.setItemShowType(this.getItemSplitType());
		ret.setDiamond(this.getDiamond()); // 同步鉴定

		ret.setDiamondcount(this.getDiamondcount());
		ret.setOpenDiamondCount(this.getOpenDiamondCount());

		if (this.getDiamondcount() > ret.MaxDiamondRoleCount) {
			ret.resetDiamondMosiacRoleInfo(this.getDiamondcount());
		}

		byte[] diamondRoleInfo = ret.getDiamondMosiacRoleInfo();
		for (int i = 0; i < this.getOpenDiamondCount(); i++) {
			if (diamondRoleInfo[i] == IEquipment.CURRENT_EQU_DIAMOND_NOTROLE) {
				diamondRoleInfo[i] = IEquipment.CURRENT_EQU_CANDIAMOND;
			}
		}
		//打开第6孔
		if(getOpen6hole() > 0 && diamondRoleInfo.length > 5){
			if (diamondRoleInfo[5] == IEquipment.CURRENT_EQU_DIAMOND_NOTROLE) {
				diamondRoleInfo[5] = IEquipment.CURRENT_EQU_CANDIAMOND;
			}
		}
		ret.setDiamondMosiacRoleInfo(diamondRoleInfo);
		
		//2013年3月29日 
		if (this.getDiamondMosiacRoleInfo() != null)
			ret.setDiamondMosiacRoleInfo(this.getDiamondMosiacRoleInfo());
		DiamondMosaic[] diamondMosaic = this.getdiamondMosaic();
		if (diamondMosaic != null){
			for(byte j=0; j<5; j++){
				if (diamondMosaic[j] != null)
					ret.diamondMosaic(j,diamondMosaic[j]);
			}
		}
		//2013年4月1日 增加精炼信息
		if (this.getEnhance() != null){
			for(byte j=0; j<this.getEnhance().size(); j++){
				if (this.getEnhance().get(j) != null)
					ret.enhance(this.getEnhance().get(j));
			}			
		}
		
		//增加属性攻
		Viany viany = ret.getViany();
		viany.setStone(this.getVianyStoneValue());
		viany.setScissors(this.getVianyScissorsValue());
		viany.setPaper(this.getVianyPaperValue());
		
		return ret;
	}

    public IntRange getMinAttack(){
        int wType = getProperty(IEquipment.EQUIP_ADD_WEAPON_TYPE);
        int min = (1+getLevel()*35/10)*WEAPON_RADIO[wType]/100;
        int max = (4+getLevel()*73/10)*WEAPON_RADIO[wType]/2/100;
        return new IntRange(min,max);
    }

    public IntRange getMaxAttack(){
        int wType = getProperty(IEquipment.EQUIP_ADD_WEAPON_TYPE);
        int min = (4+getLevel()*73/10)*WEAPON_RADIO[wType]/2/100+1;
        int max = (3+getLevel()*38/10)*WEAPON_RADIO[wType]/100;
        return new IntRange(min,max);
    }




    public IntRange getDefence(){

        int radio = 0;
        if(part==IEquipment.PART_SHIELD){
            radio = 8;
        }
        else if(part==IEquipment.PART_HEAD){
            radio = 2;
        }
        else if(part==IEquipment.PART_CHEST){
            radio = 4;
        }
        else if(part==IEquipment.PART_FEET){
            radio = 1;
        }
        else if(part==IEquipment.PART_WAIST){
            radio = 1;
        }

        int min = 0;
        int max = 0;

        if(getQuality()==0){
            min = (getLevel()*56/10+13)*radio/16;
            max = (getLevel()*56/10+23)*radio/16;
        }

        else if(getQuality()==1){
            min = (getLevel()*64/10+29)*radio/16;
            max = (getLevel()*64/10+39)*radio/16;
        }

        else if(getQuality()==2){
            min = (getLevel()*74/10+44)*radio/16;
            max = (getLevel()*74/10+54)*radio/16;
        }
        return new IntRange(min,max);
    }

    public int getAddedPoint(){

        byte part = getPart();
        short quality = getQuality();
        if(part==IEquipment.PART_SHIELD){
            if(quality==0){
                return 0;
            }
            else if(quality==1){
                return Math.round((float)getLevel()*4/100);
            }
            else if(quality==2){
                return Math.round((float)getLevel()*6/100);
            }
        }
        else if(part==IEquipment.PART_CHEST){
            if(quality==0){
                return 0;
            }
            else if(quality==1){
                return Math.round((float)getLevel()*12/100);
            }
            else if(quality==2){
                return Math.round((float)getLevel()*18/100);
            }
        }
        else if (part == IEquipment.PART_WAIST) {
            if (quality == 0) {
                return 0;
            } else if (quality == 1) {
                return Math.round((float) getLevel() * 8 / 100);
            } else if (quality == 2) {
                return Math.round((float) getLevel() * 12 / 100);
            }
        }
        else if (part == IEquipment.PART_FEET) {
            if (quality == 0) {
                return 0;
            } else if (quality == 1) {
                return Math.round((float) getLevel() * 8 / 100);
            } else if (quality == 2) {
                return Math.round((float) getLevel() * 12 / 100);
            }
        }
        else if (part == IEquipment.PART_HEAD) {
            if (quality == 0) {
                return 0;
            } else if (quality == 1) {
                return Math.round((float) (getLevel()-29) * 5 / 70);
            } else if (quality == 2) {
                return Math.round((float) (getLevel()-29) * 7 / 70+1);
            }
        }
        else if (part == IEquipment.PART_WRIST) {
            if (quality == 0) {
                return 0;
            } else if (quality == 1) {
                return Math.round((float) (getLevel()-29) * 5 / 70);
            } else if (quality == 2) {
                return Math.round((float) (getLevel()-29) * 7 / 70+1);
            }
        }
        else if (part == IEquipment.PART_FINGER) {
            if (quality == 0) {
                return 0;
            } else if (quality == 1) {
                return Math.round((float) (getLevel() - 29) * 5 / 70);
            } else if (quality == 2) {
                return Math.round((float) (getLevel() - 29) * 7 / 70 + 1);
            }
        }
        else if (part == IEquipment.PART_NECK) {
            if (quality == 0) {
                return 0;
            } else if (quality == 1) {
                return Math.round((float) (getLevel() - 29) * 5 / 70);
            } else if (quality == 2) {
                return Math.round((float) (getLevel() - 29) * 7 / 70 + 1);
            }
        }
        else if(part == IEquipment.PART_WEAPON){
            if(quality==0)
                return 0;
            else if(quality==1){
                return 1+getLevel()*5/26;
            }
            else if(quality==2){
                return getLevel()*20/51;
            }
        }
        return 0;
    }
}

class IntRange{

    private int min;
    private int max;

    public IntRange(int min,int max){
        this.min = min;
        this.max = max;
    }

    public int getMin(){
        return min;
    }

    public int getMax(){
        return max;
    }
}
