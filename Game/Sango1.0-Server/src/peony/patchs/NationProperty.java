package peony.patchs;

import peony.game.Server;
import peony.game.nation.Nation;
import peony.game.nation.NationService;

public class NationProperty implements Runnable {

	public void run() {
		NationService service = Server.server.getServiceRegistry().getNationService();
		Nation[] nations = new Nation[3];
		nations[0] = service.getNationByFaction(1);
		nations[1] = service.getNationByFaction(2);
		nations[2] = service.getNationByFaction(3);
		for(Nation nation:nations){
			System.out.println(nation.pool.getInt(Nation.PROPERTY_BATTLE_ATTACK));
			System.out.println(nation.pool.getInt(Nation.PROPERTY_BATTLE_DEFENSE));
			System.out.println(nation.pool.getInt(Nation.PROPERTY_ATTACK_VALUE));
			System.out.println(nation.pool.getInt(Nation.PROPERTY_DEFENSE_VALUE));
		}
	}

}
