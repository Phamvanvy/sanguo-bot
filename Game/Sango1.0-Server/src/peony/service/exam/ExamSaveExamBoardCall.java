package peony.service.exam;

import java.util.List;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Server;
import peony.net.ClientSession;

public class ExamSaveExamBoardCall extends ClientSessionAsyncCall {

	private Logger log = Logger.getLogger(ExamSaveExamBoardCall.class);
	
	protected List<ExamBoard> copy = null;
	
	public ExamSaveExamBoardCall(ClientSession session, List<ExamBoard> copy) {
		super(session);
		this.copy = copy;
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		ExamBoardDao dao = Server.server.getServiceRegistry().getDbService().examBoardDao;
		for(ExamBoard b : copy){
			List<ExamBoard> list = dao.list("from ExamBoard o where o.playerId=?", b.playerId);
			if(list!=null && list.size()>0)
				dao.updateEntity(b);
			else
				dao.makePersistent(b);
			log.info("[EXAM-SAVEBOARD]PLAYER["+b.playerId+"]PASS["+b.getPassCount()+"]TIME["+b.getTotalTime()+"]TODAY["+b.getTodayCount()+"]RECORDDAY"+b.getRecordDay()+"]TYPE["+b.getExamType()+"]");
		}
	}

}
