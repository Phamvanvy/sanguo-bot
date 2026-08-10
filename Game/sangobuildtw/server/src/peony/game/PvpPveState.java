package peony.game;

public class PvpPveState implements WarState{

	public void war(Player player) {
		player.setWarState(Player.PVPSTATE);
	}

	public void enter(Player player) {
		player.unPvpFaction();
	}

	public void exit(Player player) {
		player.pvp2pveMapId = 0;
		player.pvpKilledTimes = 0;
	}

	public void update(Player player) {
		if (Time.currTime > player.pvpFactionTime) {
			player.setWarState(Player.PVPSTATE);
			return;
		}
		VMap map = player.getVMap();
		if (map!=null&&(map.getStageId() != (player.pvp2pveMapId >> 4))) {
			player.setWarState(Player.PVPSTATE);
			return;
		}
	}

}