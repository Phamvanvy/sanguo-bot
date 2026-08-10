package com.pip.itimes.server.world.activityService.activity;

import org.apache.log4j.Logger;

import com.pip.itimes.server.world.activityService.ActivityEvent;
import com.pip.itimes.server.world.activityService.ActivityServer;
import com.pip.itimes.server.world.activityService.ActivityEventListener;
import com.pip.itimes.server.world.activityService.activity.ActivityData;
import com.pip.itimes.server.world.activityService.activity.IActivityImpl;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;

/**
 * 每日累积消费RMB：元送奖励活动
 * @author hchen
 *
 */
public class ConsumerActivity implements IActivityImpl, ActivityEventListener {
	private static Logger log = Logger.getLogger(ConsumerActivity.class);
	protected ActivityData activity;
	/**
	 * 消费活动赠送礼物ID
	 */
	private int giftId;
	/**
	 * 消费活动赠送礼物个数
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
	 * 消费额度
	 */
	private int amount;
	
	public ConsumerActivity(ActivityData owner) {
		this.activity = owner;
	}
	
	public void startup() throws Exception {
		
		log.info("ConsumptionActivity ActivityName[" + activity.getName() + "] ActivityId["
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
		
		log.info("ConsumptionActivity ActivityName[" + activity.getName() + "] ActivityId["
				+ activity.getId() + "] ActivityStartUp[" + activity.getBeginTime() + "] ActivityShutDown["
				+ activity.getEndTime() + "] ActivityConfig[" + activity.getConfigData() + "] RegisterListener SUCCESS!");
	}

	public void shutdown() {
		
		log.info("ConsumptionActivity ActivityName[" + activity.getName() + "] ActivityId["
				+ activity.getId() + "] ActivityStartUp[" + activity.getBeginTime() + "] ActivityShutDown["
				+ activity.getEndTime() + "] ActivityConfig[" + activity.getConfigData() + "] UnregisterListener TRY!");
		
		ActivityServer.server.getEventManager().unregisterListener(this);
		
		log.info("ConsumptionActivity ActivityName[" + activity.getName() + "] ActivityId["
				+ activity.getId() + "] ActivityStartUp[" + activity.getBeginTime() + "] ActivityShutDown["
				+ activity.getEndTime() + "] ActivityConfig[" + activity.getConfigData() + "] UnregisterListener SUCCESS!");
	}

	public void process(long time) throws Exception {
		
	}

	public int[] getEventTypes() {
		return new int[] {
				ActivityEvent.EVENT_CONSUMER
			};
	}

	public void handleEvent(ActivityEvent event) {
		switch (event.type) {
		case ActivityEvent.EVENT_CONSUMER:
			checkPlayerConsumer(((Integer)event.param1).intValue(), ((Integer)event.param2).intValue());
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

	public void checkPlayerConsumer (int playerId, int amount) {
//		boolean needRelease = false;
		log.info("ConsumptionActivity ActivityName[" + activity.getName() + "] Add playerID["
				+ playerId + "] Amount[" + amount / 100 + "] TRY!");
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
			int titleAmount = player.getOtherPool().getInt(activity.getName() + activity.getId()) + amount / 100;
			player.getOtherPool().setInt(activity.getName() + activity.getId(), titleAmount);
			
			log.info("ConsumptionActivity ActivityName[" + activity.getName() + "] Add playerID["
					+ player.getId() + "] Amount[" + amount / 100 + "] SUCCESS! TitleAmount["
					+ player.getOtherPool().getInt(activity.getName() + activity.getId()) + "]");
			
			int count = 0;
			if (titleAmount >= this.amount * Server.consumerType) {
				
				log.info("ConsumptionActivity ActivityName[" + activity.getName() + "] sendItem to playerID["
						+ player.getId() + "] titleAmount[" + titleAmount + "] TRY!");
				
				count = sendItem(player, titleAmount);
				player.getOtherPool().setInt(activity.getName() + activity.getId(), titleAmount - count * this.amount * Server.consumerType);
				
				log.info("ConsumptionActivity ActivityName[" + activity.getName() + "] sendItem to playerID["
						+ player.getId() + "] titleAmount[" + player.getOtherPool().getInt(activity.getName() + activity.getId()) + "] SUCCESS!");
			}
			
//			if (needRelease) {
//				log.info("ConsumptionActivity ActivityName[" + activity.getName() + "] Add playerID["
//						+ playerId + "] Amount SUCCESS savePlayer TRY");
//				ActivityServer.server.getplayerService().unRegistry(player);
//				ActivityServer.server.getplayerService().savePlayer(player);
//				log.info("ConsumptionActivity ActivityName[" + activity.getName() + "] Add playerID["
//						+ playerId + "] Amount SUCCESS savePlayer SUCCESS");
//			} else {
				if (count > 0) {
					ActivityServer.server.getConnectService().sendMessage(player.getId(), "您获得了每日消费" + this.amount + "元奖品并已发送到您的邮箱，请注意查收。");
				}
//			}
		} else {
			log.info("ConsumptionActivity ActivityName[" + activity.getName() + "] Add playerID["
					+ playerId + "] Amount FAILURE. loadWorldPlayer playerID["
					+ playerId + "] is null");
		}
		ActivityServer.server.getplayerService().releasePlayer(player);
	}
	
	public int sendItem (WorldPlayer player, int titleAmount) {
		IItem iit = Items.getTemplate(giftId).newInstance();
		if (iit != null) {
			int count = titleAmount / (this.amount * Server.consumerType);
			byte[] att = ItemUtils.item2dbAttachment(iit, count * giftCount);
			ActivityServer.server.getMailService().sendMail(player.getId(), player.getPlayerName(), -1, "系统",
					mailTitle, mailContent, att, 0, true);
			
			log.info("ConsumptionActivity ActivityName[" + activity.getName() + "] ActivityId[" + activity.getId() + "] amount["
					+ this.amount + "] sendItem to playerID[" + player.getId()
					+ "] playerName[" + player.getPlayerName() + "] get ItemId[" +
					iit.getItemId() + "] ItemName[" + iit.getName() + "] ItemCount[" + count * giftCount
					+ "] iMoneyType[" + Server.consumerType + "]");
			
			return count;
		} else {
			
			log.info("ConsumptionActivity sendItem FAILURE: Error! Item is null itemID[" + giftId + "]");
			
			return -1;
		}
	}
}
