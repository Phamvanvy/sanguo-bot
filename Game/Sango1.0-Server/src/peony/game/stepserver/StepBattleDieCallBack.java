package peony.game.stepserver;

import peony.game.BaseDieCallback;
import peony.game.Player;
import peony.game.Unit;

public class StepBattleDieCallBack extends BaseDieCallback {

	protected int[] getPvpCreditChanged(Player player, int maxWinLevel) {
		return null;
	}

	public void die(Player player, Unit source) {
		
	}

	protected void processPveDie(Player player, Unit source) {
		super.processPveDie(player, source);
	}

	protected void processPvpDie(Player player, Unit source) {
		
	}

	protected void processReliveOptions(Player player, int[] relivePoint) {
		
	}

}
