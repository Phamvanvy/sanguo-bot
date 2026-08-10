package peony.game.association;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class AssociationCreateCall extends ClientSessionAsyncCall {

	protected int serial;
	protected String name;
	protected Player player;
	protected Association association;
	
	public AssociationCreateCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.name = packet.getString();
		this.player = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if(success && player!=null){
			Server.server.getServiceRegistry().getAssociationService().addAssociation(association, player.id);
			Packet pt = new Packet(OpCode.ASSOCIATION_CREATE_SERFER);
			pt.putInt(serial);
			session.send(pt);
			LogUtil.logAssociationCreated(player, association.id);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.ASSOCIATION_CREATE_CLIENT, errorMessage);
		}
	}

	public void run() {
		if(player!=null){
			AssociationService service = Server.server.getServiceRegistry().getAssociationService();
			try {
				association = service.createAssociation(player, name);
				PlayerTransaction tx = player.newTransaction("ASSOCIATION");
				GameItem item = player.bag.removeGameItem(ItemUtil.ITEM_ASSOCIATION, -1, 1, tx, true);
				if(item!=null){
					tx.commit();
				}else{
					tx.rollback();
					error("Cắt máu ăn thề là việc quan trọng của các huynh đệ, nhất định phải có người kết nghĩa thể hiện được thâm tình của 2 người");
				}
			} catch (AssociationException e) {
				error(e.getMessage());
			}
		}
		addToClientSession();
	}

}
