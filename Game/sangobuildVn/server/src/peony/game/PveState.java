package peony.game;

public class PveState implements WarState{

	public void war(Player player) {
		player.pvpFactionTime = Time.currTime + Player.PVP_TIME;
		player.setWarState(Player.PVEPVPSTATE);
	}

	public void enter(Player player) {
		player.unPvpFaction();
	}

	public void exit(Player player) {
		
	}

	public void update(Player player) {
		if (player.level > Player.MAX_PVE_LEVEL) {
			player.setWarState(Player.PVPSTATE);
			return;
		}
		VMap map = player.getVMap();
		if((map != null && map.getFaction()!=player.faction)){
			player.setWarState(Player.PVEPVPSTATE);
			return;
		}
	}

}
