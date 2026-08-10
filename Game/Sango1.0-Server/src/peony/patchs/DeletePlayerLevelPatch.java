package peony.patchs;

import peony.db.DeletePlayerCall;

public class DeletePlayerLevelPatch implements Runnable {

	public void run() {
		DeletePlayerCall.DELTE_LIMIT_LEVEL = 100;
	}

}
