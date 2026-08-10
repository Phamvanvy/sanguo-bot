package peony.patchs;

import peony.game.NoInstanceVMapManager;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapManager;

public class WeiDark implements Runnable {

	public void run() {
		try {
			VMapManager manager = Server.server.getWorld().getVMapManager(272);
			VMap[] maps = ((NoInstanceVMapManager)manager).getVMaps(272);
			for (VMap map : maps) {
				map.refreshNPC(1114130, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
