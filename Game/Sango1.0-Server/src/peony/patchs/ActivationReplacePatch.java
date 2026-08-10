package peony.patchs;

import java.lang.reflect.Field;

import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerPacketHandler;
import peony.game.Server;
import peony.net.AbstractClientSession;
import peony.net.AbstractClientSessionService;
import peony.net.DispatchClientSession;
import peony.net.DispatchClientSessionService;
import peony.service.PacketHandlerService;

public class ActivationReplacePatch implements Runnable {
	public void run() {
		PacketHandlerService service = Server.server.getServiceRegistry().getPacketHandlerService();
		DispatchClientSessionService dcs = (DispatchClientSessionService)Server.server.getServiceRegistry().getService(DispatchClientSessionService.class);
		try {
			Field f = service.getClass().getDeclaredField("playerHandler");
			PlayerPacketHandler newHandler = new PlayerPacketHandlerPatch();
			f.setAccessible(true);
			f.set(service, newHandler);
			System.out.println("PacketHandlerService done, new handler is : " + service.getPlayerHandler());
			
			f = AbstractClientSessionService.class.getDeclaredField("handler");
			f.setAccessible(true);
			f.set(dcs, newHandler);
			System.out.println("DispatchClientSessionService done, new handler is : " + f.get(dcs));
			
			f = AbstractClientSession.class.getDeclaredField("handler");
			f.setAccessible(true);
			for (Player p : ObjectAccessor.players.values()) {
				if (p.session != null && p.session instanceof DispatchClientSession) {
					DispatchClientSession psession = (DispatchClientSession)p.session;
					f.set(psession, newHandler);
					System.out.println("Player done, new handler is : " + f.get(psession));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
