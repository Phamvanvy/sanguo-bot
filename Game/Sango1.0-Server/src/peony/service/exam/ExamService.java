package peony.service.exam;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.mail.MailService;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.util.TimeUtil;

/**
 * 考试活动服务类
 * @author dchen
 */
public class ExamService implements Service, ServiceEventListener {

	private Logger log = Logger.getLogger(ExamService.class);
	
	public static int EXAM_LEVEL = 30; //参加考试的最低级别
	
	//各种考试的时间（周）
	public static int[] EXAM_BASE_WEEKDAY = {1,2,3,4,5};
	public static int[] EXAM_MIDDLE_WEEKDAY = {6};
	public static int[] EXAM_HIGH_WEEKDAY = {7};
	
	//每天的答题时间段
	public static int EXAM_BEGIN_HOUR = 9;
	public static int EXAM_BEGIN_MIN = 0;
	public static int EXAM_END_HOUR = 23;
	public static int EXAM_END_MIN = 59;
	
	//决出会试资格的时间
	public static int MIDDLE_TIME_WEEKDAY = 5;
	public static int MIDDLE_TIME_HOUR = 23;
	public static int MIDDLE_TIME_MIN = 59;
	
	//决出殿试资格的时间
	public static int HIGH_TIME_WEEKDAY = 6;
	public static int HIGH_TIME_HOUR = 23;
	public static int HIGH_TIME_MIN = 59;
	
	//发榜时间
	public static int PUBLISH_TIME_WEEKDAY = 7;
	public static int PUBLISH_TIME_HOUR = 23;
	public static int PUBLISH_TIME_MIN = 59;
	
	//允许发卷最晚时间
	public static int DAY_END_HOUR = 23;
	public static int DAY_END_MIN = 50;
	
	public static int POWER_QUWEI_COUNT = 1; //特权“去伪存真”的使用次数限制
	public static int POWER_CHANGE_COUNT = 1; //特权“换一题”的使用次数限制
	public static int POWER_REDICTPASS_COUNT = 1; //特权“直接答对”的使用次数限制
	
	public static int EXAM_COUNT = 1; //每天考试次数限制
	public static final String PROPERTY_EXAM_COUNT = "examcount";
	public static final String PROPERTY_EXAM_DAY = "examday";
	
	public static final int EXAM_TYPE_IDLE = 0; // 类型:空闲
	public static final int EXAM_TYPE_BASE = 1; // 类型:乡试
	public static final int EXAM_TYPE_MIDDLE = 2; // 类型:会试
	public static final int EXAM_TYPE_HIGH = 3; // 类型:殿试
	public int currentExamType; // 当前考试类型
	public static int scorePerExam = 10; //每题得分数
	public static int countOfDay = 10; //每日答题数量
	
	private static Random random = new Random();
	
	//题库
	protected ArrayList<Exam> baseExamQuestions = new ArrayList<Exam>();
	protected ArrayList<Exam> middleExamQuestions = new ArrayList<Exam>();
	protected ArrayList<Exam> highExamQuestions = new ArrayList<Exam>();
	
	public Map<Integer, List<Exam>> cache = new HashMap<Integer, List<Exam>>();
	public Map<Integer, Integer> cacheTime = new HashMap<Integer, Integer>();
	public Map<Integer, Integer> cacheAnsCount = new HashMap<Integer, Integer>();
	
	public List<ExamBoard> boards = new ArrayList<ExamBoard>();
	public HashMap<Integer, ExamBoard> id2boards = new HashMap<Integer, ExamBoard>();
	
	public Map<Integer, Integer> powerQuweiRecord = new HashMap<Integer, Integer>();
	public Map<Integer, Integer> powerChangeRecord = new HashMap<Integer, Integer>();
	public Map<Integer, Integer> powerRedirectPassRecord = new HashMap<Integer, Integer>();
	
	public List<ExamPublishBoard> publishBoards = new ArrayList<ExamPublishBoard>();
	
