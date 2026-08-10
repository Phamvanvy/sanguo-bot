package peony.patchs;

import peony.game.Server;

public class CheatOnPatch implements Runnable {

	public void run() {
		Server.cheatOn = true;
		System.out.println("cheat on ok");
	}

}
