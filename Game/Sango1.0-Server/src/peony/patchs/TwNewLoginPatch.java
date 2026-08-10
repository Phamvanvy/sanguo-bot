package peony.patchs;

import peony.service.account.adapter.QmeAdapter;

public class TwNewLoginPatch implements Runnable {

	public void run() {
		QmeAdapter.useNewLoginProtocol = true;
	}

}
