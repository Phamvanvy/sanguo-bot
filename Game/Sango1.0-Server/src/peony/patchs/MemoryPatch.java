package peony.patchs;

import java.lang.reflect.Field;
import java.util.List;

import peony.game.Player;
import peony.game.Server;
import peony.service.activity.Activity;
import peony.service.activity.ActivityService;
import peony.service.activity.OppositeSexQuestActivity;

public class MemoryPatch implements Runnable {

	public void run() {
		ActivityService service = Server.server.getServiceRegistry().getActivityService();
		Activity act = service.getActivityByImpClass("OppositeSexQuestActivity");
		if(act!=null){
			try {
				OppositeSexQuestActivity a = (OppositeSexQuestActivity) act.getImpl();
				Field f = OppositeSexQuestActivity.class.getDeclaredField("players");
				f.setAccessible(true);
				List<Player> players = (List<Player>) f.get(a);
				System.out.println("MemoryPatch load size: "+players.size());
				players.clear();
				System.out.println("MemoryPatch load OK... ... ... ...");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
