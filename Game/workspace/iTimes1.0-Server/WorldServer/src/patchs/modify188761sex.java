package patchs;

import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.battle.Battle2;
import com.pip.itimes.server.world.battle.BattleService2;
import com.pip.itimes.server.world.battle.BattleSprite;

public class modify188761sex implements Runnable {

	public void run() {
		PlayerService service = Server.instance.playerService;
		WorldPlayer player = service.getWorldPlayer(188761);
		if (player == null) {
			System.out.println("188761 Not Found.");
			return;
		}
		
		player.setSex((byte)0);
		service.savePlayer(player);
		System.out.println("188761ok");
	}
}
