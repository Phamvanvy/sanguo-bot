package peony.service.duel;

import peony.game.BaseDieCallback;
import peony.game.CreditUtil;
import peony.game.GameObject;
import peony.game.Player;
import peony.game.ReliveOption;
import peony.game.ReliveOptions;
import peony.game.Time;
import peony.game.Unit;

public class DuelDieCallBack extends BaseDieCallback {
	
	public void die(Player player, Unit source) {
		if(player!=null){
			if(player.getVMap().instance==null){
				processReliveOptions(player, null);
			}else{
				DuelInstance instance = (DuelInstance)player.getVMap().instance;
				processReliveOptions(player,instance.getRelivePoint(player.id));
			}
			if (source != null && source.type == GameObject.TYPE_PLAYER
					&& source.faction != player.faction) {
				processPvpDie(player, source);
			} else if (source != null && source.type == GameObject.TYPE_CREATURE) {
				processPveDie(player, source);
			}
			player.enemyPlayers.clear();
		}
	}
	
	protected void processReliveOptions(Player player,int[] relivePoint){
		if(player.warState==Player.PVEPVPSTATE){
			player.setWarState(Player.PVESTATE);
		}
//		player.removePvpFlag();
		player.reliveOptions = new ReliveOptions(Time.currTime + 60 * 1000);
		if(relivePoint==null)
			relivePoint = player.map.map.getRelivePoint(player.faction);
		ReliveOption option = new ReliveOption(ReliveOption.NORMAL, "Phóng thích", 14,
				relivePoint[0], relivePoint[1], relivePoint[2]);
		player.reliveOptions.addOption(option, false);
		player.send(player.reliveOptions.getRelivePacket());
	}

	protected int[] getPvpCreditChanged(Player player, int maxWinLevel) {
		return CreditUtil.getCredit(maxWinLevel, player.level);
	}

}
