package peony.patchs;

import peony.db.MailListCall;

public class MailListCallPatch implements Runnable {

	public void run() {
		MailListCall.MAIL_LIST_TYPE = 1;
	}

}
