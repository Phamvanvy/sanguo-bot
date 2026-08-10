package peony.patchs;

import java.lang.reflect.Field;

import org.mortbay.jetty.servlet.Context;

import peony.game.Server;
import peony.service.jetty.JettyService;

public class TempChargeServletPatch implements Runnable {
	public void run() {
		JettyService js = Server.server.getServiceRegistry().getJettyService();
		try {
			Field f = js.getClass().getDeclaredField("root");
			f.setAccessible(true);
			Context root = (Context)f.get(js);
			root.addServlet(TempChargeServlet.class, "/temp_charge_notify");
			System.out.println("patch ok");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
