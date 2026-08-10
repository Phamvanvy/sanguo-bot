package peony.game;

import peony.game.attendant.Attendant;

public class NormalDieCallback extends BaseDieCallback {

	public static int MAX_PVP = 71;
	
	public void die(Player player, Unit source) {
		processReliveOptions(player,null);
		VMap map = player.getVMap();
		if (map!=null && map.mapDef.mapInfo.protect != 0 && source!=null && 
		        source.type == GameObject.TYPE_PLAYER && source.faction != player.faction) {
			if (player.warState==Player.PVPSTATE || player.warState==Player.PVEPVPSTATE) {
				player.pvpKilledTimes++;
				if(player.pvpKilledTimes>=map.mapDef.mapInfo.protect && player.level<70){
					player.pvpKilledTimes = 0;
					player.pvp2pveMapId = map.getId();
					player.pvpFactionTime = Time.currTime + 15 *60 * 1000;
					if(player.level>=50 && player.level<70)
						player.pvpFactionTime = Time.currTime + 20 * 60 * 1000;
					if(MAX_PVP>0 && player.level<MAX_PVP)
						player.setWarState(Player.PVPPVESTATE);
				}else if(player.pvpKilledTimes>=map.mapDef.mapInfo.protect && player.level==70){
					if(player.pvpKilledTimes==3){
						player.pvpFactionTime = Time.currTime + 5 * 60 * 1000;
						player.setWarState(Player.PVPPVESTATE);
					}
				}
			}
		}
		if (source != null && source.type == GameObject.TYPE_PLAYER
				&& source.faction != player.faction) {
			processPvpDie(player, source);
		} else if (source != null && source.type == GameObject.TYPE_CREATURE) {
			processPveDie(player, source);
		} else if(source != null && source.type == GameObject.TYPE_ATTENDANT){
			Attendant att = (Attendant)source;
			Player p = att.owner;
			source = (Unit)p;
			player.enemyPlayers.put(p.id, Time.currTime);
			processPvpDie(player, source);
		}
		player.enemyPlayers.clear();
	}

	@Override
	protected int[] getPvpCreditChanged(Player player,int maxWinLevel){
		return CreditUtil.getCredit(maxWinLevel, player.level);
	}
}
