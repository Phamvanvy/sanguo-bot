package com.pip.itimes.server.world.activityService.activity;

import java.util.Date;

import org.apache.log4j.Logger;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.activityService.ActivityEvent;
import com.pip.itimes.server.world.activityService.ActivityEventListener;
import com.pip.itimes.server.world.activityService.ActivityServer;

/**
 * 每充值RMB元奖励活动
 * @author hchen
 *
 */
public class RechargeActivity implements IActivityImpl, ActivityEventListener {
	private static Logger log = Logger.getLogger(RechargeActivity.class);
	protected ActivityData activity;
	/**
	 * 充值活动赠送礼物ID
	 */
	private int giftId;
	/**
	 * 充值活动赠送礼物个数
	 */
	private int giftCount;
	/**
	 * 发送邮件标题
	 */
	private String mailTitle;
	/**
	 * 发送邮件内容
	 */
	private String mailContent;
	/**
	 * 充值额度
	 */
	private int amount;
	
	public RechargeActivity(ActivityData owner) {
		this.activity = owner;
	}
	
	public void startup() throws Exception {
		
		log.info("RechargeActivity ActivityName[" + activity.getName() + "] ActivityId["
				+ activity.getId() + "] ActivityStartUp[" + activity.getBeginTime() + "] ActivityShutDown["
				+ activity.getEndTime() + "] ActivityConfig[" + activity.getConfigData() + "] RegisterListener TRY!");
		
		String config = activity.getConfigData();
		if (config != null) {
			String[] str = config.split(",");
			giftId = Integer.parseInt(str[0]);
			giftCount = Integer.parseInt(str[1]);
			mailTitle = str[2];
			mailContent = str[3];
			amount = Integer.parseInt(str[4]);
		}
		ActivityServer.server.getEventManager().registerListener(this);
		
		log.info("RechargeActivity ActivityName[" + activity.getName() + "] ActivityId["
				+ activity.getId() + "] ActivityStartUp[" + activity.getBeginTime() + "] ActivityShutDown["
				+ activity.getEndTime() + "] ActivityConfig[" + activity.getConfigData() + "] RegisterListener SUCCESS!");
	}

	public void shutdown() {
		
		log.info("RechargeActivity ActivityName[" + activity.getName() + "] ActivityId["
				+ activity.getId() + "] ActivityStartUp[" + activity.getBeginTime() + "] ActivityShutDown["
				+ activity.getEndTime() + "] ActivityConfig[" + activity.getConfigData() + "] UnregisterListener TRY!");
		
		ActivityServer.server.getEventManager().unregisterListener(this);
		
		log.info("RechargeActivity ActivityName[" + activity.getName() + "] ActivityId["
				+ activity.getId() + "] ActivityStartUp[" + activity.getBeginTime() + "] ActivityShutDown["
				+ activity.getEndTime() + "] ActivityConfig[" + activity.getConfigData() + "] UnregisterListener SUCCESS!");
	}

	public void process(long time) throws Exception {
		
	}

	public int[] getEventTypes() {
		return new int[] {
				ActivityEvent.EVENT_RECHARGE
			};
	}

	public void handleEvent(ActivityEvent event) {
		switch (event.type) {
		case ActivityEvent.EVENT_RECHARGE:
			checkPlayerRecharge(((Integer) event.param1).intValue(), ((Integer)event.param2).intValue(), (Date) event.param3);
			break;
		}
	}

	public ActivityData getActivity() {
		return activity;
	}

	public void save() {
		
	}

	public void load() {
		
	}

	public void clear() {
		
	}

