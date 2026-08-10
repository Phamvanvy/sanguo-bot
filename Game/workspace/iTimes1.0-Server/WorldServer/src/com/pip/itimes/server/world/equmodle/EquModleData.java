package com.pip.itimes.server.world.equmodle;

public class EquModleData {
	private int id = 0;
	private int diamondcount = 0;
	private int viany_stone = 0;
	private int viany_scissors = 0;
	private int viany_paper = 0;
	/**
     * 最高的孔数量
     */
    public final static byte MaxDiamondRoleCount = 5;
    /**
     * 当前的孔位信息 0为没有打孔     1为已经打孔了没有镶嵌   2为已经打孔了也镶嵌了
     */
	private byte[] diamondMosiacRoleInfo = new byte[MaxDiamondRoleCount];
	private int[] diamondStoneId = new int[MaxDiamondRoleCount];
	/**
	 * 当前模板开放的默认打孔数量
	 */
	private byte openDiamondCount;
	
	/**
	 * 长度9*2 格式为:type value
	 */
	private int[] enchances;
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
	
	public void setDiamondcount(int diamondcount){
		this.diamondcount = diamondcount;
	}
	
	public int getDiamondcount(){
		return diamondcount;
	}
	
	public void setVianystone(int viany_stone){
		this.viany_stone = viany_stone;
	}
	
	public int getVianystone(){
		return viany_stone;
	}
	
	public void setVianyscissors(int viany_scissors){
		this.viany_scissors = viany_scissors;
	}
	
	public int getVianyscissors(){
		return viany_scissors;
	}
	
	public void setVianypaper(int viany_paper){
		this.viany_paper = viany_paper;
	}
	
	public int getVianypaper(){
		return viany_paper;
	}
	
	public void setDiamondMosiacRoleInfo(int index, byte level){
		diamondMosiacRoleInfo[index] = level;
	}
	
	public byte[] getDiamodMosiacRoleInfo(){
		byte[] roleinfo = new byte[diamondMosiacRoleInfo.length];
		System.arraycopy(diamondMosiacRoleInfo, 0, roleinfo, 0, roleinfo.length);
		return roleinfo;
	}
	
	public byte getOpenDiamondCount() {
		return openDiamondCount;
	}

	public void setOpenDiamondCount(byte opendDiamondCount) {
		this.openDiamondCount = opendDiamondCount;
	}
	
	public int[] getEnchances(){
		int[] enchancescopy = new int[enchances.length];
		System.arraycopy(enchances, 0, enchancescopy, 0, enchancescopy.length);
		return enchancescopy;
	}
	
	public void setEnchances(int[] enchances){
		this.enchances = enchances;
	}
	
	public void setDiamondStoneId(int index, int id){
		diamondStoneId[index] = id;
	}
	
	public int getDiamondStoneId(int index){
		return diamondStoneId[index];
	}
}
