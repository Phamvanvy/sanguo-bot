package peony.game;

public class PvpState implements WarState{

	public void war(Player player) {
		
	}

	public void enter(Player player) {
		player.pvpFaction();
	}

	public void exit(Player player) {

	}

	public void update(Player player) {
		if(player.pvp2pveMapId>0){
			VMap map = player.getVMap();
			if(map!=null&&map.getStageId()!=(player.pvp2pveMapId>>4)){
				player.pvp2pveMapId = 0;
				player.pvpKilledTimes = 0;
			}
		}
	}

}