	public Map<Integer, Boolean> notifys = new HashMap<Integer, Boolean>();
	public Map<Integer, Integer> notifyTimes = new HashMap<Integer, Integer>();
	
	public int lastSaveTime;
	public int lastCheckEndTime;
	public int lastProcessNotifyTime;
	
	public static int rewardItem1 = 4930; //修炼丹
	public static int rewardItem1_1 = 1110; //经验卷轴
	public static int rewardItem2 = 4939; //会试资格礼包
	public static int rewardItem3 = 4940; //殿试资格礼包
	
	public static int rewardItem4 = 2785; //金榜题名状元礼包
	public static int rewardItem5 = 2786; //金榜题名榜眼礼包
	public static int rewardItem6 = 2787; //金榜题名探花礼包
	public static int rewardItem7 = 2788; //金榜题名礼包
	
	public void startup() throws Exception {
		//加载各类题库到内存
		loadExamQuestions();
		loadExamBoards();
		
		Server.server.scheduExec.schedule(new Runnable(){
			public void run() {
				Server.server.getWorld().schedule(new ExamBirthBoardCall(ExamBirthBoardCall.typeOfMiddle));
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), MIDDLE_TIME_WEEKDAY, MIDDLE_TIME_HOUR, MIDDLE_TIME_MIN), TimeUnit.MILLISECONDS);
		
		Server.server.scheduExec.schedule(new Runnable(){
			public void run() {
				Server.server.getWorld().schedule(new ExamBirthBoardCall(ExamBirthBoardCall.typeOfHigh));
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), HIGH_TIME_WEEKDAY, HIGH_TIME_HOUR, HIGH_TIME_MIN), TimeUnit.MILLISECONDS);
		
		Server.server.scheduExec.schedule(new Runnable(){
			public void run() {
				Server.server.getWorld().schedule(new ExamBirthBoardCall(ExamBirthBoardCall.typeOfPublish));
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), PUBLISH_TIME_WEEKDAY, PUBLISH_TIME_HOUR, PUBLISH_TIME_MIN), TimeUnit.MILLISECONDS);
		
		Server.server.getEventManager().registerListener(this);
	}
	
