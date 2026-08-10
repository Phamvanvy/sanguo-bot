package peony.service.exam;

import java.text.MessageFormat;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class ExamRedirectPassCall extends ClientSessionAsyncCall {

	private Logger log = Logger.getLogger(ExamRedirectPassCall.class);
	
	protected int serial;
	protected int examId;
	protected Player player;
	
	public ExamRedirectPassCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.examId = packet.getInt();
		this.player = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if(player!=null){
			ExamService examService = Server.server.getServiceRegistry().getExamService();
			if(!examService.isLegalExam(player.id, examId)){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.EXAM_REDICTPASS_CLIENT, "很抱歉，您的答题时间已失效");
				return;
			}
			int examType = examService.getAllocExamType(player.id);
			if(examType==ExamService.EXAM_TYPE_IDLE){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.EXAM_REDICTPASS_CLIENT, "很抱歉，您没有资格参加比赛");
				return;
			}
			Exam exam = examService.getExamById(examType, examId);
			if(exam==null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.EXAM_REDICTPASS_CLIENT, "对不起，您回答的题目不存在");
				return;
			}
			if(examService.getRedirectPassValue(player.id)>=ExamService.POWER_REDICTPASS_COUNT){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.EXAM_REDICTPASS_CLIENT, "对不起，本特权的使用次数已用完");
				return;
			}
			if(examService.getExamBoard(player.id)==null){
				ExamBoard board = new ExamBoard();
				board.setPlayerId(player.id);
				board.freshDayData();
				board.setTodayCount(1);
				board.setPassCount(1);
				examService.addToBoard(board);
			}else{
				ExamBoard board = examService.getExamBoard(player.id);
				board.freshDayData();
				board.setTodayCount(board.getTodayCount()+1);
				board.setPassCount(board.getPassCount()+1);
			}
			Packet packet = new Packet(OpCode.EXAM_REDICTPASS_SERVER);
			packet.putInt(serial);
			ExamBoard board = examService.getExamBoard(player.id);
			if(board!=null){
				packet.put(board.getTodayCount());
				packet.put(board.getTodayCount()*ExamService.scorePerExam);
				packet.putInt(board.getPassCount()*ExamService.scorePerExam);
				packet.putString(String.valueOf(examService.calcRank(player.id)));
				log.info("[EXAM-REDIRECTPASS]PLAYER["+player.id+"]EXAMID["+examId+"]");
			}else{
				packet.put(0);
				packet.put(0);
				packet.putInt(0);
				packet.putString("0");
			}
			int ansCount = examService.calcAnsCount(player.id);
			if(ansCount==ExamService.countOfDay){
				int totalTime = examService.calcTotalAnsTime(player.id);
				if(board!=null){
					board.setTotalTime((totalTime + board.getTotalTime()) / 1000);
				}
				examService.clearCacheData(player.id);
				
				if(board==null || board.getTodayCount()<6){
					if(board==null){
						examService.sendGift(player.id, ExamService.rewardItem1, 1, "科举答题日常奖励", MessageFormat.format("您今天科举答题获得{0}分，奖励{1}颗修炼丹，请再接再厉！", 0,1));
						examService.sendGift(player.id, ExamService.rewardItem1_1, 10, "科举答题日常奖励", MessageFormat.format("您今天科举答题获得{0}分，奖励{1}颗经验卷轴，请再接再厉！", 0,10));
					}else{
						examService.sendGift(player.id, ExamService.rewardItem1, 1, "科举答题日常奖励", MessageFormat.format("您今天科举答题获得{0}分，奖励{1}颗修炼丹，请再接再厉！", board.getTodayCount()*ExamService.scorePerExam,1));
						examService.sendGift(player.id, ExamService.rewardItem1_1, 10, "科举答题日常奖励", MessageFormat.format("您今天科举答题获得{0}分，奖励{1}颗经验卷轴，请再接再厉！", board.getTodayCount()*ExamService.scorePerExam,10));
					}
				}else if(board.getTodayCount()>=6 && board.getTodayCount()<=8){
					examService.sendGift(player.id, ExamService.rewardItem1, 2, "科举答题日常奖励", MessageFormat.format("您今天科举答题获得{0}分，奖励{1}颗修炼丹，请再接再厉！", board.getTodayCount()*ExamService.scorePerExam,2));
					examService.sendGift(player.id, ExamService.rewardItem1_1, 15, "科举答题日常奖励", MessageFormat.format("您今天科举答题获得{0}分，奖励{1}颗经验卷轴，请再接再厉！", board.getTodayCount()*ExamService.scorePerExam,15));
				}else if(board.getTodayCount()>=9){
					examService.sendGift(player.id, ExamService.rewardItem1, 3, "科举答题日常奖励", MessageFormat.format("您今天科举答题获得{0}分，奖励{1}颗修炼丹，请再接再厉！", board.getTodayCount()*ExamService.scorePerExam,3));
					examService.sendGift(player.id, ExamService.rewardItem1_1, 20, "科举答题日常奖励", MessageFormat.format("您今天科举答题获得{0}分，奖励{1}颗经验卷轴，请再接再厉！", board.getTodayCount()*ExamService.scorePerExam,20));
				}
			}
			session.send(packet);
			examService.recordRedirectPass(player.id);
		}
	}

	public void run() {
		addToClientSession();
	}

}
