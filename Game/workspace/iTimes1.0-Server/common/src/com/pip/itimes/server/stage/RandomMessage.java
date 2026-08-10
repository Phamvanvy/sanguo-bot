package com.pip.itimes.server.stage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Vector;

import com.pip.itimes.server.util.Utils;

public class RandomMessage {
	/** 存放随机喊话的map，key值在randomMessage.xml中配置*/
	public static LinkedHashMap<String, ArrayList<RandomMessage>> randomMessageMap = new LinkedHashMap<String, ArrayList<RandomMessage>>();
	
	/** 存放性别对应的喊话索引， Vector[0]:boy, Vector[1]:girl*/
	public static LinkedHashMap<String, Vector<Integer>[]> sexIndexMap = new LinkedHashMap<String, Vector<Integer>[]>();
	
	byte sex;
	String message;
	
	public RandomMessage(byte sex,String message){
		this.sex = sex;
		this.message = message;
	}
	
	public byte getSex(){
		return sex;
	}
	
	public String getMessage(){
		return message;
	}
	
	public static void addRandomMessage(String key, RandomMessage rm){
		if(randomMessageMap.get(key) == null){
			ArrayList<RandomMessage> list = new ArrayList<RandomMessage>();
			list.add(rm);
			randomMessageMap.put(key, list);
		}else{
			randomMessageMap.get(key).add(rm);
		}
		int index = randomMessageMap.get(key).size()-1;
		addSexIndex(key,rm,index);
	}
	
	public static void addSexIndex(String key,RandomMessage rm,int index){
		if(sexIndexMap.get(key) == null){
			Vector[] tmpVector = new Vector[2];
			tmpVector[0] = new Vector<Integer>();
			tmpVector[1] = new Vector<Integer>();
			sexIndexMap.put(key, tmpVector);
		}
		Vector<Integer>[] vecSexIndex = sexIndexMap.get(key);
		switch(rm.getSex()){
		case Utils.SEX_BOY:
			vecSexIndex[rm.getSex()].add(index);
			break;
		case Utils.SEX_GIRL:
			vecSexIndex[rm.getSex()].add(index);
			break;
		default:
			vecSexIndex[0].add(index);
			vecSexIndex[1].add(index);
			break;
		}
	}
	
	public static String getRandomMessage(String key, byte sex,String playername){
		if(randomMessageMap.get(key)==null || sexIndexMap.get(key) == null){
			return "";
		}
		ArrayList<RandomMessage> randomMessageList = randomMessageMap.get(key);
		Vector<Integer>[] vecSexIndex = sexIndexMap.get(key);
		int max = vecSexIndex[sex].size() - 1;
		int ranInt = Utils.getRandom(0, max);
		int index = vecSexIndex[sex].get(ranInt).intValue();
		if(index>=randomMessageList.size()){
			index = 0;
		}
		String ranMessage = randomMessageList.get(index).getMessage();
		if(ranMessage.contains("player")){
			return ranMessage.replace("player", playername);
		}else{
			return ranMessage;
		}
	}
}
