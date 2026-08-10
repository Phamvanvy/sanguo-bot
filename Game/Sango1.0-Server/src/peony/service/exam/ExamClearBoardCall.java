package peony.service.exam;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Server;
import peony.net.ClientSession;

public class ExamClearBoardCall extends ClientSessionAsyncCall {

	private Logger log = Logger.getLogger(ExamClearBoardCall.class);
	
	protected int type;
	public static int typeOfExamBoard = 1;
	public static int typeOfExamPublishBoard = 2;
	
	public ExamClearBoardCall(ClientSession session, int type) {
		super(session);
		this.type = type;
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		if(type==typeOfExamBoard){
			ExamBoardDao dao = Server.server.getServiceRegistry().getDbService().examBoardDao;
			dao.delete("delete from ExamBoard");
		}else if(type==typeOfExamPublishBoard){
			ExamPublishBoardDao dao = Server.server.getServiceRegistry().getDbService().examPublishBoardDao;
			dao.delete("delete from ExamPublishBoard");
		}
	}

}
