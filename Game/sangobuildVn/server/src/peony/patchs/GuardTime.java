package peony.patchs;

import peony.game.Server;
import peony.game.nation.Nation;
import peony.game.nation.NationService;

public class GuardTime implements Runnable {

	public void run() {
		NationService service = Server.server.getServiceRegistry().getNationService();
		Nation[] nations = new Nation[3];
		nations[0] = service.getNationByFaction(1);
		nations[1] = service.getNationByFaction(2);
		nations[2] = service.getNationByFaction(3);
		for(Nation nation:nations){
			System.out.println(nation.guardTime);
		}
	}

}
