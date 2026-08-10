package peony.patchs;

import java.util.Calendar;

import peony.game.Server;
import peony.service.fame.FameService;

public class FamePatch1 implements Runnable {
	public void run() {
		FameService fameService = Server.server.getServiceRegistry().getFameService();
//		fameService.loadFromDb();
		fameService.processData(Calendar.getInstance());
		System.out.println("_______load fames ok");
	}
}
