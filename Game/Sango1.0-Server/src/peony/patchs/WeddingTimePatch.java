package peony.patchs;

import peony.marriage.WeddingService;

public class WeddingTimePatch implements Runnable {

	public void run() {
		WeddingService.THREE_DAY = 60 * 60 * 24 * 3 * 1000L;
	}

}
