package com.pip.itimes.server.bean;

import java.util.Date;

import com.pip.itimes.server.util.PropertyPool;


public class Player implements java.io.Serializable {


    public static final byte SKILL_BLACKSMITHING = 0;
    public static final byte SKILL_ALCHEMY = 1;
    public static final byte SKILL_TAILOR = 2;
    public static final byte SKILL_HERBALISM = 3;
    public static final byte SKILL_HUNTERING = 4;
    public static final byte SKILL_MINING = 5;
    public static final byte SKILL_COOKING = 6;
    public static final byte SKILL_FISHING = 7;


    private Date questionTime;
    private int questionState = 0;

    private int id;
    private int accountId;
    private String playerName;
    private int level;
    private short mapId;
    private short x;
    private short y;
    private byte sex;
    private int exp;
    private byte returnTimes;
    private byte[] data;
    private int moeny;
    private int tongId;
    private String tongName;
    private int tongDuty;
    private String tongTitle;
    private int houseLevel;
    private Date createTime;
    private Date lastLoginTime;
    private int credit;
    private short face;
    private int strength;
    private int agility;
    private int vitality;
    private int intelligence;
    private int luck;
    private int hp;
    private int mp;
    private int leavePoints;
    private short gridSize;
    private int addedGridSize;
    private byte[] abilities;
    private byte[] techSkills;
    private byte[] basicItems;
    private byte[] pets;
    private byte[] options;
    private byte[] metaItems;
    private byte[] equipments;
    private byte[] usedEquipments;
    private byte[] usedPet;
    private byte[] taskItems;
    private byte[] recipes;
    private byte[] chatOptions;
    private byte[] friends;
    private byte[] blackList;
    private byte[] enemys;
    private TaskData taskData;
    private int point;
    private int abilityPoints;
    private int petId;
    private int petSize;
    private int abilityTimes;
    private boolean valid;
    private int messageCount;
    private Date lastMessageTime;
    private String title;
    private int modifyNameTimes;
    private byte[] bufs;
    private short jumpMapId,jumpX,jumpY;
    private Date bathHouseTime;
    private int lastKills;
    private int lastSneaks;
    private int kills;
    private int sneaks;
    private Date vipBathHouseTime;
    private int boxCount;
    private int consumePoint;
    private int contribution;
    private Date islandItemTime;
    private byte gemLightLevel;			// 宝石发光等级
    private long petPracticeMaxTime;	// 宠物修炼最长时间

    //mengjie add top10
    private Date ibuylastTime;
    private Date tonginTime;
    private int arenaV1Id;
    private int arenaV2Id;
    private int arenaV3Id;
    private int arenaLevel;
    private int arenaLevel2;
    private int arenaLevel3;
    private int arenaPoint;
    
    //mengjie add 9options;
    private byte[] key9_options;
    private Date lastlogoutTime;
    
    private byte[] useskill;
    
    private byte camp;
    private int campwin;
    private int camplost;
    private int campcredit;
    
    //yfchen add
    private Date endVoteTime;
    private byte[] image;			//add Jeremy:形象和称号数据放到一起
    //jeremy add
    private int skillPoint2;			//新的打造熟练度

	private byte[] prescription;		//新的打造配方
	
	/**
     * 参数池
     */
    private String playerPool;
    /**
     * 参数池
     */
    private PropertyPool otherPool;

