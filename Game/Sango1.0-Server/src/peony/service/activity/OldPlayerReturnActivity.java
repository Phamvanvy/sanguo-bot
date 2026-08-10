package peony.service.activity;


import org.apache.log4j.Logger;
import peony.game.GameItem;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Skills;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

/**
 * 老兵归来活动
 * @author dchen
 */
public class OldPlayerReturnActivity implements IActivityImpl, ServiceEventListener {

	private static Logger log = Logger.getLogger(OldPlayerReturnActivity.class);
	
	protected Activity activity;
	
	public static long dis = 20 * 24 * 3600 * 1000L;
//	public static int itemId = 1915;
	public static int itemId = 4154;
	public static int limitLevel = 50;
	public static int BUFF = 432;
	public static int[] SKILLS = {336,337,338,339}; //老兵回归BUFF
	public static String oldPlayerPool = "OLD_PLAYER";
	
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
						p.id, peony.Messages.STRING_00004, peony.Messages.STRING_01519, "", 0, item, 1, "ACTV");
				}
				//加“老兵回归BUFF”
				if(ObjectAccessor.getSkill(Skills.getSkillId(SKILLS[p.clazz], 1))!=null){
					p.addSkill(ObjectAccessor.getSkill(Skills.getSkillId(SKILLS[p.clazz], 1)));
					p.pool.setInt(oldPlayerPool, 1);
				}
			}
		}
	}

}
