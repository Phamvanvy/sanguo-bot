package peony.game;

import peony.common.ClientSessionAsyncCall;
import peony.game.itemeffect.GetClickExpEffect;
import peony.net.ClientSession;
import peony.net.Packet;

public class UseFuXingBagCall extends ClientSessionAsyncCall{
	
	int serial;
	int useIB;
	int itemId;
	Player player = null;
	
	public UseFuXingBagCall(ClientSession session,Packet packet) {
		super(session);
		serial = packet.getInt();
		useIB = packet.get();
		itemId = packet.getInt();
		player = (Player)session.getClient();
	}
	
	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.FUXING_BAG_SERVER);
			pt.putInt(serial);
			session.send(pt);
		}
	}
	public void run() {
		if(player!=null){
			GameItem item = ObjectAccessor.createGameItem(itemId);
			if(item != null){
				PlayerTransaction tx = player.newTransaction("FUXINLIBAO");
				try{
					new GetClickExpEffect().useItem(player, item, player, useIB, tx ,session);
				}catch(Exception e){
					return;
				}
			}else{
				return;
			}
		}
		addToClientSession();
	}
	
}
