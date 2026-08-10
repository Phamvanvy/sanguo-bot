package peony.game.association;

import peony.game.Server;

public class AssociationInvite {

	public int associationId;
	public String associationName;
	public int inviter;
	public long receiveAssoInvTime; //接受结义请求的时间
	public long endTime; //结束时间
	
	public AssociationInvite(int associationId, int inviter, long receiveAssoInvTime){
		AssociationService service = Server.server.getServiceRegistry().getAssociationService();
		this.associationId = associationId;
		this.associationName = service.getAssociationById(associationId).name;
		this.inviter = inviter;
		this.receiveAssoInvTime = receiveAssoInvTime;
		this.endTime = receiveAssoInvTime + Association.OUTTIME;
	}
	
}
