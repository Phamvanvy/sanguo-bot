package com.pip.itimes.server.stage;

import java.util.Map;
import java.util.HashMap;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Jeffery
 * @version 1.0
 */
public class BasicItem implements IBasicItem{


    private byte function;
    private String name;
    private int price;
    private int itemId;
    private byte bindType;
    private boolean binded;
    private Effect[] effects;
    private String desc;


	public void setQuarlity(byte quarlity) {
		this.quarlity = quarlity;
	}

	private byte itemType;
	

	private byte quarlity;

    
    public BasicItem() {
    }

    public void setFunction(byte function){
        this.function = function;
    }

    public byte getFunction() {
        return function;
    }

    public void setItemId(int itemId){
        this.itemId = itemId;
    }

    public int getItemId(){
        return itemId;
    }


    public int getId() {
        return 0;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }



    public byte getType() {
        return 0;
    }

    public void setPrice(int price){
        this.price = price;
    }

    public byte getBindType(){
        return bindType;
    }

    public void setBindType(byte bindType){
        this.bindType = bindType;
    }

    public boolean isBinded(){
        return bindType!=IItem.BIND_NO;
    }

    public void setBinded(boolean binded){
        this.binded = binded;
    }

    public int getPrice() {
        return price;
    }

    public byte getQuality(){
        return 0;
    }

    public Effect[] getEffects(){
        return effects;
    }

    public void setEffects(Effect[] effects){
        this.effects = effects;
    }

    public String getDesc(){
        return desc;
    }

    public void setDesc(String desc){
        this.desc = desc;
    }

    /* (non-Javadoc)
     * @see com.pip.itimes.server.stage.IItem#toClientBytes()
     * 原先的机制，
     */
    public byte[] toClientBytesWithLevel(int level){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeByte(function);
            dos.writeInt(price);
            dos.writeUTF(name);
            
            boolean canUse = effects.length == 0? false: true;
            dos.writeByte((bindType<<7)|(canUse?1:0));
            
            int effectType = -1;
            int effectValue = 0;
            
            for(int i = 0; i < effects.length; i++){
                if(effects[i].getType() != 1){
                    continue;
                }
                
                PropertyEffect effect = (PropertyEffect)effects[i];
                
                if(effect.getProperty() == Changed.HP){
                    if(effectType > 0){
                        effectType = 2;
                    }else{
                        effectType = 0;
                    }
                }else if(effect.getProperty() == Changed.MP){
                    if(effectType < 0){
                        effectType = 1;
                    }else{
                        effectType = 2;
                    }
                }
                
                effectValue = effect.getValue();
            }
            
            if(canUse){
                dos.writeByte(effectType);
                dos.writeInt(effectValue);
            }
            
            return bos.toByteArray();
        } catch (IOException ex) {
            return new byte[0];
        }
    }
    /* (non-Javadoc)
     * @see com.pip.itimes.server.stage.IItem#toClientBytes()
     * 按版本区分下发的字节
     */
    public byte[] toClientBytes(int dataVersion){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeByte(function);
            dos.writeInt(price);
            dos.writeUTF(name);
            
            boolean canUse = effects.length == 0? false: true;
            dos.writeByte((bindType<<7)|(canUse?1:0));
            
            int effectType = -1;
            int effectValue = 0;
            
            for(int i = 0; i < effects.length; i++){
                if(effects[i].getType() != 1){
                    continue;
                }
                
                PropertyEffect effect = (PropertyEffect)effects[i];
                
                if(effect.getProperty() == Changed.HP){
                    if(effectType > 0){
                        effectType = 2;
                    }else{
                        effectType = 0;
                    }
                }else if(effect.getProperty() == Changed.MP){
                    if(effectType < 0){
                        effectType = 1;
                    }else{
                        effectType = 2;
                    }
                }
                
                effectValue = effect.getValue();
            }
            
            if(canUse){
                dos.writeByte(effectType);
                dos.writeInt(effectValue);
            }
            if(dataVersion > 0){ //新版本
            	dos.writeByte(itemType);
            	dos.writeByte(quarlity);
            }
            return bos.toByteArray();
        } catch (IOException ex) {
            return new byte[0];
        }
    }
    public byte[] toDbBytes(){
        byte[] bytes = new byte[4];
        bytes[0] = (byte)(itemId>>24);
        bytes[1] = (byte)(itemId>>16);
        bytes[2] = (byte)(itemId>>8);
        bytes[3] = (byte)itemId;
        return bytes;
    }

	public byte getItemShowType() {
		// TODO Auto-generated method stub
		return itemType;
	}

	public void setItemShowType(byte itemShowType) {
		// TODO Auto-generated method stub
		this.itemType = itemShowType;
	}
}
