package peony.patchs;

import peony.game.ItemUtil;

public class NoticeItemsPatch implements Runnable {

	public void run() {
		ItemUtil.noticeItems.remove(1417);
		ItemUtil.noticeItems.remove(1459);
	}

}
