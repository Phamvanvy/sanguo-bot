package peony.service.activity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.pip.util.Utils;

import ch.javasoft.util.intcoll.IntHashMap;
import peony.game.Actor;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.service.stat.StatService;

/**
 * 畅游运营活动2：冲级活动。
 * 第一名奖励：1007478（武将）1007479（刺客）1007480（谋士）1007481（方士）
 * 第2-5名奖励：1007482
 * 第6-10名奖励：1140
 * @author lighthu
 */
public class TopLevelActivity1 implements IActivityImpl {
	private static Logger log = Logger.getLogger(TopLevelActivity1.class);
	
	protected Activity activity;
	
	public TopLevelActivity1(Activity owner) {
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
		// 服务到期关闭时，统计级别排行发放奖励
		StatService service = Server.server.getServiceRegistry().getStatService();
		service.rebuildLevelRanks();
		List<Actor> actors = service.topLevelRanks(0);
		if (actors.size() > 0) {
			Actor actor = actors.get(0);
			if (actor.clazz == Player.CLASS_1) {
				sendReward(actor, 0, 1007478);
			} else if (actor.clazz == Player.CLASS_2) {
				sendReward(actor, 0, 1007479);
			} else if (actor.clazz == Player.CLASS_3) {
				sendReward(actor, 0, 1007480);
			} else if (actor.clazz == Player.CLASS_4) {
				sendReward(actor, 0, 1007481);
			}
		}
		for (int i = 1; i < actors.size() && i < 5; i++) {
			Actor actor = actors.get(i);
			sendReward(actor, 1, 1007482);
		}
		for (int i = 5; i < actors.size() && i < 10; i++) {
			Actor actor = actors.get(i);
			sendReward(actor, 2, 1140);
		}
	}

	public void shutdown() {
	}

	/*
	 * 按照一个玩家的排名发放奖励。
	 */
	protected void sendReward(Actor actor, int rewardLevel, int itemID) {
		GameItem item = ObjectAccessor.createGameItem(itemID);
		String title = null;
		String content = null;
		switch (rewardLevel) {
		case 0:
			title = "等级排名状元奖励";
			content = "{0}，你好，因为骁勇无匹，冲级迅速，在本次等级大排行中成为状元，特此奖励。";
			break;
		case 1:
			title = "等级排名榜眼奖励";
			content = "{0}，你好，因为骁勇无匹，冲级迅速，在本次等级大排行中成为榜眼，特此奖励。";
			break;
		case 2:
			title = "等级排名探花奖励";
			content = "{0}，你好，因为骁勇无匹，冲级迅速，在本次等级大排行中成为探花，特此奖励。";
			break;
		}
		content = MessageFormat.format(content, actor.name);
		Server.server.getServiceRegistry().getMailService().sendSystemMail(
			actor.id, "<cFF0000>[系统]</c>\n<cFF0000>[hệ thống]</c>", title, content, 0, item, 1, "ACTV");
		LogUtil.logActivityReward(actor, activity, rewardLevel);
	}
}
