package peony.patchs;

import java.lang.reflect.Field;
import peony.game.Server;
import peony.service.jetty.JettyService;

public class JettyPatch2 implements Runnable {

	public void run() {
		try {
			JettyService service = Server.server.getServiceRegistry().getJettyService();
			Field f3 = JettyService.class.getDeclaredField("server");
			f3.setAccessible(true);
			org.mortbay.jetty.Server server = (org.mortbay.jetty.Server) f3.get(service);
			server.stop();
			service.startup();
			System.out.println("__________patch OK");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
