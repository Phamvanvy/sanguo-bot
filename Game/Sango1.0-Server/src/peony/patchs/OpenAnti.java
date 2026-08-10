package peony.patchs;

import peony.game.Server;

public class OpenAnti implements Runnable {

	public void run() {
		Server.antiCheat = true;
	}

}
