package peony.service.exam;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class ExamQuweiCall extends ClientSessionAsyncCall {

	private Logger log = Logger.getLogger(ExamQuweiCall.class);
	
	protected int serial;
	protected int examId;
	protected Player player;
	
	public ExamQuweiCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.examId = packet.getInt();
		this.player = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if(player!=null){
			ExamService examService = Server.server.getServiceRegistry().getExamService();
			if(!examService.isLegalExam(player.id, examId)){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.EXAM_REMOVE_CLIENT, "很抱歉，您的答题时间已失效");
				return;
			}
			if(examService.currentExamType==ExamService.EXAM_TYPE_IDLE){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.EXAM_REMOVE_CLIENT, "对不起，现在不是考试时间");
				return;
			}
			int examType = examService.getAllocExamType(player.id);
			if(examType==ExamService.EXAM_TYPE_IDLE){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.EXAM_REMOVE_CLIENT, "对不起，您没有资格参加比赛");
				return;
			}
			Exam exam = examService.getExamById(examType, examId);
			if(exam!=null){
				if(examService.getPowerQuweiValue(player.id)>=ExamService.POWER_QUWEI_COUNT){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.EXAM_REMOVE_CLIENT, "对不起，本特权的使用次数已用完");
					return;
				}
				Packet packet = new Packet(OpCode.EXAM_REMOVE_SERVER);
				packet.putInt(serial);
				int answerIndex = exam.answerIndex;
				int id1 = -1;
				int id2 = -1;
				for(Answer a : exam.answers){
					if(a.index!=answerIndex){
						if(id1<0){
							id1 = a.index;
						}else if(id2<0){
							id2 = a.index;
						}else if(id1>-1 && id2>-1){
							break;
						}
					}
				}
				packet.put(id1);
				packet.put(id2);
				session.send(packet);
				examService.recordPowerQuwei(player.id);
				log.info("[EXAM-QUWEI]PLAYER["+player.id+"]EXAMID["+examId+"]");
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.EXAM_REMOVE_CLIENT, "对不起，您的答题时间已失效");
				return;
			}
		}
	}

	public void run() {
		addToClientSession();
	}

}
