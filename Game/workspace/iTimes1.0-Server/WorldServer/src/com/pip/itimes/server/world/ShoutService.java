package com.pip.itimes.server.world;

import java.util.Date;
import java.util.Random;

import org.apache.log4j.Logger;

import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.ShoutConfig;
import com.pip.itimes.server.util.Utils;

public class ShoutService {
	private static final Logger log = Logger.getLogger(ShoutService.class);
	public static final String WORLD = "WORLD";
	public static final String CAMP = "CAMP";
	public static final int SPECIAL_ITEM_ID = 201095;
	/**
	 * 一次活动获得最大特殊物品个数
	 */
	public static final int MAXIMUM_NUMBER_SPECIAL_ITEM = 1;
	/**
	 * 重置特殊物品
	 */
	public static final int RESET_OUTPUT = -1;
	/**
	 * 获得特殊物品的概率
	 */
	public static final int RATE = 5;
	/**
	 * 活动停止
	 */
	public static final int STAGE_NOT_STARTED = -1;
	/**
	 * 本次活动产出量
	 */
	public static int outPut;
	/**
	 * 当前状态
	 */
	public static int currentSegment;
	/**
	 * 世界聊礼物Id
	 */
	private int worldGiftId;
	/**
	 * 阵营聊礼物Id
	 */
	private int campGiftId;
	/**
	 * 世界聊特殊礼物Id
	 */
	private int worldSpecialId;
	/**
	 * 阵营聊特殊礼物Id
	 */
	private int campSpecialId;
	/**
	 * 阵营喊话内容
	 */
	private String campContent;
	/**
	 * 世界喊话内容
	 */
	private String worldContent;
	/**
	 * 世界聊喊话地点
	 */
	private int worldMapId;
	/**
	 * 阵营聊喊话地点
	 */
	private int campMapId;
	
	protected MailService mailService;
	protected ChatService chatService;
	
	public ShoutService () {
	}
	 
	public void setMailService (MailService mailService) {
      this.mailService = mailService;
	}
	 
	public void setChatService (ChatService chatService) {
		this.chatService = chatService;
	}
	
	/**
	 * 设置世界聊礼物Id
	 * @param worldGiftId
	 */
	public void setWorldGiftId (int worldGiftId) {
		this.worldGiftId = worldGiftId;
	}
	
	/**
	 * 获得世界聊礼物Id
	 * @return
	 */
	public int getWorldGiftId () {
		return worldGiftId;
	}
	
	/**
	 * 设置阵营聊礼物Id
	 * @param campGiftId
	 */
	public void setCampGiftId (int campGiftId) {
		this.campGiftId = campGiftId;
	}
	
	/**
	 * 获得阵营聊礼物Id
	 * @return
	 */
	public int getCampGiftId () {
		return campGiftId;
	}
	
	/**
	 * 设置世界聊特殊礼物Id
	 * @return
	 */
	public void setWorldSpecialId (int worldSpecialId) {
		this.worldSpecialId = worldSpecialId;
	}
	
	/**
	 * 获得世界聊特殊礼物Id
	 * @return
	 */
	public int getWorldSpecialId () {
		return worldSpecialId;
	}
	
	/**
	 * 设置阵营聊特殊礼物Id
	 * @return
	 */
	public void setCampSpecialId (int campSpecialId) {
		this.campSpecialId = campSpecialId;
	}
	
	/**
	 * 获得阵营聊特殊礼物Id
	 * @return
	 */
	public int getCampSpecialId () {
		return campSpecialId;
	}
	
	/**
	 * 设置世界聊内容
	 * @param worldContent
	 */
	public void setWorldContent (String worldContent) {
		this.worldContent = worldContent;
	}
	
	/**
	 * 获得世界聊内容
	 * @return
	 */
	public String getWorldContent () {
		return worldContent;
	}
	
