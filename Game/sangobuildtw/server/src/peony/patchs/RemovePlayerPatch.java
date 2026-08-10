package peony.patchs;

import peony.game.Server;
import peony.service.player.PlayerService;

public class RemovePlayerPatch implements Runnable {

	public void run() {
		PlayerService service = Server.server.getServiceRegistry().getPlayerService();
		service.cache.remove(388567);
		System.out.println("RemovePlayerPatch OK!");
	}

}
