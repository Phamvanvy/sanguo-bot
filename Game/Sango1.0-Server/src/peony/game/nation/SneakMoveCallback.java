package peony.game.nation;

import peony.game.MoveCallback;
import peony.game.Player;

public class SneakMoveCallback implements MoveCallback {

	public void moved(Player p) {
		NationSneakBattleFieldInstance instance = (NationSneakBattleFieldInstance)p.getVMap().instance;
		instance.moveAt(p);
	}

}
