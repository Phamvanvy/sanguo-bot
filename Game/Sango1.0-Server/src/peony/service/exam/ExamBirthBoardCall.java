package peony.service.exam;

import org.apache.log4j.Logger;

import peony.common.AsyncCall;
import peony.game.Server;

public class ExamBirthBoardCall implements AsyncCall {

	private Logger log = Logger.getLogger(ExamBirthBoardCall.class);
	
	protected int type;
	
	public static int typeOfMiddle = 1;
	public static int typeOfHigh = 2;
	public static int typeOfPublish = 3;
	
	public ExamBirthBoardCall(int type) {
		this.type = type;
	}

	public void callFinish() throws Exception {
		ExamService examService = Server.server.getServiceRegistry().getExamService();
		if(type==typeOfPublish){
			examService.birthPublishBoards();
		}else if(type==typeOfMiddle){
			examService.birthMiddleExamPlayer();
		}else if(type==typeOfHigh){
			examService.birthHighExamPlayer();
		}
	}

	public void run() {
		
	}

}
