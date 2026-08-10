package com.pip.itimes.server.world.chr;

import java.util.Calendar;
import java.util.Date;

import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.ChatService;
import com.pip.itimes.server.world.ChristmasProcessor;

public class ChristmasConfig {
	public static int startYear;
	public static int startMonth;
    public static int startDay;
    public static int endYear;
    public static int endMonth;
    public static int endDay;
    public static int segment;			//段数
    public static int currentSegment = -1;		//当前使用段
    public static int lastSegment;		// 最近一次的状态
    public static int giftId;
    public static int itemid;			//每捐一次获得的物品ID
    public static ChristmasAuctionConfig[] auctionConfig;
    
    public static ChatService chatService;
    
    public static long startTime;
    public static long endTime;
    
    /**
	 * 查看光明阵营排行榜
	 */
    public static final int VIEW_BRIGHT_LIST = 6;
	/**
	 * 查看黑暗阵营排行榜
	 */
    public static final int VIEW_DARK_LIST = 7;
    /**
	 * 活动停止
	 */
	public static final int STAGE_NOT_STARTED = -1;
	/**
	 * 排行榜人数
	 */
	public static final int TOP = 10;
    
    /**
	 * 计算当前时间段是哪段
	 * 如果没有找到属于的时间段 返回-1
	 * @return
	 */
	public static int calcCurrentSegment(){
		Date now = new Date();
		long time = now.getTime();
		
		//活动没有开始或是结束后总是返回-1
		if(time < startTime || time > endTime){
			return STAGE_NOT_STARTED;
		}
		
		for(int i=0; i<ChristmasConfig.segment; i++){
//			Date startDate = getConfigDate(true, i);
//			Date endDate = getConfigDate(false, i);
//			if (startDate != null && endDate != null) {
//				if(time >= startDate.getTime() && time < endDate.getTime()){
				if(time >= auctionConfig[i].getStartTime() && time < auctionConfig[i].getEndTime()){
					if (currentSegment == STAGE_NOT_STARTED) {
						// 下轮开始清楚数据
						ChristmasProcessor.darkChrItemPlayer.clear();
						ChristmasProcessor.brightChrItemPlayer.clear();
						ChristmasProcessor.darkChrItemTotal = 0;
						ChristmasProcessor.brightChrItemTotal = 0;
						// 设置奖品
						setGiftId(ChristmasConfig.auctionConfig[i].getGiftId());
						sendChat(true, i);
					}
					return i;
				}
//			}
		}
		if (currentSegment > STAGE_NOT_STARTED) {
			ChristmasProcessor.setTopList();
			sendChat(false, currentSegment);
			sendResult(getVictoryCamp());
		}
		return STAGE_NOT_STARTED;
	}
	
	public static void setGiftId (int id) {
		giftId = id;
	}
	
	/**
	 * 设置当前使用的时间段 没有使用时为-1
	 * @param segment
	 */
	public static void setCurrentSegment(int segment){
		currentSegment = segment;
	}
	
	/**
	 * 重置时间 0点的时候调用
	 */
	public static void resetTime(){
		for(int i=0; i<segment; i++){
			auctionConfig[i].resetTime();
		}
	}
	
	public static void setLastSegment (int segment) {
		lastSegment = segment;
	}
	
	static public Date getConfigDate(boolean start, int segment){
		if(segment < 0 || segment >= ChristmasConfig.segment) {
			return null;
		}
		ChristmasAuctionConfig config = ChristmasConfig.auctionConfig[segment];
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.SECOND, 0);
		if(start){
			cal.set(Calendar.HOUR_OF_DAY, config.getStartHour());
			cal.set(Calendar.MINUTE, config.getStartMinute());
		}else{
			cal.set(Calendar.HOUR_OF_DAY, config.getEndHour());
			cal.set(Calendar.MINUTE, config.getEndMinute());
		}
		return cal.getTime();
	}
	
	/**
	 * 获得开始时间的消息
	 * @return
	 */
	static public String getAdMessage(int segment){
		if(segment < 0 || segment >= auctionConfig.length){
			return null;
		}
		return auctionConfig[segment].getAdMessage();
	}
	/**
	 * 获得结束时间的消息
	 * @return
	 */
	static public String getEndMessage(int segment){
		if(segment < 0 || segment >= auctionConfig.length){
			return null;
		}
		return auctionConfig[segment].getEndMessage();
	}
	
	static public void setChatService(ChatService service){
		chatService = service;
	}
	
	static public void sendChat(boolean start, int segment){
		if(chatService == null) return;
		String msg = start ? getAdMessage(segment) : getEndMessage(segment);
		if(msg == null) return;
		chatService.sendRoarMessage( -1, "狮子吼", msg, true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
	}
	
	public static void sendResult (int campType) {
		String campBrightName = "光明阵营";
		String campDark = "黑暗阵营";
		String msg = null;
		switch (campType) {
		case Utils.CAMP_BRIGHT:
			msg = campDark + "完败了，" + campBrightName + "获得了最终的胜利!";
			break;
		case Utils.CAMP_DARK:
			msg = campBrightName + "完败了，" + campDark + "获得了最终的胜利!";
			break;
		case -1:
			msg = "很遗憾，" + "由于" + campDark + "与" + campBrightName + "势均力敌，所以没有赢得最终奖励。";
			break;
		}
		if (msg != null) {
			chatService.sendRoarMessage( -1, "狮子吼", msg, true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
		}
	}
	
	public static int getVictoryCamp () {
		int ret = 0;
		if (ChristmasProcessor.darkChrItemTotal > ChristmasProcessor.brightChrItemTotal) {
			ret = Utils.CAMP_DARK;
		} else if (ChristmasProcessor.brightChrItemTotal > ChristmasProcessor.darkChrItemTotal) {
			ret = Utils.CAMP_BRIGHT;
		} else {
			ret = -1;
		}
		return ret;
	}
	
    /**
     * 重置时间 启动的时候计算
     */
    static public void resetActTime(){
    	startTime = getConfigDate(true);
    	endTime = getConfigDate(false);
    }
    
	static public long getConfigDate(boolean start){
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if(start){
			cal.set(Calendar.YEAR, startYear);
			cal.set(Calendar.MONTH, startMonth - 1);
			cal.set(Calendar.DAY_OF_MONTH, startDay);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
		}else{
			cal.set(Calendar.YEAR, endYear);
			cal.set(Calendar.MONTH, endMonth - 1);
			cal.set(Calendar.DAY_OF_MONTH, endDay);
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
		}
		return cal.getTime().getTime();
	}
}
