package peony.game.nation;

import peony.game.BaseDieCallback;
import peony.game.CreditUtil;
import peony.game.GameObject;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Unit;

public class NationBattleFieldDieCallback extends BaseDieCallback {
	
	public static final int[] EXP = {
		0,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		0 ,
		1286 ,
		1440 ,
		1607 ,
		1787 ,
		1983 ,
		2377 ,
		2624 ,
		2890 ,
		3175 ,
		3481 ,
		4101 ,
		299 ,
		325 ,
		354 ,
		384 ,
		476 ,
		515 ,
		557 ,
		601 ,
		648 ,
		784 ,
		421 ,
		452 ,
		485 ,
		519 ,
		679 ,
		429 ,
		528 ,
		638 ,
		759 ,
		1063 ,
		271 ,
		336 ,
		407 ,
		486 ,
		893 ,
		1379 ,
		2272 ,
		3650 ,
		5922 ,
		9573 ,
		15495 ,
		25067 ,
		40562 ,
		65629 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
		106192 ,
	};
	
	public void die(Player player, Unit source) {
		processReliveOptions(player,null);
		if (source != null && source.type == GameObject.TYPE_PLAYER
				&& source.faction != player.faction) {
			processPvpDie(player, source);
			Player p = (Player)source;
			PlayerTransaction tx = p.newTransaction("NBT");
			p.addExp(EXP[player.level], tx, true);
			tx.commit();
		} else if (source != null && source.type == GameObject.TYPE_CREATURE) {
			processPveDie(player, source);
		}
		player.enemyPlayers.clear();
	}

	@Override
	protected int[] getPvpCreditChanged(Player player,int maxWinLevel){
		return CreditUtil.getCredit(maxWinLevel, player.level);
	}

}
