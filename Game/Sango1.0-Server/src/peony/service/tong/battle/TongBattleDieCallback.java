package peony.service.tong.battle;

import peony.game.BaseDieCallback;
import peony.game.CreditUtil;
import peony.game.GameObject;
import peony.game.Player;
import peony.game.Unit;

public class TongBattleDieCallback extends BaseDieCallback {

	
	@Override
	protected int[] getPvpCreditChanged(Player player, int maxWinLevel) {
		return CreditUtil.getCredit(maxWinLevel, player.level);
	}

	public void die(Player player, Unit source) {
		TongBattleFieldInstance instance = (TongBattleFieldInstance)player.getVMap().instance;
		processReliveOptions(player,instance.getRelivePoint(player.id));
		if (source != null && source.type == GameObject.TYPE_PLAYER
				&& source.faction != player.faction) {
			processPvpDie(player, source);
		} else if (source != null && source.type == GameObject.TYPE_CREATURE) {
			processPveDie(player, source);
		}
		player.enemyPlayers.clear();
	}

}
