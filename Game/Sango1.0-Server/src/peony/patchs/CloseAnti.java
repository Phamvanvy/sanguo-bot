package peony.patchs;

import peony.game.Server;

public class CloseAnti implements Runnable {

	public void run() {
		Server.antiCheat = false;
	}

}
