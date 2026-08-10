package com.pip.itimes.server.world.riddles;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;

public class RiddlesConfig2 {
	static private Logger log = Logger.getLogger(RiddlesConfig2.class);
	
	public static PlayerService playerService = null;
	
	public static int startYear;
	public static int startMonth;
    public static int startDay;
    public static int endYear;
    public static int endMonth;
    public static int endDay;
    public static long startTime;
    public static long endTime;
    public static long dayStartTime;
    public static long dayEndTime;
    
    public static int startHour;
    public static int startMinute;
    public static int startSecond;
    public static int endHour;
    public static int endMinute;
    public static int endSecond;
    
    public static int hourTime = 5;
    
    
    public static ArrayList<Riddles> riddles;
    
    public static int currentDayTime = 0;		//当前次数
    public static int currentIndex = 0;			//当前歌的序号
    public static long startActionTime = 0;		//活动开始计算时间
    public static long endActionTime = 0;		//活动结束时间
    public static boolean endAction = false;	//活动是否结束
    public static int hourCount = 0;			//过去的小时数
    
    public static Hashtable<Integer, Riddles> oldRiddle = new Hashtable<Integer, Riddles>();		//记录着猜过的谜
    
    public static HashMap<Integer, String> rightPlayer = new HashMap<Integer, String>();	//记录答对的玩家
    public static ArrayList<String> right5Player = new ArrayList<String>();				//前5名答对的玩家
    
    public static long TIME = 60 * 1000 * 1;		//每次活动时间
//    public static final long HOURTIME = 60 * 60 * 1000;	//一小时
    public static long HOURTIME = 7 * 60 * 1000;	//一小时
    public static long TIME5 = 1 * 60 * 1000;		//5分钟
    
    public static final byte NOTICEMESSAGE = 0;			//5分钟提前公告活动开始状态
    public static final byte WAITACTION = 1;			//等待活动开始状态
    public static final byte ACTIONSTART = 2;			//活动开始状态
    public static final byte ACTIONEND = 3;				//活动结束状态
    
    public static boolean sendCredit = false;		//发送了荣誉包
    public static final int CREDIT_ID = 201095;		//10000点荣誉包ID
    public static final int GIFT_ID = 201237;		//咏春礼盒
    public static final int FIRST_ID = 201221;		//裂开的蛋蛋 第一名获得物品
    public static final int GIFT5_ID = 201222;		//前5名送的物品ID 修正石
    public static final int PLAYERCOUNT = 5;		//特别玩家个数
    
    public static byte state = NOTICEMESSAGE;
    public static Random rnd = new Random();
    
    public static final String ACTION_NAME = "咏春诗歌大赛";
    
    public static void reset(){
    	state = NOTICEMESSAGE;
    	currentIndex = 0;
    	endAction = false;
    	currentDayTime = 0;
    	oldRiddle.clear();
    	rightPlayer.clear();
    	right5Player.clear();
    	hourCount = 0;
    	sendCredit = false;
    }
    
    public static void reload(){
    	long now = System.currentTimeMillis();
    	if(now < dayStartTime || now >= dayEndTime){
    		hourCount = 0;
    		currentDayTime = 0;
    		state = NOTICEMESSAGE;
    		log.info("state = NOTICEMESSAGE");
    		return;
    	}
    	hourCount = getHourCount(now);
    	currentDayTime = getCurrentTime(now);
    	if(currentDayTime >= hourTime){
    		state = ACTIONEND;
    		endAction = true;
    		log.info("state = ACTIONEND");
    	}else if(currentDayTime >= 0){
	    	state = ACTIONSTART;
	    	log.info("state = ACTIONSTART");
    	}else{
    		state = NOTICEMESSAGE;
    		currentDayTime = 0;
    		hourCount = 0;
    		log.info("state = NOTICEMESSAGE");
    	}
    }
    
