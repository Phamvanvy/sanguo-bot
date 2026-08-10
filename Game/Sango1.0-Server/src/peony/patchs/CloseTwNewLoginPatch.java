package peony.patchs;

import peony.service.account.adapter.QmeAdapter;

public class CloseTwNewLoginPatch implements Runnable {

	public void run() {
		QmeAdapter.useNewLoginProtocol = false;
	}

}
