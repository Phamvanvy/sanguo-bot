package peony.patchs;

import peony.game.ObjectAccessor;
import peony.game.Player;

public class ModifyTeacherPatch1 implements Runnable{
	public static int playerId = 1195993;
	public static int teacherId = 998585;
	public void run() {
		Player player = ObjectAccessor.getPlayer(playerId);
		if(player!=null){
			player.pool.remove(Player.PROPERTY_GRADUATE_TEACHER);
			if(player.relations!=null && player.relations.mateId==-1 && player.level>=70){
				player.pool.setInt(Player.PROPERTY_GRADUATE_TEACHER, teacherId);
			}
		}
	}
}