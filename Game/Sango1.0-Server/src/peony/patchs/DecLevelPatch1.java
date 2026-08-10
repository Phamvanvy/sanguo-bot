package peony.patchs;

import peony.game.Player;
import peony.game.Server;
import peony.service.player.PlayerService;

public class DecLevelPatch1 implements Runnable {

	public static int actorId = 106484;
	
	public void run() {
		PlayerService service = Server.server.getServiceRegistry().getPlayerService();
		Player player = service.loadPlayerSilent(actorId);
		if(player!=null){
			player.setSkillPoint(0, false);
			player.setPropertyPoint(0, false);
		}
	}

}
