package peony.service.exam;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.net.ClientSession;
import peony.net.Packet;

public class ExamResultCall extends ClientSessionAsyncCall {

	private Logger log = Logger.getLogger(ExamResultCall.class);
	
	protected int serial;
	protected Player player;
	
	public ExamResultCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.player = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if(player!=null){
			ExamService examService = Server.server.getServiceRegistry().getExamService();
			Packet pt = new Packet(OpCode.EXAM_RESULT_SERVER);
			pt.putInt(serial);
			ExamBoard board = examService.getExamBoard(player.id);
			boolean canJoin = examService.getAllocExamType(player.id)!=ExamService.EXAM_TYPE_IDLE && !examService.hasAnsToday(player);
			if(board!=null){
				pt.putInt(board.getPassCount()*ExamService.scorePerExam);
				//星期六日开始没有考试资格的人不再排名
				if(Time.currentWeekDay==7 && board.examType==0 || Time.currentWeekDay==1 && board.examType==0
						|| examService.getAllocExamType(player.id)==ExamService.EXAM_TYPE_IDLE)
					pt.putString("无");
				else
					pt.putString(String.valueOf(examService.calcRank(player.id)));
				pt.put(canJoin?1:0);
			}else{
				pt.putInt(0);
				if(examService.getAllocExamType(player.id)==ExamService.EXAM_TYPE_IDLE)
					pt.putString("无");
				else
					pt.putString("0");
				pt.put(canJoin?1:0);
			}
			if(!canJoin){
				if(examService.hasAnsToday(player))
					pt.putString("对不起，您今天已经参加过一次科举答题，请明日再来");
				else if(examService.currentExamType==ExamService.EXAM_TYPE_MIDDLE)
					pt.putString("很遗憾本周您没有入围会试，请下周再接再厉");
				else if(examService.currentExamType==ExamService.EXAM_TYPE_HIGH)
					pt.putString("很遗憾本周您没有入围殿试，请下周再接再厉");
				else 
					pt.putString("10:00-23:50是答题时间");
			}
			session.send(pt);
		}
	}

	public void run() {
		addToClientSession();
	}

}