	public Player() {
    }


    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAccountId() {
        return this.accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public short getMapId() {
        return this.mapId;
    }

    public void setMapId(short mapId) {
        this.mapId = mapId;
    }

    public short getX() {
        return this.x;
    }

    public void setX(short x) {
        this.x = x;
    }

    public short getY() {
        return this.y;
    }

    public void setY(short y) {
        this.y = y;
    }
    
    public void setLightLevel(byte gemLightLevel) {
    	this.gemLightLevel = gemLightLevel;
    }
    
    public byte getLightLevel() {
    	return this.gemLightLevel;
    }
    
    public void setPetPracticeMaxTime (long petPracticeMaxTime) {
    	this.petPracticeMaxTime = petPracticeMaxTime;
    }
    
    public long getPetPracticeMaxTime () {
    	return this.petPracticeMaxTime;
    }
    
    public byte getSex() {
        return this.sex;
    }

    public void setSex(byte sex) {
        this.sex = sex;
    }

    public int getExp() {
        return this.exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public byte getReturnTimes() {
        return this.returnTimes;
    }

    public void setReturnTimes(byte returnTimes) {
        this.returnTimes = returnTimes;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getMoeny() {
        return this.moeny;
    }

    public void setMoeny(int moeny) {
        this.moeny = moeny;
    }


    public int getTongId() {
        return this.tongId;
    }

    public void setTongId(int tongId) {
        this.tongId = tongId;
    }

    public String getTongName() {
        return this.tongName;
    }

    public void setTongName(String tongName) {
        this.tongName = tongName;
    }

    public int getTongDuty() {
        return this.tongDuty;
    }

    public void setTongDuty(int tongDuty) {
        this.tongDuty = tongDuty;
    }

    public int getHouseLevel() {
        return this.houseLevel;
    }

    public void setHouseLevel(int houseLevel) {
        this.houseLevel = houseLevel;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastLoginTime() {
        return this.lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public int getCredit() {
        return this.credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public short getFace() {
        return this.face;
    }

    public void setFace(short face) {
        this.face = face;
    }

    public int getStrength() {
        return this.strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getAgility() {
        return this.agility;
    }

    public void setAgility(int agility) {
        this.agility = agility;
    }

    public int getVitality() {
        return this.vitality;
    }

    public void setVitality(int vitality) {
        this.vitality = vitality;
    }

    public int getIntelligence() {
        return this.intelligence;
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    public int getLuck() {
        return this.luck;
    }

    public void setLuck(int luck) {
        this.luck = luck;
    }

    public int getHp() {
        return this.hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMp() {
        return this.mp;
    }

    public void setMp(int mp) {
        this.mp = mp;
    }

    //剩余的属性点
    public int getLeavePoints() {
        return this.leavePoints;
    }

    public void setLeavePoints(int leavePoints) {
        this.leavePoints = leavePoints;
    }

    public byte[] getAbilities() {
        return this.abilities;
    }

    public void setAbilities(byte[] abilities) {
        this.abilities = abilities;
    }

    public byte[] getTechSkills() {
        return this.techSkills;
    }

    public void setTechSkills(byte[] techSkills) {
        this.techSkills = techSkills;
    }

    public byte[] getBasicItems() {
        return this.basicItems;
    }

    public void setBasicItems(byte[] basicItems) {
        this.basicItems = basicItems;
    }

    public byte[] getPets() {
        return this.pets;
    }

    public void setPets(byte[] pets) {
        this.pets = pets;
    }

    public byte[] getOptions() {
        return this.options;
    }

    public void setOptions(byte[] options) {
        this.options = options;
    }

    public byte[] getMetaItems() {
        return this.metaItems;
    }

    public void setMetaItems(byte[] metaItems) {
        this.metaItems = metaItems;
    }

    public byte[] getEquipments() {
        return this.equipments;
    }

    public void setEquipments(byte[] equipments) {
        this.equipments = equipments;
    }

    public byte[] getUsedEquipments() {
        return this.usedEquipments;
    }

    public void setUsedEquipments(byte[] usedEquipments) {
        this.usedEquipments = usedEquipments;
    }

    public byte[] getUsedPet() {
        return this.usedPet;
    }

    public void setUsedPet(byte[] usedPet) {
        this.usedPet = usedPet;
    }

    public byte[] getTaskItems() {
        return this.taskItems;
    }

    public void setTaskItems(byte[] taskItems) {
        this.taskItems = taskItems;
    }

    public byte[] getRecipes() {
        return this.recipes;
    }

    public void setRecipes(byte[] recipes) {
        this.recipes = recipes;
    }

    public void setChatOptions(byte[] chatOptions) {
        this.chatOptions = chatOptions;
    }

    public byte[] getChatOptions() {
        return chatOptions;
    }

    public void setTaskData(TaskData taskData) {
        this.taskData = taskData;
    }


    public TaskData getTaskData() {
        return taskData;
    }

    public void setGridSize(short gridSize) {
        this.gridSize = gridSize;
    }

    public short getGridSize() {
        return gridSize;
    }

    public int getAddedGridSize() {
        return addedGridSize;
    }

    public void setAddedGridSize(int addedGridSize) {
        this.addedGridSize = addedGridSize;
    }

    public void setFriends(byte[] friends) {
        this.friends = friends;
    }

    public byte[] getFriends() {
        return friends;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public int getAbilityPoints() {
        return abilityPoints;
    }

    public void setAbilityPoints(int abilityPoints) {
        this.abilityPoints = abilityPoints;
    }

    public void setTongTitle(String tongTitle) {
        this.tongTitle = tongTitle;
    }

    public String getTongTitle() {
        return tongTitle;
    }

    public void setPetId(int petId){
        this.petId = petId;
    }

    public int getPetId(){
        return petId;
    }

    public void setPetSize(int petSize){
        this.petSize = petSize;
    }

    public int getPetSize(){
        return petSize;
    }

    public void setAbilityTimes(int abilityTimes){
        this.abilityTimes = abilityTimes;
    }

    public int getAbilityTimes(){
        return abilityTimes;
    }

    public void setValid(boolean valid){
        this.valid = valid;
    }

    public boolean getValid(){
        return valid;
    }

    public void setMessageCount(int count){
        this.messageCount = count;
    }

    public int getMessageCount(){
        return messageCount;
    }

    public void setLastMessageTime(Date time){
        this.lastMessageTime = time;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setModifyNameTimes(int modifyNameTimes) {
        this.modifyNameTimes = modifyNameTimes;
    }

    public void setBlackList(byte[] blackList) {
        this.blackList = blackList;
    }

    public void setBufs(byte[] bufs) {
        this.bufs = bufs;
    }

    public void setJumpY(short jumpY) {
        this.jumpY = jumpY;
    }

    public void setJumpX(short jumpX) {
        this.jumpX = jumpX;
    }

    public void setJumpMapId(short jumpMapId) {
        this.jumpMapId = jumpMapId;
    }

    public void setBathHouseTime(Date bathHouseTime) {
        this.bathHouseTime = bathHouseTime;
    }

    public Date getLastMessageTime(){
        return lastMessageTime;
    }

    public String getTitle() {
        return title;
    }

    public int getModifyNameTimes() {
        return modifyNameTimes;
    }

    public byte[] getBlackList() {
        return blackList;
    }

    public byte[] getBufs() {
        return bufs;
    }

    public short getJumpY() {
        return jumpY;
    }

    public short getJumpX() {
        return jumpX;
    }

    public short getJumpMapId() {
        return jumpMapId;
    }

    public Date getBathHouseTime() {
        return bathHouseTime;
    }

    public void setQuestionTime(Date question) {
    	this.questionTime = question;
    }

    public Date getQuestionTime() {
    	return questionTime;
    }

    public void setQuestionState(int questionState) {
    	this.questionState = questionState;
    }

    public int getQuestionState() {
    	return questionState;
    }

    public void setLastKills(int lastKills){
        this.lastKills = lastKills;
    }

    public int getLastKills(){
        return lastKills;
    }

    public void setLastSneaks(int lastSneaks){
        this.lastSneaks = lastSneaks;
    }

    public int getLastSneaks(){
        return lastSneaks;
    }

    public void setKills(int kills){
        this.kills = kills;
    }

    public int getKills(){
        return this.kills;
    }

    public void setSneaks(int sneaks){
        this.sneaks = sneaks;
    }

    public void setVipBathHouseTime(Date vipBathHouseTime) {
        this.vipBathHouseTime = vipBathHouseTime;
    }

    public void setEnemys(byte[] enemys) {
        this.enemys = enemys;
    }

    public void setBoxCount(int boxCount) {
        this.boxCount = boxCount;
    }

    public void setContribution(int contribution) {
        this.contribution = contribution;
    }

    public void setConsumePoint(int consumePoint) {
        this.consumePoint = consumePoint;
    }

    public void setIslandItemTime(Date islandItemTime) {
        this.islandItemTime = islandItemTime;
    }

    public int getSneaks(){
        return this.sneaks;
    }

    public Date getVipBathHouseTime() {
        return vipBathHouseTime;
    }

    public byte[] getEnemys() {
        return enemys;
    }

    public int getBoxCount() {
        return boxCount;
    }

    public int getContribution() {
        return contribution;
    }

    public int getConsumePoint() {
        return consumePoint;
    }

    public Date getIslandItemTime() {
        return islandItemTime;
    }
    
	public Date getIbuylastTime() {
		return ibuylastTime;
	}
	public void setIbuylastTime(Date ibuylastTime) {
		this.ibuylastTime = ibuylastTime;
	}


	public Date getTonginTime() {
		return tonginTime;
	}


	public void setTonginTime(Date tonginTime) {
		this.tonginTime = tonginTime;
	}


	public int getArenaV1Id() {
		return arenaV1Id;
	}


	public void setArenaV1Id(int arenaV1Id) {
		this.arenaV1Id = arenaV1Id;
	}


	public int getArenaV2Id() {
		return arenaV2Id;
	}


	public void setArenaV2Id(int arenaV2Id) {
		this.arenaV2Id = arenaV2Id;
	}


	public int getArenaV3Id() {
		return arenaV3Id;
	}


	public void setArenaV3Id(int arenaV3Id) {
		this.arenaV3Id = arenaV3Id;
	}


	public int getArenaLevel() {
		return arenaLevel;
	}


	public void setArenaLevel(int arenaLevel) {
		this.arenaLevel = arenaLevel;
	}


	public int getArenaLevel2() {
		return arenaLevel2;
	}


	public void setArenaLevel2(int arenaLevel2) {
		this.arenaLevel2 = arenaLevel2;
	}


	public int getArenaLevel3() {
		return arenaLevel3;
	}


	public void setArenaLevel3(int arenaLevel3) {
		this.arenaLevel3 = arenaLevel3;
	}


	public int getArenaPoint() {
		return arenaPoint;
	}


	public void setArenaPoint(int arenaPoint) {
		this.arenaPoint = arenaPoint;
	}


	public Date getLastlogoutTime() {
		return lastlogoutTime;
	}


	public void setLastlogoutTime(Date lastlogoutTime) {
		this.lastlogoutTime = lastlogoutTime;
	}


	public byte[] getUseskill() {
		return useskill;
	}


	public void setUseskill(byte[] useskill) {
		this.useskill = useskill;
	}


	public byte[] getKey9_options() {
		return key9_options;
	}


	public void setKey9_options(byte[] key9Options) {
		key9_options = key9Options;
	}


	public byte getCamp() {
		return camp;
	}
	public void setCamp(byte camp) {
		this.camp = camp;
	}
	public int getCampwin() {
		return campwin;
	}
	public void setCampwin(int campwin) {
		this.campwin = campwin;
	}
	public int getCamplost() {
		return camplost;
	}
	public void setCamplost(int camplost) {
		this.camplost = camplost;
	}
	public int getCampcredit() {
		return campcredit;
	}
	public void setCampcredit(int campcredit) {
		this.campcredit = campcredit;
	}
	public Date getEndVoteTime() {
		return endVoteTime;
	}
	
	public void setEndVoteTime(Date endVoteTime) {
		this.endVoteTime = endVoteTime;
	}


	public byte[] getImage() {
		return image;
	}


	public void setImage(byte[] image) {
		this.image = image;
	}
	/**
	 *新的打造配方
	 * @return
	 */
	public byte[] getPrescription() {
		return prescription;
	}


	public void setPrescription(byte[] prescription) {
		this.prescription = prescription;
	}
	
    public int getskillPoint2() {
		return skillPoint2;
	}

	public void setskillPoint2(int skillPoint2) {
		this.skillPoint2 = skillPoint2;
	}
	
	public String getPlayerPool () {
        return playerPool;
    }

    public void setPlayerPool (String playerPool) {
        this.playerPool = playerPool;
    }
    
    public PropertyPool getOtherPool () {
    	return otherPool;
    }
    public void setOtherPool (PropertyPool otherPool) {
    	this.otherPool = otherPool;
    }
}