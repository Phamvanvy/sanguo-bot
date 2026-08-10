package peony.service.towerdefend;

import peony.game.BaseDieCallback;
import peony.game.CreditUtil;
import peony.game.GameObject;
import peony.game.Player;
import peony.game.Unit;

public class TowerDefendDieCallBack extends BaseDieCallback {

	@Override
	protected int[] getPvpCreditChanged(Player player, int maxWinLevel) {
		return CreditUtil.getCredit(maxWinLevel, player.level);
	}

	public void die(Player player, Unit source) {
		TowerDefendInstance instance = (TowerDefendInstance)player.getVMap().instance;
		processReliveOptions(player,instance.getRelivePoint(player));
		if (source != null && source.type == GameObject.TYPE_PLAYER
				&& source.faction != player.faction) {
			processPvpDie(player, source);
		} else if (source != null && source.type == GameObject.TYPE_CREATURE) {
			processPveDie(player, source);
		}
		player.enemyPlayers.clear();
	}

}
