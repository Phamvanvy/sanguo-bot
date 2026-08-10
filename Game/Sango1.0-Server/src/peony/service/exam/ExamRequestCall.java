package peony.service.exam;

import java.util.List;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.net.ClientSession;
import peony.net.Packet;

public class ExamRequestCall extends ClientSessionAsyncCall {

	private Logger log = Logger.getLogger(ExamRequestCall.class);
	
	public static int strategytCount = 11;
	public static int requestCount = 10;
	
	protected Player player;
	protected int serial;
	
	protected List<Exam> list;
	protected int examType;
	
	public ExamRequestCall(ClientSession session, Packet packet) {
		super(session);
		player = (Player)session.getClient();
		serial = packet.getInt();
	}

	public void callFinish() throws Exception {
		if(player!=null){
			ExamService examService = Server.server.getServiceRegistry().getExamService();
			Packet packet = new Packet(OpCode.EXAM_QUESTION_REQUEST_SERVER);
			packet.putInt(serial);
			packet.put(examType);
			examService.addToCache(player, list);
			packet.put(requestCount);
			for(int i=0;i<requestCount;i++){
				Exam exam = list.get(i);
				packet.putInt(exam.id);
				packet.putString(exam.title);
				packet.put(exam.answers.size());
				for(int j=0;j<exam.answers.size();j++){
					Answer answer = exam.answers.get(j);
					packet.put(answer.index);
					packet.putString(answer.desc);
				}
			}
			player.pool.setInt(ExamService.PROPERTY_EXAM_COUNT, 1);
			player.pool.setInt(ExamService.PROPERTY_EXAM_DAY, Time.day);
			session.send(packet);
		}
	}

	public void run() {
		if(player!=null){
			ExamService examService = Server.server.getServiceRegistry().getExamService();
			if(examService.currentExamType==ExamService.EXAM_TYPE_IDLE){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.EXAM_QUESTION_REQUEST_CLIENT, "对不起，科举答题时间为10:00-23:50");
				return;
			}
			if(examService.hasAnsToday(player)){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.EXAM_QUESTION_REQUEST_CLIENT, "对不起，您今天已经参加过一次科举答题，请明日再来");
				return;
			}
			if(!examService.canRequestExam()){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.EXAM_QUESTION_REQUEST_CLIENT, "对不起，科举答题时间为10:00-23:50");
				return;
			}
			if(player.level<ExamService.EXAM_LEVEL){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.EXAM_QUESTION_REQUEST_CLIENT, "对不起，30级以上玩家才能参与科举答题");
				return;
			}
			examType = examService.getAllocExamType(player.id);
			if(examType==ExamService.EXAM_TYPE_IDLE){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.EXAM_QUESTION_REQUEST_CLIENT, "对不起，您没有资格参加比赛");
				return;
			}
			list = examService.randomQuests(examType, strategytCount);
			addToClientSession();
		}
	}

}
