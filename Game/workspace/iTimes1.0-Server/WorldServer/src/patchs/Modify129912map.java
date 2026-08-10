package patchs;

import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.battle.Battle2;
import com.pip.itimes.server.world.battle.BattleService2;
import com.pip.itimes.server.world.battle.BattleSprite;

public class Modify129912map implements Runnable {

	public void run() {
		PlayerService service = Server.instance.playerService;
		WorldPlayer player = service.getWorldPlayer(129912);
		if (player == null) {
			System.out.println("129912 Not Found.");
			return;
		}
		Server.instance.worldService.getNoInstanceMap((short)1649).addPlayer(player);
		service.savePlayer(player);
		System.out.println("129912ok");
	}
}
