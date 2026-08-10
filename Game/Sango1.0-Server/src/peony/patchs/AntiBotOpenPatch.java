package peony.patchs;

import peony.game.Player;

public class AntiBotOpenPatch implements Runnable {

	public void run() {
		Player.antiBotModel = Player.ANTIPLUG_MODEL_NONBENEFIT;
	}

}
