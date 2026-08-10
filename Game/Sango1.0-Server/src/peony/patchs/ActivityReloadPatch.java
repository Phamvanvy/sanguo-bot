package peony.patchs;

import java.lang.reflect.Field;
import java.util.List;

import peony.game.Server;
import peony.service.activity.Activity;
import peony.service.activity.ActivityDAO;
import peony.service.activity.ActivityService;

public class ActivityReloadPatch implements Runnable {
	public void run() {
		try {
			ActivityService actService = Server.server.getServiceRegistry().getActivityService();
			Field f = ActivityService.class.getDeclaredField("activities");
			f.setAccessible(true);
			List<Activity> acts = (List<Activity>)f.get(actService);
			List<Activity> newacts = new ActivityDAO().getActivities();
			for (Activity act : acts) {
				for (Activity act2 : newacts) {
					if (act.getId() == act2.getId()) {
						act.setSchedule(act2.getSchedule());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
