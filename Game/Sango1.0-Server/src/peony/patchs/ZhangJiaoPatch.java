package peony.patchs;

import peony.game.Creature;
import peony.game.NoInstanceVMapManager;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapManager;
import peony.game.buff.GodBuff;

public class ZhangJiaoPatch implements Runnable {

	public void run() {
		try {
			VMapManager manager = Server.server.getWorld().getVMapManager(2032);
			VMap[] maps = ((NoInstanceVMapManager) manager).getVMaps(2032);
			for(VMap map : maps){
				if(map!=null){
					Creature c = map.getCreatureById(8323136);
					if(c!=null && c.isAlive()){
						c.canBeAttacked = true;
						c.buffs.removeBuff(GodBuff.GOD_BUFFID);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
