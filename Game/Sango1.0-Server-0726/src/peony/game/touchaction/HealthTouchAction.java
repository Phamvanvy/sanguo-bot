package peony.game.touchaction;

import peony.game.Creature;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.attendant.Attendant;
import peony.game.pk.PkInfo;

public class HealthTouchAction implements TouchAction {

	public void touch(Player player, Creature npc) {
		if (player.pkInfo == null
				|| player.pkInfo.state != PkInfo.STATE_STARTED) {
			Attendant attendant = player.attendant;
			if(attendant==null){
				if (player.maxhp == player.hp && player.maxmp == player.mp) {
					ErrorHandler.sendErrorMessage(player.session, -1, OpCode.TOUCHNPC_CLIENT, "你已经不需要治疗了");
					return;
				} else {
					player.setHp(player.maxhp, true);
					player.setMp(player.maxmp, true);
				}
			}else{
				if(player.maxhp == player.hp && player.maxmp == player.mp 
						&& attendant.maxhp == attendant.hp && attendant.maxmp == attendant.mp){
					ErrorHandler.sendErrorMessage(player.session, -1, OpCode.TOUCHNPC_CLIENT, "你已经不需要治疗了");
					return;
				}else{
					player.setHp(player.maxhp, true);
					player.setMp(player.maxmp, true);
					attendant.setHp(attendant.maxhp, true);
					attendant.setMp(attendant.maxmp, true);
				}
			}
		} else {
			ErrorHandler.sendErrorMessage(player.session, -1, OpCode.TOUCHNPC_CLIENT, "决斗过程中不能治疗");
		}
	}

}
