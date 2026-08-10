package peony.patchs;

import java.lang.reflect.Field;

import peony.game.GameObject;
import peony.game.Server;
import peony.game.buff.NationBuff;
import peony.game.nation.Nation;

public class NationBuffPatch implements Runnable{
	public void run(){
		Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(GameObject.FACTION_WEI);
		try {
			Field field = NationBuff.class.getDeclaredField("instanceId");
			field.setAccessible(true);
			field.set(nation, 10000000);
			System.out.println("hotfixok");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