    static public int getHourCount(long now){
    	int count = (int)((now - dayStartTime) / HOURTIME);
    	if(now >= dayStartTime + count * HOURTIME + TIME * hourTime){
    		count ++;
    	}
    	return count;
    }
    
    static public int getCurrentTime(long start){
    	int hcount = (int)((start - dayStartTime) / HOURTIME);
    	if(start >= dayStartTime + hcount * HOURTIME + TIME * hourTime){
    		return hourTime;
    	}
    	int count = (int)((start - (dayStartTime + hcount * HOURTIME)) / TIME);
    	if(count >= hourTime){
    		count = hourTime;
    	}
    	return count;
    }
    
    /**
     * 开始活动的时候调用一次
     */
    public static void startAction(){
    	startActionTime = new Date().getTime();
    	endActionTime = startActionTime + TIME;
    	currentIndex = 0;
    	endAction = false;
    	sendCredit = false;		//重置万点荣誉包
    	setNextRiddle();
    	hourCount = getHourCount(startActionTime);
    	currentDayTime = getCurrentTime(startActionTime);
    	if(currentDayTime >= hourTime){
    		state = ACTIONEND;
    		endAction = true;
    	}else{
	    	startCurrent();
	    	state = ACTIONSTART;
    	}
    }
    
    /**
     * 设置下一首歌 不能重复
     */
    public static void setNextRiddle(){
    	while(true){
    		int index = Utils.getRandom(rnd, 0, riddles.size() - 1);
    		if(oldRiddle.size() >= riddles.size()){
    			oldRiddle.clear();
    		}
    		if(!oldRiddle.containsKey(new Integer(index))){
    			currentIndex = index;
    			oldRiddle.put(new Integer(index), riddles.get(index));
    			return;
    		}
    	}
    }
    
    public static boolean nextAction(boolean start){
		currentDayTime ++;
		if(start){
			startCurrent();
		}
		startActionTime = new Date().getTime();
		endActionTime = startActionTime + TIME;
		if(currentDayTime >= hourTime){
			endAction = true;
			state = ACTIONEND;
			hourCount ++;
			return false;
		}
		return true;
    }
    
