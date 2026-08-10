package peony.patchs;

import peony.game.ObjectAccessor;

public class ItemCache implements Runnable {

	public void run() {
		System.out.println("ItemCacheSize:" + ObjectAccessor.cachedItems.size());
	}

}
