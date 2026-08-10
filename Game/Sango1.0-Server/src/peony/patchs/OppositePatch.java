package peony.patchs;

import java.lang.reflect.Field;
import peony.game.Server;
import peony.game.VMapManager;
import peony.service.activity.Activity;
import peony.service.activity.OppositeSexQuestActivity;

public class OppositePatch implements Runnable {
	
	public void run() {
		VMapManager manager = Server.server.getWorld().getVMapManager(1361);
		if(manager!=null && manager instanceof OppositeSexQuestActivity){
			try {
				Field activityField = OppositeSexQuestActivity.class.getDeclaredField("activity");
				activityField.setAccessible(true);
				Activity activity = (Activity) activityField.get(manager);
				if(activity!=null){
					activity.getSchedule().weekdays = new int[]{1};
				}
				System.out.println("OppositePatch OK");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
