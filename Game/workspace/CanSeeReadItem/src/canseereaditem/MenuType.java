package canseereaditem;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class MenuType implements Const{
	/**
	 * 存物品
	 */
	public static Map<Integer, String> items = new HashMap<Integer, String>();

	/**
	 * 用于存装备
	 */
	public static Map<Integer, String> equs = new HashMap<Integer, String>();
	
	/**
	 * 存储技能（宠物和玩家）
	 */
	public static Map<Integer, String> Skills = new HashMap<Integer, String>();
	/**
	 * 存配方
	 */
	public static Map<Integer, String> recipesNew = new HashMap<Integer, String>();
	/**
	 * 宠物颜色
	 */
	public static HashMap<String, String[]> petColor = new HashMap<String, String[]>();
	public int type;
	public String[] titleName;
	public abstract int checkMessage(String s,int k);
	public abstract String splitMessage(String s,int index,int i);
	public abstract boolean parseMessage(String s, String tmps,String strID,int index) throws IOException;
	public long count;	//计数器
	
	protected static void writeText(String s, boolean flag) {
		readitem.writeText(s, flag);
	}
	
	protected static void writeTextln(String s, boolean flag) {
		readitem.writeTextln(s, flag);
	}
	
	protected static byte[] getdata(String s){
		return readitem.getdata(s);
	}
	
	protected void init(){
		count = 0;
	}
}
