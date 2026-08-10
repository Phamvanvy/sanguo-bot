package peony.patchs;

import peony.game.PlayerPacketHandler;

public class CloseAyncVersionComparePatch implements Runnable {

	public void run() {
		PlayerPacketHandler.asyncVersionCompare = false;
	}

}