	public void checkPlayerRecharge (int playerId, int amount, Date date) {
//		boolean needRelease = false;
		log.info("RechargeActivity ActivityName[" + activity.getName() + "] Add playerID["
				+ playerId + "] RechargeAmount[" + amount + "] TRY!");
//		WorldPlayer player = ActivityServer.server.getplayerService().getWorldPlayer(playerId);
//		if (player == null) {
//			log.info("ConsumptionActivity ActivityName[" + activity.getName() + "] playerID["
//					+ playerId + "] loadWorldPlayer TRY");
//			try {
//				player = ActivityServer.server.getplayerService().loadWorldPlayer(playerId);
//			} catch (Exception e) {
//				log.info("ConsumptionActivity ActivityName[" + activity.getName() + "] playerID["
//						+ playerId + "] loadWorldPlayer ERROR");
//				e.printStackTrace();
//			}
//			needRelease = true;
//			log.info("ConsumptionActivity ActivityName[" + activity.getName() + "] playerID["
//					+ playerId + "] loadWorldPlayer SUCCESS");
//		}
		WorldPlayer player = ActivityServer.server.getplayerService().getWorldPlayerAndCatch(playerId);
		if (player != null) {
			int titleAmount = player.getOtherPool().getInt(activity.getName() + activity.getId()) + amount;
			int count = 0;
			player.getOtherPool().setInt(activity.getName() + activity.getId(), titleAmount);
			
			log.info("RechargeActivity ActivityName[" + activity.getName() + "] Add playerID["
					+ player.getId() + "] RechargeAmount[" + amount + "] SUCCESS! TitleRechargeAmount["
					+ player.getOtherPool().getInt(activity.getName() + activity.getId()) + "]");
			
			if (titleAmount >= this.amount) {
				
				log.info("RechargeActivity ActivityName[" + activity.getName() + "] sendItem to playerID["
						+ player.getId() + "] titleAmount[" + titleAmount + "] TRY!");
				
				count = sendItem(player, titleAmount);
				player.getOtherPool().setInt(activity.getName() + activity.getId(), titleAmount - count * this.amount);
				
				log.info("RechargeActivity ActivityName[" + activity.getName() + "] sendItem to playerID["
						+ player.getId() + "] titleAmount[" + player.getOtherPool().getInt(activity.getName() + activity.getId()) + "] SUCCESS!");
				
			}
//			if (needRelease) {
//				log.info("RechargeActivity ActivityName[" + activity.getName() + "] Add playerID["
//						+ playerId + "] RechargeAmount SUCCESS savePlayer TRY");
//				ActivityServer.server.getplayerService().unRegistry(player);
//				ActivityServer.server.getplayerService().savePlayer(player);
//				log.info("RechargeActivity ActivityName[" + activity.getName() + "] Add playerID["
//						+ playerId + "] RechargeAmount SUCCESS savePlayer SUCCESS");
//			} else {
				if (count > 0) {
					ActivityServer.server.getConnectService().sendMessage(player.getId(), "您获得了每充值" + this.amount + "元的奖品并已发送到您的邮箱，请注意查收。");
				}
//			}
		} else {
			log.info("RechargeActivity ActivityName[" + activity.getName() + "] Add playerID["
					+ playerId + "] RechargeAmount FAILURE. loadWorldPlayer playerID["
					+ playerId + "] is null");
		}
		ActivityServer.server.getplayerService().releasePlayer(player);
	}
	
	public int sendItem (WorldPlayer player, int titleAmount) {
		IItem iit = Items.getTemplate(giftId).newInstance();
		if (iit != null) {
			int count = titleAmount / this.amount;
			byte[] att = ItemUtils.item2dbAttachment(iit, count * giftCount);
			ActivityServer.server.getMailService().sendMail(player.getId(), player.getPlayerName(), -1, "系统",
					mailTitle, mailContent, att, 0, true);
			
			log.info("RechargeActivity ActivityName[" + activity.getName() + "] ActivityId[" + activity.getId() + "] recharge["
					+ this.amount + "] sendItem to playerId[" + player.getId()
					+ "] playerName[" + player.getPlayerName() + "] get ItemId[" +
					iit.getItemId() + "] ItemName[" + iit.getName() + "] ItemCount[" + count * giftCount
					+ "]");
			
			return count;
		} else {
			
			log.info("RechargeActivity sendItem FAILURE: Error! Item is null itemID[" + giftId + "]");
			
			return -1;
		}
	}
}