	public void loadExamQuestions() throws Exception{
		baseExamQuestions.clear();
		middleExamQuestions.clear();
		highExamQuestions.clear();
		
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("exam/exambase.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc, 0, EXAM_TYPE_BASE);
		
		bytes = Server.server.getServiceRegistry().getDataService().data.findFile("exam/exammiddle.xml");
		doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc, 0, EXAM_TYPE_MIDDLE);
		
		bytes = Server.server.getServiceRegistry().getDataService().data.findFile("exam/examhigh.xml");
		doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc, 0, EXAM_TYPE_HIGH);
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc, int volume, int examType) {
		Element root = doc.getRootElement();
		List list = root.elements("exam");
		if(list.size()==0)
			throw new IllegalArgumentException();
		for(int i=0;i<list.size();i++){
			Element examEl = (Element) list.get(i);
			int examId = Integer.parseInt(examEl.attributeValue("id"));
			String title = examEl.attributeValue("title");
			int answerIndex = Integer.parseInt(examEl.attributeValue("answer"));
			Exam exam = new Exam();
			exam.id = examId;
			exam.title = title;
			exam.answerIndex = answerIndex;
			
			List answers = examEl.elements("answer");
			for(int j=0;j<answers.size();j++){
				Element answerEl = (Element) answers.get(j);
				int answerId = Integer.parseInt(answerEl.attributeValue("index"));
				String desc = answerEl.attributeValue("desc");
				
				Answer ans = new Answer(answerId, desc);
				exam.answers.add(ans);
			}
			
			if(examType==EXAM_TYPE_BASE){
				baseExamQuestions.add(exam);
			}else if(examType==EXAM_TYPE_MIDDLE){
				middleExamQuestions.add(exam);
			}else if(examType==EXAM_TYPE_HIGH){
				highExamQuestions.add(exam);
			}
		}
	}
	
	public void loadExamBoards(){
		boards = Server.server.getServiceRegistry().getDbService().examBoardDao.getAllBoards();
		for(ExamBoard b : boards){
			id2boards.put(b.playerId, b);
		}
		publishBoards = Server.server.getServiceRegistry().getDbService().examPublishBoardDao.getAllBoards();
	}
	
	public void clearPowerControl(){
		powerChangeRecord.clear();
		powerQuweiRecord.clear();
		powerRedirectPassRecord.clear();
	}
	
	public void initPowerControl(int playerId){
		powerChangeRecord.remove(playerId);
		powerQuweiRecord.remove(playerId);
		powerRedirectPassRecord.remove(playerId);
	}
	
	public void recordPowerChange(int playerId){
		Integer value = powerChangeRecord.get(playerId);
		if(value!=null){
			powerChangeRecord.put(playerId, value.intValue()+1);
		}else{
			powerChangeRecord.put(playerId, 1);
		}
	}
	
	public void recordPowerQuwei(int playerId){
		Integer value = powerQuweiRecord.get(playerId);
		if(value!=null){
			powerQuweiRecord.put(playerId, value.intValue()+1);
		}else{
			powerQuweiRecord.put(playerId, 1);
		}
	}
	
	public void recordRedirectPass(int playerId){
		Integer value = powerRedirectPassRecord.get(playerId);
		if(value!=null){
			powerRedirectPassRecord.put(playerId, value.intValue()+1);
		}else{
			powerRedirectPassRecord.put(playerId, 1);
		}
	}
	
	public int getPowerChangeValue(int playerId){
		Integer value = powerChangeRecord.get(playerId);
		if(value!=null){
			return value.intValue();
		}else{
			return 0;
		}
	}
	
	public int getPowerQuweiValue(int playerId){
		Integer value = powerQuweiRecord.get(playerId);
		if(value!=null){
			return value.intValue();
		}else{
			return 0;
		}
	}
	
	public int getRedirectPassValue(int playerId){
		Integer value = powerRedirectPassRecord.get(playerId);
		if(value!=null){
			return value.intValue();
		}else{
			return 0;
		}
	}
	
	public void update(int diff){
		checkExamTime(diff);
		checkEnd();
		loopSaveExamBoard();
		processNotify();
	}
	
	protected void checkEnd(){
		if(Time.currTime-lastCheckEndTime<10000)
			return;
		lastCheckEndTime = Time.currTime;
		List<Integer> endPlayers = new ArrayList<Integer>();
		for(int playerId : cacheTime.keySet()){
			Integer time = cacheTime.get(playerId);
			if(time!=null){
				if(Time.currTime-time.intValue()>230000){
					endPlayers.add(playerId);
				}
			}
		}
		for(int p : endPlayers){
			ExamBoard board = id2boards.get(p);
			if(board!=null){
				board.setTotalTime(board.getTotalTime()+2000);
			}
			clearCacheData(p);
			
			if(board==null || board.getTodayCount()<6){
				if(board==null){
					sendGift(p, rewardItem1, 1, "科举答题日常奖励", MessageFormat.format("您今天科举答题获得{0}分，奖励{1}颗修炼丹，请再接再厉！", 0,1));
					sendGift(p, rewardItem1_1, 10, "科举答题日常奖励", MessageFormat.format("您今天科举答题获得{0}分，奖励{1}颗经验卷轴，请再接再厉！", 0,10));
				}else{
					sendGift(p, rewardItem1, 1, "科举答题日常奖励", MessageFormat.format("您今天科举答题获得{0}分，奖励{1}颗修炼丹，请再接再厉！", board.getTodayCount()*scorePerExam,1));
					sendGift(p, rewardItem1_1, 10, "科举答题日常奖励", MessageFormat.format("您今天科举答题获得{0}分，奖励{1}颗经验卷轴，请再接再厉！", board.getTodayCount()*scorePerExam,10));
				}
			}else if(board.getTodayCount()>=6 && board.getTodayCount()<=8){
				sendGift(p, rewardItem1, 2, "科举答题日常奖励", MessageFormat.format("您今天科举答题获得{0}分，奖励{1}颗修炼丹，请再接再厉！", board.getTodayCount()*scorePerExam,2));
				sendGift(p, rewardItem1_1, 15, "科举答题日常奖励", MessageFormat.format("您今天科举答题获得{0}分，奖励{1}颗经验卷轴，请再接再厉！", board.getTodayCount()*scorePerExam,15));
			}else if(board.getTodayCount()>=9){
				sendGift(p, rewardItem1, 3, "科举答题日常奖励", MessageFormat.format("您今天科举答题获得{0}分，奖励{1}颗修炼丹，请再接再厉！", board.getTodayCount()*scorePerExam,3));
				sendGift(p, rewardItem1_1, 20, "科举答题日常奖励", MessageFormat.format("您今天科举答题获得{0}分，奖励{1}颗经验卷轴，请再接再厉！", board.getTodayCount()*scorePerExam,20));
			}
		}
	}
	
	private void checkExamTime(int diff){
		int systemWeek = Time.currentWeekDay;
		int chinaWeekDay = --systemWeek;
		chinaWeekDay = chinaWeekDay==0 ? 7 : chinaWeekDay;
		int systemHour = Time.currentHour;
		int systemMin = Time.currentMin;
		
		if(isInTime(EXAM_BEGIN_HOUR, EXAM_BEGIN_MIN, EXAM_END_HOUR, EXAM_END_MIN, systemHour, systemMin)){
			if(isInWeek(chinaWeekDay, EXAM_BASE_WEEKDAY)){
				if(currentExamType != EXAM_TYPE_BASE){
					currentExamType = EXAM_TYPE_BASE;
					clearPowerControl();
					clearAllCacheData();
				}
			}else if(isInWeek(chinaWeekDay, EXAM_MIDDLE_WEEKDAY)){
				if(currentExamType != EXAM_TYPE_MIDDLE){
					currentExamType = EXAM_TYPE_MIDDLE;
					clearPowerControl();
					clearAllCacheData();
				}
			}else if(isInWeek(chinaWeekDay, EXAM_HIGH_WEEKDAY)){
				if(currentExamType != EXAM_TYPE_HIGH){
					currentExamType = EXAM_TYPE_HIGH;
					clearPowerControl();
					clearAllCacheData();
				}
			}else{
				if(currentExamType != EXAM_TYPE_IDLE){
					currentExamType = EXAM_TYPE_IDLE;
					clearPowerControl();
					clearAllCacheData();
				}
			}
		}else{
			if(currentExamType != EXAM_TYPE_IDLE){
				currentExamType = EXAM_TYPE_IDLE;
				clearPowerControl();
				clearAllCacheData();
			}
		}
	}
	
	protected void notifyAllPlayers(){
		for(Player player : ObjectAccessor.players.values()){
			if(player!=null && player.level>=EXAM_LEVEL){
				notifyExam(player);
			}
		}
	}
	
	protected void notifyExam(Player player){
		if(hasAnsToday(player))
			return;
		if(!canRequestExam())
			return;
		if(!player.acceptMoving)
			return;
		if(player.getVMap()!=null && player.getVMap().instance!=null)
			return;
		Packet pt = new Packet(OpCode.OPENUI_SERVER);
		pt.putString("ui_npc_dialog");
		ExamBoard board = getExamBoard(player.id);
		if(board==null)
			pt.putString("EXAMINATION|0|0");
		else
			pt.putString("EXAMINATION|"+board.getPassCount()*scorePerExam+"|"+calcRank(player.id));
		player.send(pt);
		notifys.put(player.id, true);
	}
	
	public void addToCache(Player player, List<Exam> list){
		if(player!=null){
			cache.put(player.id, list);
			cacheTime.put(player.id, Time.currTime);
			cacheAnsCount.remove(player.id);
		}
	}
	
	public void clearCacheData(int playerId){
		cacheTime.remove(playerId);
		cacheAnsCount.remove(playerId);
		cache.remove(playerId);
	}
	
	public void clearAllCacheData(){
		cacheTime.clear();
		cacheAnsCount.clear();
		cache.clear();
		notifys.clear();
	}
	
	public int calcTotalAnsTime(int playerId){
		Integer lastTime = cacheTime.get(playerId);
		int value = 2000;
		if(lastTime!=null){
			value = (Time.currTime-lastTime.intValue()) / 100;
		}
		return value;
	}
	
	public int calcAnsCount(int playerId){
		Integer count = cacheAnsCount.get(playerId);
		if(count==null){
			cacheAnsCount.put(playerId, 1);
			return 1;
		}else{
			int c = count.intValue() + 1;
			cacheAnsCount.put(playerId, c);
			return c;
		}
	}
	
	public boolean isLegalExam(int playerId, int examId){
		if(cache.get(playerId)==null)
			return false;
		for(Exam e : cache.get(playerId)){
			if(e.id==examId)
				return true;
		}
		return false;
	}
	
	public List<Exam> randomQuests(int examType, int count){
		List<Exam> list = getCurrentExams(examType);
		if(list.size()<=count)
			return list;
		
		List<Exam> result = new ArrayList<Exam>();
		int size = list.size();
		int num = size / count;
		int leave = size % count;
		for(int i=0;i<count;i++){
			int r = 0;
			if(i==count-1)
				r = random.nextInt(num+leave);
			else
				r = random.nextInt(num);
			int index = num * i + r;
			result.add(list.get(index));
		}
		
		return result;
	}
	
	private boolean isInWeek(int chinaWeekDay, int[] arr){
		for(int day : arr){
			if(day==chinaWeekDay)
				return true;
		}
		return false;
	}
	
	private boolean isInTime(int beginHour, int beginMin, int endHour, int endMin, int systemHour, int systemMin){
		if(systemHour==beginHour && systemMin>=beginMin
				|| systemHour==endHour && systemMin<=endMin
				|| systemHour>beginHour && systemHour<endHour)
			return true;
		return false;
	}
	
	public boolean canRequestExam(){
		if(currentExamType!=EXAM_TYPE_IDLE
				&& (Time.currentHour<DAY_END_HOUR || Time.currentHour==DAY_END_HOUR && Time.currentMin<=DAY_END_MIN))
			return true;
		return false;
	}
	
	public void addToBoard(ExamBoard board){
		boards.add(board);
		id2boards.put(board.playerId, board);
	}
	
	public ExamBoard getExamBoard(int playerId){
		return id2boards.get(playerId);
	}
	
	public boolean hasAnsToday(Player player){
		return player.pool.getInt(ExamService.PROPERTY_EXAM_COUNT, 0)>0 && player.pool.getInt(ExamService.PROPERTY_EXAM_DAY, 0)==Time.day;
	}
	
	public int calcRank(int playerId){
		ExamBoard owner = id2boards.get(playerId);
		int passCount = owner.passCount;
		int totalTime = owner.totalTime;
		if(cacheTime.get(playerId)!=null)
			totalTime += 2000;
		int rank = 1;
		for(ExamBoard b : boards){
			if(b.playerId!=playerId && (b.passCount>passCount || b.passCount==passCount
					&& b.totalTime<totalTime)){
				rank++;
			}
		}
		return rank;
	}
	
	public void birthMiddleExamPlayer(){
		List<ExamBoard> list = getTop(100, EXAM_TYPE_BASE);
		clearExamFlag();
		for(ExamBoard eb : list){
			eb.examType = EXAM_TYPE_MIDDLE;
			sendGift(eb.playerId, rewardItem2, 1, "会试资格奖励", "恭喜您科举乡试的成绩良好，获得会试资格，特颁发礼包以资鼓励。请您于周六10:00-23:50参加会试。");
			log.info("[EXAM-BIRTHMIDDLE]PLAYER["+eb.playerId+"]PASS["+eb.getPassCount()+"]TIME["+eb.getTotalTime()+"]");
		}
		for(ExamBoard eb : boards){
			eb.resetData();
		}
	}
	
	public void birthHighExamPlayer(){
		List<ExamBoard> list = getTop(50, EXAM_TYPE_MIDDLE);
		clearExamFlag();
		for(ExamBoard eb : list){
			eb.examType = EXAM_TYPE_HIGH;
			sendGift(eb.playerId, rewardItem3, 1, "殿试资格奖励", "恭喜您科举会试的成绩优秀，获得殿试资格，特颁发礼包以资鼓励。请您于周日10:00-23:50参加殿试。");
			log.info("[EXAM-BIRTHHIGH]PLAYER["+eb.playerId+"]PASS["+eb.getPassCount()+"]TIME["+eb.getTotalTime()+"]");
		}
		for(ExamBoard eb : boards){
			eb.resetData();
		}
	}
	
	public void birthPublishBoards(){
		publishBoards.clear();
		List<ExamBoard> list = getTop(10, EXAM_TYPE_HIGH);
		for(int i=0;i<list.size();i++){
			ExamBoard b = list.get(i);
			ExamPublishBoard pb = new ExamPublishBoard();
			pb.setPassCount(b.getPassCount());
			pb.setPlayerId(b.playerId);
			pb.setRanking(i+1);
			pb.setTotalTime(b.getTotalTime());
			publishBoards.add(pb);
			if(i+1==1){
				sendGift(pb.playerId, rewardItem4, 1, "金榜题名状元奖励", "金榜题名时——恭喜您殿试成绩第一名，御赐金科状元，御赐“金榜题名状元礼包”！");
			}else if(i+1==2){
				sendGift(pb.playerId, rewardItem5, 1, "金榜题名榜眼奖励", "金榜题名时——恭喜您殿试成绩第二名，御赐金科榜眼，御赐“金榜题名榜眼礼包”！");
			}else if(i+1==3){
				sendGift(pb.playerId, rewardItem6, 1, "金榜题名探花奖励", "金榜题名时——恭喜您殿试成绩第三名，御赐金科探花，御赐“金榜题名探花礼包”！");
			}else{
				sendGift(pb.playerId, rewardItem7, 1, "金榜题名进士奖励", "金榜题名时——恭喜您殿试成绩优秀，御赐二甲进士出身，御赐“金榜题名礼包”！");
			}
			log.info("[EXAM-BIRTHPUBLISH]PLAYER["+pb.playerId+"]PASS["+pb.getPassCount()+"]TIME["+pb.getTotalTime()+"]RANKING["+pb.getRanking()+"]");
		}
		
		//数据持久化
		savePublishBoards();
		Server.server.getServiceRegistry().getDbService().schedule(new ExamClearBoardCall(null, ExamClearBoardCall.typeOfExamBoard));
		boards.clear();
		id2boards.clear();
		clearAllCacheData();
	}
	
	public void loopSaveExamBoard(){
		if(Time.currTime-lastSaveTime>3600000){
			lastSaveTime = Time.currTime;
			saveExamBoards();
		}
	}
	
	public void saveExamBoards(){
		List<ExamBoard> copy = new ArrayList<ExamBoard>();
		for(ExamBoard b : boards){
			copy.add(b);
		}
		Server.server.getServiceRegistry().getDbService().schedule(new ExamSaveExamBoardCall(null, boards));
	}
	
	public void savePublishBoards(){
		List<ExamPublishBoard> copy = new ArrayList<ExamPublishBoard>();
		for(ExamPublishBoard epb : publishBoards){
			copy.add(epb);
		}
		Server.server.getServiceRegistry().getDbService().schedule(new ExamSavePublishBoardCall(null, copy));
	}
	
	public void clearExamFlag(){
		for(ExamBoard b : boards){
			b.examType = 0;
		}
	}
	
	public List<ExamBoard> getTop(int top, int examType){
		List<ExamBoard> list = new ArrayList<ExamBoard>();
		List<ExamBoard> temp = new ArrayList<ExamBoard>();
		if(examType==EXAM_TYPE_BASE){
			temp = boards;
		}else if(examType==EXAM_TYPE_MIDDLE){
			for(ExamBoard b : boards){
				if(b.examType==EXAM_TYPE_MIDDLE)
					temp.add(b);
			}
		}else if(examType==EXAM_TYPE_HIGH){
			for(ExamBoard b : boards){
				if(b.examType==EXAM_TYPE_HIGH)
					temp.add(b);
			}
		}
		Collections.sort(temp, new Comparator<ExamBoard>(){
			public int compare(ExamBoard o1, ExamBoard o2) {
				if(o1.getPassCount()>o2.getPassCount() || o1.getPassCount()==o2.getPassCount() && o1.getTotalTime()<o2.getTotalTime()){
					return -1;
				}else if(o1.getPassCount()==o2.getPassCount() && o1.getTotalTime()==o2.getTotalTime()){
					return 0;
				}else{
					return 1;
				}
			}
		});
		int count = Math.min(top, temp.size());
		for(int i=0;i<count;i++){
			list.add(temp.get(i));
		}
		return list;
	}
	
	public Exam getExamById(int examType, int examId){
		List<Exam> list = getCurrentExams(examType);
		for(Exam e : list){
			if(e!=null && e.id==examId)
				return e;
		}
		return null;
	}
	
	private List<Exam> getCurrentExams(int examType){
		List<Exam> list = null;
		if(examType==EXAM_TYPE_BASE)
			list = baseExamQuestions;
		else if(examType==EXAM_TYPE_MIDDLE)
			list = middleExamQuestions;
		else if (examType==EXAM_TYPE_HIGH)
			list = highExamQuestions;
		return list;
	}
	
	public int getAllocExamType(int playerId){
		int flagType = -1;
		ExamBoard b = getExamBoard(playerId);
		if(b!=null){
			flagType = b.examType;
		}
		if(currentExamType==EXAM_TYPE_BASE)
			return EXAM_TYPE_BASE;
		if(currentExamType==EXAM_TYPE_MIDDLE){
			if(flagType==EXAM_TYPE_MIDDLE)
				return EXAM_TYPE_MIDDLE;
		}
		if(currentExamType==EXAM_TYPE_HIGH){
			if(flagType==EXAM_TYPE_HIGH)
				return EXAM_TYPE_HIGH;
		}
		return EXAM_TYPE_IDLE;
	}

	public void shutdown() {
		saveExamBoards();
		Server.server.getEventManager().unregisterListener(this);
	}
	
	public void sendGift(int playerId, int itemId, int count, String title, String content){
		MailService mailService = Server.server.getServiceRegistry().getMailService();
		GameItem gameItem = ObjectAccessor.createGameItem(itemId);
		mailService.sendSystemMail(playerId, "系统", title, content, 0, gameItem, count, "EXAM");
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_PLAYER_FIRSTLOAD
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
			processLoad((Player)event.param1);
			break;
		}
	}
	
	protected void processLoad(Player player){
		if(player!=null){
			int playerId = player.id;
			if(notifys.get(playerId)==null && player.level>=EXAM_LEVEL 
					&& !hasAnsToday(player)
					&& getAllocExamType(player.id)!=EXAM_TYPE_IDLE && canRequestExam()){
				notifyTimes.put(playerId, Time.currTime);
			}
		}
	}
	
	public void processNotify(){
		if(Time.currTime-lastProcessNotifyTime<10000)
			return;
		lastProcessNotifyTime = Time.currTime;
		List<Integer> players = new ArrayList<Integer>();
		for(int playerId : notifyTimes.keySet()){
			Integer value = notifyTimes.get(playerId);
			if(value!=null && Time.currTime-value.intValue()>60000){
				players.add(playerId);
			}
		}
		for(int playerId : players){
			Player player = ObjectAccessor.getPlayer(playerId);
			if(player!=null){
				notifyExam(player);
			}
			notifyTimes.remove(playerId);
		}
	}
	
}
