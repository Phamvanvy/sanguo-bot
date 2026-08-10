package peony.game.stepserver;

import peony.game.MoveCallback;
import peony.game.Player;

public class StepBattleMoveCallBack implements MoveCallback {

	public void moved(Player p) {
		StepBattleInstance instance = (StepBattleInstance)p.getVMap().instance;
		instance.moveAt(p);
	}

}
