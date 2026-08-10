package peony.patchs;

import peony.game.Horse;
import peony.game.Player;
import peony.game.Server;

public class HorseFixPatch implements Runnable {

	public static int[] playerIds = {1015978};
	
	public void run() {
		for(int playerId : playerIds){
			Player player = Server.server.getServiceRegistry().getPlayerService().loadPlayerSilent(playerId);;
			if(player!=null){
				for(Horse horse : player.horseBag.horses){
					if(horse!=null){
						if(horse.iconId>6 && horse.iconImage==1){
							horse.iconImage = 0;
							System.out.println("-------------OK");
						}
					}
				}
			}
		}
	}

}
