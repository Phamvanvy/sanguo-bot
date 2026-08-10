package peony.patchs;

import peony.game.Server;

public class MonthlyPayPatch implements Runnable {

	public void run() {
		Server.server.getEventManager().registerListener(
				Server.server.getServiceRegistry().getMonthlyPayService());
	}

}
