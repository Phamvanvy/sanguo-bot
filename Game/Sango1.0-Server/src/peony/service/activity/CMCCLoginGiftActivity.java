package peony.service.activity;

import java.util.Calendar;

import peony.game.Gain;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

/**
 * 移动版本，订购套餐的玩家登陆送礼品的活动。
 * @author light.hu
 */
public class CMCCLoginGiftActivity implements IActivityImpl, ServiceEventListener {
	protected Activity activity;
	protected Calendar cal = Calendar.getInstance();
	protected static final int GIFT_ITEMID = 2684;
	
	public CMCCLoginGiftActivity(Activity owner){
		this.activity = owner;
	}

	public void clear() {
	}

	public Activity getActivity() {
		return activity;
	}
	
	public void load() {
	}

	public void save() {
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_PLAYER_LOGINED
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_PLAYER_LOGINED:
			onPlayerLogined((Player)event.param1);
			break;
		}
	}
	
	protected void onPlayerLogined(Player p) {
		// 判断今天有没有给过礼品
		long lastGiftTime = p.pool.getLong("CLGA_Time", 0);
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		if (lastGiftTime != 0 && lastGiftTime >= cal.getTimeInMillis()) {
			return;
		}
		
		// 判断是否套餐订购玩家
		if (p.getAccount().getCmccAttr() == null || !p.getAccount().getCmccAttr().contains("package")) {
			return;
		}
		
		// 给玩家发奖品
		GameItem item = ObjectAccessor.createGameItem(GIFT_ITEMID);
		PlayerTransaction tx = p.newTransaction("ACTV_CMCC");
		Gain gain = new Gain(p);
		gain.addGainItem(item, 1);
		try {
			p.addGainComplete(gain, tx, true);
			tx.commit();
			String content = "欢迎您，尊敬的“游戏玩家”，三国接引大使注意到了您的到来，并在您的背包里放了一份礼物，打开看看吧！";
			p.message(-1, content, -1, -1);
		} catch (Exception e) {
			tx.rollback();
			String content = "欢迎您，尊敬的“游戏玩家”，三国接引大使注意到了您的到来，为您送上一份礼物，打开看看吧！";
			Server.server.getServiceRegistry().getMailService().sendSystemMail(
				p.id, peony.Messages.STRING_00004, "游戏玩家礼包", content, 0, item, 1, "ACTV");
			p.message(-1, content, -1, -1);
		}
		p.pool.setLong("CLGA_Time", System.currentTimeMillis());
	}
}
