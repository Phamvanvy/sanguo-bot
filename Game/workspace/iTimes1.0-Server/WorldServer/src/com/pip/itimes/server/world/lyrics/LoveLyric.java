package com.pip.itimes.server.world.lyrics;

public class LoveLyric {
	public static final byte SEX_BOY = 0;
	public static final byte SEX_GIRL = 1;
	public static final byte SEX_BOYGIRL = 2;
	private String singer;
	private String name;
	private String systip;
	private String[] othertip;
	private byte[] sex;
	
	public void setSinger(String singer){
		this.singer = singer;
	}
	
	public String getSinger(){
		return singer;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public void setSysTip(String systip){
		this.systip = systip;
	}
	
	public String getSysTip(){
		return systip;
	}
	
	public void setOtherTip(String[] othertip){
		this.othertip = othertip;
	}
	
	public String[] getOtherTip(){
		return othertip;
	}
	
	public String getOtherTip(int index){
		if(othertip == null) return null;
		return othertip[index];
	}
	
	public int getOtherTipLength(int index){
		if(othertip == null) return 0;
		return othertip[index].length();
	}
	
	public void setSex(byte[] sex){
		this.sex = sex;
	}
	
	public byte[] getSex(){
		return sex;
	}
	
	public byte getSex(int index){
		return sex[index];
	}
}
