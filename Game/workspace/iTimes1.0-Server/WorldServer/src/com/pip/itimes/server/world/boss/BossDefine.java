package com.pip.itimes.server.world.boss;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import com.pip.itimes.server.world.question.Question;

/**
 * @author wpjiang
 *世界boss的时间还有地点定义
 */

public class BossDefine{
	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getHpMax() {
		return hpMax;
	}

	public void setHpMax(int hpMax) {
		this.hpMax = hpMax;
	}

	public int getMpMax() {
		return mpMax;
	}

	public void setMpMax(int mpMax) {
		this.mpMax = mpMax;
	}

	/**
	 * 对象全局索引
	 */
	private int groupId;
	/**
	 * 血量比值
	 */
	private int hpMax;
	/**
	 * 蓝量比值
	 */
	private int mpMax;
	/**
	 * 刷新时间
	 */
	private long refreshTime;
	
	/**
	 * 最大级别
	 */
	private int maxLevel;
	
	public int getMaxLevel(){
	    return maxLevel;
	}
	
	public void setMaxLevel(int maxLevel){
	    this.maxLevel = maxLevel;
	}
	
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	/**
	 * 起始时间
	 */
	private long startTime;
	/**
	 * 终止时间
	 */
	private long endTime;
	/**
	 * 是否已经加载过
	 */
	private byte load;
	
	
	/**
	 * 刷新计数器，用于控制战斗结束后的boss销毁
	 * 
	 */
	private byte trick;
	
	public byte getTrick() {
		return trick;
	}

	public void setTrick(byte trick) {
		this.trick = trick;
	}

	/**
	 * 最终目标生成地图
	 */
	private int mapId;
	
	
	/**
	 * 当前刷新时间
	 */
	private long currentRefreshTime;
	
	public long getCurrentRefreshTime() {
		return currentRefreshTime;
	}
	
	/**
	 * 世界boss使用的装备id。只有boss可用
	 */
	private int bossEquid;
	
	public int getBossEquid() {
		return bossEquid;
	}

	public void setBossEquid(int bossEquid) {
		this.bossEquid = bossEquid;
	}

	public void setCurrentRefreshTime(long currentRefreshTime) {
		this.currentRefreshTime = currentRefreshTime;
	}

	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	public byte getLoad() {
		return load;
	}

	public void setLoad(byte load) {
		this.load = load;
	}

	public static final byte BOSS_UNLOAD = 0;
	public static final byte BOSS_LOAD = 1;
	
	public static final byte BOSS_BATTLE = 2;
	public static final byte BOSS_DETROY = 3;

	private Vector<BossMapDefine> bossDefine = new Vector<BossMapDefine>();
	
	public int getBossDefineMapSize(){
		return bossDefine.size();
		
	}
	public BossDefine(int groupId, int hpMax, int mpMax, long refreshTime, long startTime, long endTime, long preTime){
		
		this.groupId = groupId;
		this.hpMax = hpMax;
		this.mpMax = mpMax;
		this.refreshTime = refreshTime;
		this.startTime = startTime;
		this.currentRefreshTime = startTime;
		this.endTime = endTime;
		this.preFreshChatTime = preTime;
	}
	
	public void addBossDefine(int mapId, int mapX, int mapY, int rect){
		BossMapDefine bossMapDefine = new BossMapDefine(mapId, mapX, mapY, rect);
		bossDefine.add(bossMapDefine);
	}
	
	/**
	 * @param index
	 * @return获得重新定义的地图号， 地图x, 地图y,矩形大小
	 */
	public int[] getBossMapDefine(int index){
		int[] map = null;
		if(index > bossDefine.size() || bossDefine.size() == 0){
			return map;
		}
		
		BossMapDefine bossMapDefine = bossDefine.get(index);
		map = new int[4];
		map[0] = bossMapDefine.getMapId();
		map[1] = bossMapDefine.getMapX();
		map[2] = bossMapDefine.getMapY();
		map[3] = bossMapDefine.getRect();
		
		return map;
	}
	
	private class BossMapDefine{
		private int mapX;
		private int mapY;
		private int mapId;
		private int rect;
		private BossMapDefine(int mapId, int mapX, int mapY, int rect){
			this.mapId = mapId;
			this.mapX = mapX;
			this.mapY = mapY;
			this.rect = rect;
		}
		
		public int getRect(){
			return rect;
		}
		public int getMapX() {
			return mapX;
		}


		public int getMapY() {
			return mapY;
		}


		public int getMapId() {
			return mapId;
		}

	}
	
	  /**
     * @return是否处在开始时间内
     */
    public boolean inTime(){
        long second = getNowTime();
        if(second >= startTime && second <= endTime){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    
    /**
     * @return是否处在开始时间内
     */
    public  boolean needFresh(){
        long nowTime = getNowTime();
        if((nowTime >= currentRefreshTime && nowTime <= endTime) && needRecreate){
        	needRecreate = false;
            return true;
        }else{
        	return false;
        }
        
    }
    
    private long preFreshChatTime;
   
    /**
     * @return是否需要提前喊话
     */
    public  boolean needPreChat(){
    	
        long nowTime = getNowTime();
        if (nowTime >=  currentRefreshTime - preFreshChatTime
        		&& nowTime <= currentRefreshTime + refreshTime
        			&& nowTime <= endTime
        				&& needRecreate) {
            if(preChat == false){
            	preChat = true;
            	return true;
            }
        }else{
        	return false;
        }
		return false;
        
    }
    
    /**
     * 重新设置刷新时间  防止中途重启的时候用
     */
    public void resetRefreshTime(){
    	long nowTime = getNowTime();
    	
    	//计算是第几次刷新，，然后配置成正确的时间
    	int trick = (int) ((nowTime - startTime) / refreshTime + 1);
    	
    	setTrick((byte) trick);
    	currentRefreshTime = startTime + trick * refreshTime;
    	preChat = false;
    }
    
    public void setNextFreshTime () {
		long nowTime = getNowTime();
    	
    	//计算是第几次刷新，，然后配置成正确的时间
    	int trick = (int) ((nowTime - startTime) / refreshTime + 1);
    	
    	setTrick((byte) trick);
    	currentRefreshTime = startTime + trick * refreshTime;
    }
    
    /**
     * 获得当前时间的秒数
     * @return
     */
    public long getNowTime(){
    	Date date = new Date();
    	int hour = date.getHours();
    	long twelveSecond = 12 * 60 * 60;//12个小时秒
    	Calendar cal = Calendar.getInstance();
    	cal.setTimeInMillis(System.currentTimeMillis());
    	long currentSecond = cal.get(Calendar.SECOND) + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.HOUR) * 60 * 60;
    	if(hour > 12){
    		currentSecond += twelveSecond;
    	}
    	return currentSecond;
    }
    
    private boolean preChat = false;
	public void setPreChat(boolean preChat) {
		this.preChat = preChat;
	}
    
	 /**
     * 重新设置刷新时间  防止中途重启的时候用
     */
    public void resetCurrentRefreshTime(){
    	currentRefreshTime = startTime;
    }
    
    /**
     * 是否需要重置，用于boss产生的东西消失
     */
    public boolean needRecreate = true;
    
	public boolean isNeedRecreate() {
		return needRecreate;
	}

	public void setNeedRecreate(boolean needRecreate) {
		this.needRecreate = needRecreate;
	}
	
	public boolean firstTime = true;
    
}

	
