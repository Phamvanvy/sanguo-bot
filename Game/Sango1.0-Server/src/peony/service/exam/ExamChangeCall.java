package peony.service.exam;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class ExamChangeCall extends ClientSessionAsyncCall {

	private Logger log = Logger.getLogger(ExamChangeCall.class);
	
	protected int serial;
	protected Player player;
	
	public ExamChangeCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.player = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		if(player!=null){
			ExamService examService = Server.server.getServiceRegistry().getExamService();
			if(examService.currentExamType==ExamService.EXAM_TYPE_IDLE){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.EXAM_CHANGE_QUESTION_CLIENT, "对不起，您已超时");
				return;
			}
			if(examService.getPowerChangeValue(player.id)>=ExamService.POWER_CHANGE_COUNT){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.EXAM_CHANGE_QUESTION_CLIENT, "对不起，本特权的使用次数已用完");
				return;
			}
			Exam exam = null;
			try{exam = examService.cache.get(player.id).get(ExamService.countOfDay);}catch(Exception e){}
			if(exam!=null){
				Packet packet = new Packet(OpCode.EXAM_CHANGE_QUESTION_SERVER);
				packet.putInt(serial);
				packet.putInt(exam.id);
				packet.putString(exam.title);
				packet.put(exam.answers.size());
				for(Answer a : exam.answers){
					packet.put(a.index);
					packet.putString(a.desc);
				}
				session.send(packet);
				examService.recordPowerChange(player.id);
				log.info("[EXAM-CHANGE]PLAYER["+player.id+"]EXAMID["+exam.id+"]");
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.EXAM_CHANGE_QUESTION_CLIENT, "对不起，答题时间已失效");
				return;
			}
		}
	}

}
