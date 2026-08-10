package com.pip.itimes.server.bean;

import java.util.Date;

import com.pip.itimes.server.stage.Pet;



public class Petmanager  implements java.io.Serializable {

	private int playerId;
    private int id;
    private int petId;
    private byte[] pet;
    private int stone;	//petversion >= 4 改为需要修炼的时间1-5小时
    /**
     * petversion >= 4
     */
    // 宠物开始修炼的时间
    private Date eattime;
    
    /**
     * petversion >= 4将寄养宠物改造成宠物修炼
     */
    // 宠物修炼持续的时间(最多5个小时收益)
    private long practiceTime;
    
    public static int petcount = 3;//最大寄养宠物数量
    public static int stoneid = 211002;//喂养宠物所需要物品id
    public static int stonecount = 1;//喂养宠物所需要物品数量
    public static String stonename = "精炼石";//喂养宠物所需要物品名称
    public static int expforbathhouse = 8;//1次获得相当于澡票的经验
    public Petmanager() {
    }
    public void setpetdata(Pet pet){
    	this.petId = pet.getId();
    	byte[] petbyte  = pet.toDbBytes_version6(Pet.CURRENT_VERSION);
    	byte[] petbyte_new  = new byte[petbyte.length + 1];
    	System.arraycopy(petbyte, 0, petbyte_new, 1, petbyte.length);
    	petbyte_new[0]=Pet.CURRENT_VERSION;//petversion 更新后改动！增加宠物培养
        this.pet = petbyte_new;
    }
	public int getPlayerId() {
		return playerId;
	}
	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getPetId() {
		return petId;
	}
	public void setPetId(int petId) {
		this.petId = petId;
	}
	public byte[] getPet() {
		return pet;
	}
	public void setPet(byte[] pet) {
		this.pet = pet;
	}
	public int getStone() {
		return stone;
	}
	public void setStone(int stone) {
		this.stone = stone;
	}
	public Date getEattime() {
		return eattime;
	}
	public void setEattime(Date eattime) {
		this.eattime = eattime;
	}
    public void setPracticeTime (long practiceTime) {
    	this.practiceTime = practiceTime;
    }
    public long getPracticeTime () {
    	return practiceTime;
    }
}
