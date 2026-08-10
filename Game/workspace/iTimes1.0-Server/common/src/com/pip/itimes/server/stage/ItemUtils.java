package com.pip.itimes.server.stage;

import java.io.*;

import com.pip.itimes.server.bean.Auction;
import com.pip.itimes.server.bean.Buy;
import com.pip.itimes.server.bean.Oem;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import com.pip.itimes.server.util.Utils;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class ItemUtils {

    public static byte[] getAuctionBytes(IItem item, int count, int dataVersion, int level) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            if (item.getType() == IItem.TYPE_BASIC) {

                dos.writeByte(2);
                dos.writeByte(1);
                dos.write(item.toClientBytes(dataVersion));
                dos.writeByte(count);
            }
            if (item.getType() == IItem.TYPE_TASK) {
                dos.writeByte(3);
                dos.writeByte(1);
                dos.write(item.toClientBytes(dataVersion));
                dos.writeByte(count);
            }
            if (item.getType() == IItem.TYPE_EXTENDED) {
                dos.writeByte(4);
                dos.writeByte(1);
                dos.write(item.toClientBytes(dataVersion));
                dos.writeByte(count);
            }

            if (item.getType() == IItem.TYPE_EQU) {
                dos.writeByte(5);
                dos.writeByte(1);
                dos.write(item.toClientBytesWithLevel(level));
            }

            if (item.getType() == IItem.TYPE_PET) {
                dos.writeByte(6);
                dos.writeByte(1);
                dos.write(item.toClientBytesWithLevel(-1));
            }
        } catch (IOException ex) {
            return new byte[0];
        }
        return bos.toByteArray();
    }


    public static byte[] money2dbAttachment(int count) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.write((byte)2); //items version
            dos.write(8); //todo magic number
            dos.writeInt(count);
        } catch (IOException ex) {
        }
        return bos.toByteArray();
    }

    public static byte[] item2dbAttachment(IItem item, int count) {
        if (item.getType() == IItem.TYPE_BASIC ||
            item.getType() == IItem.TYPE_EXTENDED ||
            item.getType() == IItem.TYPE_TASK) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            try {
                //dos.write((byte)2); //items version
                //dos.write((byte)3); //items version
            	
            	//dos.write((byte)4);				//items version 4  增加鉴定
            	//dos.write((byte)5);				//items version 5 装备刻字
//            	dos.write((byte)6);                 //items version 6 增加宝石系统
//              dos.write((byte)7);					//items version 7增加附魔系统
//                dos.write((byte)8);					//items version 8调整附魔数值
//                dos.write((byte)9);					//items version 9增加属性攻
                dos.write((byte)10);					//items version 10宝石养成
                dos.write(item.getType());
                dos.write(item.toDbBytes());
                dos.writeShort(count);
            } catch (IOException ex) {
            }
            return bos.toByteArray();
        } else if (item.getType() == IItem.TYPE_EQU) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            try {
               // dos.write((byte)2); //items version
            	
                //为什么附件里面没有3的版本
            	//dos.write((byte)4);				//items version 4  增加鉴定
            	//dos.write((byte)5);				//items version 5 装备刻字
//            	dos.write((byte)6);                 //items version 6 增加宝石系统
//              dos.write((byte)7);					//items version 7增加附魔系统
//                dos.write((byte)8);					//items version 8调整附魔数值
                dos.write((byte)9);					//items version 9增加属性攻
                dos.write(item.getType());
                dos.write(item.toDbBytes());
            } catch (IOException ex) {
            }
            return bos.toByteArray();
        }
        return new byte[0];
    }

    public static byte[] pet2dbAttachment(Pet pet) {
        byte[] bytes = pet.toDbBytes();
        byte[] ret = new byte[bytes.length + 2];
        ret[0] = 2;
        ret[1] = 6;
        System.arraycopy(bytes, 0, ret, 2, bytes.length);
        return ret;
    }

    public static byte[] getShopData(Auction auction, int clientVersion) {

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(auction.getStartPrice());
            dos.writeInt(auction.getCurrentPrice());
            dos.writeInt(auction.getEndPrice());
            byte[] bytes = dbAttachment2Client(auction.getItem(), clientVersion);
            dos.writeShort(bytes.length);
            dos.write(bytes);
            //modify
            dos.writeByte(auction.getQuality());
            return bos.toByteArray();
        } catch (IOException ex) {
            return new byte[0];
        }

    }

    public static byte[] getShopData(Buy buy) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(buy.getItemId());
            dos.writeShort(buy.getTotal());
            dos.writeShort(buy.getCurrent());
            dos.writeInt(buy.getPrice());
            //modify
            dos.write(buy.getQuality());
            return bos.toByteArray();
        } catch (IOException ex) {
            return new byte[0];
        }
    }
    public static int getAttachementEquId (byte[] bytes){//判断附件是白装还是绿装。。是返回id,否返回-1
    		int AttachementEquId = -1;
    		try {
	    		 ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
	    		 DataInputStream dis = new DataInputStream(bis);
	    		 byte version = dis.readByte();
	             byte type = dis.readByte();
	    		 if (type == IItem.TYPE_EQU){
	    			 IEquipment equ = EquipmentHelper.createFromDbBytes(version,dis);
	    			 if(Utils.CLR_EQUIP[equ.getQuality()] == Utils.CLR_WHITE || Utils.CLR_EQUIP[equ.getQuality()] == Utils.CLR_GREEN){
	    				 AttachementEquId = equ.getItemId();
	    			 }
	    		 }
	    		}catch (Exception e) {
				// TODO: handle exception
	    		}
        
	    		return AttachementEquId;
    }
    public static Attachment dbBytes2Attachment(byte[] bytes, int dataVersion) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            byte version = dis.readByte();
            byte type = dis.readByte();
            if (type == 8) { //todo magic number
                int count = dis.readInt();
                return new MoneyAttachment(count);
            } else if (type == IItem.TYPE_BASIC || type == IItem.TYPE_EXTENDED ||
                       type == IItem.TYPE_TASK) {
                int itemId = dis.readInt();
                Grid grid = new Grid();
                IItemTemplate template = Items.getTemplate(itemId);
                return new ItemAttachment(template.newInstance(), dis.readShort());
            } else if (type == IItem.TYPE_EQU) {
                IEquipment equ = EquipmentHelper.createFromDbBytes(version,dis);
                equ.setDataVersion(dataVersion);
                return new ItemAttachment(equ, 1);
            }
        } catch (Exception ex) {
        }
        return null;
    }

    public static byte[] getShopData(Grid grid, int dataVersion) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            if(grid.item instanceof IEquipment){
            	((IEquipment) grid.item).setDataVersion(dataVersion);
            }
            byte[] bytes = getAuctionBytes(grid.item, grid.count, dataVersion, -1);
            dos.writeShort(bytes.length);
            dos.write(bytes);
            return bos.toByteArray();
        } catch (IOException ex) {
            return new byte[0];
        }
    }

    public static byte[] getShopData(Oem oem) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(oem.getItemId());
            dos.writeShort(oem.getTotal());
            dos.writeShort(oem.getCurrent());
            dos.writeInt(oem.getPay());
            dos.writeShort(oem.getWorkPoint());
            dos.writeUTF("");
            //modify
            dos.write(oem.getQuality());
            return bos.toByteArray();
        } catch (IOException ex) {
            return new byte[0];
        }
    }


    public static byte[] dbAttachment2Client(byte[] bytes, int dataVersion) {
        if (bytes.length == 0)
            return bytes;
        Attachment attachment = dbBytes2Attachment(bytes, dataVersion);
        Changed changed = new Changed();
        if (attachment instanceof MoneyAttachment) {
            changed.addProperty(Changed.MONEY,
                                ((MoneyAttachment) attachment).getCount());
        } else {
            ItemAttachment ia = (ItemAttachment) attachment;
            changed.addItem(ia.getItem(), ia.count());
        }
        Object[] os = changed.toClientBytes(dataVersion);
        return (byte[]) os[0];
    }

    public static String getEquipmentString(IEquipment equ) {
        return equ.getDesc();
    }
    
    public static String getItemString(IItem item) {
    	String desc;
    	String color = "";
    	switch(item.getQuality()) {
    	case 0:
    		color = "FFFFFF";
    		break;
    	case 1:
    		color = "70E970";
    		break;
    	case 2:
    		color = "6FBBF9";
    		break;
    	case 3:
    		color = "C73FFF";
    		break;
    	case 4:
    		color = "FFA800";
    		break;
    	case 5:
    		color = "FFFF00";
    		break;
    	case 6:
    		color = "FF7777";
    		break;
    	}
    	if (item.getType() == 3) {
    		return item.getDesc();
    	} else {
    		desc = "<c" + color + ">" + item.getName()  + "</c>" + '\n' + item.getDesc();
    		return desc;
    	}
    }

    private static final Random rnd = new Random();
    //获得减耐久度前的标志
    public static boolean[] getAllDuragbility(PlayerData player){
    	//先前大于5，或者为0都需要发
    	boolean[] flag = new boolean[9];
        IEquipment[] equs = player.getUsedEquipments();
        for (int i = 0; i < equs.length; i++) {
            if (equs[i] != null) {
            	if(equs[i].isWeapon() || equs[i].isArmor()){
            		int currengDurablity =equs[i].getCurrentDurability(); 
            		if( currengDurablity > (equs[i].getDurability() * 5 / 100 )|| currengDurablity == 0){
            			flag[i] = true;
            		}
            	}
            }
        }
    	return flag;
    }
    //获得减后的标志并返回装备部位
    public static boolean[] getAllDownDuragbility(PlayerData player){
    	boolean[] flag = new boolean[9];
        IEquipment[] equs = player.getUsedEquipments();
        for (int i = 0; i < equs.length; i++) {
            if (equs[i] != null) {
            	if(equs[i].isWeapon() || equs[i].isArmor()){
            		int currengDurablity =equs[i].getCurrentDurability(); 
            		if( currengDurablity <= (equs[i].getDurability() * 5 / 100 ) && currengDurablity >= 0){
            			flag[i] = true;
            		}
            	}
            }
        }
    	return flag;    	                             
    }
    public static void removeDurability(PlayerData player, boolean die,Changed changed) {
    	if(player.getLevel() > 15){
            if (!die) {
                List l = new ArrayList(5);
                IEquipment[] equs = player.getUsedEquipments();
                for (int i = 0; i < equs.length; i++) {
                    if (equs[i] != null) {
                        if (equs[i].isWeapon()) {
                        	if(player.getVipNewLevel() > 0){//vip玩家不消耗武器耐久
                        	}else{
                        		player.removeUsedEquipmentDurability(equs[i], 1,changed);
                        	}
                        } else if (equs[i].isArmor() &&
                                   equs[i].getCurrentDurability() > 0) {
                            l.add(equs[i]);
                        }
                        if(equs[i] != null && (new Date()).getTime() > equs[i].getFAILURE_TIME()){//当日已超过过期日期
                    		//耐久度置为0
                        	if (equs[i].getFAILURE_TIME() != -1){//装备过期当前耐久归0vip玩家不消耗耐久
                        		equs[i].setCurrentDurability((short) 0);
                        		if(player.getVipNewLevel() > 0){
                        		}else{
                        			player.removeUsedEquipmentDurability(equs[i],0,changed);
                        		}
                        	}
                    	}
                    }
                }
                if (l.size() <= 2) {
                    for (int i = 0; i < l.size(); i++) {
                    	if(player.getVipNewLevel() > 0){
                    	}else{
                    		player.removeUsedEquipmentDurability((IEquipment) l.get(i),
                    				1,changed);
                    	}
                    }
                } else {
                    int[] indexes = Utils.getCounts(rnd, 0, l.size()-1, 2);
                    if(player.getVipNewLevel() > 0){
                    }else{
                    	player.removeUsedEquipmentDurability((IEquipment) l.get(indexes[0]), 1,changed);
                    	player.removeUsedEquipmentDurability((IEquipment) l.get(indexes[1]), 1,changed);
                    }
                }
                
                //宠物
                l = new ArrayList(5); 
                Pet mypet = player.getPet();
                if (mypet != null && mypet.getFavor() > 30){
                	Grid[] petequs = mypet.getUsedEquipments();
                	for (int i = 0; i < petequs.length; i++) {
                        if (petequs[i] != null) {
                        	IEquipment petequ = (IEquipment)petequs[i].item;
                            if (petequ.isWeapon()) {
                            	if(player.getVipNewLevel() > 0){
                            	}else{
                            		player.removeUsedEquipmentDurability(petequ, 1,changed);
                            	}
                            } else if (petequ.isArmor() &&
                            		petequ.getCurrentDurability() > 0) {
                                l.add(petequ);
                            }
                            if(petequ != null && (new Date()).getTime() > petequ.getFAILURE_TIME()){//当日已超过过期日期
                        		//耐久度置为0
                            	if (petequ.getFAILURE_TIME() != -1){//装备过期耐久归0vip宠物的装备不掉耐久
                            		petequ.setCurrentDurability((short) 0);//当前耐久度
                            		if(player.getVipNewLevel() > 0){
                            		}else{
                            			player.removeUsedEquipmentDurability(petequ,0,changed);
                            		}
                            	}
                        	}
                            
                        }
                    }
                }
                if (l.size() <= 2) {
                    for (int i = 0; i < l.size(); i++) {
                    	if(player.getVipNewLevel() > 0){
                    	}else{
                    		player.removeUsedEquipmentDurability((IEquipment) l.get(i),
                    				1,changed);
                    	}
                    }
                } else {
                    int[] indexes = Utils.getCounts(rnd, 0, l.size()-1, 2);
                    if(player.getVipNewLevel() > 0){
                    }else{
                    	player.removeUsedEquipmentDurability((IEquipment) l.get(indexes[0]), 1,changed);
                    	player.removeUsedEquipmentDurability((IEquipment) l.get(indexes[1]), 1,changed);
                    }
                }
            } else {
                IEquipment[] equs = player.getUsedEquipments();
                for (int i = 0; i < equs.length; i++) {
                	if(equs[i] != null && (new Date()).getTime() > equs[i].getFAILURE_TIME()){//当日已超过过期日期
                		//耐久度置为0
                		if (equs[i].getFAILURE_TIME() != -1){
                			equs[i].setCurrentDurability((short) 0);//当前耐久度
                			if(player.getVipNewLevel() > 0){
                			}else{
                				player.removeUsedEquipmentDurability(equs[i],0,changed);
                			}
                    	}
                	}
                    if (equs[i] != null && (equs[i].isArmor() || equs[i].isWeapon()) &&
                        equs[i].getCurrentDurability() > 0) {
                        if(player.getVipNewLevel() > 0){
                        }else{
                        	player.removeUsedEquipmentDurability(equs[i],
                        			equs[i].getDurability() / 10,changed);
                        }
                    }
                }
                //宠物
                Pet mypet = player.getPet();
                if (mypet != null && mypet.getFavor() > 30){
                	Grid[] petequs = mypet.getUsedEquipments();
                	for (int i = 0; i < petequs.length; i++) {
                		if (petequs[i]!=null){
                			IEquipment petequ = (IEquipment)petequs[i].item;
                    		if(petequ != null && (new Date()).getTime() > petequ.getFAILURE_TIME()){//当日已超过过期日期
                        		//耐久度置为0
                        		if (petequ.getFAILURE_TIME() != -1){
                        			petequ.setCurrentDurability((short) 0);//当前耐久度
                        			if(player.getVipNewLevel() > 0){
                        			}else{
                        				player.removeUsedEquipmentDurability(petequ,0,changed);
                        			}
                            	}
                        	}
                            if (petequ != null && (petequ.isArmor() || petequ.isWeapon()) &&
                            		petequ.getCurrentDurability() > 0) {
                            	if(player.getVipNewLevel() > 0){
                            	}else{
                            		player.removeUsedEquipmentDurability(petequ,petequ.getDurability() / 10,changed);
                            	}
                            }
                		}
                    }
                }
            }
    	}
    }

    public static int getRepaireMoney(IEquipment equ) {
        return 1 +
                equ.getPrice() * (equ.getDurability() - equ.getCurrentDurability()) /
                (equ.getDurability() * 5);
    }

    public static boolean hasEffect(IItem item,byte type){
        if(item.getType()!=IItem.TYPE_EXTENDED)
            return false;
        Effect[] effects = ((ExtendedItem)item).getEffects();
        if(effects.length==0)
            return false;
        return effects[0].getType()==type;
    }
    public static boolean isFreeEhanceItem(int equItemId , int enhanceTime){
    	if(equItemId == 1001137 || equItemId == 1001138 || equItemId == 1001236 || equItemId == 1001237){
    		if(!(enhanceTime >= 1)){
    			return true;
    		}
    	}
        return false;
    }
    
    public static String splitItemDescSting(String itemDesc){
    	String itemDescDown = itemDesc;
		int numberFlag = 0;
		for(int i = 0; i < itemDescDown.length(); i++){
			char c = itemDescDown.charAt(i);
			int s = itemDescDown.codePointAt(i);
			s = s- 48;
			
			if(s >=0 && s <= 9){
				numberFlag ++;
			}else{
				break;
				
			}
		}
		
		if(numberFlag > 0){
			itemDescDown = itemDescDown.substring(numberFlag);
		}
		
		return itemDescDown;
	}
}
