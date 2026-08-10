package peony.service.activity;


import org.apache.log4j.Logger;
import peony.game.GameItem;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

/**
 * 老兵归来活动
 * @author dchen
 */
public class OldPlayerReturnActivity implements IActivityImpl, ServiceEventListener {

	private static Logger log = Logger.getLogger(OldPlayerReturnActivity.class);
	
	protected Activity activity;
	
	public static long dis = 60 * 24 * 3600 * 1000L;
	public static int itemId = 1915;
	public static int limitLevel = 50;
	
	public OldPlayerReturnActivity(Activity owner){
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
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
			case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
				oldPlayerLoad((Player)event.param1);
				break;
		}
	}
	
	protected void oldPlayerLoad(Player p){
		if(p!=null){
			if(p.getLastLogoutElapseTime()>=dis && p.level>=limitLevel){
				PlayerTransaction tx = p.newTransaction("OLDRETURN");
				GameItem item = ObjectAccessor.createGameItem(itemId);
				try {
					p.bag.addGameItemComplete(item, 1, tx, true);
					tx.commit();
				} catch (NoEnoughSpaceException e) {
					tx.rollback();
					Server.server.getServiceRegistry().getMailService().sendSystemMail(
						p.id, "系统", "欢迎老兵归来！", "", 0, item, 1, "ACTV");
				}
			}
		}
	}

}
