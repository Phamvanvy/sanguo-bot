package peony.game.instance;

import peony.game.Server;
import peony.game.VMapManager;

public class ListInstances implements Runnable {

	public void run() {
		VMapManager manager = Server.server.getWorld().getVMapManager(432);
		if(manager!=null&&manager instanceof NormalVMapManager){
			NormalVMapManager m = (NormalVMapManager)manager;
			System.out.println("instances:"+m.instances.size());
		}
	}

}
