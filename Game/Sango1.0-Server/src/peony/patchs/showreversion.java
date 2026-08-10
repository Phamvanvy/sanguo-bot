package peony.patchs;

import peony.game.Server;

public class showreversion implements Runnable {

	public void run() {
		try {
			String revision = Server.server.revision;
			System.out.println("--------------------------revision-------------------" + revision);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

}
