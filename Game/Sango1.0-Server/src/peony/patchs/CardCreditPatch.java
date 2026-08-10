package peony.patchs;

import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.Server;
import peony.game.mail.MailService;

public class CardCreditPatch implements Runnable {

	protected int[] players = {779,779,779,1498,1498,1498,26395,1470,1470,1470,1470,35787,1523,1523,1523,1523,112299,1470,1470};
	protected int[] credits = {20,20,20,20,20,20,20,20,20,20,20,20,50,20,20,20,50,20,50};
	protected int[] counts = {1,1,1,1,1,1,1,1,1,1,2,1,1,4,1,1,1,1,1};
	
	public void run() {
		MailService service = Server.server.getServiceRegistry().getMailService();
		for(int i=0;i<players.length;i++){
			int playerId = players[i];
			int credit = credits[i];
			int itemId = 0;
			if(credit==50)
				itemId = 972;
			else if(credit==20)
				itemId = 2303;
			else if(credit==5)
				itemId = 3794;
			else if(credit==1)
				itemId = 3769;
			int count = counts[i];
			GameItem item = null;
			try {item = ObjectAccessor.createGameItem(itemId);} catch (Exception e) {}
			if(item!=null)
				service.sendSystemMailAsync(playerId, "系统", "道具补发", "道具补发请查收", 0, item, count, "CARDRECRUIT");
		}
	}

}
