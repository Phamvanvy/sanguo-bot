package peony.patchs;

import peony.game.Player;
import peony.game.Server;

public class CreditModify implements Runnable {
	
	private static final int[] ids = {
		9968,
		7517,
		31890,
		3398,
		31445,
		11917,
		1029,
		};

	public void run() {
		for(int i=0;i<ids.length;i++){
			Player p = Server.server.getServiceRegistry().getPlayerService().loadPlayerSilent(ids[i]);
			p.money += 4860;
			p.setCredit(p.getCredit()-4860,"GM");
			p.setWeekCredit(p.getWeekCredit()-4860);
			Server.server.getServiceRegistry().getPlayerService().savePlayer(p);
			System.out.println(ids[i]+"ok");
		}
	}

}
