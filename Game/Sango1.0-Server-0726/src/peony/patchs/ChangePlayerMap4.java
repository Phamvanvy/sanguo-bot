package peony.patchs;

import peony.db.DBService;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;

public class ChangePlayerMap4 implements Runnable {

	public int[] playerIds = { 158333, 25267, 1543, 2175, 189024, 2073, 22749,
			174364, 37214, 44001, 183782, 186153, 27906, 220565, 186994, 34160,
			51372, 141678, 55764, 448, 82370, 68776, 45721, 161780, 24426,
			224309, 62024, 148621, 23, 982 };
	public int mapId = 1568;

	public void run() {
		DBService dbService = Server.server.getServiceRegistry().getDbService();
		for (int playerId : playerIds) {
			Player player = ObjectAccessor.getPlayer(playerId);
			if (player == null)
				player = dbService.playerDAO.getPlayerById(playerId);
			if (player != null) {
				// if(player.map.id==mapId){
				player.map.id = getMapByFaction(player.faction);
				player.x = 300;
				player.y = 300;
				try {
					dbService.playerDAO.updateEntity(player);
				} catch (Exception e) {

				}
				// }
				System.out.println("RELOAD PLAYER[" + playerId + "]OK");
			}
		}
	}

	public int getMapByFaction(int faction) {
		switch (faction) {
		case 1:
			return 272;
		case 2:
			return 240;
		case 3:
			return 352;
		}
		return 848;
	}

}
