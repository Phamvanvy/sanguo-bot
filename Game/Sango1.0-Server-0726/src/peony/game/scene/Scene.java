package peony.game.scene;

import peony.game.Player;

/**
 * 一个游戏中的场景，管理场景中的NPC和玩家。
 * @author lighthu
 */
public class Scene {
	public static int getPlayerSight(Player p) {
		return 240;
	}
	
	public static int getPlayerMaxVision(Player p) {
		return 10;
	}
	
	class ScenePlayer {
		Player player;
		
	}
}
