package com.pip.itimes.server.stage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

/**
 * 玩家选举天赋定义
 * @author wpjiang
 *
 */

public class VotePlayerGift {
	int id; 
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getVoteLevel() {
		return voteLevel;
	}

	public void setVoteLevel(int voteLevel) {
		this.voteLevel = voteLevel;
	}

	public int getVoteplayerlevel() {
		return voteplayerlevel;
	}

	public void setVoteplayerlevel(int voteplayerlevel) {
		this.voteplayerlevel = voteplayerlevel;
	}

	public String getVoteContent() {
		return voteContent;
	}

	public void setVoteContent(String voteContent) {
		this.voteContent = voteContent;
	}
	
	public String getVoteBag() {
		return voteBag;
	}

	public void setVoteBag(String voteBag) {
		this.voteBag = voteBag;
	}

	public Vector<VoteGiftDefine> getVoteGiftDefines() {
		return voteGiftDefines;
	}

	public void setVoteGiftDefines(Vector<VoteGiftDefine> voteGiftDefines) {
		this.voteGiftDefines = voteGiftDefines;
	}
	
	public String getVoteTitle() {
		return voteTitle;
	}

	public void setVoteTitle(String voteTitle) {
		this.voteTitle = voteTitle;
	}
	/**
	 * 参加选举者等级
	 */
	int voteLevel;
	/**
	 * 选举其他玩家的起始等级
	 */
	int voteplayerlevel; 
	
	/**
	 * 选举的标题
	 */
	String voteTitle;
	/**
	 * 选举内容介绍
	 */
	String voteContent;
	
	/**
	 * 选举包满介绍
	 */
	String voteBag;
	
	/**
	 * 设置参赛的性别，男为1，女为2， 全部可以的话为0
	 */
	int mainType;
	
	/**
	 * 设置可以投票的性别， 
	 */
	int voteType;
	
	public int getVoteType() {
		return voteType;
	}

	public void setVoteType(int voteType) {
		this.voteType = voteType;
	}

	public String getManIntroduction() {
		return manIntroduction;
	}

	public void setManIntroduction(String manIntroduction) {
		this.manIntroduction = manIntroduction;
	}

	public String getWomanIntroduction() {
		return womanIntroduction;
	}

	public void setWomanIntroduction(String womanIntroduction) {
		this.womanIntroduction = womanIntroduction;
	}
	/**
	 * 男性对白介绍
	 */
	String manIntroduction ;
	
	/**
	 * 女性对白介绍
	 */
	String womanIntroduction ;
	
	public String getManAwardIntroduction() {
		return manAwardIntroduction;
	}

	public void setManAwardIntroduction(String manAwardIntroduction) {
		this.manAwardIntroduction = manAwardIntroduction;
	}

	public String getWomanAwardIntroduction() {
		return womanAwardIntroduction;
	}

	public void setWomanAwardIntroduction(String womanAwardIntroduction) {
		this.womanAwardIntroduction = womanAwardIntroduction;
	}
	/**
	 * 男性奖励介绍
	 */
	String manAwardIntroduction;
	
	/**
	 * 女性奖励介绍
	 */
	String womanAwardIntroduction;
	/**
	 * 选举所有的天赋
	 */
	private Vector<VoteGiftDefine> voteGiftDefines= new Vector<VoteGiftDefine>();
	
	public void addVoteGiftDefine(VoteGiftDefine voteGiftDefine){
		voteGiftDefines.add(voteGiftDefine);
	}
	
	public int getMainType() {
		return mainType;
	}

