package peony.service.activity;

import peony.game.Player;
import peony.game.Server;
import peony.game.buff.BuffUtil;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

/**
 * 新春活动奖励：创建新角色获得BUFF
 */
public class CreatePlayerActivity implements IActivityImpl, ServiceEventListener{
	
	private static final String PLAYER_CREATE_ADDBUFF = "playercreateaddbuff";
	protected Activity activity;
	
	
	public CreatePlayerActivity(Activity owner){
		this.activity = owner;
	}

	public Activity getActivity() {
		return activity;
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}

	public int[] getEventTypes() {
		return new int[] { ServiceEvent.EVENT_PLAYER_CREATED, ServiceEvent.EVENT_PLAYER_FIRSTLOAD};
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_PLAYER_CREATED:
			 rememberCreate((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
			addBuff((Player)event.param1);
			break;
		}
	}
	
	public void rememberCreate(Player p){
		if(p!=null){
	       p.pool.setInt(PLAYER_CREATE_ADDBUFF,1);
		}
	}
	
	public void addBuff(Player p){
		if(p!=null){
			int count = p.pool.getInt(PLAYER_CREATE_ADDBUFF, 0);
			if(count == 1){
				p.buffs.addBuff(BuffUtil.createBuff(396, 1, p, p, 0));
				p.pool.remove(PLAYER_CREATE_ADDBUFF);
				String content = "新人有好礼,您获得了1个小时的双倍经验时间及攻击增强时间，还不趁此时间奋勇杀敌！";
				p.message(-1, content, -1, -1);
			}
		}
	}
	
    public void clear() {
		
	}
    
    public void load() {
		
	}

	public void save() {
		
	}
}
