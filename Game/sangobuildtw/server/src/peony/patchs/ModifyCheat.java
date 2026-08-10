package peony.patchs;

import peony.game.Server;

public class ModifyCheat implements Runnable {

	public void run() {
		Server.server.cheat = "showmemoney@@!!!!";
	}

}
