package com.pip.itimes.server.stage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.DataOutputStream;

/**
 * @author Jeffery
 * @version 1.0
 */
public class ExtendedItem implements IValuableItem,IEffectItem {

    private String name;
    private int price;
    private int itemId;
    private byte bindType;
    private boolean canUse;
    private boolean autoUse;
    private String autoUseMessage;
    private String desc;
    private Effect[] effects;

    public ExtendedItem() {
    }

    public void setItemId(int id){
        this.itemId = id;
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
        return 2;
    }

    public void setPrice(int price){
        this.price = price;
    }

    public int getPrice() {
        return price;
    }

    public Effect[] getEffects(){
        return effects;
    }

    public void setEffects(Effect[] effects){
        this.effects = effects;
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

    public void setBinded(boolean bind){
    }

    public void setCanUse(boolean canUse){
        this.canUse = canUse;
    }
    
    public void setAutoUse(boolean autoUse){
        this.autoUse = autoUse;
    }

    public void setAutoUseMessage(String autoUseMessage) {
        this.autoUseMessage = autoUseMessage;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean getCanUse(){
        return canUse;
    }
    
    public boolean getAutoUse(){
        return autoUse;
    }
    
    public String getAutoUseMessage(){
        return autoUseMessage;
    }

    public String getDesc() {
        return desc;
    }
    
    private byte quality;
    
    public byte getQuality(){
        return this.quality;
    }
    
    public void setQuality(byte quality){
    	this.quality = quality;
    }

	public byte getItemShowType() {
		return itemShowType;
	}

	public void setItemShowType(byte itemShowType) {
		this.itemShowType = itemShowType;
	}

    /**
     * 物品是否单独成列
     */
    private byte itemShowType;
    
    public byte[] toClientBytesWithLevel(int level){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(itemId);
            dos.writeInt(price);
            dos.writeUTF(name);
            dos.writeByte((bindType<<7)|(canUse?1:0));
            return bos.toByteArray();
        } catch (IOException ex) {
            return new byte[0];
        }
    }
    
    
    /**
     * @param dataVersion
     * @return新版的下发
     */
    public byte[] toClientBytes(int dataVersion){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(itemId);
            dos.writeInt(price);
            dos.writeUTF(name);
            dos.writeByte((bindType<<7)|(canUse?1:0));
            if(dataVersion > 0){ //新版本
	            dos.writeByte(itemShowType);
	            dos.writeByte(quality);
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
}
