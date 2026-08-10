package patchs;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;

public class KickPlayerPatch1 implements Runnable {
	
	public void run() {
		PlayerService service = Server.instance.playerService;
		WorldPlayer player = service.getWorldPlayer(2625315);
		if (player != null) {
			try {
				Field field = PlayerService.class.getDeclaredField("names");
				field.setAccessible(true);
				ConcurrentHashMap names = (ConcurrentHashMap) field.get(service);
				names.remove(player.getPlayerName());
				System.out.println("remove name ok");
				field = PlayerService.class.getDeclaredField("players");
				field.setAccessible(true);
				ConcurrentHashMap players = (ConcurrentHashMap) field.get(service);
				players.remove(player.getId());
				System.out.println("remove player ok");
				field = PlayerService.class.getDeclaredField("accounts");
				field.setAccessible(true);
				ConcurrentHashMap accounts = (ConcurrentHashMap) field.get(service);
				accounts.remove(player.getAccountId());
				System.out.println("remove account ok");
			} catch (Exception e) {
				e.printStackTrace();
			} 
		} else {
			System.out.println("player not found.");
		}
		
	}

}