	public void setMainType(int mainType) {
		this.mainType = mainType;
	}
    public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		//this.beginTime = beginTime;
		this.beginTime = getTimeData(beginTime);
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		//this.endTime = endTime;
		if(endTime > 0){
			this.endTime = getTimeData(endTime);
		}else{
			this.endTime = getTimeData(Long.MAX_VALUE);
		}
	}
	/**
     * 开始日期
     */
    private Date beginTime;
    /**
     * 结束日期
     */
    private Date endTime;
    
    /**
     * @param time
     * @return 通过时间设置日历
     */
    private Date getTimeData(long time){
    	int year = (int)(time / 100000000);
        int month = (int)((time / 1000000) % 100 - 1);
        int day = (int)((time / 10000) % 100);
        int hour = (int)((time / 100) % 100);
        int minute = (int)(time % 100);

        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }
    
    public boolean isAvlib(){
    	boolean flag = false;
    	Date date_tmp = new Date();
    	if(date_tmp.getTime() >= beginTime.getTime() && date_tmp.getTime() <= endTime.getTime()){
    		flag = true;
    	}
    	return flag;
    }
    
    public boolean notShowVotesKing () {
    	boolean ret = false;
    	Date date_tmp = new Date();
    	if (date_tmp.getTime() <= endTime.getTime()) {
    		ret = true;
    	}
    	return ret;
    }
    /**
     * 用于与service的交互
     * 活动开始的日期
     */
    private Date startDate;
    /**
     * 用于与service的交互
     * 活动结束的日期
     */
    private Date endDate;
    /**
     * 狮子吼内容
     */
    public String roar;
    /**
     * 当前选举活动的状态
     */ 
	private int stage = -1;
	/**
	 * 是否有狮子吼未发出
	 */
	private boolean sendChat = true;
	/**
	 * 获奖的人数(参选)
	 */
	private int winnersNumEntry;
	/**
	 * 获奖的人数(投票)
	 */
	private int winnersNumVote;
	/**
	 * 选美奖品
	 */
	private LinkedHashMap <String, VoteGiftItemGroup> voteGiftItems = new LinkedHashMap<String, VoteGiftItemGroup>();
	
	/**
	 * 投票大王奖品
	 */
	private LinkedHashMap <String, VoteGiftItemGroup> votesKingGiftItems = new LinkedHashMap<String, VoteGiftItemGroup>();
	/**
	 * 用于返回有效的选举类型和选举记录集合
	 */
	private final Map<Integer, ArrayList<VoteInfo>> voteMap = new HashMap<Integer, ArrayList<VoteInfo>>();
	
	/**
	 * 用于返回有效的选角参赛纪录
	 */
	private final Map<Integer, Map<Integer,VoteShowInfo>> voteContentMap = new HashMap<Integer, Map<Integer,VoteShowInfo>>();
	/**
	 * 用于返回有效的付费道具投票大王Id,票数
	 */
	private final Map <Integer, ArrayList <VotesKing> > votesKingMap = new HashMap <Integer, ArrayList <VotesKing> > ();
	
	/**
	 * 收费道具投票大王的详细信息
	 */
	private final Map <Integer, Map <Integer, VoteKingInfo> > votesKingInfoMap = new HashMap <Integer, Map <Integer, VoteKingInfo> > ();
	
	/**
	 * 投票结束后的狮子吼和世界公告
	 */
	public static final String STR_WINER = "winner";
	public static final String STR_VOTE = "vote";
	public static final String STR_NOBODY = "没人";
	private String endRoar = null;
	private String endWorld = null;
	
    public void setStartDate (long time) {
    	this.startDate = new Date(time);
    }
    public Date getStartDate () {
    	return startDate;
    }
    
	public void setEndDate (long time) {
		this.endDate = new Date(time);
	}
	public Date getEndDate () {
		return endDate;
	}
	
	public void setStage (int stage) {
		this.stage = stage;
	}
	public int getStage () {
		return stage;
	}
	
	public void setRoar (String roar) {
		this.roar = roar;
	}
	public String getRoar () {
		return roar;
	}
	
	public void setSendChat (boolean sendChat) {
		this.sendChat = sendChat;
	}
	public boolean getSendChat () {
		return sendChat;
	}
	
	public void setWinnersNumEntry (int winnersNumEntry) {
		this.winnersNumEntry = winnersNumEntry;
	}
	public int getWinnersNumEntry () {
		return winnersNumEntry;
	}
	
	public void setWinnersNumVote (int winnersNumVote) {
		this.winnersNumVote = winnersNumVote;
	}
	public int getWinnersNumVote () {
		return winnersNumVote;
	}
	
	public void addVoteGiftItem(VoteGiftItemGroup items){
		voteGiftItems.put(items.getName(), items);
	}
	
	public void addVotesKingGiftItem (VoteGiftItemGroup items) {
		votesKingGiftItems.put(items.getName(), items);
	}

	public void clearVoteGiftItemGroup () {
		voteGiftItems.clear();
		votesKingGiftItems.clear();
	}
	
    public VoteGiftItem[] getGiftItem (int rank, int type) {
    	int i = 0;
		Collection<VoteGiftItemGroup> vgs = getVoteGfitItems(type);
		for (VoteGiftItemGroup group : vgs) {
			for (VoteGiftItem item : group.getItems()){
				if(item.rank == rank) {
					i ++;
				}
			}
		}
		if (i > 0) {
			int j = 0;
			VoteGiftItem[] voteGift = new VoteGiftItem [i];
			for (VoteGiftItemGroup group : vgs) {
				for (VoteGiftItem item : group.getItems()){
					if(item.rank == rank) {
						voteGift[j] = item;
						j ++;
					}
				}
			}
			return voteGift;
		} else {
			return null;
		}
    }
    
    public Collection<VoteGiftItemGroup> getVoteGfitItems (int type) {
		if (type == VoteGiftItemGroups.ENTRY) {
			return voteGiftItems.values();
		} else if (type == VoteGiftItemGroups.VOTE) {
			return votesKingGiftItems.values();
		}
		return null;
    }
    
    public ArrayList<VoteInfo> getVoteSet (int voteType) {
		return voteMap.get(voteType);
	}
    
    public void putVoteMap (int key, ArrayList<VoteInfo> value) {
    	voteMap.put(key, value);
    }
    public Map<Integer, ArrayList<VoteInfo>> getVoteMap () {
    	return voteMap;
    }
    
    public void putVoteContentMap (int key, Map<Integer, VoteShowInfo> value) {
    	voteContentMap.put(key, value);
    }
    public Map<Integer, Map<Integer,VoteShowInfo>> getVoteContentMap () {
    	return voteContentMap;
    }
    
    public ArrayList <VotesKing> getVotesKingSet (int voteType) {
		return votesKingMap.get(voteType);
	}
    public void putVotesKingMap (int key, ArrayList<VotesKing> value) {
    	votesKingMap.put(key, value);
    }
    public Map <Integer, ArrayList <VotesKing> > getVotesKingMap () {
    	return votesKingMap;
    }
	
	public Map <Integer, VoteKingInfo> getVotesKingInfo (int voteType) {
		return votesKingInfoMap.get(voteType);
	}
	public void putVotesKingInfoMap (int key, Map <Integer, VoteKingInfo> value) {
		votesKingInfoMap.put(key, value);
	}
	public Map <Integer, Map <Integer, VoteKingInfo> > getVotesKingInfoMap () {
		return votesKingInfoMap;
	}
	
	public void setEndRoar(String endRoar){
		this.endRoar = endRoar;
	}
	public String getEndRoar(){
		return endRoar;
	}
	public void setEndWorld(String endWorld){
		this.endWorld = endWorld;
	}
	public String getEndWorld(){
		return endWorld;
	}
	
}