    public static void startCurrent(){
    	Riddles riddle = (Riddles)riddles.get(currentIndex);
    	StringBuffer sb = new StringBuffer();
    	if(currentDayTime == 0){
    		sb.append(riddle.getQuestion());
    		sb.append("，请接下一句(");
    		sb.append(riddle.getAnswer().length());
    		sb.append("个字)。");
    	}else{
    		sb.append("下一句应该是“");
    		sb.append(riddle.getAnswer());
    		
    		int size = 5;
    		if(rightPlayer.size() < size){
    			size = rightPlayer.size();
    		}
    		if(size == 0){
    			sb.append("”，很遗憾没有人答对，要加油哦~");
    		}else{
    			sb.append("”，你答对了吗？");
    			sb.append("前五名答对的玩家为：");
    			ArrayList<Map.Entry<Integer, String>> list = new ArrayList<Map.Entry<Integer, String>>(rightPlayer.entrySet());
	    		for(int i=0; i<size; i++){
	    			sb.append(right5Player.get(i));
	    			if(i + 1 == size){
	    				sb.append("。");
	    			}else{
	    				sb.append("，");
	    			}
	    		}
	    		try{
		    		for(int i=0; i<list.size(); i++){
		    			//发奖品
		    			//没有出现过万吨荣誉包时 尝试获取
		    			int rndValue = 0;
		    			Entry<Integer, String> entry = list.get(i);
		    			int playerid = (Integer)entry.getKey();
		    			WorldPlayer player = Server.instance.playerService.getWorldPlayerAndCatch(playerid);
		    			if(player != null){
			    			if(!sendCredit){
			    				rndValue = Utils.getRandom(rnd, 1, 100);
			    				if(rndValue == 1){
			    					IItem iit = Items.getTemplate(CREDIT_ID).newInstance();
			    					if(iit != null){
				    					byte[] att = ItemUtils.item2dbAttachment(iit, 1);
				        				Server.instance.mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
				        						iit.getName() + "*" + 1, "您在" + ACTION_NAME + "活动中获得了一个" + iit.getName() + "！这可是能一次性获得10000点荣誉的荣誉包啊！", att, 0, true);
				        				Server.instance.chatService.sendRoarMessage( -1, "狮子吼", "哇~！这不是真的吧！" + player.getPlayerName() + "在" + ACTION_NAME + "活动中获得了一个" + iit.getName() + "！这可是能一次性获得10000点荣誉的荣誉包啊！", true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
				        				sendCredit = true;
				        				log.info("RiddlesConfig2 sendGiftCredit to playerId[" + player.getId()
				        						+ "] playerName[" + player.getPlayerName() + "] get ItemId[" +
				        						iit.getItemId() + "] ItemName [" + iit.getName() + "] ItemCount[" + 1 + "]");
			    					}
			    				}
			    			}
			    			for(int n=0; n<right5Player.size(); n++){
			    				if(player.getPlayerName().equals(right5Player.get(n))){
			    					if(n == 0){
			    						IItem iit = Items.getTemplate(FIRST_ID).newInstance();
			    						int count = 1;
			    						byte[] att = ItemUtils.item2dbAttachment(iit, count);
		    							Server.instance.mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
		    									iit.getName() + "*" + count, "您在" + ACTION_NAME + "活动中获得了第一名，这是额外奖励，请再接再厉哦~", att, 0, true);
		    							log.info("RiddlesConfig2 sendGift5 to playerId[" + player.getId()
		    									+ "] playerName[" + player.getPlayerName() + "] get ItemId[" +
		    									iit.getItemId() + "] ItemName [" + iit.getName() + "] ItemCount[" + count + "]");
			    					}
		    						IItem iit = Items.getTemplate(GIFT5_ID).newInstance();
		    						int count = 1;
		    						byte[] att = ItemUtils.item2dbAttachment(iit, count);
		    						Server.instance.mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
		    								iit.getName() + "*" + count, "您在" + ACTION_NAME + "活动中获得了前五名，这是额外奖励，请再接再厉哦~", att, 0, true);
		    						log.info("RiddlesConfig2 sendGift5 to playerId[" + player.getId()
		    								+ "] playerName[" + player.getPlayerName() + "] get ItemId[" +
		    								iit.getItemId() + "] ItemName [" + iit.getName() + "] ItemCount[" + count + "]");
			    				}
			    			}
	    					IItem iit = Items.getTemplate(GIFT_ID).newInstance();
	    					if(iit != null){
		    					byte[] att = ItemUtils.item2dbAttachment(iit, 1);
		        				Server.instance.mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
		        						iit.getName() + "*" + 1, "您在" + ACTION_NAME + "活动答对了答案，这是奖励，请再接再厉哦~", att, 0, true);
		        				log.info("RiddlesConfig2 sendItem to playerId[" + player.getId()
		        						+ "] playerName[" + player.getPlayerName() + "] get ItemId[" +
		        						iit.getItemId() + "] ItemName [" + iit.getName() + "] ItemCount[" + 1 + "]");
	    					}
		    			}
    					Server.instance.playerService.releasePlayer(player);
		    		}
	    		}catch(Exception e){
	    		}
    		}
    		if(currentDayTime >= hourTime){
    			if((hourCount + 1) * HOURTIME + dayStartTime >= dayEndTime){
    				if(dayStartTime + 24 * 60 * 60 * 1000 >= endTime){
    					sb.append("本次的" + ACTION_NAME + "活动于今天全部结束了，明年再会吧。");
    				}else{
    					sb.append("今日的活动已经结束，明天" + getFormatTime(startHour) + ":" + getFormatTime(startMinute) + "继续哦~");
    				}
    			}else{
    				sb.append("本轮活动结束，下一个整点继续。");
    			}
    		}else{
	    		sb.append("下面开始下一句诗歌。");
	    		setNextRiddle();
	    		riddle = (Riddles)riddles.get(currentIndex);
	    		sb.append(riddle.getQuestion());
	    		sb.append("(");
	    		sb.append(riddle.getAnswer().length());
	    		sb.append("个字)。");
    		}
    		//清除掉猜对的玩家
    		rightPlayer.clear();
    		right5Player.clear();
    	}
    	Server.instance.chatService.sendSystemMessage(sb.toString());
    }
    
    public static String getFormatTime(int time){
    	String str_time = "" + time;
    	if(str_time.length() < 2){
    		str_time = "0" + str_time;
    	}
    	return str_time;
    }
    
    public static void playerChat(String msg, WorldPlayer player){
    	if(state != ACTIONSTART) return;
    	Riddles riddle = (Riddles)riddles.get(currentIndex);
    	if(msg.equals(riddle.getAnswer())){
    		if(!rightPlayer.containsKey(new Integer(player.getId()))){
    			if(right5Player.size() < PLAYERCOUNT){
    				right5Player.add(player.getPlayerName());
    			}
    			rightPlayer.put(new Integer(player.getId()), player.getPlayerName());
    			log.info("WorldChat RiddlesAction PlayerID[" + player.getId() + "]" + " RiddlesIndex[" + currentIndex + "] Segment[" + (currentDayTime + 1) + "]");
    		}
    	}
    }
    
    public static void startNoticeMessage(){
    	state = WAITACTION;
    	Server.instance.chatService.sendRoarMessage( -1, "狮子吼", TIME5 / 60000 + "分钟后开始“" + ACTION_NAME + "”活动，" + TIME / 60000 + "分钟内在世界聊里喊出正确答案就有丰厚的奖励哦，前5名还有额外奖励哦。", true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
    }
    
    /**
     * 重置时间 启动和结束的时候计算
     */
    static public void resetTime(){
    	startTime = getConfigDate(true);
    	endTime = getConfigDate(false);
    	dayStartTime = getDayDate(true);
    	dayEndTime = getDayDate(false);
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
	
	static public long getDayDate(boolean start){
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if(start){
			cal.set(Calendar.HOUR_OF_DAY, startHour);
			cal.set(Calendar.MINUTE, startMinute);
			cal.set(Calendar.SECOND, startSecond);
		}else{
			cal.set(Calendar.HOUR_OF_DAY, endHour);
			cal.set(Calendar.MINUTE, endMinute);
			cal.set(Calendar.SECOND, endSecond);
		}
		return cal.getTime().getTime();
	}
	
	static public void action(long now){
//		log.info("dayStartTime:" + new Date(dayStartTime).toString() + " dayEndTime:" + new Date(dayEndTime));
		if(now < startTime || now >= endTime) return;
        if(state == NOTICEMESSAGE && now < dayEndTime - TIME * hourTime && now + TIME5 >= dayStartTime){
        	log.info("state[NOTICEMESSAGE]:startNoticeMessage()");
        	startNoticeMessage();
        }else if(state == NOTICEMESSAGE && now >= dayStartTime){
        	log.info("state[NOTICEMESSAGE]:state = WAITACTION");
        	state = WAITACTION;
        }else if(state == WAITACTION && now >= dayStartTime + hourCount * HOURTIME && now < dayEndTime){
        	log.info("state[WAITACTION]:startAction()");
        	startAction();
        }else if(state == ACTIONSTART && !endAction && now >= endActionTime){
        	log.info("state[ACTIONSTART]:nextAction()");
        	nextAction(true);
        }else if(state == ACTIONEND && now < dayEndTime - TIME * hourTime && now + TIME5 >= dayStartTime + hourCount * HOURTIME){
        	log.info("state[ACTIONEND]:startNoticeMessage()");
        	startNoticeMessage();
        }
	}
}
