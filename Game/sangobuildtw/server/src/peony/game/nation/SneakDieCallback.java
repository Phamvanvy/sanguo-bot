package peony.game.nation;

import peony.game.Player;
import peony.game.Unit;

public class SneakDieCallback extends NationBattleFieldDieCallback {

	public void die(Player player, Unit source) {
		if(player.flag!=null){
			player.setFlag(null);
		}
		super.die(player, source);
	}

}
