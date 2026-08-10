package patchs;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;

public class KickPlayerPatch5157925 implements Runnable {
	
	public void run() {
		PlayerService service = Server.instance.playerService;
		WorldPlayer player = service.getWorldPlayer(5157925);
		if (player != null) {
			try {
				Field field = PlayerService.class.getDeclaredField("names");
				field.setAccessible(true);
				ConcurrentHashMap names = (ConcurrentHashMap) field.get(service);
				names.remove(player.getPlayerName());
				System.out.println("remove 5157925 name ok");
				field = PlayerService.class.getDeclaredField("players");
				field.setAccessible(true);
				ConcurrentHashMap players = (ConcurrentHashMap) field.get(service);
				players.remove(player.getId());
				System.out.println("remove 5157925 player ok");
				field = PlayerService.class.getDeclaredField("accounts");
				field.setAccessible(true);
				ConcurrentHashMap accounts = (ConcurrentHashMap) field.get(service);
				accounts.remove(player.getAccountId());
				System.out.println("remove 5157925 account ok");
			} catch (Exception e) {
				e.printStackTrace();
			} 
		} else {
			System.out.println("player 5157925 not found.");
		}
		
	}

}
