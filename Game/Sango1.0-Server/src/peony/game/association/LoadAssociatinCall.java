package peony.game.association;

import peony.common.ClientSessionAsyncCall;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;

public class LoadAssociatinCall extends ClientSessionAsyncCall {

	protected Player player;
	protected Association association;
	
	protected static String ASSOCIATION = "associationid";
	
	public LoadAssociatinCall(ClientSession session, Player player) {
		super(session);
		this.player = player;
	}

	public void callFinish() throws Exception {
		if(success){
			AssociationService service = Server.server.getServiceRegistry().getAssociationService();
			if(association!=null && player!=null){
				service.addAssociation(association, player.id);
			}
		}else{
			if(player!=null)
				player.pool.setInt(ASSOCIATION, 0);
		}
	}

	public void run() {
		if(player!=null){
			int associationId = player.pool.getInt(ASSOCIATION, 0);
			AssociationService service = Server.server.getServiceRegistry().getAssociationService();
			association = service.getAssociationById(associationId);
			if(association==null){
				AssociationDao dao = Server.server.getServiceRegistry().getDbService().associationDao;
				association = (Association) dao.uniqueResult("from Association a where a.id=?", associationId);
				if(association!=null){
					AssociationMember mem = association.getMember(player.id);
					if(mem==null){
						error("");
					}
				}else{
					error("");
				}
			}
		}
		addToClientSession();
	}

}
