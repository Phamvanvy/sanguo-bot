package peony.patchs;

import peony.game.Player;

public class AntiPlugPatch implements Runnable {

	public void run() {
		Player.antiPlugModel = Player.ANTIPLUG_MODEL_LOG;
	}

}
