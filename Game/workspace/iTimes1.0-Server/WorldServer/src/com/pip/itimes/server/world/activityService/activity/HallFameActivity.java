package com.pip.itimes.server.world.activityService.activity;


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
 * 名人堂活动
 * @author hchen
 *
 */
public class HallFameActivity implements IActivityImpl, ActivityEventListener {
	private static Logger log = Logger.getLogger(HallFameActivity.class);
	protected ActivityData activity;
	/**
	 * 活动赠送礼物ID
	 */
	private int[] giftId;
	/**
	 * 活动赠送礼物个数
	 */
	private int[] giftCount;
	/**
	 * 名人堂人数
	 */
	private int count;
	
	public HallFameActivity(ActivityData owner) {
		this.activity = owner;
	}
	
	public void startup() throws Exception {
		log.info("HallFameActivity ActivityName[" + activity.getName() + "] ActivityId["
				+ activity.getId() + "] ActivityStartUp[" + activity.getBeginTime() + "] ActivityShutDown["
				+ activity.getEndTime() + "] ActivityConfig[" + activity.getConfigData() + "] RegisterListener TRY!");
		
		String config = activity.getConfigData();
		if (config != null) {
			String[] str = config.split(",");
			count = Integer.parseInt(str[0]);
			giftId = new int[count];
			giftCount = new int[count];
			int j = 0;
			for (int i = 1; i < str.length; i += 2) {
				giftId[j] = Integer.parseInt(str[i]);
				giftCount[j] = Integer.parseInt(str[i + 1]);
				j ++;
			}
		}
		ActivityServer.server.getEventManager().registerListener(this);
		
		log.info("HallFameActivity ActivityName[" + activity.getName() + "] ActivityId["
				+ activity.getId() + "] ActivityStartUp[" + activity.getBeginTime() + "] ActivityShutDown["
				+ activity.getEndTime() + "] ActivityConfig[" + activity.getConfigData() + "] RegisterListener SUCCESS!");
	}

	public void shutdown() {
		log.info("HallFameActivity ActivityName[" + activity.getName() + "] ActivityId["
				+ activity.getId() + "] ActivityStartUp[" + activity.getBeginTime() + "] ActivityShutDown["
				+ activity.getEndTime() + "] ActivityConfig[" + activity.getConfigData() + "] UnregisterListener TRY!");
		
		ActivityServer.server.getEventManager().unregisterListener(this);
		
		log.info("HallFameActivity ActivityName[" + activity.getName() + "] ActivityId["
				+ activity.getId() + "] ActivityStartUp[" + activity.getBeginTime() + "] ActivityShutDown["
				+ activity.getEndTime() + "] ActivityConfig[" + activity.getConfigData() + "] UnregisterListener SUCCESS!");
	}

	public void process(long time) throws Exception {
		
	}

	public int[] getEventTypes() {
		return new int[] {
				ActivityEvent.EVENT_HALL_FAME
			};
	}

	public void handleEvent(ActivityEvent event) {
		switch (event.type) {
		case ActivityEvent.EVENT_HALL_FAME:
			sendItem(((Integer) event.param1).intValue(), ((Integer) event.param2).intValue());
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
	
	public void sendItem (int playerId, int rank) {
		if (rank <= count) {
			IItem iit = Items.getTemplate(giftId[rank]).newInstance();
			if (iit != null) {
//				boolean needRelease = false;
				log.info("HallFameActivity ActivityName[" + activity.getName() + "] playerID["
						+ playerId + "] sendItem TRY");
//				WorldPlayer player = ActivityServer.server.getplayerService().getWorldPlayer(playerId);
//				if (player == null) {
//					try {
//						player = ActivityServer.server.getplayerService().loadWorldPlayer(playerId);
//					} catch (Exception e) {
//						log.info("HallFameActivity ActivityName[" + activity.getName() + "] playerID["
//								+ playerId + "] loadWorldPlayer ERROR");
//						e.printStackTrace();
//					}
//					needRelease = true;
//					log.info("HallFameActivity ActivityName[" + activity.getName() + "] playerID["
//							+ playerId + "] loadWorldPlayer SUCCESS");
//				}
				WorldPlayer player = ActivityServer.server.getplayerService().getWorldPlayerAndCatch(playerId);
				byte[] att = ItemUtils.item2dbAttachment(iit, giftCount[rank - 1]);
				ActivityServer.server.getMailService().sendMail(player.getId(), player.getPlayerName(), -1, "系统",
						"名人堂奖品", "亲爱的玩家恭喜您获得活动期间奖品" + iit.getName() + "*" + giftCount[rank - 1], att, 0, true);
				ActivityServer.server.getConnectService().sendMessage(player.getId(), "您获得了名人堂前" + count + "名的奖品并已发送到您的邮箱，请注意查收。");
				log.info("HallFameActivity ActivityName[" + activity.getName() + "] playerID[" + player.getId()
						+ "] ItemId[" + iit.getItemId() + "] ItemCount[" + giftCount[rank - 1] + "] sendItem SUCCESS");
//				if (needRelease) {
//					log.info("HallFameActivity ActivityName[" + activity.getName() + "] playerID["
//							+ playerId + "] sendItem SUCCESS savePlayer TRY");
//					ActivityServer.server.getplayerService().unRegistry(player);
//					ActivityServer.server.getplayerService().savePlayer(player);
//					log.info("HallFameActivity ActivityName[" + activity.getName() + "] playerID["
//							+ playerId + "] sendItem SUCCESS savePlayer SUCCESS");
//				}
				ActivityServer.server.getplayerService().releasePlayer(player);
			} else {
				log.info("HallFameActivity sendItem FAILURE: Error! Item is null itemID[" + giftId + "]");
			}
			
		}
	}
}