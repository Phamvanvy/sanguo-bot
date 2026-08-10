package peony.patchs;

import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;

public class CardPunchPatch implements Runnable {

	public static int playerId = 34540;
	
	public void run() {
		Player player = ObjectAccessor.getPlayer(playerId);
		if(player==null)
			player = Server.server.getServiceRegistry().getPlayerService().loadPlayerSilent(playerId);
		if(player!=null){
			System.out.println("___________"+player.name+"    day:"+player.pool.getInt("CARD_PUNCH_DAY", 0)+"     week:"+
					player.pool.getInt("CARD_PUNCH_WEEKDAY", 0)+"      punchs:"+player.pool.getInt("CARD_PUNCH", 0));
		}
	}

}
