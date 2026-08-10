package peony.patchs;

import peony.db.SyncExecutorService;

public class RevertAutoMerge implements Runnable {

	public void run() {
		SyncExecutorService.async = 0;
	}

}
