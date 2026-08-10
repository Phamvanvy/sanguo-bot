package peony.service.duelmetting;

import peony.game.BaseDieCallback;
import peony.game.GameObject;
import peony.game.Player;
import peony.game.ReliveOption;
import peony.game.ReliveOptions;
import peony.game.Time;
import peony.game.Unit;


public class DuelMettingDieCallBack extends BaseDieCallback {
	
	protected DuelMettingService service;
	
	protected DuelMettingDieCallBack(DuelMettingService service){
		this.service = service;
	}
	
	public void die(Player player, Unit source) {
		if(player!=null){
			if(player.getVMap().instance==null){
				processReliveOptions(player, null);
			}else{
				processReliveOptions(player);
			}
		}
	}
	
	protected void processReliveOptions(Player player){
		if(player.warState==Player.PVEPVPSTATE){
			player.setWarState(Player.PVESTATE);
		}
		player.reliveOptions = new ReliveOptions(Time.currTime + 60 * 1000);
		int[] point = service.getRevivePoint(player);
		ReliveOption option = new ReliveOption(ReliveOption.NORMAL, peony.Messages.STRING_00962, 14,
				point[0], point[1], point[2]);
		player.reliveOptions.addOption(option, false);
		player.send(player.reliveOptions.getRelivePacket());
	}

	protected int[] getPvpCreditChanged(Player player, int maxWinLevel) {
		return new int[]{0,0};
	}


}
