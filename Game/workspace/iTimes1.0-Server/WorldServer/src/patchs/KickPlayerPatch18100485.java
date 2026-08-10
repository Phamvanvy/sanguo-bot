package patchs;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;

public class KickPlayerPatch18100485 implements Runnable {
	
	public void run() {
		PlayerService service = Server.instance.playerService;
		WorldPlayer player = service.getWorldPlayer(18100485);
		if (player != null) {
			try {
				Field field = PlayerService.class.getDeclaredField("names");
				field.setAccessible(true);
				ConcurrentHashMap names = (ConcurrentHashMap) field.get(service);
				names.remove(player.getPlayerName());
				System.out.println("remove name 18100485 ok");
				field = PlayerService.class.getDeclaredField("players");
				field.setAccessible(true);
				ConcurrentHashMap players = (ConcurrentHashMap) field.get(service);
				players.remove(player.getId());
				System.out.println("remove player 18100485 ok");
				field = PlayerService.class.getDeclaredField("accounts");
				field.setAccessible(true);
				ConcurrentHashMap accounts = (ConcurrentHashMap) field.get(service);
				accounts.remove(player.getAccountId());
				System.out.println("remove account 18100485 ok");
			} catch (Exception e) {
				e.printStackTrace();
			} 
		} else {
			System.out.println("player 18100485 not found.");
		}
		
	}

}
