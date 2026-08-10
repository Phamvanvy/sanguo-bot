package peony.service.apprentice;


import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Time;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.game.Server;

public class ApprenticeInvitOkCall extends ClientSessionAsyncCall{
	int requestId ;
	Player player = null;
	public ApprenticeInvitOkCall(Packet packet,ClientSession session) {
		super(session);
		this.requestId = packet.getInt();
		player = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		if(player!=null){
			ApprenticeService service = Server.server.getServiceRegistry().getApprenticeService();
			ApprenticeRequest request = service.getAndRemoveRequest(requestId);
			if (request != null && (Time.currTime - request.time) < 60000) { // 一分钟之内有效
				Player source = (Player) ObjectAccessor
						.getGameObject(request.ref);
				if (source != null) {
					Player teacher = source;
					Player apprentice = source;
					if(source.level >=70){
						apprentice = player;
					} else {
						teacher = player;
					}
					service.createTeaAndApp(teacher,apprentice);
				}	
			} else {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_INVIT_OK_CLIENT, peony.Messages.STRING_01036);
			}
		}
	}
}
