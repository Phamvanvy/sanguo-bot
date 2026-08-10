package peony.game.clickexp;

import java.util.Calendar;

import peony.game.GameItem;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.Server;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

public class ClickExpService implements Service, ServiceEventListener {

	public void shutdown() {

	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_PLAYER_LOGINED, // 角色登录事件
				ServiceEvent.EVENT_PLAYER_LOGOUTED, // 进入场景事件
				};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
			case ServiceEvent.EVENT_PLAYER_LOGINED:
				login((Player)event.param1);
				break;
			case ServiceEvent.EVENT_PLAYER_LOGOUTED:
				logout((Player)event.param1);
				break;
		}
	}
	
	protected void login(Player p){
		if(p != null){
			GameItem item = p.bag.getGameItem(ItemUtil.ITEM_ONLINEEXP_CLICK);
			int lastStartDay = getDayOfYear(p.pool.getLong(Player.PROPERTY_CLICKEXP_START_TIME, 0L));
			int today = getDayOfYear(System.currentTimeMillis());
			if(lastStartDay!=today){
				p.pool.setLong(Player.PROPERTY_CLICKEXP_CUMULATE_TIME, 0);
			}
			if(item!=null){
				p.pool.setLong(Player.PROPERTY_CLICKEXP_START_TIME, System.currentTimeMillis());
			}else{
				if(p.pool.getLong(Player.PROPERTY_CLICKEXP_START_TIME, 0L)!=0||p.pool.getInt(Player.PROPERTY_CLICKEXP_CUMULATE_TIME,0)!=0){
					p.pool.setLong(Player.PROPERTY_CLICKEXP_START_TIME, 0L);
					p.pool.setInt(Player.PROPERTY_CLICKEXP_START_TIME, 0);
				}
			}
		}
	}
	
	protected void logout(Player p){
		if(p != null){
			GameItem item = p.bag.getGameItem(ItemUtil.ITEM_ONLINEEXP_CLICK);
			if(item!=null){
				long t = p.pool.getLong(Player.PROPERTY_CLICKEXP_START_TIME, 0L);
				long onlineExp = p.pool.getLong(Player.PROPERTY_CLICKEXP_CUMULATE_TIME,0L);
				long diff = System.currentTimeMillis() - t;
				if(diff>0){
					onlineExp += diff;
					p.pool.setLong(Player.PROPERTY_CLICKEXP_START_TIME, 0L);
					p.pool.setLong(Player.PROPERTY_CLICKEXP_CUMULATE_TIME, onlineExp);
				}
			}else{
				if(p.pool.getLong(Player.PROPERTY_CLICKEXP_START_TIME, 0L)!=0||p.pool.getLong(Player.PROPERTY_CLICKEXP_CUMULATE_TIME,0L)!=0){
					p.pool.setLong(Player.PROPERTY_CLICKEXP_START_TIME, 0L);
					p.pool.setLong(Player.PROPERTY_CLICKEXP_START_TIME, 0L);
				}
			}
		}
	}
	
	protected int getDayOfYear(long times){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(times);
		return cal.get(Calendar.DAY_OF_YEAR);
	}

}
