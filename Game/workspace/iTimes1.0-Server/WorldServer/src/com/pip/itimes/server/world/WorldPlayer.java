package com.pip.itimes.server.world;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.gift.GiftData;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.DiamondShineBuf;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.MagicPosMessage;
import com.pip.itimes.server.stage.PlayerData;
import com.pip.itimes.server.stage.RandomQuestion;
import com.pip.itimes.server.stage.WorldPlayerKillMg;
import com.pip.itimes.server.util.IntHashSet;
import com.pip.itimes.server.util.TestRandom.RandomMake;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.fee.ChargePlan;
import com.pip.itimes.server.world.fee.FeePlan;
import com.pip.itimes.server.world.game.*;
import com.pip.itimes.server.world.unline.UnlineExpConfig;
import com.pip.itimes.server.stage.Buf;
import com.pip.itimes.server.suit.SuitEffect;
import com.pip.itimes.server.suit.Suits;

/**
 * @author Jeffrey
 * @version 1.0
 */
/**
 * @author wpjiang
 *
 */
public class WorldPlayer extends PlayerData implements ILockOwner, PositionSprite, IPlayerData{

    public final static int OFFLINE = 0;
    public final static int ONLINE = 1;

    public final static int TEAM_NONE = -1;
    public final static int TEAM_NORMAL = 0;
    public final static int TEAM_FOLLOW = 1;
   

	private Team team;
    private List locks = new ArrayList(3);
    private int state = OFFLINE;
    private volatile int ref = 0;
    private GameMap map;
    private int teamState = -1;
    private long longIMoney;
    private long bBalance;	//游戏B币

//    private int monthFee;
//    private boolean needPay;
    private boolean isMonth;
    private boolean isSubscribe;
    private long lastLifeTime = 0;
    private long lastFeeTime = 0;
    private long positionTime = 0;
    private IntHashSet positionHistory = new IntHashSet(50);
    private String model;
    private String accountName;
    private int color;
    private boolean inBattle;
    private String key;
    private String password;
    private String phone;
    private int modifyPasswordTimes;
    private boolean isFirstEnter;
    private int deadTime;
    private boolean needRefreshPosition;
    private boolean isOnce;
    
    private int questionId;

    private FeePlan feePlan;
    private ChargePlan[] chargePlan;

    private Changed changed;   //只做老兵经验用途，在登陆完成是计算，然后在第一个position收到以后通知

    public short startX,startY,endX,endY;
    //mengjie add
    public int monthibuy = 0;
    public String cityname = "";
    public HashSet playercompletedTask = new HashSet();
    public String Cmcc_list = "";
    public WorldPlayerKillMg[] killmgid = new WorldPlayerKillMg[10];
    //mengjie add end
    public long lstChatTime = 0;
    
    private Client client; //用户
    
    private HashMap<Integer, MercenaryPlayer> mercenary = new HashMap<Integer, MercenaryPlayer>();
    
    //战场切换时进行通知势力
    public boolean showCampMessage = false;
    //是否显示过统御力不足的消息
    public boolean showLeadershipMessage = false;
    
	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
		
