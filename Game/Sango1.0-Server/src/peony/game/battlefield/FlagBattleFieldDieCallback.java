package peony.game.battlefield;

import peony.game.BaseDieCallback;
import peony.game.CreditUtil;
import peony.game.GameObject;
import peony.game.Player;
import peony.game.Unit;

public class FlagBattleFieldDieCallback extends BaseDieCallback {

	@Override
	protected int[] getPvpCreditChanged(Player player, int maxWinLevel) {
		return CreditUtil.getFlagBattleFieldCredit(maxWinLevel, player.level);
	}

	public void die(Player player, Unit source) {
		processReliveOptions(player,null);
		if (source != null && source.type == GameObject.TYPE_PLAYER
				&& source.faction != player.faction) {
			processPvpDie(player, source);
		} else if (source != null && source.type == GameObject.TYPE_CREATURE) {
			processPveDie(player, source);
		}
		player.enemyPlayers.clear();
		FlagBattleFieldInstance instance = (FlagBattleFieldInstance)player.getVMap().instance;
		instance.died(player, source);
	}

}
