package com.pip.itimes.server.stage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.pip.itimes.server.util.IDGenerator;
import com.pip.itimes.server.util.Utils;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class NormalEquipmentTemplate extends EquipmentTemplate{
	
    public NormalEquipmentTemplate(){
        setCreateType(IEquipment.CREATE_NORMAL);
    }
    private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
   
    
    /* (non-Javadoc)
     * @see com.pip.itimes.server.stage.IItemTemplate#newInstance()
     * 
     * 用于最新生成的
     */
    public IItem newInstance(){
        int id = IDGenerator.getEquipmentId();
        NormalEquipment ret = new NormalEquipment(this,id);
        ret.setCurrentDurability(getDurability());
        if (getBindType() == IEquipment.BIND_GET) {
            ret.setBinded(true);
        }
      //mengjie add 过期日期
        if (getProperty(IEquipment.EQUIP_FAILURE_DATE) == 0){
     	   if (getProperty(IEquipment.EQUIP_FAILURE_TIME) > 0){
     		   //到期日
     		   long long_date = (new Date()).getTime() + 24 * 3600 * 1000 * getProperty(IEquipment.EQUIP_FAILURE_TIME);
     		   ret.setFAILURE_TIME(long_date);
     	   }else{
     		   ret.setFAILURE_TIME(-1);
     	   }
        }else{
     	   String str_date = Integer.valueOf(getProperty(IEquipment.EQUIP_FAILURE_DATE)).toString();
     	   Date date_tmp = new Date();
 			try {
 				date_tmp = format.parse(str_date);
 			} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
     	   //优先级，到期日期
     	   ret.setFAILURE_TIME(date_tmp.getTime());
        }
        
        //新生成的话则需要设置字符串
        //if(ret.canLettering()){//没有可过可以刻则从服务器配置表读入
		String letteringTemplateString = Enhance.getLettering(ret.getItemId());
		if(letteringTemplateString != null){
			int extendFlag = ret.getExtendFlag();
			try {
				extendFlag = Utils.SetIntN(extendFlag, NormalEquipment.CAN_LETTERING);
				ret.setExtendFlag(extendFlag);
				ret.setLetteringString(letteringTemplateString);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		ret.setItemShowType(this.getItemSplitType());
		
		//同步一次
		ret.setDiamond(this.getDiamond()); //同步鉴定
		ret.setDiamondcount(this.getDiamondcount());
		ret.setOpenDiamondCount(this.getOpenDiamondCount());
		
		if(this.getDiamondcount() > ret.MaxDiamondRoleCount){
			ret.resetDiamondMosiacRoleInfo(this.getDiamondcount());
		}
		//同步开放孔位信息
		byte[] diamondRoleInfo = ret.getDiamondMosiacRoleInfo();
		for(int i = 0; i < this.getOpenDiamondCount(); i++){
			if(diamondRoleInfo[i] == IEquipment.CURRENT_EQU_DIAMOND_NOTROLE){
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
		//2013年3月29日 增加宝石信息
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


    /* (non-Javadoc)
     * @see com.pip.itimes.server.stage.EquipmentTemplate#newInstance(int, int)
     * 用于数据库读取物品时候创建的
     */
    public IEquipment newInstance(int id,int seed){
        NormalEquipment ret = new NormalEquipment(this,id);
        ret.setCurrentDurability(getDurability());
        if(getBindType()==IEquipment.BIND_GET){
            ret.setBinded(true);
        }
        //mengjie add 过期日期
        if (getProperty(IEquipment.EQUIP_FAILURE_DATE) == 0){
     	   if (getProperty(IEquipment.EQUIP_FAILURE_TIME) > 0){
     		   //到期日
     		   long long_date = (new Date()).getTime() + 24 * 3600 * 1000 * getProperty(IEquipment.EQUIP_FAILURE_TIME);
     		   ret.setFAILURE_TIME(long_date);
     	   }else{
     		   ret.setFAILURE_TIME(-1);
     	   }
        }else{
     	   String str_date = Integer.valueOf(getProperty(IEquipment.EQUIP_FAILURE_DATE)).toString();
     	   Date date_tmp = new Date();
 			try {
 				date_tmp = format.parse(str_date);
 			} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
     	   //优先级，到期日期
     	   ret.setFAILURE_TIME(date_tmp.getTime());
        }
        ret.setItemShowType(this.getItemSplitType());
        
        ret.setDiamond(this.getDiamond()); //同步鉴定
		ret.setDiamondcount(this.getDiamondcount());
		ret.setOpenDiamondCount(this.getOpenDiamondCount());
		
		if(this.getDiamondcount() > ret.MaxDiamondRoleCount){
			ret.resetDiamondMosiacRoleInfo(this.getDiamondcount());
		}
		
		byte[] diamondRoleInfo = ret.getDiamondMosiacRoleInfo();
		for(int i = 0; i < this.getOpenDiamondCount(); i++){
			if(diamondRoleInfo[i] == IEquipment.CURRENT_EQU_DIAMOND_NOTROLE){
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
        return ret;
    }
}
