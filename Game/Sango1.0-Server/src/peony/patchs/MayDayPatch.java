package peony.patchs;

import peony.game.GameObject;
import peony.game.NoInstanceVMapManager;
import peony.game.Player;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapManager;

public class MayDayPatch implements Runnable{
	public void run() {
		try {
			VMapManager manager = Server.server.getWorld().getVMapManager(2144);
			VMap[] maps = ((NoInstanceVMapManager) manager).getVMaps(2144);
			for(int i=0;i<maps.length;i++){
				VMap map = maps[i];
				if(map!=null){
					for(GameObject go : map.instanceid2objects.values()){
						if(go!=null && go.type==GameObject.TYPE_PLAYER){
							Player player = (Player)go;
						    player.goMap(2032, 654, 824);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