	/**
	 * 设置阵营聊内容
	 * @param campContent
	 */
	public void setCampContent (String campContent) {
		this.campContent = campContent;
	}
	
	/**
	 * 获得阵营聊内容
	 * @return
	 */
	public String getCampContent () {
		return campContent;
	}
	
	/**
	 * 设置阵营聊地点
	 */
	public void setCampMapId (int campMapId) {
		this.campMapId = campMapId;
	}
	
	/**
	 * 获得阵营聊地点
	 */
	public int getCampMapId () {
		return campMapId;
	}
	
	/**
	 * 设置阵营聊地点
	 */
	public void setWorldMapId (int worldMapId) {
		this.worldMapId = worldMapId;
	}
	
	/**
	 * 获得阵营聊地点
	 */
	public int getWorldMapId () {
		return worldMapId;
	}
	
	/**
	 * 检查活动是否开始，没有开始返回-1,开始返回当天的第几次活动
	 * @return 
	 */
	public int checkEffectivePeriod () {
		Date now = new Date();
		long time = now.getTime();
		
		if (time < ShoutConfig.startDate.getTime() || time > ShoutConfig.endDate.getTime()) {
			return STAGE_NOT_STARTED;
		} else {
			for (int i = 0; i < ShoutConfig.segment; i++) {
				if (time >= ShoutConfig.shoutActivityConfig[i].getActivityStartTime().getTime()
						&& time < ShoutConfig.shoutActivityConfig[i].getActivityEndTime().getTime()) {
					if (currentSegment == STAGE_NOT_STARTED) {
						sendActivityChat(true, i);
						setResetData(i);
					}
					return i;
				}
			}
			if (currentSegment > STAGE_NOT_STARTED) {
				sendActivityChat(false, currentSegment);
			}
		}
		return STAGE_NOT_STARTED;
	}
	
	/**
	 * 重置数据
	 * @param index
	 */
	public void setResetData (int index) {
		setWorldGiftId(ShoutConfig.shoutActivityConfig[index].getShoutChat(WORLD).getGiftId());
		setCampGiftId(ShoutConfig.shoutActivityConfig[index].getShoutChat(CAMP).getGiftId());
		setWorldSpecialId(ShoutConfig.shoutActivityConfig[index].getShoutChat(WORLD).getSpecialId());
		setCampSpecialId(ShoutConfig.shoutActivityConfig[index].getShoutChat(CAMP).getSpecialId());
		setWorldContent(ShoutConfig.shoutActivityConfig[index].getShoutChat(WORLD).getMessage());
		setCampContent(ShoutConfig.shoutActivityConfig[index].getShoutChat(CAMP).getMessage());
		setWorldMapId(ShoutConfig.shoutActivityConfig[index].getShoutChat(WORLD).getMapId());
		setCampMapId(ShoutConfig.shoutActivityConfig[index].getShoutChat(CAMP).getMapId());
		setOutPut(RESET_OUTPUT);
	}
	
