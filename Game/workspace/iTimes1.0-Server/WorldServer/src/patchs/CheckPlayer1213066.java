package patchs;

import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.battle.Battle2;
import com.pip.itimes.server.world.battle.BattleService2;
import com.pip.itimes.server.world.battle.BattleSprite;

public class CheckPlayer1213066 implements Runnable {

	public void run() {
		PlayerService service = Server.instance.playerService;
		WorldPlayer player = service.getWorldPlayer(1213066);
		 if(player==null){
		 System.out.println("1213066 Not Found.");
		 return;
		 }
		 if(player.getMap()==null){
		 System.out.println("1213066 Not Map.");
		 }
		BattleService2 bs2 = Server.instance.battleService;
		if (bs2.inBattle(player)) {
			Battle2 bl = bs2.getBattleByPlayer(player.getId());
			System.out.println("Battle InstaceOf " + bl.getClass());
			BattleSprite[] s1 = bl.getSide1();
			BattleSprite[] s2 = bl.getSide2();
			for (int i = 0; i < s1.length; i++) {
				if (s1[i] != null) {
					System.out.println("side1:"+s1[i].name);
				}
			}
			for (int i = 0; i < s2.length; i++) {
				if (s2[i] != null) {
					System.out.println("side2:"+s2[i].name);
				}
			}
			bs2.removeBattle(bl);
		}
	}
}
