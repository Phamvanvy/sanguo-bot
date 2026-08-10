package peony.game.touchaction;

import peony.game.Creature;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.pk.PkInfo;

public class HealthTouchAction implements TouchAction {

	public void touch(Player player, Creature npc) {
		if (player.pkInfo == null
				|| player.pkInfo.state != PkInfo.STATE_STARTED) {
			if (player.maxhp == player.hp && player.maxmp == player.mp) {
				ErrorHandler.sendErrorMessage(player.session, -1, OpCode.TOUCHNPC_CLIENT, "你已經不需要治療了");
				return;
			} else {
				player.setHp(player.maxhp, true);
				player.setMp(player.maxmp, true);
			}
		} else {
			ErrorHandler.sendErrorMessage(player.session, -1, OpCode.TOUCHNPC_CLIENT, "決斗過程中不能治療");
		}
	}

}