	/**
	 * 活动通知
	 * @param start
	 * @param segment
	 */
	public void sendActivityChat (boolean start, int segment) {
		if(chatService != null) {
			String msg = getActivityMessage(start, segment);
			if (msg != null) {
				chatService.sendRoarMessage( -1, "狮子吼", msg, true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
			}
		}
	}
	
	/**
	 * 获得活动的通知
	 * @param start
	 * @param segment
	 * @return
	 */
	public String getActivityMessage (boolean start, int segment) {
		if (segment >= 0 && segment < ShoutConfig.shoutActivityConfig.length) {
			if (start) {
				return ShoutConfig.shoutActivityConfig[segment].getStartMessage();
			} else {
				return ShoutConfig.shoutActivityConfig[segment].getEndMessage();
			}
		} else {
			return null;
		}
	}
	
	/**
	 * 设置当前活动次数 -1无活动
	 * @param segment
	 */
	public void setCurrentSegment (int segment) {
		currentSegment = segment;
	}
	
	/**
	 * 设置本次活动产出量
	 * @param outPut
	 */
	public void setOutPut (int count) {
		outPut = count;
	}
    
    /**
     * 发送喊话后的礼物,首先根据本次产出判断是否可以发送特殊礼物
     * @param player
     * @param giftId
     */
    public void sendChatGift (WorldPlayer player, String chatChannle) {
    	if (player != null) {
    		synchronized (player) {
    			IItem iit;
    			int giftId;
    			Random rnd = new Random();
    			if (outPut < 0 && Utils.hit(rnd, RATE, 100)) {
    				setOutPut(MAXIMUM_NUMBER_SPECIAL_ITEM);
    				giftId = chatChannle.equals(WORLD) ? getWorldSpecialId() : getCampSpecialId();
    				iit = Items.getTemplate(giftId).newInstance();
    			} else {
    				giftId = chatChannle.equals(WORLD) ? getWorldGiftId() : getCampGiftId();
    				iit = Items.getTemplate(giftId).newInstance();
    			}
    			if (iit != null) {
    				byte[] att = ItemUtils.item2dbAttachment(iit, 1);
    				mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
    						iit.getName() + "*" + 1, "", att, 0, true);
    				if (iit.getItemId() == SPECIAL_ITEM_ID) {
    					chatService.sendRoarMessage( -1, "狮子吼", "哇~！这不是真的吧！" + player.getPlayerName() + "在使用元旦礼盒后获得了一个" + iit.getName() + "！这可是能一次性获得10000点荣誉的荣誉包啊！", true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
    				}
    				log.info("ShoutService sendChatGift to playerID[" + player.getId()
    						+ "] playerName[" + player.getPlayerName() + "] get ItemId[" +
    						iit.getItemId() + "] ItemName [" + iit.getName() + "] ItemCount[" + 1 + "]");
    			} else {
    				log.info("ShoutService sendChatGift failure: Error! giftId is " + giftId);
    			}
    		}
		} else {
			log.info("ShoutService sendChatGift failure: Error! player is null");
		}
    }
    
    /**
     * 检查玩家是否完成喊话活动
     * @param player
     * @return
     */
    public boolean checkPlayerCompleted (WorldPlayer player, String chatChannle) {
    	if (chatChannle.equals(WORLD)) {
    		if (player.getLastWorldCompleteTime().getTime()
    				>= ShoutConfig.shoutActivityConfig[currentSegment].getActivityStartTime().getTime()) {
    			return true;
    		} else {
    			return false;
    		}
    	} else if (chatChannle.equals(CAMP)) {
    		if (player.getLastCampCompleteTime().getTime()
    				>= ShoutConfig.shoutActivityConfig[currentSegment].getActivityStartTime().getTime()) {
    			return true;
    		} else {
    			return false;
    		}
    	}
    	return true;
    }
    
    /**
     * 检查活动是否开始
     */
    public boolean checkStarted () {
    	if (currentSegment > STAGE_NOT_STARTED) {
    		return true;
    	}
		return false;
    }
    
    /**
     * 检查是否在指定地图喊话
     */
    public boolean checkPosition (int mapId, String chatChannle) {
    	if (chatChannle.equals(WORLD)) {
    		if (mapId == getWorldMapId()) {
    			return true;
    		}
    	} else if (chatChannle.equals(CAMP)) {
    		if (mapId == getCampMapId()) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * 检查喊话内容
     */
    public boolean checkMessageContent (String message, String chatChannle) {
    	if (message != null) {
    		String provision;
    		if (chatChannle.equals(WORLD)) {
    			provision = getWorldContent();
    			if (provision != null && message.equals(provision)) {
    				return true;
    			} 
    		} else if (chatChannle.equals(CAMP)) {
    			provision = getCampContent();
    			if (provision != null && message.equals(provision)) {
    				return true;
    			}
    		}
    	}
    	return false;
    }
}
