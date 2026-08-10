package peony.service.activity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.pip.util.Utils;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus;

import ch.javasoft.util.intcoll.IntHashMap;
import peony.game.Gain;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.sleepycat.SleepyCatService;

/**
 * 畅游运营活动1：鼓励用户消费。消费送东西。
 * @author lighthu
 */
public class IBuyActivity1 implements IActivityImpl, ServiceEventListener {
	private static Logger log = Logger.getLogger(IBuyActivity1.class);
	
	protected Activity activity;
	// 缓存累计消费金额，提高数据库效率
	protected IntHashMap<Integer> consumeCache = new IntHashMap<Integer>();
	
	public IBuyActivity1(Activity owner) {
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
	protected void playerBuyOK(int playerID, int amount) {
		int total;
		if (consumeCache.containsKey(playerID)) {
			total = consumeCache.get(playerID) + amount;
		} else {
			total = Server.server.getServiceRegistry().getDbService().ibuyDAO
				.getTotalConsume(playerID, activity.getSchedule().startTime);
		}
		consumeCache.put(playerID, new Integer(total));
		int old = total - amount;
		if (old < 1000 && total >= 1000) {
			// 赠送消费1元奖励
			sendReward(playerID, 0);
		}
		if (old < 1000000 && total >= 1000000) {
			// 赠送消费100元奖励
			sendReward(playerID, 1);
		}
	}
	
	protected void sendReward(int playerID, int type) {
		Player p = ObjectAccessor.getPlayer(playerID);
		int itemID = type == 0 ? 1138 : 1139;
		GameItem item = ObjectAccessor.createGameItem(itemID);
		
		// 为玩家添加到背包
		PlayerTransaction tx = p.newTransaction("ACTV");
		Gain gain = new Gain(p);
		gain.addGainItem(item, 1);
		try {
			p.addGainComplete(gain, tx, true);
			tx.commit();
			String content;
			if (type == 0) {
				content = MessageFormat.format("感谢您的支持，本商城特在此赠送您超值回馈大礼包一个。（奖励已放入背包，请查收。）", p.name);
			} else {
				content = MessageFormat.format("感谢您的支持，本商城特在此赠送您真情回馈大礼包一个。（奖励已放入背包，请查收。）", p.name);
			}
			p.message(-1, content, -1, -1);
		} catch (Exception e) {
			tx.rollback();
			
			// 如果背包满则发送邮件
			String content;
			if (type == 0) {
				content = MessageFormat.format("感谢您的支持，本商城特在此赠送您超值回馈大礼包一个。", p.name);
			} else {
				content = MessageFormat.format("感谢您的支持，本商城特在此赠送您真情回馈大礼包一个。", p.name);
			}
			Server.server.getServiceRegistry().getMailService().sendSystemMail(
				p.id, "<cFF0000>[系统]</c>\n<cFF0000>[hệ thống]</c>", "系统奖励", content, 0, item, 1, "ACTV");
			if (type == 0) {
				content = MessageFormat.format("感谢您的支持，本商城特在此赠送您超值回馈大礼包一个。（您的背包已满，奖励已经以飞鸽附件的方式发送到你的飞鸽里了，请及时查收。）", p.name);
			} else {
				content = MessageFormat.format("感谢您的支持，本商城特在此赠送您真情回馈大礼包一个。（您的背包已满，奖励已经以飞鸽附件的方式发送到你的飞鸽里了，请及时查收。）", p.name);
			}
			p.message(-1, content, -1, -1);
		}
		
		// 记录日志
		LogUtil.logActivityReward(p, activity, type);
	}
}
