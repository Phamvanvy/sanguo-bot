package peony.patchs;

import peony.game.Server;
import peony.game.beautyparade.BeautyParadeService;

public class BeautyTitlePatch implements Runnable {

	public void run() {
		BeautyParadeService service = Server.server.getServiceRegistry().getBeautyParadeService();
		service.canProcessTitleIds0 = true;
	}

}
