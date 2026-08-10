package peony.patchs;

import java.lang.reflect.Field;
import java.util.HashMap;
import peony.game.Server;
import peony.mobiphone.TelcoChargeService;

public class TelcoMessagePatch implements Runnable {

	public void run() {
		TelcoChargeService service = Server.server.getServiceRegistry().getTelcoChargeService();
		try {
			Field messagesF = TelcoChargeService.class.getDeclaredField("messages");
			messagesF.setAccessible(true);
			HashMap<Integer, String> messages0 = (HashMap<Integer, String>) messagesF.get(service);
			messages0.clear();
			service.startup();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
