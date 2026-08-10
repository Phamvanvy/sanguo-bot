package peony.game;

public class NormalDieCallback extends BaseDieCallback {

	public void die(Player player, Unit source) {
		processReliveOptions(player,null);
		VMap map = player.getVMap();
		if (map!=null && map.mapDef.mapInfo.protect != 0 && source!=null && 
		        source.type == GameObject.TYPE_PLAYER && source.faction != player.faction) {
			if (player.warState==Player.PVPSTATE) {
				player.pvpKilledTimes++;
				if(player.pvpKilledTimes>=map.mapDef.mapInfo.protect){
					player.pvpKilledTimes = 0;
					player.pvp2pveMapId = map.getId();
					player.pvpFactionTime = Time.currTime + 15 *60 * 1000;
					player.setWarState(Player.PVPPVESTATE);
				}
			}
		}
		if (source != null && source.type == GameObject.TYPE_PLAYER
				&& source.faction != player.faction) {
			processPvpDie(player, source);
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
