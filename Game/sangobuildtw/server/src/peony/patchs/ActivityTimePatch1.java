package peony.patchs;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.List;

import peony.game.Server;
import peony.service.activity.Activity;
import peony.service.activity.ActivityService;

public class ActivityTimePatch1 implements Runnable {
	public void run() {
		try {
			ActivityService actService = Server.server.getServiceRegistry().getActivityService();
			Field f = ActivityService.class.getDeclaredField("activities");
			f.setAccessible(true);
			List<Activity> acts = (List<Activity>)f.get(actService);
			Calendar cal = Calendar.getInstance();
			cal.set(2021, Calendar.JANUARY, 1);
			for (Activity act : acts) {
				act.getSchedule().stopTime = cal.getTime();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
