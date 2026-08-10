package peony.game;

public class PvePvpState implements WarState{

	public void war(Player player) {
		player.pvpFactionTime = Time.currTime + Player.PVP_TIME;
	}

	public void enter(Player player) {
		player.pvpFaction();
	}

	public void exit(Player player) {
		
	}

	public void update(Player player) {
		if (player.level > Player.MAX_PVE_LEVEL) {
			player.setWarState(Player.PVPSTATE);
			return;
		}
		if (Time.currTime > player.pvpFactionTime) {
			VMap map = player.getVMap();
			if((map != null && map.getFaction()==player.faction))
				player.setWarState(Player.PVESTATE);
		}
	}

}
