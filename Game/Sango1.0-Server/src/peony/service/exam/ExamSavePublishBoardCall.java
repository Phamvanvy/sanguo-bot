package peony.service.exam;

import java.util.List;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Server;
import peony.net.ClientSession;

public class ExamSavePublishBoardCall extends ClientSessionAsyncCall {

	private Logger log = Logger.getLogger(ExamSavePublishBoardCall.class);
	
	protected List<ExamPublishBoard> copy = null;
	
	public ExamSavePublishBoardCall(ClientSession session, List<ExamPublishBoard> copy) {
		super(session);
		this.copy = copy;
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		ExamPublishBoardDao dao = Server.server.getServiceRegistry().getDbService().examPublishBoardDao;
		dao.delete("delete from ExamPublishBoard");
		for(ExamPublishBoard b : copy){
			dao.makePersistent(b);
			log.info("[EXAM-SAVEPUBLISHBOARD]PLAYER"+b.playerId+"]PASS["+b.getPassCount()+"]TIME["+b.getTotalTime()+"]RANKING["+b.getRanking()+"]");
		}
	}

}
