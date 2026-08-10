package peony.patchs;

import peony.game.Player;
import peony.game.Server;

public class ClearMoney implements Runnable {

	public void run() {
		Player p = Server.server.getServiceRegistry().getPlayerService().loadPlayerSilent(36389);
		if(p.name.equals("孤鴻雪")){
			p.money = 0;
		}
		Server.server.getServiceRegistry().getDbService().playerDAO.updateEntity(p);
		System.out.println("36389ok");
	}

}
