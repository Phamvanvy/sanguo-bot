package patchs;

import java.lang.reflect.Field;
import java.util.Map;

import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.sports.Sport;
import com.pip.itimes.server.world.sports.SportsService;

public class StopSportsPatch implements Runnable {
	public void run() {
		SportsService service = Server.instance.sportsService;
		try {
			Field field = SportsService.class.getDeclaredField("sports");
			field.setAccessible(true);
			Map<String, Sport> sports = (Map<String,Sport>)field.get(service);
			for(Sport sport:sports.values()){
				sport.end();
			}
			sports.clear();
			System.out.println("all sports clear");
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

}
