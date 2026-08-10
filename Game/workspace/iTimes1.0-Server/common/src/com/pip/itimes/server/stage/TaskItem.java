package com.pip.itimes.server.stage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.*;

/**
 * @author Jeffery
 * @version 1.0
 */

public class TaskItem implements ITaskItem {

    private String name;
    private short taskId;
    private int itemId;
    private int max;
    private String desc;


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
    
    public TaskItem(){

    }

    public void setTaskId(short taskId){
        this.taskId = taskId;
    }

    public short getTaskId() {
        return taskId;
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
        return 1;
    }

    public void setMax(int max){
        this.max = max;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getMax(){
        return max;
    }

    public String getDesc() {
        return desc;
    }
    
    private byte quality;
    
    public byte getQuality(){
        return quality;
    }
    
    public void setQuality(byte quality){
    	this.quality = quality;
    }
    public boolean isBinded(){
        return true;
    }

    public void setBinded(boolean binded){

    }

    public byte getBindType(){
        return IItem.BIND_GET;
    }


    public byte[] toClientBytesWithLevel(int level){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeUTF(getName());
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
            dos.writeUTF(getName());
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
