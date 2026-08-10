package peony.patchs;

import peony.game.Server;
import peony.game.gift.GameChannelService;

public class ChannelGiftPatch implements Runnable {

	public void run() {
		GameChannelService service = Server.server.getServiceRegistry().getGameChannelService();
		try {
			service.loadConfig();
			System.out.println("reload ChannelGiftPatch OK");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
