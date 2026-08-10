package peony.patchs;

import java.lang.reflect.Field;

import peony.game.NoInstanceVMapManager;
import peony.game.Server;
import peony.game.VMap;
import peony.game.World;

public class InstancePkModify implements Runnable {

	public static final int[] MAPS = {448,768};
	
	public void run() {
		for (int i = 0; i < MAPS.length; i++) {
			World world = Server.server.getWorld();
			NoInstanceVMapManager manager = (NoInstanceVMapManager)world.getVMapManager(MAPS[i]);
			VMap[] maps = manager.getVMaps(MAPS[i]);
			try {
				Field field = VMap.class.getDeclaredField("neutral");
				field.setAccessible(true);
				for (VMap map : maps) {
					field.set(map, Boolean.TRUE);
				}
				System.out.println("MAP "+MAPS[i]+" OK");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
