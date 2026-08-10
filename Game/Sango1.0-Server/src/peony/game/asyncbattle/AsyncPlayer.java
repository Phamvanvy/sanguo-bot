package peony.game.asyncbattle;

import peony.game.Player;

public class AsyncPlayer {

	public int instanceId;
	public int mapInstanceId;
	public Player asyncPlayer;
	public int target;
	
	public AsyncPlayer(int instanceId, int mapInstanceId, Player asyncPlayer, int target){
		this.instanceId = instanceId;
		this.mapInstanceId = mapInstanceId;
		this.asyncPlayer = asyncPlayer;
		this.target = target;
	}
	
	public static String getSearchKey(int instanceId, int mapInstanceId){
		return instanceId + "_" + mapInstanceId;
	}
	
}
