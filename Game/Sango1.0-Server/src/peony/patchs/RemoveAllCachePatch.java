package peony.patchs;

import peony.game.Server;

public class RemoveAllCachePatch implements Runnable {

	public void run() {
		Server.server.getServiceRegistry().getPlayerService().cache.removeAll();
		System.out.println("！！！！！！！！！！！！！！！！！！！！remove OK");
	}

}
