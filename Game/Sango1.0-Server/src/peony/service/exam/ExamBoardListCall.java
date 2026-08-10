package peony.service.exam;

import java.text.MessageFormat;
import java.util.List;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class ExamBoardListCall extends ClientSessionAsyncCall {

	private Logger log = Logger.getLogger(ExamBoardListCall.class);
	
	protected int serial;
	protected Player player;
	
	public ExamBoardListCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.player = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		ExamService examService = Server.server.getServiceRegistry().getExamService();
		List<ExamPublishBoard> publishBoards = examService.publishBoards;
		Packet pt = new Packet(OpCode.EXAM_BOARD_SERVER);
		pt.putInt(serial);
		pt.put(publishBoards.size());
		for(ExamPublishBoard b : publishBoards){
			Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(b.playerId);
			pt.put(b.ranking);
			pt.put(actor.faction);
			pt.putString(actor.name);
			pt.putInt(b.passCount);
			int ttime = b.getTotalTime();
			float ftime = ttime / 10f;
			pt.putString(MessageFormat.format("{0}√Î", String.valueOf(ftime)));
		}
		session.send(pt);
	}

	public void run() {
		addToClientSession();
	}

}
