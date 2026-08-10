package peony.patchs;

import peony.game.Player;
import peony.game.Server;

public class DecCreditPatch implements Runnable {

	protected int[][] datas = new int[][]{{16286343, 21202366}};
	
	public void run() {
		for(int i=0;i<datas.length;i++){
			int[] arr = datas[i];
			int playerId = arr[0];
			int accountId = arr[1];
			Player player = Server.server.getServiceRegistry().getPlayerService().loadPlayer(accountId, playerId);
			if(player!=null){
				player.setCredit(0, "BUG");
				System.out.println("[DECCREDITOK]PLAYER[" + playerId + "]CREDIT[" + player.getCredit() + "]");
			}
		}
	}
}
