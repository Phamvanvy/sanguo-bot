package peony.patchs;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import peony.game.Player;
import peony.game.Server;
import peony.game.buff.Buff;
import peony.game.buff.Buffs;
import peony.service.player.PlayerService;

public class PlayerBuffPatch implements Runnable {

	public static int playerId = 282934;
	
	public void run() {
		PlayerService service = Server.server.getServiceRegistry().getPlayerService();
		Player player = service.loadPlayerSilent(playerId);
		if(player!=null && player.name.contains("De")){
			try {
				Field buffField = Buffs.class.getDeclaredField("buffs");
				buffField.setAccessible(true);
				List<Buff> buffs = (List<Buff>) buffField.get(player.buffs);
				Iterator<Buff> it = buffs.iterator();
				while(it.hasNext()){
					Buff b = it.next();
					if(b==null)
						it.remove();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("LOAD PlayerBuffPatch OK");
		}
	}

}
