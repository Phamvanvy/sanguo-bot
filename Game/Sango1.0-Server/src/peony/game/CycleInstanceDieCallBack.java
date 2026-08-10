package peony.game;

import peony.service.CycleInstanceMapManager;

public class CycleInstanceDieCallBack extends BaseDieCallback {
	
	public void die(Player player, Unit source) {
		if(player!=null){
			processReliveOptions(player,CycleInstanceMapManager.out.get(player.faction));
			if(source != null && source.type == GameObject.TYPE_PLAYER && source.faction != player.faction) {
				processPvpDie(player, source);
			}else if (source != null && source.type == GameObject.TYPE_CREATURE) {
				processPveDie(player, source);
			}
			player.enemyPlayers.clear();
		}
	}
	
	protected void processReliveOptions(Player player,int[] relivePoint){
		if(player.warState==Player.PVEPVPSTATE)
			player.setWarState(Player.PVESTATE);
		player.reliveOptions = new ReliveOptions(Time.currTime + 60 * 1000);
		if(relivePoint==null)
			relivePoint = player.map.map.getRelivePoint(player.faction);
		ReliveOption option = new ReliveOption(ReliveOption.NORMAL, peony.Messages.STRING_00962, 14,
				relivePoint[0], relivePoint[1], relivePoint[2]);
		player.reliveOptions.addOption(option, false);
		player.send(player.reliveOptions.getRelivePacket());
		player.pool.setInt(CycleInstanceMapManager.propertyOfCycleDieDay, CycleInstanceMapManager.currentDay);
	}

	protected int[] getPvpCreditChanged(Player player, int maxWinLevel) {
		return CreditUtil.getCredit(maxWinLevel, player.level);
	}

}
