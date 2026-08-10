package com.pip.itimes.server.stage;

import java.util.Enumeration;
import java.util.Hashtable;

import com.pip.itimes.server.util.Utils;

public class PetColor {
	private static Hashtable<String,PetColor> petColor = new Hashtable<String,PetColor>();
	
	private byte type;//类型
	
	private byte bindType;//几代宠   0:1代；1：2代
	
	private short[] random;//随机颜色
	
	private short[]	fixed;//指定分配的颜色
	
	private Hashtable<Integer,Short> items; //变色道具
	
	private String[] colors;
	
	private short[] petSynthetizeColor;//1代宠作为主宠进行宠物合成后的颜色
	
	public PetColor(byte type,byte bindType,short[] random, short[] fixed,String[] colors){
		this.type = type;
		this.bindType = bindType;
		this.random = random;
		this.fixed = fixed;
		this.colors = colors;
		items = new Hashtable<Integer,Short>();
		petSynthetizeColor = null;
	}
	
	public byte getType(){
		return type;
	}
	
	public byte getBindType(){
		return bindType;
	}
	
	public short[] getRandomArray(){
		return random;
	}
	
	public short[] getFixedArray(){
		return fixed;
	}
	
	public void setPetSynthetizeColor(short[] petSynColor){
		petSynthetizeColor = null;
		petSynthetizeColor = petSynColor;
	}
	
	public short getPetSynthetizeColorIndex(short curIndex){
		if(petSynthetizeColor!=null){
			if(curIndex < petSynthetizeColor.length){
				return petSynthetizeColor[curIndex];
			}
			return 0;
		}
		return curIndex;
	}
	
	public short getRandomIndex(){
		int len = random.length;
		if(len>0)
			return random[Utils.getRandom(0, len-1)];
		return 0;
	}
	
	public String getColorString(short index){
		if(index<colors.length){
			return colors[index];
		}
		return "默认";
	}
	
	public String[] getRandomColorStringArray(){
		if(random.length>0){
			String[] str = new String[random.length];
			for(int i=0;i<random.length;i++){
				str[i]=getColorString(random[i]);
			}
			return str;
		}
		return null;
	}
	
	public String getRandomColorString(){
		if(random.length > 0){
			StringBuilder sb = new StringBuilder();
			for(int i=0;i<random.length;i++){
				sb.append(getColorString(random[i]) + "、");
			}
			sb.deleteCharAt(sb.length()-1);
			return sb.toString();
		}
		return "";
	}
	
	public boolean checkItemID(int itemID){
		return items.containsKey(itemID);
	}
	
	public boolean checkColorIndex(short index){
		for(int i=0;i<fixed.length;i++){
			if(index==fixed[i])
				return true;
		}
		for(int i=0;i<random.length;i++){
			if(index==random[i])
				return true;
		}
		return false;
	}
	
	public void addItem(int itemID,short colorIndex){
		items.put(itemID, colorIndex);
	}
	
	public short getColorIndex(int itemID){
		if(items.containsKey(itemID)){
			return items.get(itemID).shortValue();
		}
		return 0;
	}
	
	public int[] getItemIDArray(){
		Enumeration<Integer> emu = items.keys();
		int size = items.size();
		int[] array = new int[size];
		int i = 0;
		while(emu.hasMoreElements() && i < size){
			array[i] = emu.nextElement().intValue();
			i++;
		}
		return array; 
	}
	
	public int getItemsSize(){
		return items.size();
	}
	
	public static void addPetColor(PetColor petC){
		petColor.put(petC.type + "_" + petC.bindType, petC);
	}
	
	public static PetColor getPetColor(String key){
		return petColor.get(key);
	}
	
	public static PetColor getPetColor(byte type,byte bindType){
		return petColor.get(type+"_"+bindType);
	}
	
	public static int getItemsSize(byte type,byte bindType){
		PetColor petC = getPetColor(type,bindType);
		if(petC!=null){
			return petC.getItemsSize();
		}
		return 0;
	}
	
	public static int[] getItemIDArray(byte type,byte bindType){
		PetColor petC = getPetColor(type,bindType);
		if(petC!=null){
			return petC.getItemIDArray();
		}
		return new int[0];
	}
	
	public static short getRandomIndex(byte type,byte bindType){
		PetColor petC = getPetColor(type,bindType);
		if(petC!=null){
			return petC.getRandomIndex();
		}
		return 0;
	}
	
	public static String[] getRandomColorString(byte type,byte bindType){
		PetColor petC = getPetColor(type,bindType);
		if(petC!=null){
			return petC.getRandomColorStringArray();
		}
		return null;
	}
	
	public static short getColorIndex(byte type,byte bindType,int itemID){
		PetColor petC = getPetColor(type,bindType);
		if(petC != null){
			return petC.getColorIndex(itemID);
		}
		return 0;
	}
	
	public static boolean checkColorIndex(byte type,byte bindType,short index){
		PetColor petC = getPetColor(type,bindType);
		if(petC!=null){
			return petC.checkColorIndex(index);
		}
		return false;
	}
	
	public static short getPetSynthetizeColorIndex(byte type,byte bindType,short curIndex){
		if(bindType == 0){
			PetColor petC = getPetColor(type,bindType);
			if(petC != null){
				return petC.getPetSynthetizeColorIndex(curIndex);
			}
			return 0;
		}
		return curIndex;
	}
}