		this.clientDataVersion = getClientDataVersion();
	}
    
    public int getClientDataVersion(){
		int flag = 0;
		if(client != null ){
			flag = client.getDataVersion();
		}
		return flag;
	}
    
    public boolean isShowCampMessage(){
    	return showCampMessage;
    }
    
    public void setShowCampMessage(boolean showCampMessage){
    	this.showCampMessage = showCampMessage;
    }

	public String getcityname(){
		String flag = "";
		if(client != null ){
			flag = client.cityname;
		}
		return flag;
	}
    //jwp add  当前回答的随机库 .不下发id，这样无法让玩家去尝试用静态随机题库id去做答案尝试
    public final int randomQuestionSize = 100000;
    public final int battleRandomQuestionSize = 1;
    
    
    /**
     * @param id
     * @return 是否可以减少装备时间
     */
    public boolean reduceEquDiamondTimeFlag(int id){
    	return battleFalg;
    }
    
    
    /**
     * 玩家的战斗标示，是否处于战斗中
     */
    private boolean battleFalg  = false;
    public boolean hasBattle(){
    	return battleFalg;
    }
    public void setBattle(boolean battleFlag){
    	this.battleFalg = battleFlag;
    }
    
    /**
     * 装备的固化时间为5分钟
     */
    public final static int equDiamondTime = 5 * 60 * 1000; 
    /**
     * 本地战斗的随机数
     */
    public RandomMake rand = new RandomMake();
    public RandomMake getRand() {
		return rand;
	}

	public void setRand(RandomMake rand) {
		this.rand = rand;
	}

	/**
     * 刷怪机制最多连续出错数
     */
    public static short errorKickLimitCount = 6;
    /**
     * 用于战斗放刷机制 key为出错类型，value为连续出错数量
     */
    public Map<Integer, Integer> errorKickMap = new HashMap<Integer, Integer>();
    
    /**
     * @param type 放入一次出错类型 ,并返回时否已经允许t除
     */
    public boolean putErrorKick(int type){
    	boolean kickFlag = false;
    	if(errorKickMap.containsKey(type)){
    		errorKickMap.put(type, errorKickMap.get(type) + 1);
    	}else{
    		errorKickMap.put(type, 1);
    	}
    	
    	if(errorKickMap.get(type) >= errorKickLimitCount){
    		kickFlag  = true;
    	}
    	return kickFlag;
    }
    /**
     * 当前怪物经验倍数
     */
    public final int randomExpSize = 10;
	public Vector battleRandomQuestionMap = new Vector<RandomQuestion>();
	
	public void addRandQuestion(RandomQuestion randomQuestion){
		
		battleRandomQuestionMap.add(randomQuestion);
	}
	
	public RandomQuestion getRandomQuestion(int id){
		
		RandomQuestion randomQuestion = null;
		for(int i = 0; i < battleRandomQuestionMap.size(); i++){
			RandomQuestion randomQuestionTemp = (RandomQuestion) battleRandomQuestionMap.get(i);
			if(randomQuestionTemp.getId() == id){
				randomQuestion = randomQuestionTemp;
				break;
			}
		}
		return randomQuestion;
	}
	
	/**
	 * @return获得合法的问题id
	 */
	public int getRandomQuestionLegal(){
		Random random = new Random();
		
		int id = 0;
		
		boolean flag = true;
		while(flag){
			id = random.nextInt(randomQuestionSize);
			int k = 0;
			int i = 0;
			for(i = 0; i < battleRandomQuestionMap.size(); i++){
				RandomQuestion randomQuestionTemp = (RandomQuestion) battleRandomQuestionMap.get(i);
				if(randomQuestionTemp.getId() != id){
					k++;
				}
			}
			
			if(i == k){
				flag = false;
			}
		}
		
		return id;
	}
	public void randmoQuestionClear(){
		battleRandomQuestionMap.clear();
	}
	
	public int getRandomQuestionIndex(int id){
		int index = battleRandomQuestionSize;
		for(int i = 0; i < battleRandomQuestionMap.size(); i++){
			RandomQuestion randomQuestionTemp = (RandomQuestion) battleRandomQuestionMap.get(i);
			if(randomQuestionTemp.getId() == id){
				index = i;
				break;
			}
		}
		return index;
	}
	/**
	 * 上线战斗的时候重新生成题库，防止用户尝试
	 */
	//jwp add end
    public long getLastPetTradeTime() {
		return lastPetTradeTime;
	}


	public void setLastPetTradeTime(long lastPetTradeTime) {
		this.lastPetTradeTime = lastPetTradeTime;
	}


	public long getLasPkRequestTime() {
		return lastPkRequestTime;
	}


	public void setLasPkRequestTime(long lastPkRequestTime) {
		this.lastPkRequestTime = lastPkRequestTime;
	}
	
	/**
	 * 获得玩家有效的VIP等级
	 * @param playerVipLevel
	 * @param vipValidTime
	 */
	public int getPlayerVipLevel (int playerVipLevel, Date vipValidTime) {
		int ret = 0;
		if (vipValidTime != null && vipValidTime.equals("") == false) {
			long validTime = vipValidTime.getTime();
			long timeNow = new Date().getTime();
			if (validTime >= timeNow) {
				ret = playerVipLevel;
			}
		}
		return ret;
	}
	
	//宠物交易限制
    public long lastPetTradeTime = 0;
    //pk请求限制
    public long lastPkRequestTime = 0;
    
//    public long lstareaChatTime = 0;
    
    public boolean inArenaBattle = false;
    
    private String loginmsg = "";
    private HashMap<Integer, GiftData> giftDataCache = new HashMap<Integer, GiftData>();
    //常规一次性奖励jwp add start
    private HashMap<Integer, GiftData> onlyGiftDataCache = new HashMap<Integer, GiftData>();
    //jwp add end
    public WorldPlayer(Player player) throws Exception {
        super(player);
        long time = System.currentTimeMillis();
        lastLifeTime = time;
        lastFeeTime = time;
        positionTime = time;
        int[] diamondShineLevel = Suits.getActualPointSuitEffect2(getUsedEquipments());
        addDiamondShineBuf(diamondShineLevel);
    	setSuitEffProperty();
//    	adjustProperty();
    }

    //为loginmsg添加指定的消息并自动添加换行符
    public void addLoginMessage(String msg){
    	if(loginmsg.equals("")){
    		loginmsg += msg;
    	}else{
    		loginmsg += "\n" + msg; 
    	}
    }
    
    public void setLoginMessage(String msg){
    	loginmsg = msg;
    }
    
    public String getLoginMessage(){
    	return loginmsg;
    }
    
    public void clearLoginMessage(){
    	loginmsg = "";
    }

    public int getQuestionId() {
    	return questionId;
    }

    public void setQuestionId(int id) {
    	questionId = id;
    }

    public Team getTeam(){
        return team;
    }

    public void setTeam(Team team){
        this.team = team;
        if(team==null){
            teamState = TEAM_NONE;
        }
    }

    public void addLock(ILock lock){
        locks.add(lock);
    }


    public void removeLock(ILock lock){
        locks.remove(lock);
    }

    public void releaseLocks(){
        for(int i=0;i<locks.size();i++){
            ILock lock = (ILock)locks.get(i);
            lock.cancel(this);
        }
    }

    public void setState(int state){
        this.state = state;
    }

    public int getState(){
        return state;
    }

    public void acquire(){
            ref++;
    }

    public void release(){
            ref--;
    }

    public int getRef(){
        return ref;
    }

    public boolean online(){
        return state==ONLINE;
    }

    public boolean offlinemode(){
    	short[] option = getOption();
    	if (option.length >= 10){//old & new
    		return option[0]==1;
    	}
        return false;
    }

    public GameMap getMap(){
        return map;
    }

    public void setMap(GameMap map){
        this.map = map;
    }

    public ResourceObject getResourceLock(int resourceId){
        for(int i=0;i<locks.size();i++){
            ResourceObject ro = (ResourceObject)locks.get(i);
            if(ro.getId()==resourceId)
                return ro;
        }
        return null;
    }

    public void setTeamState(int state){
        this.teamState = state;
    }

    public int getTeamState(){
        return teamState;
    }

    public long getLongIMoney(){
        return longIMoney;
    }

    public void setLongIMoney(long iMoney){
        this.longIMoney = iMoney;
    }
    
    public long getBBalance(){
    	return bBalance;
    }
    
    public void setBBalance(long balance){
    	this.bBalance = balance;
    }

    public void setLastLifeTime(long time){
        this.lastLifeTime = time;
    }

    public long getLastLifeTime(){
        return lastLifeTime;
    }

    public void setFeePlan(FeePlan feePlan){
        this.feePlan = feePlan;
    }

    public FeePlan getFeePlan(){
        return feePlan;
    }

    public void setChargePlan(ChargePlan[] chargePlan){
        this.chargePlan = chargePlan;
    }

    public ChargePlan[] getChargePlan(){
        return chargePlan;
    }

    public long getLastFeeTime(){
        return lastFeeTime;
    }

    public void setLastFeeTime(long lastFeeTime){
        this.lastFeeTime = lastFeeTime;
    }

