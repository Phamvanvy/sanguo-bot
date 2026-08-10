package com.pip.itimes.server.world.activityService.activity;

import java.text.ParseException;
import java.util.Date;

import org.apache.log4j.Logger;

import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.activityService.ActivityEvent;
import com.pip.itimes.server.world.activityService.ActivityEventListener;
import com.pip.itimes.server.world.activityService.ActivityServer;

/**
 * 每日充值RMB元送奖励活动
 * @author hchen
 */
public class DailyRechargeActivity implements IActivityImpl, ActivityEventListener {
	private static Logger log = Logger.getLogger(DailyRechargeActivity.class);
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
	
	public DailyRechargeActivity(ActivityData owner) {
		this.activity = owner;
	}
	
	public void startup() throws Exception {
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
	}

	public void shutdown() {
		ActivityServer.server.getEventManager().unregisterListener(this);
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
			checkPlayerRecharge((WorldPlayer) event.param1, ((Integer)event.param2).intValue(), (Date) event.param3);
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

	public void checkPlayerRecharge (WorldPlayer player, int amount, Date date) {
		Date getGiftDate = null;
		long getTime = 0;
		try {
			getGiftDate = ActivityServer.format.parse(player.getOtherPool().getString(activity.getName() + activity.getId()));
		} catch (ParseException e) {
		}
		if (getGiftDate != null) {
			getTime = getGiftDate.getTime();
		}
		int accountId = player.getAccountId();
		int chargeMoney = ActivityServer.server.getIrechargeService().getChargeByDay(accountId, date) / 100;
		if (chargeMoney >= this.amount * Server.consumerType && (getGiftDate == null || getTime < Utils.getTodayStart())) {
			sendItem(player);
			ActivityServer.server.getConnectService().sendMessage(player.getId(), "您获得了每日充值" + this.amount + "元的奖品并已发送到您的邮箱，请注意查收。");
			player.getOtherPool().setString(activity.getName() + activity.getId(), ActivityServer.format.format(date));
		}
	}
	
	public void sendItem (WorldPlayer player) {
		IItem iit = Items.getTemplate(giftId).newInstance();
		if (iit != null) {
			byte[] att = ItemUtils.item2dbAttachment(iit, giftCount);
			ActivityServer.server.getMailService().sendMail(player.getId(), player.getPlayerName(), -1, "系统",
					mailTitle, mailContent, att, 0, true);
			log.info("DailyRechargeActivity sendItem to playerId[" + player.getId()
					+ "] playerName[" + player.getPlayerName() + "] get ItemId[" +
					iit.getItemId() + "] ItemName[" + iit.getName() + "] ItemCount[" + giftCount
					+ "] Remain PlayerActivityConsumer[" + player.getActivityConsumer() + "] iMoneyType[" + Server.consumerType + "]");
		} else {
			log.info("DailyRechargeActivity sendItem failure: Error! Item is " + giftId);
		}
	}
}
