package peony.service.activity;

import java.text.MessageFormat;
import org.apache.log4j.Logger;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import ch.javasoft.util.intcoll.IntHashMap;

/**
 * 消费活动1：活动期间消费满500元，送礼物。
 */

public class IBuyRewardActivity1 implements IActivityImpl, ServiceEventListener {
	private static Logger log = Logger.getLogger(IBuyRewardActivity1.class);
	
	private static final int BASEMONEY = 16200000; //基础消费
	
	private static final int itemID = 832; // 奖励的物品ID
	
	protected Activity activity;
	// 缓存累计消费金额
	protected IntHashMap<Integer> ibuyCache = new IntHashMap<Integer>();
	
	public IBuyRewardActivity1(Activity owner) {
		this.activity = owner;
	}
	
	public Activity getActivity() {
		return activity;
	}

	/**
	 * 如果有历史数据，载入历史数据。
	 */
	public void load() {
		
	}

	/**
	 * 服务器关闭时，把临时数据保存到bdb中。
	 */
	public void save() {
	}

	/**
	 * 删除临时数据。
	 */
	public void clear() {
	}
	
	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public int[] getEventTypes() {
		return new int[] {
			ServiceEvent.EVENT_IBUY
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_IBUY:
			playerBuyOK(((Integer)event.param1).intValue(), ((Integer)event.param2).intValue());
			break;
		}
	}
	
	/*
	 * 玩家消费通知。
	 */
	protected void playerBuyOK(int playerId, int money) {
		int total;
		if (ibuyCache.containsKey(playerId)) {
			total = ibuyCache.get(playerId) + money;
		} else {
			total = Server.server.getServiceRegistry().getDbService().ibuyDAO
				.getTotalConsume(playerId, activity.getSchedule().startTime);
		}
		ibuyCache.put(playerId, new Integer(total));
		int old = total - money;
		if (old < BASEMONEY && total >= BASEMONEY) {
			// 赠送消费500元奖励
			sendGift(playerId);
		}
	}
	
	/**
	 * 通过飞鸽发送奖励物品，活动期间消费满500元赠送一个3级宝石兑换符
	 * @param p
	 * @param money
	 */
	public void sendGift(int playerId) {
		GameItem item = ObjectAccessor.createGameItem(itemID);
		Player p = ObjectAccessor.getPlayer(playerId);
		String content = MessageFormat.format("感谢您的参与，更多精彩请继续关注本活动!",
					p.name);
		Server.server.getServiceRegistry().getMailService().sendSystemMail(
				playerId, "<cFF0000>[系统]</c>\n<cFF0000>[hệ thống]</c>", "活动奖励", content, 0, item, 1, "ACTV");
        // 记录日志
		LogUtil.logActivityRewards(p, activity);
		}
}
