package peony.service.activity;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import peony.game.GameItem;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.chat.ChatService;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.account.AccountProperty;

/**
 * 新老兵回归活动
 * @author dchen
 */
public class OldPlayerReturnNewActivity implements IActivityImpl, ServiceEventListener {

	private static Logger log = Logger.getLogger(OldPlayerReturnNewActivity.class);
	
	protected Activity activity;
	
	public static long dis = 90 * 24 * 3600 * 1000L;
	public static int itemId = 4678;
	public static int limitLevel = 65;
	public static String oldPlayerPool = "OLD_PLAYER_NEW";
	
	public String message1 = "亲爱的玩家，欢迎你重回三国，我们发了老兵回归礼包给你，打开背包，邀上朋友，继续在三国的世界里驰骋吧！";
	public String message2 = "亲爱的玩家，欢迎你重回三国，我们发了老兵回归礼包给你，打开飞鸽，邀上朋友，继续在三国的世界里驰骋吧！";
	protected Map<Integer, Integer> messages = new HashMap<Integer, Integer>();
	
	public OldPlayerReturnNewActivity(Activity owner){
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
		return new int[] {
				ServiceEvent.EVENT_PLAYER_FIRSTLOAD,
				ServiceEvent.EVENT_ACCOUNTPROPERTY_LOADED
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
			case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
				notify((Player)event.param1);
				break;
			case ServiceEvent.EVENT_ACCOUNTPROPERTY_LOADED:
				oldPlayerLoad((Player)event.param1);
				break;
		}
	}
	
	protected void notify(Player player){
		if(player!=null && messages.get(player.id)!=null){
			int messageIndex = messages.get(player.id);
			ChatService chatService = Server.server.getServiceRegistry().getChatService();
			if(messageIndex==1){
				chatService.sendPrivateMessage(player.id, message1);
				messages.remove(player.id);
			}else if(messageIndex==2){
				chatService.sendPrivateMessage(player.id, message2);
				messages.remove(player.id);
			}
		}
	}
	
	protected void oldPlayerLoad(Player p){
		if(p!=null){
			AccountProperty accountProperty = Server.server.getServiceRegistry().getVipPrivilegeService().getAccountProperty(p.accountId);
			if((accountProperty!=null && accountProperty.pool.getInt(oldPlayerPool, 0)==0) 
					&& p.getLastLogoutElapseTime()>=dis && p.level>=limitLevel){
				PlayerTransaction tx = p.newTransaction("OLDRETURNNEW");
				GameItem item = ObjectAccessor.createGameItem(itemId);
				try {
					p.bag.addGameItemComplete(item, 1, tx, true);
					tx.commit();
					messages.put(p.id, 1);
				} catch (NoEnoughSpaceException e) {
					tx.rollback();
					Server.server.getServiceRegistry().getMailService().sendSystemMail(
						p.id, peony.Messages.STRING_00004, peony.Messages.STRING_01519, message2, 0, item, 1, "ACTV");
					messages.put(p.id, 2);
				}
				accountProperty.pool.setInt(oldPlayerPool, 1);
			}
		}
	}

}
