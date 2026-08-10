package com.pip.itimes.server.stage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PhizTitleData {
	private short index; //表情称号索引
	private byte type; //表情称号类型
	private String name; //表情称号名字
	
	private static Map<Short,PhizTitleData> phizMap = new HashMap<Short,PhizTitleData>();//存放所有表情
	
	public static void addPhizTitle(short index,PhizTitleData phiz){
		phizMap.put(index, phiz);
	}
	
	public static PhizTitleData getPhizTitle(short index){
		return phizMap.get(index);
	}
	
	public static int getPhizMapSize(){
		return phizMap.size();
	}
	
	public static Iterator<Map.Entry<Short, PhizTitleData>> getPhizMapIterator(){
		return phizMap.entrySet().iterator();
	}
	
	public static String getPhizTitleName(short index){
		PhizTitleData tmpPhiz = phizMap.get(index);
		if(tmpPhiz==null)
			return null;
		return tmpPhiz.getName();
	}
	
	public static byte getPhizTitleType(short index){
		if(index == 0)
			return 1;
		PhizTitleData tmpPhiz = phizMap.get(index);
		if(tmpPhiz==null)
			return 0;
		return tmpPhiz.getType();
	}
	
	public static boolean checkPhizTitle(short index){
		PhizTitleData tmpPhiz = phizMap.get(index);
		return tmpPhiz!=null;
	}
	
	public PhizTitleData(short index,byte type,String name){
		this.index = index;
		this.type = type;
		this.name = name;
	}
	
	public short getIndex(){
		return index;
	}
	
	public byte getType(){
		return type;
	}
	
	public String getName(){
		return name;
	}
	
	public void setIndex(int index){
		this.index = (short)index;
	}
	
	public void setType(byte type){
		this.type = type;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public byte[] toDbBytes(){
		try{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        DataOutputStream dos = new DataOutputStream(bos);
	        dos.writeShort(index);
	        return bos.toByteArray();
		}catch(Exception e){
			return new byte[0];
		}
	}
	
	
}
