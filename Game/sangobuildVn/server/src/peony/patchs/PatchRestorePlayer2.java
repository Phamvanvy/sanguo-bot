package peony.patchs;

import peony.game.Actor;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;

public class PatchRestorePlayer2 implements Runnable {

	public void run() {
		int[] ids = new int[] { 5, 257 };
		for (int id : ids) {
			Player p = ObjectAccessor.getPlayer(id);
			if (p != null) {
				p.exist = 1;
			}
			p = Server.server.getServiceRegistry().getPlayerService().getFromCache(id);
			if (p != null) {
				p.exist = 1;
			}
			Actor a = Server.server.getServiceRegistry().getActorCacheService().find(id);
			if (a != null) {
				a.exist = 1;
			}
		}
	}

}
