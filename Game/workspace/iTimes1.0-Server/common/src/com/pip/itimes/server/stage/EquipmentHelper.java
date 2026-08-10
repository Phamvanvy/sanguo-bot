package com.pip.itimes.server.stage;

import java.io.DataInputStream;
import java.util.Date;

import org.apache.log4j.Logger;
import org.mortbay.log.Log;

import com.pip.itimes.server.util.Utils;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class EquipmentHelper {
	
	private static final Logger log = Logger.getLogger(EquipmentHelper.class);

    public static final IEquipment createFromDbBytes(byte version,DataInputStream dis) throws Exception{
        if(version==1){
            int itemId = dis.readInt();
            int id = dis.readInt();
            int seed = dis.readInt();
            boolean binded = dis.readBoolean();
            short currentDurability = dis.readShort();
            if (currentDurability < 0)
                currentDurability = 0;
            long l = dis.readLong();
            IItemTemplate template = Items.getTemplate(itemId);
            EquipmentTemplate equTemplate = (EquipmentTemplate) template;
            IEquipment equ = equTemplate.newInstance(id, seed);
            equ.setBinded(binded);
            equ.setCurrentDurability(currentDurability);
            return equ;
        }else if (version==2){
            int itemId = dis.readInt();
           int id = dis.readInt();
           int seed = dis.readInt();
           boolean binded = dis.readBoolean();
           short currentDurability = dis.readShort();
           if (currentDurability < 0)
               currentDurability = 0;
//           long l = dis.readLong();
           IItemTemplate template = Items.getTemplate(itemId);
           EquipmentTemplate equTemplate = (EquipmentTemplate) template;
           IEquipment equ = equTemplate.newInstance(id, seed);
           equ.setBinded(binded);
           equ.setCurrentDurability(currentDurability);//当前耐久度
           int size = dis.read();
           for(int i=0;i<size;i++){
               int property = dis.read();
               //mengjie modify
               Enhance enhance = Enhance.getEnhance(property,template.getLevel());
               //mengjie modify end
               equ.enhance(enhance);
           }
           boolean lastEnhanceStatus = dis.read()==0?false:true;
           int enhanceStatusTimes = dis.read();
           equ.setLastEnhanceStatus(lastEnhanceStatus);
           equ.setEnhanceStatusTimes(enhanceStatusTimes);
           return equ;
        }else if(version == 3){//mengjie add version = 3
            int itemId = dis.readInt();
            int id = dis.readInt();
            int seed = dis.readInt();
            boolean binded = dis.readBoolean();
            short currentDurability = dis.readShort();
            if (currentDurability < 0)
                currentDurability = 0;
//            long l = dis.readLong();
            IItemTemplate template = Items.getTemplate(itemId);
            EquipmentTemplate equTemplate = (EquipmentTemplate) template;
            IEquipment equ = equTemplate.newInstance(id, seed);
            if(equTemplate.getBindType()==IItem.BIND_GET)
            	binded = true;
            equ.setBinded(binded);
            equ.setCurrentDurability(currentDurability);//当前耐久度
            int size = dis.read();
            for(int i=0;i<size;i++){
                int property = dis.read();
                Enhance enhance = Enhance.getEnhance(property,template.getLevel());
                equ.enhance(enhance);
            }
            boolean lastEnhanceStatus = dis.read()==0?false:true;
            int enhanceStatusTimes = dis.read();
            equ.setLastEnhanceStatus(lastEnhanceStatus);
            equ.setEnhanceStatusTimes(enhanceStatusTimes);
            //mengjie add 失效时间
            long long_tmp = dis.readLong();
            if (long_tmp>0){
            	equ.setFAILURE_TIME(long_tmp);
            	if((new Date()).getTime() > long_tmp){//当日已超过过期日期
            		//耐久度置为0
            		equ.setCurrentDurability((short) 0);//当前耐久度
            	}
            }else{
            	equ.setFAILURE_TIME(-1);
            }
            return equ;
         }else if(version == 4){  //增加鉴定
        	 int itemId = dis.readInt();
             int id = dis.readInt();
             int seed = dis.readInt();
             boolean binded = dis.readBoolean();
             short currentDurability = dis.readShort();
             if (currentDurability < 0)
                 currentDurability = 0;
//             long l = dis.readLong();
             IItemTemplate template = Items.getTemplate(itemId);
             EquipmentTemplate equTemplate = (EquipmentTemplate) template;
             IEquipment equ = equTemplate.newInstance(id, seed);
             if(equTemplate.getBindType()==IItem.BIND_GET)
             	binded = true;
             equ.setBinded(binded);
             equ.setCurrentDurability(currentDurability);//当前耐久度
             int size = dis.read();
             for(int i=0;i<size;i++){
                 int property = dis.read();
                 Enhance enhance = Enhance.getEnhance(property,template.getLevel());
                 equ.enhance(enhance);
             }
             boolean lastEnhanceStatus = dis.read()==0?false:true;
             int enhanceStatusTimes = dis.read();
             equ.setLastEnhanceStatus(lastEnhanceStatus);
             equ.setEnhanceStatusTimes(enhanceStatusTimes);
             //mengjie add 失效时间
             long long_tmp = dis.readLong();
             if (long_tmp>0){
             	equ.setFAILURE_TIME(long_tmp);
             	if((new Date()).getTime() > long_tmp){//当日已超过过期日期
             		//耐久度置为0
             		equ.setCurrentDurability((short) 0);//当前耐久度
             	}
             }else{
             	equ.setFAILURE_TIME(-1);
             }
             //jwp add 星级钻数
             byte diamond = dis.readByte();
             equ.setDiamond(diamond);
             return equ;
         }else if(version == 5){//大于4 加1位拓展（刻字用）
        	 int itemId = dis.readInt();
             int id = dis.readInt();
             int seed = dis.readInt();
             boolean binded = dis.readBoolean();
             short currentDurability = dis.readShort();
             if (currentDurability < 0)
                 currentDurability = 0;
//             long l = dis.readLong();
             IItemTemplate template = Items.getTemplate(itemId);
             EquipmentTemplate equTemplate = (EquipmentTemplate) template;
             IEquipment equ = equTemplate.newInstance(id, seed);
             if(equTemplate.getBindType()==IItem.BIND_GET)
             	binded = true;
             equ.setBinded(binded);
             equ.setCurrentDurability(currentDurability);//当前耐久度
             int size = dis.read();
             for(int i=0;i<size;i++){
                 int property = dis.read();
                 Enhance enhance = Enhance.getEnhance(property,template.getLevel());
                 equ.enhance(enhance);
             }
             boolean lastEnhanceStatus = dis.read()==0?false:true;
             int enhanceStatusTimes = dis.read();
             equ.setLastEnhanceStatus(lastEnhanceStatus);
             equ.setEnhanceStatusTimes(enhanceStatusTimes);
             //mengjie add 失效时间
             long long_tmp = dis.readLong();
             if (long_tmp>0){
             	equ.setFAILURE_TIME(long_tmp);
             	if((new Date()).getTime() > long_tmp){//当日已超过过期日期
             		//耐久度置为0
             		equ.setCurrentDurability((short) 0);//当前耐久度
             	}
             }else{
             	equ.setFAILURE_TIME(-1);
             }
             //jwp add 星级钻数
             byte diamond = dis.readByte();
             equ.setDiamond(diamond);
             int extendFlag = 0;
             try{
            	 extendFlag = dis.readInt(); 
             }catch(Exception e){
             }
             
             try {
				equ.setExtendFlag(extendFlag);
				//顺序为刻字
				if(equ instanceof NormalEquipment){
					if(((NormalEquipment) equ).lettered()){ //刻过了则从数据库读入
						String letteringString = dis.readUTF();
						equ.setLetteringString(letteringString);
					}else if(((NormalEquipment) equ).canLettering()){//没有可过可以刻则从服务器配置表读入
						String letteringTemplateString = Enhance.getLettering(equ.getItemId());
						if(letteringTemplateString != null){
							equ.setLetteringString(letteringTemplateString);
						}
					}				
				}else if(equ instanceof DynamicEquipment){
					if(((DynamicEquipment) equ).lettered()){ //刻过了则从数据库读入
						String letteringString = dis.readUTF();
						equ.setLetteringString(letteringString);
					}else if(((DynamicEquipment) equ).canLettering()){//没有可过可以刻则从服务器配置表读入
						String letteringTemplateString = Enhance.getLettering(equ.getItemId());
						if(letteringTemplateString != null){
							equ.setLetteringString(letteringTemplateString);
						}
					}	
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
            return equ;
         }else if(version == 6){
        	 int itemId = dis.readInt();
             int id = dis.readInt();
             int seed = dis.readInt();
             boolean binded = dis.readBoolean();
             short currentDurability = dis.readShort();
             if (currentDurability < 0)
                 currentDurability = 0;
//             long l = dis.readLong();
             IItemTemplate template = Items.getTemplate(itemId);
             EquipmentTemplate equTemplate = (EquipmentTemplate) template;
             IEquipment equ = equTemplate.newInstance(id, seed);
             if(equTemplate.getBindType()==IItem.BIND_GET)
             	binded = true;
             equ.setBinded(binded);
             equ.setCurrentDurability(currentDurability);//当前耐久度
             int size = dis.read();
             for(int i=0;i<size;i++){
                 int property = dis.read();
                 Enhance enhance = Enhance.getEnhance(property,template.getLevel());
                 equ.enhance(enhance);
             }
             boolean lastEnhanceStatus = dis.read()==0?false:true;
             int enhanceStatusTimes = dis.read();
             equ.setLastEnhanceStatus(lastEnhanceStatus);
             equ.setEnhanceStatusTimes(enhanceStatusTimes);
             //mengjie add 失效时间
             long long_tmp = dis.readLong();
             if (long_tmp>0){
             	equ.setFAILURE_TIME(long_tmp);
             	if((new Date()).getTime() > long_tmp){//当日已超过过期日期
             		//耐久度置为0
             		equ.setCurrentDurability((short) 0);//当前耐久度
             	}
             }else{
             	equ.setFAILURE_TIME(-1);
             }
             //jwp add 星级钻数
             byte diamond = dis.readByte();
             equ.setDiamond(diamond);
             int extendFlag = 0;
             try{
            	 extendFlag = dis.readInt(); 
             }catch(Exception e){
             }
             
             try {
				equ.setExtendFlag(extendFlag);
				//顺序为刻字
				if(equ instanceof NormalEquipment){
					if(((NormalEquipment) equ).lettered()){ //刻过了则从数据库读入
						String letteringString = dis.readUTF();
						equ.setLetteringString(letteringString);
					}else if(((NormalEquipment) equ).canLettering()){//没有可过可以刻则从服务器配置表读入
						String letteringTemplateString = Enhance.getLettering(equ.getItemId());
						if(letteringTemplateString != null){
							equ.setLetteringString(letteringTemplateString);
						}
					}				
				}else if(equ instanceof DynamicEquipment){
					if(((DynamicEquipment) equ).lettered()){ //刻过了则从数据库读入
						String letteringString = dis.readUTF();
						equ.setLetteringString(letteringString);
					}else if(((DynamicEquipment) equ).canLettering()){//没有可过可以刻则从服务器配置表读入
						String letteringTemplateString = Enhance.getLettering(equ.getItemId());
						if(letteringTemplateString != null){
							equ.setLetteringString(letteringTemplateString);
						}
					}	
				}
			} catch (Exception e) {
			}
			
			//增加宝石的读取，并进行同步孔位数量 ,读取镶钻的数量
			byte diamondMoasiacRoleInfoCount = dis.readByte();
			byte[] diamondMoasiacRoleInfo = new byte[diamondMoasiacRoleInfoCount];
			dis.read(diamondMoasiacRoleInfo);
			equ.setDiamondMosiacRoleInfo(diamondMoasiacRoleInfo);
			//读取模板进行同步孔位数量
			//说明此刻孔位信息有变化了,这里只处理增大，因为如果处理减小的话，万一玩家已经打孔了，会造成数据混乱, 玩家信息丢失
			if(equTemplate.getDiamondcount() > diamondMoasiacRoleInfoCount){ 
				equ.resetDiamondMosiacRoleInfo(equTemplate.getDiamondcount());
			}
			
			//同步玩家孔位开放信息
			byte dismondMosaicMapSize = dis.readByte();
			/*if(equ.getName().endsWith("99巨龙之爪")){
				System.out.println("一");
			}*/
			for(int i = 0; i < dismondMosaicMapSize; i++){
				byte role = dis.readByte();
				int itemRoleId = dis.readInt();
				equ.diamondMosaic(role, DiamondMosaic.getDiamondMosaicMap().get(itemRoleId));
			}
			byte[] diamondRoleInfo = equ.getDiamondMosiacRoleInfo();
			for(int i = 0; i < equ.getOpenDiamondCount(); i++){
				if(diamondRoleInfo[i] == IEquipment.CURRENT_EQU_DIAMOND_NOTROLE){
					diamondRoleInfo[i] = IEquipment.CURRENT_EQU_CANDIAMOND;
				}
			}
			equ.setDiamondMosiacRoleInfo(diamondRoleInfo);
            return equ;
        	 
         } else if (version == 7) {	// 附魔数值调整
        	 int itemId = dis.readInt();
             int id = dis.readInt();
             int seed = dis.readInt();
             boolean binded = dis.readBoolean();
             short currentDurability = dis.readShort();
             if (currentDurability < 0)
                 currentDurability = 0;
//             long l = dis.readLong();
             IItemTemplate template = Items.getTemplate(itemId);
             EquipmentTemplate equTemplate = (EquipmentTemplate) template;
             IEquipment equ = equTemplate.newInstance(id, seed);
             if(equTemplate.getBindType()==IItem.BIND_GET)
             	binded = true;
             equ.setBinded(binded);
             equ.setCurrentDurability(currentDurability);//当前耐久度
             int size = dis.read();
             for(int i=0;i<size;i++){
                 int property = dis.read();
                 Enhance enhance = Enhance.getEnhance(property,template.getLevel());
                 equ.enhance(enhance);
             }
             boolean lastEnhanceStatus = dis.read()==0?false:true;
             int enhanceStatusTimes = dis.read();
             equ.setLastEnhanceStatus(lastEnhanceStatus);
             equ.setEnhanceStatusTimes(enhanceStatusTimes);
             //mengjie add 失效时间
             long long_tmp = dis.readLong();
             if (long_tmp>0){
             	equ.setFAILURE_TIME(long_tmp);
             	if((new Date()).getTime() > long_tmp){//当日已超过过期日期
             		//耐久度置为0
             		equ.setCurrentDurability((short) 0);//当前耐久度
             	}
             }else{
             	equ.setFAILURE_TIME(-1);
             }
             //jwp add 星级钻数
             byte diamond = dis.readByte();
             equ.setDiamond(diamond);
             int extendFlag = 0;
             try{
            	 extendFlag = dis.readInt(); 
             }catch(Exception e){
             }
             
             try {
				equ.setExtendFlag(extendFlag);
				//顺序为刻字
				if(equ instanceof NormalEquipment){
					if(((NormalEquipment) equ).lettered()){ //刻过了则从数据库读入
						String letteringString = dis.readUTF();
						equ.setLetteringString(letteringString);
					}else if(((NormalEquipment) equ).canLettering()){//没有可过可以刻则从服务器配置表读入
						String letteringTemplateString = Enhance.getLettering(equ.getItemId());
						if(letteringTemplateString != null){
							equ.setLetteringString(letteringTemplateString);
						}
					}				
				}else if(equ instanceof DynamicEquipment){
					if(((DynamicEquipment) equ).lettered()){ //刻过了则从数据库读入
						String letteringString = dis.readUTF();
						equ.setLetteringString(letteringString);
					}else if(((DynamicEquipment) equ).canLettering()){//没有可过可以刻则从服务器配置表读入
						String letteringTemplateString = Enhance.getLettering(equ.getItemId());
						if(letteringTemplateString != null){
							equ.setLetteringString(letteringTemplateString);
						}
					}	
				}
			} catch (Exception e) {
			}
			
			//增加宝石的读取，并进行同步孔位数量 ,读取镶钻的数量
			byte diamondMoasiacRoleInfoCount = dis.readByte();
			byte[] diamondMoasiacRoleInfo = new byte[diamondMoasiacRoleInfoCount];
			dis.read(diamondMoasiacRoleInfo);
			equ.setDiamondMosiacRoleInfo(diamondMoasiacRoleInfo);
			//读取模板进行同步孔位数量
			//说明此刻孔位信息有变化了,这里只处理增大，因为如果处理减小的话，万一玩家已经打孔了，会造成数据混乱, 玩家信息丢失
			if(equTemplate.getDiamondcount() > diamondMoasiacRoleInfoCount){ 
				equ.resetDiamondMosiacRoleInfo(equTemplate.getDiamondcount());
			}
			
			//同步玩家孔位开放信息
			byte dismondMosaicMapSize = dis.readByte();
			/*if(equ.getName().endsWith("99巨龙之爪")){
				System.out.println("一");
			}*/
			for(int i = 0; i < dismondMosaicMapSize; i++){
				byte role = dis.readByte();
				int itemRoleId = dis.readInt();
				equ.diamondMosaic(role, DiamondMosaic.getDiamondMosaicMap().get(itemRoleId));
			}
			byte[] diamondRoleInfo = equ.getDiamondMosiacRoleInfo();
			for(int i = 0; i < equ.getOpenDiamondCount(); i++){
				if(diamondRoleInfo[i] == IEquipment.CURRENT_EQU_DIAMOND_NOTROLE){
					diamondRoleInfo[i] = IEquipment.CURRENT_EQU_CANDIAMOND;
				}
			}
			equ.setDiamondMosiacRoleInfo(diamondRoleInfo);
			
			//version 7 增加附魔属性
			Enchanting enchan = equ.getEnchanting();
			enchan.setEnchantingItemId(dis.readInt());
			enchan.setArrtType(dis.readByte());
			enchan.setArrtValue(dis.readByte());
			enchan.setStoneType(dis.readByte());
			enchan.setStoneValue(dis.readByte());
			// 2011年3月10日附魔v1.3调整数值
			int stoneLevel = Enchanting.hasStoneType(equ, enchan.getStoneType());
			if (enchan.getEnchantingItemId() == 201110) {
				byte newStoneValue = 0;
				byte vale = enchan.getArrtValue();
				if (vale > 0) {
					if (vale < 10) {
						vale += 7;
					} else {
						vale += 15;
					}
				}
				enchan.setArrtValue(vale);
				if (stoneLevel > 0) {
					if(enchan.getStoneType() == IEquipment.EQUIP_ADD_HIT){
						newStoneValue = (byte) ((stoneLevel * stoneLevel) << 1);
					} else {
						newStoneValue = (byte) (stoneLevel * stoneLevel);
					}
				}
				enchan.setStoneValue(newStoneValue);
			} else if (enchan.getEnchantingItemId() == 201111) {
				byte newStoneValue = 0;
				byte vale = enchan.getArrtValue();
				if (vale < 10) {
					vale += 17;
				} else {
					vale += 25;
				}
				enchan.setArrtValue(vale);
				if (stoneLevel > 0) {
					if(enchan.getStoneType() == IEquipment.EQUIP_ADD_HIT){
						newStoneValue = (byte) ((stoneLevel * stoneLevel) << 1);
					} else {
						newStoneValue = (byte) (stoneLevel * stoneLevel);
					}
				}
				enchan.setStoneValue(newStoneValue);
			}
			
            return equ;
         } else if (version == 8) {	// 附魔数值调整
        	 int itemId = dis.readInt();
             int id = dis.readInt();
             int seed = dis.readInt();
             boolean binded = dis.readBoolean();
             short currentDurability = dis.readShort();
             if (currentDurability < 0)
                 currentDurability = 0;
//             long l = dis.readLong();
             IItemTemplate template = Items.getTemplate(itemId);
             EquipmentTemplate equTemplate = (EquipmentTemplate) template;
             IEquipment equ = equTemplate.newInstance(id, seed);
             if(equTemplate.getBindType()==IItem.BIND_GET)
             	binded = true;
             equ.setBinded(binded);
             equ.setCurrentDurability(currentDurability);//当前耐久度
             int size = dis.read();
             for(int i=0;i<size;i++){
                 int property = dis.read();
                 Enhance enhance = Enhance.getEnhance(property,template.getLevel());
                 equ.enhance(enhance);
             }
             boolean lastEnhanceStatus = dis.read()==0?false:true;
             int enhanceStatusTimes = dis.read();
             equ.setLastEnhanceStatus(lastEnhanceStatus);
             equ.setEnhanceStatusTimes(enhanceStatusTimes);
             //mengjie add 失效时间
             long long_tmp = dis.readLong();
             if (long_tmp>0){
             	equ.setFAILURE_TIME(long_tmp);
             	if((new Date()).getTime() > long_tmp){//当日已超过过期日期
             		//耐久度置为0
             		equ.setCurrentDurability((short) 0);//当前耐久度
             	}
             }else{
             	equ.setFAILURE_TIME(-1);
             }
             //jwp add 星级钻数
             byte diamond = dis.readByte();
             equ.setDiamond(diamond);
             int extendFlag = 0;
             try{
            	 extendFlag = dis.readInt(); 
             }catch(Exception e){
             }
             
             try {
				equ.setExtendFlag(extendFlag);
				//顺序为刻字
				if(equ instanceof NormalEquipment){
					if(((NormalEquipment) equ).lettered()){ //刻过了则从数据库读入
						String letteringString = dis.readUTF();
						equ.setLetteringString(letteringString);
					}else if(((NormalEquipment) equ).canLettering()){//没有可过可以刻则从服务器配置表读入
						String letteringTemplateString = Enhance.getLettering(equ.getItemId());
						if(letteringTemplateString != null){
							equ.setLetteringString(letteringTemplateString);
						}
					}				
				}else if(equ instanceof DynamicEquipment){
					if(((DynamicEquipment) equ).lettered()){ //刻过了则从数据库读入
						String letteringString = dis.readUTF();
						equ.setLetteringString(letteringString);
					}else if(((DynamicEquipment) equ).canLettering()){//没有可过可以刻则从服务器配置表读入
						String letteringTemplateString = Enhance.getLettering(equ.getItemId());
						if(letteringTemplateString != null){
							equ.setLetteringString(letteringTemplateString);
						}
					}	
				}
			} catch (Exception e) {
			}
			
			//增加宝石的读取，并进行同步孔位数量 ,读取镶钻的数量
			byte diamondMoasiacRoleInfoCount = dis.readByte();
			byte[] diamondMoasiacRoleInfo = new byte[diamondMoasiacRoleInfoCount];
			dis.read(diamondMoasiacRoleInfo);
			equ.setDiamondMosiacRoleInfo(diamondMoasiacRoleInfo);
			//读取模板进行同步孔位数量
			//说明此刻孔位信息有变化了,这里只处理增大，因为如果处理减小的话，万一玩家已经打孔了，会造成数据混乱, 玩家信息丢失
			if(equTemplate.getDiamondcount() > diamondMoasiacRoleInfoCount){ 
				equ.resetDiamondMosiacRoleInfo(equTemplate.getDiamondcount());
			}
			
			//同步玩家孔位开放信息
			byte dismondMosaicMapSize = dis.readByte();
			/*if(equ.getName().endsWith("99巨龙之爪")){
				System.out.println("一");
			}*/
			for(int i = 0; i < dismondMosaicMapSize; i++){
				byte role = dis.readByte();
				int itemRoleId = dis.readInt();
				equ.diamondMosaic(role, DiamondMosaic.getDiamondMosaicMap().get(itemRoleId));
			}
			byte[] diamondRoleInfo = equ.getDiamondMosiacRoleInfo();
			for(int i = 0; i < equ.getOpenDiamondCount(); i++){
				if(diamondRoleInfo[i] == IEquipment.CURRENT_EQU_DIAMOND_NOTROLE){
					diamondRoleInfo[i] = IEquipment.CURRENT_EQU_CANDIAMOND;
				}
			}
			equ.setDiamondMosiacRoleInfo(diamondRoleInfo);
			
			//version 7 增加附魔属性
			Enchanting enchan = equ.getEnchanting();
			enchan.setEnchantingItemId(dis.readInt());
			enchan.setArrtType(dis.readByte());
			enchan.setArrtValue(dis.readByte());
			enchan.setStoneType(dis.readByte());
			enchan.setStoneValue(dis.readByte());
			
			// 2011年3月15日附魔v1.3调整数值(修复BUG)
			int stoneLevel = Enchanting.hasStoneType(equ, enchan.getStoneType());
			if (enchan.getEnchantingItemId() == 201110) {
				if (enchan.getStoneValue() > 0 || enchan.getStoneValue() < 0) {
					byte newStoneValue = 0;
					enchan.setArrtValue((byte) 0);
					if (stoneLevel > 0) {
						if(enchan.getStoneType() == IEquipment.EQUIP_ADD_HIT){
							newStoneValue = (byte) ((stoneLevel * stoneLevel) << 1);
						} else {
							newStoneValue = (byte) (stoneLevel * stoneLevel);
						}
					}
					enchan.setStoneValue(newStoneValue);
				} else {
					if (enchan.getArrtType() == IEquipment.EQUIP_ADD_HIT) {
						if (enchan.getArrtValue() < 0 || enchan.getArrtValue() > 21) {
							enchan.setArrtValue((byte) 21);
						}
					} else {
						if (enchan.getArrtValue() < 0 || enchan.getArrtValue() > 15) {
							enchan.setArrtValue((byte) 15);
						}
					}
				}
			} else if (enchan.getEnchantingItemId() == 201111) {
				byte newStoneValue = 0;
				if (enchan.getArrtType() == IEquipment.EQUIP_ADD_HIT) {
					if (enchan.getArrtValue() < 0 || enchan.getArrtValue() > 41) {
						enchan.setArrtValue((byte) 41);
					}
				} else {
					if (enchan.getArrtValue() < 0 || enchan.getArrtValue() > 25) {
						enchan.setArrtValue((byte) 25);
					}
				}
				if (stoneLevel > 0) {
					if (enchan.getStoneType() == IEquipment.EQUIP_ADD_HIT) {
						newStoneValue = (byte) ((stoneLevel * stoneLevel) << 1);
					} else {
						newStoneValue = (byte) (stoneLevel * stoneLevel);
					}
				}
				enchan.setStoneValue(newStoneValue);
			}
			
            return equ;
         }else if(version == 9){ // 增加属性攻
        	 int itemId = dis.readInt();
             int id = dis.readInt();
             int seed = dis.readInt();
             boolean binded = dis.readBoolean();
             short currentDurability = dis.readShort();
             if (currentDurability < 0)
                 currentDurability = 0;
//             long l = dis.readLong();
             IItemTemplate template = Items.getTemplate(itemId);
             EquipmentTemplate equTemplate = (EquipmentTemplate) template;
             IEquipment equ = equTemplate.newInstance(id, seed);
             if(equTemplate.getBindType()==IItem.BIND_GET)
             	binded = true;
             equ.setBinded(binded);
             equ.setCurrentDurability(currentDurability);//当前耐久度
             int size = dis.read();
             for(int i=0;i<size;i++){
                 int property = dis.read();
                 Enhance enhance = Enhance.getEnhance(property,template.getLevel());
                 equ.enhance(enhance);
             }
             boolean lastEnhanceStatus = dis.read()==0?false:true;
             int enhanceStatusTimes = dis.read();
             equ.setLastEnhanceStatus(lastEnhanceStatus);
             equ.setEnhanceStatusTimes(enhanceStatusTimes);
             //mengjie add 失效时间
             long long_tmp = dis.readLong();
             if (long_tmp>0){
             	equ.setFAILURE_TIME(long_tmp);
             	if((new Date()).getTime() > long_tmp){//当日已超过过期日期
             		//耐久度置为0
             		equ.setCurrentDurability((short) 0);//当前耐久度
             	}
             }else{
             	equ.setFAILURE_TIME(-1);
             }
             //jwp add 星级钻数
             byte diamond = dis.readByte();
             equ.setDiamond(diamond);
             int extendFlag = 0;
             try{
            	 extendFlag = dis.readInt(); 
             }catch(Exception e){
             }
             
             try {
				equ.setExtendFlag(extendFlag);
				//顺序为刻字
				if(equ instanceof NormalEquipment){
					if(((NormalEquipment) equ).lettered()){ //刻过了则从数据库读入
						String letteringString = dis.readUTF();
						equ.setLetteringString(letteringString);
					}else if(((NormalEquipment) equ).canLettering()){//没有可过可以刻则从服务器配置表读入
						String letteringTemplateString = Enhance.getLettering(equ.getItemId());
						if(letteringTemplateString != null){
							equ.setLetteringString(letteringTemplateString);
						}
					}				
				}else if(equ instanceof DynamicEquipment){
					if(((DynamicEquipment) equ).lettered()){ //刻过了则从数据库读入
						String letteringString = dis.readUTF();
						equ.setLetteringString(letteringString);
					}else if(((DynamicEquipment) equ).canLettering()){//没有可过可以刻则从服务器配置表读入
						String letteringTemplateString = Enhance.getLettering(equ.getItemId());
						if(letteringTemplateString != null){
							equ.setLetteringString(letteringTemplateString);
						}
					}	
				}
			} catch (Exception e) {
			}
			
			//增加宝石的读取，并进行同步孔位数量 ,读取镶钻的数量
			byte diamondMoasiacRoleInfoCount = dis.readByte();
			byte[] diamondMoasiacRoleInfo = new byte[diamondMoasiacRoleInfoCount];
			dis.read(diamondMoasiacRoleInfo);
			equ.setDiamondMosiacRoleInfo(diamondMoasiacRoleInfo);
			//读取模板进行同步孔位数量
			//说明此刻孔位信息有变化了,这里只处理增大，因为如果处理减小的话，万一玩家已经打孔了，会造成数据混乱, 玩家信息丢失
			if(equTemplate.getDiamondcount() > diamondMoasiacRoleInfoCount){ 
				equ.resetDiamondMosiacRoleInfo(equTemplate.getDiamondcount());
			}
			
			//同步玩家孔位开放信息
			byte dismondMosaicMapSize = dis.readByte();
			/*if(equ.getName().endsWith("99巨龙之爪")){
				System.out.println("一");
			}*/
			for(int i = 0; i < dismondMosaicMapSize; i++){
				byte role = dis.readByte();
				int itemRoleId = dis.readInt();
				equ.diamondMosaic(role, DiamondMosaic.getDiamondMosaicMap().get(itemRoleId));
			}
			byte[] diamondRoleInfo = equ.getDiamondMosiacRoleInfo();
			for(int i = 0; i < equ.getOpenDiamondCount(); i++){
				if(diamondRoleInfo[i] == IEquipment.CURRENT_EQU_DIAMOND_NOTROLE){
					diamondRoleInfo[i] = IEquipment.CURRENT_EQU_CANDIAMOND;
				}
			}
			equ.setDiamondMosiacRoleInfo(diamondRoleInfo);
			
			//version 7 增加附魔属性
			Enchanting enchan = equ.getEnchanting();
			enchan.setEnchantingItemId(dis.readInt());
			enchan.setArrtType(dis.readByte());
			enchan.setArrtValue(dis.readByte());
			enchan.setStoneType(dis.readByte());
			enchan.setStoneValue(dis.readByte());
			
			//vesion 9 增加属性攻
			Viany viany = equ.getViany();
			viany.setStone(dis.readInt());
			viany.setScissors(dis.readInt());
			viany.setPaper(dis.readInt());
			
			//TODO 修正装备中的孔数和宝石不对应问题 毒瘤
			if(itemId == 1001947){
				fixDiamond(equ);
			}
			//TODO end
			
            return equ;
         }else { // 宝石养成
        	 int itemId = dis.readInt();
             int id = dis.readInt();
             int seed = dis.readInt();
             boolean binded = dis.readBoolean();
             short currentDurability = dis.readShort();
             if (currentDurability < 0)
                 currentDurability = 0;
//             long l = dis.readLong();
             IItemTemplate template = Items.getTemplate(itemId);
             EquipmentTemplate equTemplate = (EquipmentTemplate) template;
             IEquipment equ = equTemplate.newInstance(id, seed);
             if(equTemplate.getBindType()==IItem.BIND_GET)
             	binded = true;
             equ.setBinded(binded);
             equ.setCurrentDurability(currentDurability);//当前耐久度
             int size = dis.read();
             for(int i=0;i<size;i++){
                 int property = dis.read();
                 Enhance enhance = Enhance.getEnhance(property,template.getLevel());
                 equ.enhance(enhance);
             }
             boolean lastEnhanceStatus = dis.read()==0?false:true;
             int enhanceStatusTimes = dis.read();
             equ.setLastEnhanceStatus(lastEnhanceStatus);
             equ.setEnhanceStatusTimes(enhanceStatusTimes);
             //mengjie add 失效时间
             long long_tmp = dis.readLong();
             if (long_tmp>0){
             	equ.setFAILURE_TIME(long_tmp);
             	if((new Date()).getTime() > long_tmp){//当日已超过过期日期
             		//耐久度置为0
             		equ.setCurrentDurability((short) 0);//当前耐久度
             	}
             }else{
             	equ.setFAILURE_TIME(-1);
             }
             //jwp add 星级钻数
             byte diamond = dis.readByte();
             equ.setDiamond(diamond);
             int extendFlag = 0;
             try{
            	 extendFlag = dis.readInt(); 
             }catch(Exception e){
             }
             
             try {
				equ.setExtendFlag(extendFlag);
				//顺序为刻字
				if(equ instanceof NormalEquipment){
					if(((NormalEquipment) equ).lettered()){ //刻过了则从数据库读入
						String letteringString = dis.readUTF();
						equ.setLetteringString(letteringString);
					}else if(((NormalEquipment) equ).canLettering()){//没有可过可以刻则从服务器配置表读入
						String letteringTemplateString = Enhance.getLettering(equ.getItemId());
						if(letteringTemplateString != null){
							equ.setLetteringString(letteringTemplateString);
						}
					}				
				}else if(equ instanceof DynamicEquipment){
					if(((DynamicEquipment) equ).lettered()){ //刻过了则从数据库读入
						String letteringString = dis.readUTF();
						equ.setLetteringString(letteringString);
					}else if(((DynamicEquipment) equ).canLettering()){//没有可过可以刻则从服务器配置表读入
						String letteringTemplateString = Enhance.getLettering(equ.getItemId());
						if(letteringTemplateString != null){
							equ.setLetteringString(letteringTemplateString);
						}
					}	
				}
			} catch (Exception e) {
			}
			
			//增加宝石的读取，并进行同步孔位数量 ,读取镶钻的数量
			byte diamondMoasiacRoleInfoCount = dis.readByte();
			byte[] diamondMoasiacRoleInfo = new byte[diamondMoasiacRoleInfoCount];
			dis.read(diamondMoasiacRoleInfo);
			equ.setDiamondMosiacRoleInfo(diamondMoasiacRoleInfo);
			//读取模板进行同步孔位数量
			//说明此刻孔位信息有变化了,这里只处理增大，因为如果处理减小的话，万一玩家已经打孔了，会造成数据混乱, 玩家信息丢失
			if(equTemplate.getDiamondcount() > diamondMoasiacRoleInfoCount){ 
				equ.resetDiamondMosiacRoleInfo(equTemplate.getDiamondcount());
			}
			
			//同步玩家孔位开放信息
			byte dismondMosaicMapSize = dis.readByte();
			/*if(equ.getName().endsWith("99巨龙之爪")){
				System.out.println("一");
			}*/
			for(int i = 0; i < dismondMosaicMapSize; i++){
				byte role = dis.readByte();
				int itemRoleId = dis.readInt();
				equ.diamondMosaic(role, DiamondMosaic.getDiamondMosaicMap().get(itemRoleId));
			}
			byte[] diamondRoleInfo = equ.getDiamondMosiacRoleInfo();
			for(int i = 0; i < equ.getOpenDiamondCount(); i++){
				if(diamondRoleInfo[i] == IEquipment.CURRENT_EQU_DIAMOND_NOTROLE){
					diamondRoleInfo[i] = IEquipment.CURRENT_EQU_CANDIAMOND;
				}
			}
			equ.setDiamondMosiacRoleInfo(diamondRoleInfo);
			
			//version 7 增加附魔属性
			Enchanting enchan = equ.getEnchanting();
			enchan.setEnchantingItemId(dis.readInt());
			enchan.setArrtType(dis.readByte());
			enchan.setArrtValue(dis.readByte());
			enchan.setStoneType(dis.readByte());
			enchan.setStoneValue(dis.readByte());
			
			//vesion 9 增加属性攻
			Viany viany = equ.getViany();
			viany.setStone(dis.readInt());
			viany.setScissors(dis.readInt());
			viany.setPaper(dis.readInt());
			
			//version 10宝石养成
			int length = dis.readByte();
			short[] developAddCount = equ.getDevelopAddCount();
			for(int i=0; i<length; i++){
				if(equ instanceof NormalEquipment){
					developAddCount[i] = dis.readShort();
				}else{
					developAddCount[i] = (short)dis.readInt();
				}
			}
			length = dis.readByte();
			short[] developAddPoint = equ.getDevelopAddPoint();
			for(int i=0; i<length; i++){
				developAddPoint[i] = dis.readShort();
			}
			
			//TODO 修正装备中的孔数和宝石不对应问题 毒瘤
			if(itemId == 1001947){
				fixDiamond(equ);
			}
			//TODO end
			
			//宝石养成信息在后面读取 需要重置一下宝石的信息
			byte[] tmp = equ.getDiamondMosiacRoleInfo();
			for(byte i = 0; i < tmp.length; i++){
				DiamondMosaic diamondMosaic = equ.getDiamondMosaicRole(i);
				if(diamondMosaic != null){
					int itemRoleId = diamondMosaic.getItemId();
					equ.diamondMosaic(i, DiamondMosaic.getDiamondMosaicMap().get(itemRoleId));
				}
			}
			
            return equ;
         }
    }
    
    /**
     * 修正装备宝石问题 当孔上的信息和镶嵌的信息不一致时 使用宝石信息中的宝石为准
     * @param equ
     */
    public static void fixDiamond(IEquipment equ){
    	if(equ != null){
    		int size = 0;
    		byte[] roleinfo = equ.getDiamondMosiacRoleInfo();
    		for(int i=0; i<roleinfo.length; i++){
    			if(roleinfo[i] > 1){
    				size ++;
    			}
    		}
    		if(size != equ.getDiamondMosaicRoleSize()){
    			log.info("fixDiamond equid[" + equ.getId() + "] itemId[" + 
    					equ.getItemId() + "] holeInfoSize[" + size + 
    					"] mosaicSize[" + equ.getDiamondMosaicRoleSize() + "] Try");
    			size = equ.getDiamondMosaicRoleSize();
    			for(int i=0; i<5; i++){
    				DiamondMosaic dm = equ.getDiamondMosaicRole((byte)i);
    				if(dm != null){
    					int itemId = dm.getItemId();
    					byte diamondMosaicEmbedLevel = DiamondMosaic.findDiamondMosaicLevel(itemId);
    					roleinfo[i] = (byte)(IEquipment.CURRENT_EQU_CANDIAMOND + diamondMosaicEmbedLevel);
    					log.info("fixDiamond equid[" + equ.getId() + "] itemId[" + 
    	    					equ.getItemId() + "] hole[" + i + 
    	    					"] stoneid[" + itemId + "] diamondMosaicEmbedLevel[" + diamondMosaicEmbedLevel + "]");
    				}else{
    					roleinfo[i] = IEquipment.CURRENT_EQU_DIAMOND_NOTROLE;
    				}
    			}
    			if(equ.getOpenDiamondCount() < equ.getDiamondMosaicRoleSize()){
    				equ.setOpenDiamondCount((byte)equ.getDiamondMosaicRoleSize());
    				log.info("fixDiamond equid[" + equ.getId() + "] itemId[" + 
        					equ.getItemId() + "] OpenDiamondCount less");
    			}
    			log.info("fixDiamond equid[" + equ.getId() + "] itemId[" + 
    					equ.getItemId() + "] End");
    		}
    		for(int i = 0; i < equ.getOpenDiamondCount(); i++){
				if(roleinfo[i] == IEquipment.CURRENT_EQU_DIAMOND_NOTROLE){
					roleinfo[i] = IEquipment.CURRENT_EQU_CANDIAMOND;
				}
			}
    	}
    }
}