//    public void setMonthFee(int monthFee){
//        this.monthFee = monthFee;
//    }
//
//    public int getMonthFee(){
//        return monthFee;
//    }

    public long getPositionTime(){
        return positionTime;
    }

    public void setPositionTime(long time) {
        if (positionTime + 60000L <= time) {
            positionHistory.clear();
            positionTime = time;
        }
    }

    public void addPositionDest(int id){
        positionHistory.add(id);
    }

    public void removePositionDest(int id){
        positionHistory.remove(id);
    }

    public boolean containsPosition(int id){
        return positionHistory.contains(id);
    }

    public void clearPosition(){
        positionHistory.clear();
    }

    public String getModel(){
        return model;
    }

    public void setModel(String model){
        this.model = model;
    }

    public String getAccountName(){
        return accountName;
    }

    public int getColor() {
        return deadTime>=3?0x00ff00:0xff0000;
    }

    public boolean isInBattle() {
        return inBattle;
    }

    public String getkey() {
        return key;
    }

    public int getModifyPasswordTimes() {
        return modifyPasswordTimes;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isIsFirstEnter() {
        return isFirstEnter;
    }
    
    public boolean isOnce () {
    	return isOnce;
    }

    public boolean isNeedPay() {
        return !(isMonth||isSubscribe);
    }

    public void setAccountName(String accountName){
        this.accountName = accountName;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setInBattle(boolean inBattle) {
        this.inBattle = inBattle;
    }

    public void setKey(String password) {
        this.key = password;
    }

    public void setModifyPasswordTimes(int modifyPasswordTimes) {
        this.modifyPasswordTimes = modifyPasswordTimes;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setIsFirstEnter(boolean isFirstEnter) {
        this.isFirstEnter = isFirstEnter;
    }
    
    public void setIsOnce (boolean isOnce) {
    	this.isOnce = isOnce;
    }

    public boolean isMonth(){
        return isMonth;
    }

    public void setMonth(boolean isMonth){
        this.isMonth = isMonth;
    }

    public boolean isSubscribe(){
        return isSubscribe;
    }

    public void setSubscribe(boolean isSubscribe){
        this.isSubscribe = isSubscribe;
    }

    public void setNeedRefreshPosition(boolean needRefreshPosition) {
        this.needRefreshPosition = needRefreshPosition;
    }

    public void setChanged(Changed changed) {
        this.changed = changed;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMonthibuy() {
		return monthibuy;
	}


	public void setMonthibuy(int monthibuy) {
		this.monthibuy = monthibuy;
	}


	public boolean inPkMap(){
        if(map==null){
            return false;
        }
        return map.isCanPk();
    }

    public boolean inInstance(int instanceId){
        GameMap map = getMap();
        if(map==null)
            return false;
        if(map.getInstance()==null)
            return false;
        if(map.getInstance().getId()==instanceId)
            return true;
        return false;
    }

    public int getDeadTime(){
        return deadTime;
    }

    public void clearDeadTime(){
        deadTime = 0;
        if(removeBuf((byte)-3,null)){  //清除保护buf
            needRefreshPosition = true;
        }
    }

    public boolean isNeedRefreshPosition() {
        return needRefreshPosition;
    }

    public Changed getChanged() {
        return changed;
    }

    public String getPassword() {
        return password;
    }

    public void incDeadTime(){
        if(!hasBuf(Buf.GUARD)){
            deadTime++;
            if (deadTime == 2) {
                Buf buf = new Buf( -1, Buf.GUARD, 0, 3600 * 6, Buf.UNIT_SECOND);
                buf.setTimestamp(System.currentTimeMillis());
                addBuf(buf, null);
                needRefreshPosition = true;
            }
        }
    }
    
    /**
     * @param campLost
     * 根据参数来决定是否要挂盾 低于80级两次挂盾，80到90一次挂盾，90以上不挂
     */
    public void incCampDeadTime(int playerLevel){
    	byte campLost = 1;
    	if(playerLevel >= 70 && playerLevel < 90){
    		campLost = 2;
    	}else if(playerLevel >= 90){
    		campLost = 0;
    	}
    	
        if(!hasBuf(Buf.GUARD)){
            deadTime++;
            
            if (deadTime == campLost) {
                Buf buf = new Buf( -1, Buf.GUARD, 0, 1800, Buf.UNIT_SECOND);
                buf.setTimestamp(System.currentTimeMillis());
                addBuf(buf, null);
                needRefreshPosition = true;
            }
        }
    }
    
    public void setDeadTime(int value){
        deadTime = value;
    }

    public void removeBuf(Buf buf,Changed changed){
        super.removeBuf(buf,changed);
        if(buf.getProperty()==-3){
            deadTime = 0;
            changed.setProperty(Changed.GUARDSTATE,0);
            needRefreshPosition = true;
        }
    }

//    public boolean equals(Object o){
//        if(this==o)
//            return true;
//        if(o==null)
//            return false;
//    }


    //    public void setLuckyBufTime(long time){
//        this.luckyBufTime = time;
//    }
//
//    public boolean hasLuckyBuf(){
//        return System.currentTimeMillis()<luckyBufTime;
//    }

    public static final byte PLAYER_ATTRIBUTE_PMIN = 0;
    public static final byte PLAYER_ATTRIBUTE_PMAX = 1;
    public static final byte PLAYER_ATTRIBUTE_PDEF = 2;
    public static final byte PLAYER_ATTRIBUTE_MMIN = 3;
    public static final byte PLAYER_ATTRIBUTE_MMAX = 4;
    public static final byte PLAYER_ATTRIBUTE_MDEF = 5;
    public static final byte PLAYER_ATTRIBUTE_PHIT = 6;
    public static final byte PLAYER_ATTRIBUTE_MHIT = 7;
    public static final byte PLAYER_ATTRIBUTE_FLEE = 8;
    public static final byte PLAYER_ATTRIBUTE_PCRI = 9;
    public static final byte PLAYER_ATTRIBUTE_MCRI = 10;
    
    //此方法为402方法，现在已经废弃，无视
    public int[] calculateSpecialAttributes(){
        BattleSprite sprite = new BattleSprite();

        int vit = this.getRealVitality();
        int str = this.getRealStrength();
        int inte = this.getRealIntelligence();
        int agi = this.getRealAgility();
        int hp = this.getHp() + this.getBufProperty(Changed.HP);
        int mp = this.getMp() + this.getBufProperty(Changed.MP);
        int [] addpoint = getSuitEffectDiamondAddValue();	//各属性宝石加成
        int [] trainlevel = getTrainLevel();
        int [] trainlevelstone = getTrainAttributeAddValue();
        int []magicposlevel = getMagicPosLevel();
	    int []magicposfloor = getMagicPosFloor();
        sprite.initBattleData((byte)0, this.getLevel(), vit, str, inte, agi, this.getLuck(), hp, mp, this.getVianyType(), 0, 0, addpoint,trainlevel,trainlevelstone,magicposlevel,magicposfloor);
        sprite.id = this.getId();
        sprite.initEquipData(this.getUsedEquipments());

        int[] result = new int[11];

        result[PLAYER_ATTRIBUTE_PMIN] = sprite.getShowAttribute(BattleSprite.ATTR_PMIN);
        result[PLAYER_ATTRIBUTE_PMAX] = sprite.getShowAttribute(BattleSprite.ATTR_PMAX);
        result[PLAYER_ATTRIBUTE_PDEF] = sprite.getShowAttribute(BattleSprite.ATTR_PDEF);
        result[PLAYER_ATTRIBUTE_MMIN] = sprite.getShowAttribute(BattleSprite.ATTR_MMIN);
        result[PLAYER_ATTRIBUTE_MMAX] = sprite.getShowAttribute(BattleSprite.ATTR_MMAX);
        result[PLAYER_ATTRIBUTE_MDEF] = sprite.getShowAttribute(BattleSprite.ATTR_MDEF);
        result[PLAYER_ATTRIBUTE_PHIT] = sprite.getShowAttribute(BattleSprite.ATTR_PHIT);
        result[PLAYER_ATTRIBUTE_MHIT] = sprite.getShowAttribute(BattleSprite.ATTR_MHIT);
        result[PLAYER_ATTRIBUTE_FLEE] = sprite.getShowAttribute(BattleSprite.ATTR_FLEE);
        result[PLAYER_ATTRIBUTE_PCRI] = sprite.getShowAttribute(BattleSprite.ATTR_PCRI);
        result[PLAYER_ATTRIBUTE_MCRI] = sprite.getShowAttribute(BattleSprite.ATTR_MCRI);

        return result;
    }
    
    public void addGiftData(GiftData gData){
        giftDataCache.put(gData.getGiftGroup().getId(), gData);
    }
    
    public GiftData getGiftData(int groupId){
        return giftDataCache.get(groupId);
    }
    public void addOnlyGiftData(GiftData gData){
        onlyGiftDataCache.put(gData.getOnlyGiftGroup().getId(), gData);
    }
    
    public GiftData getOnlyGiftData(int groupId){
        return onlyGiftDataCache.get(groupId);
    }
    //jwp add
    private long lastMailTime;
    private long lastGmMailTime;
    private byte gmMailCount;
	public long lstareaChatTime = 0;
    

    public byte getGmMailCount() {
		return gmMailCount;
	}


	public void setGmMailCount(byte gmMailCount) {
		this.gmMailCount = gmMailCount;
	}


	public long getLastGmMailTime() {
		return lastGmMailTime;
	}


	public void setLastGmMailTime(long lastGmMailTime) {
		this.lastGmMailTime = lastGmMailTime;
	}


	/**
     * @return 最后一次发信时间包括gm信件
     */
    public long getLastMailTime() {
		return lastMailTime;
	} 


	public void setLastMailTime(long lastMailTime) {
		this.lastMailTime = lastMailTime;
	}
	
	
	//jwp add end
    //mengjie add
    public int getRecommendvalue(){
    	int result = 0;
    	if (this.getLevel() == 50){
    		result = 36000 * 2;
    	}else if (this.getLevel() == 65){
    		result = 36000 * 4;
    	}else if (this.getLevel() == 80){
    		result = 36000 * 6;
    	}else if (this.getLevel() == 90){
    		result = 36000 * 8;
    	}else if (this.getLevel() == 100){
    		result = 36000 * 10;
    	}
        return result;
    }
    public int getRecommend2value(){
    	int result = 0;
    	if (this.getLevel() == 50){
    		result = 36000 * 1;
    	}else if (this.getLevel() == 65){
    		result = 36000 * 2;
    	}else if (this.getLevel() == 80){
    		result = 36000 * 3;
    	}else if (this.getLevel() == 90){
    		result = 36000 * 4;
    	}else if (this.getLevel() == 100){
    		result = 36000 * 5;
    	}
        return result;
    }
    
    
    /**
     * 获得当前记录的活力值和在线活力值
     * @return
     */
    public int getAllLife(){
    	int lifeValue = getLifeValue();		//当前活力
    	int hour = UnlineExpConfig.getExpHour(getUnlineOnlineLife());
    	int onlineHour = UnlineExpConfig.getHour(getLastLoginTime());
    	
//    	//测试用
//    	if(player.getLastLoginTime() != null){
//    		onlineHour = (int)((new Date().getTime() - player.getLastLoginTime().getTime()) / 1000 / 60);
//    	}
    	
    	onlineHour += hour;
    	if(onlineHour > 4){
    		onlineHour = 4;
    	}
    	
    	Date loginTime = getLastLoginTime();	//最后登录时间
    	if(loginTime != null){
    		Date date = getUnlineDate();		
    		long loginTimer = loginTime.getTime();	//登录时间
    		long resetTimer = date.getTime();		//重置
    		if(resetTimer > loginTimer){
    			long subTimer = resetTimer - loginTimer;
    			int subHour = (int)(subTimer / 1000 / 3600);
    			if(subHour > 0){
    				onlineHour -= subHour;
    			}
    		}
    	}
    	
    	if(onlineHour <= 0){
    		if(lifeValue < 0) lifeValue = 0;
    		return lifeValue;
    	}
    	
    	int onlineLife = UnlineExpConfig.UnlineExp[onlineHour - 1];	//在线活力
    	lifeValue += onlineLife;
    	if(lifeValue >= UnlineExpConfig.LIFEVALUE_MAX){		//大于最大限制
    		lifeValue = UnlineExpConfig.LIFEVALUE_MAX;
    		setLifeValue(UnlineExpConfig.LIFEVALUE_MAX);
    		setUnlineOnlineLife(0);
    		setUnlineDate(new Date());
    	}
    	if(lifeValue < 0){
    		lifeValue = 0;
    	}
    	return lifeValue;
    }
    
    /**
     * 计算在线的活力值
     * @return
     */
    public int calcOnlineLife(){
    	int hour = UnlineExpConfig.getExpHour(getUnlineOnlineLife());
    	int onlineHour = UnlineExpConfig.getHour(getLastLoginTime());
    	
//    	//测试用
//    	if(player.getLastLoginTime() != null){
//    		onlineHour = (int)((new Date().getTime() - player.getLastLoginTime().getTime()) / 1000 / 60);
//    	}
    	
    	onlineHour += hour;
    	if(onlineHour > 4){
    		onlineHour = 4;
    	}
    	
    	Date loginTime = getLastLoginTime();	//最后登录时间
    	if(loginTime != null){
    		Date date = getUnlineDate();		
    		long loginTimer = loginTime.getTime();	//登录时间
    		if(date == null){
    			date = new Date();
    			setUnlineDate(date);
    		}
    		long resetTimer = date.getTime();		//重置
    		if(resetTimer > loginTimer){
    			long subTimer = resetTimer - loginTimer;
    			int subHour = (int)(subTimer / 1000 / 3600);
    			if(subHour > 0){
    				onlineHour -= subHour;
    			}
    		}
    	}
    	
    	//在一次调系统时间之后 发现由于这里出现了负数。。。太囧了
    	if(onlineHour <= 0){
    		return 0;
    	}
    	return UnlineExpConfig.UnlineExp[onlineHour - 1];
    }
    
    
    public void removeAllEffect (Changed changed_1) {
    	if (changed_1 != null) {
    		//还原宝辉效果
    		changed_1.setProperty(Changed.ADDHPMAX, 0);
    		changed_1.setProperty(Changed.ADDMPMAX, 0);
    		changed_1.setProperty(Changed.ADDPATTCMAX, 0);
    		changed_1.setProperty(Changed.ADDPATTCMIN, 0);
    		changed_1.setProperty(Changed.ADDMATTCMAX, 0);
    		changed_1.setProperty(Changed.ADDMATTCMIN,0);
    		changed_1.setProperty(Changed.ADDNOCRI, 0);
    		changed_1.setProperty(Changed.ADDAGILITY, 0);
    		changed_1.setProperty(Changed.ADDSTRENGTH, 0);
    		changed_1.setProperty(Changed.ADDINTELLIGENCE, 0);
    		changed_1.setProperty(Changed.ADDPCRITICAL, 0);
    		changed_1.setProperty(Changed.ADDMCRITICAL, 0);
    		
    		// 还原套装效果属性
    		changed_1.setProperty(Changed.ADD_SUIT_STRENGTH, 0);
    		changed_1.setProperty(Changed.ADD_SUIT_AGILITY, 0);
    		changed_1.setProperty(Changed.ADD_SUIT_VITALITY, 0);
    		changed_1.setProperty(Changed.ADD_SUIT_INTELLIGENCE, 0);
    		
    		removeDiamondShineList();
    		removeAllSuitEffect();
    	}
    }
    
    public void addAllEffect (Changed changed_2) {
    	if (changed_2 != null) {
    		// 判断是否是增加属性的套装效果，如果是加入属性池
    		setSuitEffProperty();
    		ArrayList tmpSuitEffect = (ArrayList) getPropertySuitEffect();
    		int [] addpoint = getSuitEffectDiamondAddValue();
    		for (int i = 0; i < tmpSuitEffect.size(); i++) {
    			SuitEffect se = (SuitEffect) tmpSuitEffect.get(i);
    			switch (-se.getType()) {
    			case Changed.STRENGTH:
    				changed_2.setProperty(Changed.ADD_SUIT_STRENGTH, se.getValue());
    				break;
    			case Changed.AGILITY:
    				changed_2.setProperty(Changed.ADD_SUIT_AGILITY, se.getValue());
    				break;
    			case Changed.VITALITY:
    				changed_2.setProperty(Changed.ADD_SUIT_VITALITY, se.getValue());
    				break;
    			case Changed.INTELLIGENCE:
    				changed_2.setProperty(Changed.ADD_SUIT_INTELLIGENCE, se.getValue());
    				break;
    			}
    		}
    		if(addpoint != null){
    			//新套装宝石加成效果
    			changed.setProperty(Changed.ADD_SUIT_VITALITY_DIAMOND, addpoint[VitStone]);
    			changed.setProperty(Changed.ADD_SUIT_INTELLIGENCE_DIAMOND,addpoint[IneStone]);
    			changed.setProperty(Changed.ADD_SUIT_STRENGTH_DIAMOND, addpoint[StrStone]);
    			changed.setProperty(Changed.ADD_SUIT_AGILITY_DIAMOND, addpoint[AgiStone]);
    			changed.setProperty(Changed.ADD_SUIT_PATTACK,addpoint[PattackStone]);//物攻
    			changed.setProperty(Changed.ADD_SUIT_MATTACK,addpoint[MattackStone]);//魔攻
    			changed.setProperty(Changed.ADD_SUIT_PDEFENSE,addpoint[PdefStone]); //物防
    			changed.setProperty(Changed.ADD_SUIT_MDEFENSE,addpoint[MdefStone]); //魔防
    			changed.setProperty(Changed.ADD_SUIT_HIT,addpoint[HitStone]); 		//命中
    			changed.setProperty(Changed.ADD_SUIT_PARRY,addpoint[ParryStone]); 	//闪避
    			changed.setProperty(Changed.ADD_SUIT_HP, addpoint[HpStone]);//血量
    			changed.setProperty(Changed.ADD_SUIT_MP, addpoint[MpStone]);//魔法
    			changed.setProperty(Changed.ADD_SUIT_ARMOR,addpoint[NocriStone]);//免暴
    		}
    		//计算全身9钻效果
    		if(IsEquIdentifyEffcet()){
        		changed.setProperty(Changed.ADD_NINE_EQU_DIAMOND_STR, 90);
        		changed.setProperty(Changed.ADD_NINE_EQU_DIAMOND_INT, 90);
        		changed.setProperty(Changed.ADD_NINE_EQU_DIAMOND_VIT, 90);
        		changed.setProperty(Changed.ADD_NINE_EQU_DIAMOND_AGI, 90);
        	}else{
        		changed.setProperty(Changed.ADD_NINE_EQU_DIAMOND_STR, 0);
        		changed.setProperty(Changed.ADD_NINE_EQU_DIAMOND_INT, 0);
        		changed.setProperty(Changed.ADD_NINE_EQU_DIAMOND_VIT, 0);
        		changed.setProperty(Changed.ADD_NINE_EQU_DIAMOND_AGI, 0);
        	}
    		
    		//计算宝辉套装效果
    		int[] diamondShineLevel = Suits.getActualPointSuitEffect2(getUsedEquipments());
    		addDiamondShineBuf(diamondShineLevel);
    		ArrayList tempAry = (ArrayList)getDiamondShineList();
    		BattleSprite sprite = new BattleSprite();
    		int vit = getRealVitality();
    		int str = getRealStrength();
    		int inte = getRealIntelligence();
    		int agi = getRealAgility();
    		for (int i = 0; i < tempAry.size(); i++) {
    			DiamondShineBuf dsBuf = (DiamondShineBuf)tempAry.get(i);
    			switch(dsBuf.getProperty()){
    			case DiamondShineBuf.AGI:
    				int ret = getRealAgility() * getDiamondShineBufAttri(DiamondShineBuf.AGI) / 100;
    				//changed_2.setProperty(Changed.ADDAGILITY, ret);
    				agi += ret;
    				break;
    			case DiamondShineBuf.STR:
    				ret = getRealStrength() * getDiamondShineBufAttri(DiamondShineBuf.STR) / 100;
    				//changed_2.setProperty(Changed.ADDSTRENGTH, ret);
    				str += ret;
    				break;
    			case DiamondShineBuf.INT:
    				ret = getRealIntelligence() * getDiamondShineBufAttri(DiamondShineBuf.INT) / 100;
    				//changed_2.setProperty(Changed.ADDINTELLIGENCE, ret);
    				inte += ret;
    				break;
    			case DiamondShineBuf.STR_VALUE:
        			ret = getDiamondShineBufAttri(DiamondShineBuf.STR_VALUE);
        			//changed_2.setProperty(Changed.ADDSTRENGTH, ret);
        	        str += ret;
        			break;
        		case DiamondShineBuf.AGI_VALUE:
        			ret = getDiamondShineBufAttri(DiamondShineBuf.AGI_VALUE);
        			//changed_2.setProperty(Changed.ADDAGILITY, ret);
    				agi += ret;
        			break;
        		case DiamondShineBuf.VIT_VALUE:
        			ret = getDiamondShineBufAttri(DiamondShineBuf.VIT_VALUE);
        			//changed_2.setProperty(Changed.ADDVITALITY, ret);
        	        vit += ret;
        			break;
        		case DiamondShineBuf.INT_VALUE:
        			ret = getDiamondShineBufAttri(DiamondShineBuf.INT_VALUE);
        			//changed_2.setProperty(Changed.ADDINTELLIGENCE, ret);
        	        inte += ret;
        			break;
    			default:
    				break;
    			}
    		}
    		 int val = str - getRealStrength();
    		    if(val != 0){
    		    	changed_2.setProperty(Changed.ADDSTRENGTH, val);
    		    }
    		    val = agi - getRealAgility();
    		    if(val != 0){
    		    	changed_2.setProperty(Changed.ADDAGILITY, val);
    		    }
    		    val = vit - getRealVitality();
    		    if(val != 0){
    		    	changed_2.setProperty(Changed.ADDVITALITY, val);
    		    }
    		    val = inte - getRealIntelligence();
    		    if(val != 0){
    		    	changed_2.setProperty(Changed.ADDINTELLIGENCE, val);
    		    }
    		Buf bufEva = getCampBuf(Buf.CAMP_EVA);
    		int evaValue = 0;
    		if(bufEva != null){
    			evaValue = bufEva.getValue();
    		}
    		Buf bufStone = getCampBuf(Buf.CAMP_STONE);
    		int stoneValue = 0;
    		if(bufStone != null){
    			stoneValue = bufStone.getValue();
    		}
    		int []trainlevel = getTrainLevel();
    		int [] trainlevelstone = getTrainAttributeAddValue();
    		int []magicposlevel = getMagicPosLevel();
			int []magicposfloor = getMagicPosFloor();
    		sprite.initBattleData((byte) 0, getLevel(), vit, str, inte, agi,
    				getLuck(), getHp(), getMp(), getVianyType(), evaValue, stoneValue, addpoint,trainlevel,trainlevelstone,magicposlevel,magicposfloor);
    		IEquipment[] equips = getUsedEquipments();
    		sprite.initEquipData(equips);
    		
    		int [] trainattributepoint = getTrainLevelattributepoint();
      	   	if(trainattributepoint != null){
      	   		changed.setProperty(Changed.ADD_TRAIN_PHIT ,trainattributepoint[BattleSprite.train_attack]);
      	   		changed.setProperty(Changed.ADD_TRAIN_PDEF ,trainattributepoint[BattleSprite.train_pdef]);
      	   		changed.setProperty(Changed.ADD_TRAIN_MHIT ,trainattributepoint[BattleSprite.train_mattack]);
      	   		changed.setProperty(Changed.ADD_TRAIN_MDEF ,trainattributepoint[BattleSprite.train_mdef]);
      	   		changed.setProperty(Changed.ADD_TRAIN_HIT ,trainattributepoint[BattleSprite.train_hit]);
      	   		changed.setProperty(Changed.ADD_TRAIN_NOCRI ,trainattributepoint[BattleSprite.train_nocri]);
      	    } 
    		
      	  if(trainlevelstone != null){
      		  	changed.setProperty(Changed.ADD_TRAINLEVE_DIAMOND_STR ,trainlevelstone[StrStone]);
				changed.setProperty(Changed.ADD_TRAINLEVE_DIAMOND_INE ,trainlevelstone[IneStone]);
				changed.setProperty(Changed.ADD_TRAINLEVE_DIAMOND_VIT ,trainlevelstone[VitStone]);
				changed.setProperty(Changed.ADD_TRAINLEVE_DIAMOND_AGI ,trainlevelstone[AgiStone]);
				changed.setProperty(Changed.ADD_TRAINLEVE_DIAMOND_PATT ,trainlevelstone[PattackStone]);
				changed.setProperty(Changed.ADD_TRAINLEVE_DIAMOND_PDEF ,trainlevelstone[PdefStone]);
				changed.setProperty(Changed.ADD_TRAINLEVE_DIAMOND_MATT ,trainlevelstone[MattackStone]);
				changed.setProperty(Changed.ADD_TRAINLEVE_DIAMOND_MDEF ,trainlevelstone[MdefStone]);
				changed.setProperty(Changed.ADD_TRAINLEVE_DIAMOND_HIT ,trainlevelstone[HitStone]);
				changed.setProperty(Changed.ADD_TRAINLEVE_DIAMOND_FLEE ,trainlevelstone[ParryStone]);
				changed.setProperty(Changed.ADD_TRAINLEVE_DIAMOND_NOCRI ,trainlevelstone[NocriStone]);
				changed.setProperty(Changed.ADD_TRAINLEVE_DIAMOND_HP ,trainlevelstone[HpStone]);
				changed.setProperty(Changed.ADD_TRAINLEVE_DIAMOND_MP ,trainlevelstone[MpStone]);
			}
      	   	
	      	if(getWaterLevel() > 0 && getWaterFloor() > 0){
				int []waterAttrpoint = MagicPosMessage.getMagicPosAttr(0, getWaterLevel(), getWaterFloor());
				if(waterAttrpoint != null){
					changed.setProperty(Changed.ADD_MAGIC_POS_ATTACK, waterAttrpoint[0]);
					changed.setProperty(Changed.ADD_MAGIC_POS_MATTACK, waterAttrpoint[1]);
				}	
			}
			if(getSoilLevel() > 0 && getSoilFloor() > 0){
				int [] soilAttrpoint = MagicPosMessage.getMagicPosAttr(1, getSoilLevel(), getSoilFloor());
				if(soilAttrpoint != null){
					changed.setProperty(Changed.ADD_MAGIC_POS_PDEF, soilAttrpoint[0]);
					changed.setProperty(Changed.ADD_MAGIC_POS_MDEF, soilAttrpoint[1]);
				}
			}
			if(getFireLevel() > 0 && getFireFloor() > 0){
				int [] fireAttrpoint = MagicPosMessage.getMagicPosAttr(2, getFireLevel(), getFireFloor());
				if(fireAttrpoint != null){
					changed.setProperty(Changed.ADD_MAGIC_POS_HIT, fireAttrpoint[0]);
					changed.setProperty(Changed.ADD_MAGIC_POS_PCRI, fireAttrpoint[1]);
					changed.setProperty(Changed.ADD_MAGIC_POS_MCRI, fireAttrpoint[2]);
				}
			}
			if(getWindLevel() > 0 && getWindFloor() > 0){
				int [] windAttrpoint = MagicPosMessage.getMagicPosAttr(3, getWindLevel(), getWindFloor());
				if(windAttrpoint != null){
					changed.setProperty(Changed.ADD_MAGIC_POS_FLEE, windAttrpoint[0]);
					changed.setProperty(Changed.ADD_MAGIC_POS_NOCRI, windAttrpoint[1]);
				}
			}
			if(getMindLevel() > 0 && getMindFloor() > 0){
				int [] mindAttrpoint = MagicPosMessage.getMagicPosAttr(4, getMindLevel(), getMindFloor());
				if(mindAttrpoint != null){
					changed.setProperty(Changed.ADD_MAGIC_POS_HP, mindAttrpoint[0]);
					changed.setProperty(Changed.ADD_MAGIC_POS_MP, mindAttrpoint[1]);
				}
			}
      	  
    		for (int i = 0; i < tempAry.size(); i++) {
    			DiamondShineBuf dsBuf = (DiamondShineBuf)tempAry.get(i);
    			switch(dsBuf.getProperty()){
    			case DiamondShineBuf.ADD_HPMAX:
    				int ret = sprite.attributes[BattleSprite.ATTR_HPMAX] * getDiamondShineBufAttri(DiamondShineBuf.ADD_HPMAX) / 100;
    				changed_2.setProperty(Changed.ADDHPMAX, ret);
    				break;
    			case DiamondShineBuf.ADD_MPMAX:
    				ret = sprite.attributes[BattleSprite.ATTR_MPMAX] * getDiamondShineBufAttri(DiamondShineBuf.ADD_MPMAX) / 100;
    				changed_2.setProperty(Changed.ADDMPMAX, ret);
    				break;
    			case DiamondShineBuf.PHYSIC_ATTC:
    				ret = sprite.attributes[BattleSprite.ATTR_PMAX] * getDiamondShineBufAttri(DiamondShineBuf.PHYSIC_ATTC) / 100;
    				changed_2.setProperty(Changed.ADDPATTCMAX, ret);
    				ret = sprite.attributes[BattleSprite.ATTR_PMIN] * getDiamondShineBufAttri(DiamondShineBuf.PHYSIC_ATTC) / 100;
    				changed_2.setProperty(Changed.ADDPATTCMIN, ret);
    				break;
    			case DiamondShineBuf.MAGIC_ATTC:
    				ret = sprite.attributes[BattleSprite.ATTR_MMAX] * getDiamondShineBufAttri(DiamondShineBuf.MAGIC_ATTC) / 100;
    				changed_2.setProperty(Changed.ADDMATTCMAX, ret);
    				ret = sprite.attributes[BattleSprite.ATTR_MMIN] * getDiamondShineBufAttri(DiamondShineBuf.MAGIC_ATTC) / 100;
    				changed_2.setProperty(Changed.ADDMATTCMIN,ret);
    				break;
    			case DiamondShineBuf.PHYSIC_CRI:
    				ret = sprite.attributes[BattleSprite.ATTR_PCRI] * getDiamondShineBufAttri(DiamondShineBuf.PHYSIC_CRI) / 100;
    				changed_2.setProperty(Changed.ADDPCRITICAL, ret);
    				break;
    			case DiamondShineBuf.MAGIC_CRI:
    				ret = sprite.attributes[BattleSprite.ATTR_MCRI] * getDiamondShineBufAttri(DiamondShineBuf.MAGIC_CRI) / 100;
    				changed_2.setProperty(Changed.ADDMCRITICAL, ret);
    				break;
    			case DiamondShineBuf.NOCRI:
    				ret = sprite.attributes[BattleSprite.ATTR_NOCRI] * getDiamondShineBufAttri(DiamondShineBuf.NOCRI) / 100;
    				changed_2.setProperty(Changed.ADDNOCRI, ret);
    				break;
    			}
    		}
    	}
    }
    
    public HashMap<Integer, MercenaryPlayer> getMercenary(){
    	return mercenary;
    }
    
    public void addMercenary(MercenaryPlayer mp){
    	int id = mp.getMercenaryShop().getMercenary().getId();
    	mercenary.put(id, mp);
    	addMercenaryId(id);
    }
    
    public void removeMercenary(MercenaryPlayer mp){
    	int id = mp.getMercenaryShop().getMercenary().getId();
    	if(mercenary.containsKey(id)){
    		mercenary.remove(id);
    		removeMercenaryId(id);
    	}
    }
    
    public boolean containsMercenary(MercenaryPlayer mp){
    	if(mp != null && mercenary.containsKey(mp.getMercenaryShop().getMercenary().getId())){
    		return true;
    	}
    	return false;
    }
}
