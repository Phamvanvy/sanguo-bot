package peony.patchs;

import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;

public class CheatOffPatch implements Runnable {

	public void run() {
		Server.cheatOn = false;
		for(Player player : ObjectAccessor.players.values()){
			if(player!=null)
				player.cheat = false;
		}
		System.out.println("cheat off ok");
	}

}
