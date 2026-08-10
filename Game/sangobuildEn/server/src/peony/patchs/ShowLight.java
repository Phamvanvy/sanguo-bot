package peony.patchs;

import peony.game.GameObject;
import peony.game.NoInstanceVMapManager;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapManager;

public class ShowLight implements Runnable {

	public void run() {
		VMapManager manager = Server.server.getWorld().getVMapManager(272);
		VMap[] maps = ((NoInstanceVMapManager)manager).getVMaps(272);
		for (VMap map : maps) {
			for(GameObject o:map.instanceid2objects.values()){
				if(o.id==1114143||o.id==1114130){
					System.out.println(o.id+" "+o.isVisible());
				}
			}
		}
	}

}
