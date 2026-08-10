package peony.marriage;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import com.pip.sanguo.data.map.GameMapNPC;
import ch.javasoft.util.intcoll.IntHashMap;
import peony.game.Actor;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.GatherEndCall;
import peony.game.GatherUnit;
import peony.game.Instance;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapUtil;
import peony.game.chat.ChatService;
import peony.game.mail.MailService;
import peony.net.Packet;

public class WeddingInstance implements Instance {
	
	protected final Logger log = Logger.getLogger(WeddingInstance.class);

	protected WeddingService manager = Server.server.getServiceRegistry().getWeddingService();
	
	protected VMap map;
	
	protected static AtomicInteger IDS = new AtomicInteger(1);
	
	protected int id;
	
	public Actor man;
	
	public Actor woman;
	
	protected Date startTime;
	
	protected Date endTime;
	
	protected int level;
	
	protected int guestLevel;
	
	protected int fetchCount = 0;
	
	protected int[] MAXCOUNT = {0,42,72,90};
	
	protected int count = 0;
	
	protected List<Player> players = new ArrayList<Player>();
	
	protected List<Actor> signIns = new ArrayList<Actor>();
	
	public List<Integer> getgift = new ArrayList<Integer>();
	
	public List<Integer> kicked = new ArrayList<Integer>();
	
	public List<Integer> deskgift = new ArrayList<Integer>();
	
	public Map<Integer,Long> playerentertime = new HashMap<Integer,Long>();
	
	
	public static int DATI = 0;//答题阶段
	
	public static int HONGBAO = 1;//发红包
	
	public static int BEGIN = 2;//正式开始
	
	public static int END = 3;//结束
	
	public int stat = END;
	
	public static long WEDDINGUDRATION = 2 * 60 * 60 * 1000; 
	
	/**********************二期改造后新加**************************************************************/
	
	public static int MAX_PEOPLE_NUM = 40;
	
	public  int peopleNum = 0;//当前人数
	
	public int jewelNum = 0;//捐赠的宝石数
	
	public List<Integer> jewels = new ArrayList<Integer>();//捐赠的宝石
	
	Timer questionTimer = new Timer();
	
	Timer hongBaoTimer = new Timer();
	
	public List<AnswerQue> answerQues = new ArrayList<AnswerQue>();//答题集合
	
	public IntHashMap<Integer> answerNum = new IntHashMap<Integer>();//答题次数记录
	
	public static long TEN_MINUTE = 10 * 60 * 1000;
	
	public SendHongBao sendHongBao = null;
	
	public HongBaoGatherEndCall hongBaoGatherEndCall = null;
	
	public int[] levelMoney = new int[]{0,6000,12000,18000};//单个红包价格
	
	public int alreadyHongbaoNum = 0;//已领取的红包个数
	
	public int refreshHongBaoNum = 0;//已刷新红包个数
	
	public static int TOTLE_HONGBAO_NUM = 200;
	
	public int banLangId = 0;
	
	public int banNiangId = 0;
	
	protected IntHashMap<Player> players2 = new IntHashMap<Player>();//除新娘新郎伴郎伴娘后的人
	
	public WeddingInstance(VMap map,Player man, Player woman, Date startTime ,int jewelNum,List<Integer> jewels){
		this.map = map;
		this.man = Server.server.getServiceRegistry().getActorCacheService().find(man.id);
		this.woman = Server.server.getServiceRegistry().getActorCacheService().find(woman.id);
		Player p1 = ObjectAccessor.getPlayer(man.id);
		Player p2 = ObjectAccessor.getPlayer(woman.id);
		if(p1!=null&&p1.pool.getInt(MarriageService.PROPERTY_WEDDING_BAN)!=0)
			banLangId = p1.pool.getInt(MarriageService.PROPERTY_WEDDING_BAN);
		if(p2!=null&&p2.pool.getInt(MarriageService.PROPERTY_WEDDING_BAN)!=0)
			banNiangId = p2.pool.getInt(MarriageService.PROPERTY_WEDDING_BAN);
		this.id = IDS.incrementAndGet();
		this.startTime = startTime;
		this.endTime = new Date(startTime.getTime()+WEDDINGUDRATION);
		this.jewelNum = jewelNum;
		this.jewels = jewels;
		this.stat = DATI;
		log.info("WEDDINGOPEN[id]" + id + "[MAN]" + man.id + "[WOMAN]" + woman.id + "[JWELNUM]" + jewelNum);
		questionTimer.schedule(new TimerTask(){
			//答题结束  开始送红包
			public void run(){
				stat = HONGBAO;
				if(answerQues.size()>0){
					Iterator<AnswerQue> it = answerQues.iterator();
					while(it.hasNext()){
						AnswerQue aq = it.next();
						aq.questionOver(false);
						log.info("WEDDINGDATIEND[id]" + id);
					}
					answerQues.clear();
				}
				beginSendHongBao();
			}
		}, TEN_MINUTE);
		hongBaoTimer.schedule(new TimerTask(){
			//送红包结束 进入正常流程
			public void run(){
				stat = BEGIN;
				if(sendHongBao != null && sendHongBao.refreshTimer != null){
					sendHongBao.refreshTimer.cancel();
				}
				//将剩余的钱和宝石补发给新郎
				giveBackMoneyAndJewel();
				log.info("WEDDINGHONGBAOEND[id]" + id);
			}
		}, TEN_MINUTE * 2 + 1000 * 30);
	}
	
	/**将剩余的钱和宝石补发给新郎**/
	public void giveBackMoneyAndJewel(){
		//清除红包
		Iterator<GatherUnit> gatherIterator = sendHongBao.currentGathers2.iterator();
		while(gatherIterator.hasNext()){
			gatherIterator.next().removeFromWorld();
			gatherIterator.remove();
		}
		//归还新郎剩余的钱
		int money = 1200000 *  level - alreadyHongbaoNum * levelMoney[level];
		Player p = ObjectAccessor.getPlayer(man.id);
		if(p!=null){
			if(money > 0){
				PlayerTransaction tx = p.newTransaction("GIVEBACKHONGBAO");
				p.addMoney(money, tx, true);
				tx.commit();
			}
			if(jewels.size()>0){
				Iterator<Integer> iterator = jewels.iterator();
				while(iterator.hasNext()){
					int itemId = (Integer)iterator.next();
					iterator.remove();
					PlayerTransaction tx2 = p.newTransaction("GIVEBACKHONGBAO");
					GameItem item = ObjectAccessor.createGameItem(itemId);
					if(item != null){
						try {
							p.bag.addGameItemComplete(item, 1, tx2, true);
							tx2.commit();
						} catch (NoEnoughSpaceException e) {
							tx2.rollback();
							MailService service = Server.server.getServiceRegistry().getMailService();
							service.sendSystemMail(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_01852, "", 0, item, 1, "WEDDINGHONGBAO");
						}
					}
				}
			}
		}
	}
	
	/**开始发红包**/
	public void beginSendHongBao(){
		new SendHongBao(this);
	}
	
	public void addPlayer(Player player) throws VMapException {
		for(WeddingInstance instance : manager.instances){
			Iterator<Player> it = instance.players.iterator();
			while(it.hasNext()){
				Player p = it.next();
				if(p.id==player.id){
					it.remove();
				}
			}
		}
		players.add(player);
		if(player.id != man.id && player.id != woman.id && player.id != banLangId && player.id != banNiangId
				&& !players2.containsKey(player.id)){
			players2.put(player.id,player);
		}
	}

	public int getId() {
		return id;
	}

	public VMap getMap(int mapId) {
		if(map.getId()==mapId){
			return map;
		}
		return null;
	}

	public String getName() {
		return map.mapDef.mapInfo.name;
	}

	public void loadingFinished(Player player) {
		
	}

	public void removePlayer(Player player) {
		Iterator<Player> it = players.iterator();
		while(it.hasNext()){
			Player p = it.next();
			if(p.id==player.id){
				it.remove();
			}
		}
		
	    Iterator<Integer> it2 = players2.keySet().iterator();
		while(it.hasNext()){
			int pId = it2.next();
			if(pId == player.id){
				it2.remove();
			}
		}
		
	}

	public void update(int diff) {
		if(endTime.getTime() - System.currentTimeMillis() <= 5*60*1000 && count == 0){
			count = 1;
			ChatService service= Server.server.getServiceRegistry().getChatService();
			String msg = MessageFormat.format(peony.Messages.STRING_01853,man.name,woman.name);
			service.sendAreaSystemMessage(msg, Integer.parseInt(map.getId()+""+getId()));
		}
		Date date = new Date();
		if(date.after(endTime) && stat==BEGIN){
			stat = END;
			transPlayers();
		}
		if(map!=null){
			map.update(diff);
		}
		if(answerQues.size() > 0){
			Iterator<AnswerQue> it = answerQues.iterator();
			if(it.hasNext()){
				AnswerQue aq = it.next();
				aq.update();
			}
		}
	}
	
	protected void transPlayers(){
		Iterator<GameObject> itor = map.instanceid2objects.iterator2();
		while (itor.hasNext()) {
			GameObject o = itor.next();
			if(o.type==GameObject.TYPE_PLAYER){
				Player p = (Player)o;
				Position po = manager.outInfo[p.faction];
				try {
					p.goMap(po.mapId, po.x, po.y);
				} catch (VMapException e) {
					
				}
			}
		}
	}
	
	/**增加一个答题对象**/
	public void addAnswerQuestion(Player p1,Player p2){
		this.answerQues.add(new AnswerQue(p1,p2,this));
	}
	
	/**检测是否有人离开了礼堂**/
	public boolean isLevelWedding(Player apply,Player beApply){
		if(apply.map.id != manager.inInfo[apply.faction].mapId || beApply.map.id !=  manager.inInfo[beApply.faction].mapId){
			return true;
		}
		return false;
	}
}

class SendHongBao{
	
	public WeddingInstance instance;//隶属副本
	
	public Timer refreshTimer = new Timer();//刷新计时器
	
	public static long REFRESH_TIME = 30 * 1000;//刷新时间
	
	public List<GameMapNPC> gathers = new ArrayList<GameMapNPC>();
	
	public Map<GatherUnit,GameMapNPC> currentGathers = new HashMap<GatherUnit,GameMapNPC>();//当前屏幕中的宝箱
	
	public List<GatherUnit> currentGathers2 = new ArrayList<GatherUnit>();//屏幕中宝箱集合
	
	public Map<GatherUnit,Integer> isContainJewel = new HashMap<GatherUnit,Integer>();//与宝箱对应     value：0含宝石    1：不含宝石
	
	public static Random random = new Random();
	
	public int refreshJewelNum = 0;//每分钟刷新的宝石数
	
	public int lastJewelNum = 0;//最后一分钟次刷新的宝石数
	
	public int refreshNum = 0;//目前的刷新次数
	
	public SendHongBao(WeddingInstance instance){
		this.instance = instance;
		instance.hongBaoGatherEndCall = new HongBaoGatherEndCall(instance);
		instance.sendHongBao = this;
		refreshJewelNum = instance.jewelNum / 10;
		lastJewelNum = instance.jewelNum % 10;		
		getGather();
		refreshTimer.schedule(new TimerTask(){
			public void run(){
					refresh();
			}
		}, 0, REFRESH_TIME);
	}
	
	/**获得地图中的宝箱集合**/
	public void getGather(){
		for (Object gobj : instance.map.mapDef.mapInfo.objects) {
			if (gobj instanceof GameMapNPC && ((GameMapNPC)gobj).template.id == 1635) {
				 gathers.add((GameMapNPC)gobj);
			}
		}
	}
	
	/**一次刷新**/
	@SuppressWarnings("unchecked")
	public void refresh(){
		if(instance.refreshHongBaoNum <= WeddingInstance.TOTLE_HONGBAO_NUM){
			int size = gathers.size();
			for(int i = 0;i < 10;i++){
				int index = random.nextInt(size);
				if(!currentGathers.containsValue(gathers.get(index))){
					GatherUnit gu = (GatherUnit)VMapUtil.addCreature(instance.map,gathers.get(index), true, -1, Server.server.revision);
					gu.gatherTime = 0;
					gu.call = instance.hongBaoGatherEndCall;
					currentGathers.put(gu,gathers.get(index));
					currentGathers2.add(gu);
					isContainJewel.put(gu, 0);
					instance.refreshHongBaoNum++;
					if(instance.refreshHongBaoNum >= WeddingInstance.TOTLE_HONGBAO_NUM)
						break;
				}
			}
			processJewel();
		}
	}
	
	/**处理红包刷新**/
	public void processJewel(){
		int num = 0;//应该刷新的宝石数
		if(refreshNum == 19){//最后一次刷新
			num = refreshJewelNum + lastJewelNum;
		}
		if(refreshNum%2==0){
			num = refreshJewelNum;
		}
		if(num > 0){
			for(int i = 0;i < num;i++){
				int size = currentGathers2.size();
				int randomNum = random.nextInt(size);
				if(isContainJewel.get(currentGathers2.get(randomNum)) == 0){
					isContainJewel.put(currentGathers2.get(randomNum), 1);
				}
			}
		}
		refreshNum ++;
	}
}

class HongBaoGatherEndCall implements GatherEndCall{

	public WeddingInstance instance;
	
	public HongBaoGatherEndCall(WeddingInstance instance){
		this.instance = instance;
	}
	
	/**发送祝福语**/
	public void sendBless(Player p){
		int index = instance.manager.random.nextInt(instance.manager.slang.length);
		ChatService service = Server.server.getServiceRegistry().getChatService();
		service.sendFactionSystemMessage(instance.man.faction,MessageFormat.format(peony.Messages.STRING_01854, instance.man.name,instance.woman.name,instance.manager.slang[index]));
	}
	
	public void gatherEnd(GatherUnit unit, Player p) {
		instance.alreadyHongbaoNum ++;
		unit.removeFromWorld();
		PlayerTransaction tx = p.newTransaction("WEDDINGHONGBAO");
		p.addMoney(instance.levelMoney[instance.level], tx, true);
		tx.commit();
		PlayerTransaction tx2 = p.newTransaction("WEDDINGHONGBAO");
		if(instance.sendHongBao.isContainJewel.get(unit) == 1){
			GameItem item = ObjectAccessor.createGameItem(instance.jewels.get(0));
			instance.jewels.remove(0);
			if(item != null){
				try {
					p.bag.addGameItemComplete(item, 1, tx2, true);
					sendBless(p);
					tx2.commit();
				} catch (NoEnoughSpaceException e) {
					tx2.rollback();
					MailService service = Server.server.getServiceRegistry().getMailService();
					service.sendSystemMail(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_01852, "", 0, item, 1, "WEDDINGHONGBAO");
				}
			}
		}
		instance.sendHongBao.currentGathers.remove(unit);
		instance.sendHongBao.currentGathers2.remove(unit);
		instance.sendHongBao.isContainJewel.remove(unit);
	}
}

class AnswerQue{
	
	public Player player1;
	
	public Player player2;
	
	public int questionNum;//答题计数
	
	public int rightNum;//正确答题数
	
	public int errorNum;//错误答题数
	
	public long lastAnswerTime = 0L;//最后一次答题时间
	
	public WeddingInstance weddingInstance;
	
	public static int QUESTIONNUM = 5;//一轮答题数
	
	public static Random random = new Random();
	
	public int currentQuestionId = -1;//当前答题序号
	
	public int questionAnswer1 = -1;//每道题中第一个人的应答
	
	public int questionAnswer2 = -1;//每道题中第二个人的应答
	
	public int state = -1;//问题的状态
	
	public static int END = 1;//答题已经结束
	
	public AnswerQue(Player player1,Player player2,WeddingInstance instance){
		this.player1 = player1;
		this.player2 = player2;
		this.weddingInstance = instance;
		this.sendQuestion();
	}
	
	/**随机选题**/
	public WedQues randomQuetion(){
		int randomNum = random.nextInt(weddingInstance.manager.questions.size());
		return weddingInstance.manager.questions.get(randomNum);
	}
	
	/**发送问题**/
	public void sendQuestion(){
		WedQues wedQuest = randomQuetion();
		int[] answerId1 = randomId();
		Packet pt1 = new Packet(OpCode.WEDDING_QUESTION1_SERVER);
		pt1.putInt(OpCode.WEDDING_QUESTION1_CLIENT);
		pt1.putInt(wedQuest.id);
		pt1.putString(MessageFormat.format("{0}本次问题:{1}/5  当前轮次:{2}/4", wedQuest.question, (questionNum+1),weddingInstance.answerNum.get(player1.id)==null?1:(weddingInstance.answerNum.get(player1.id)+1)));
		for(int i = 0;i < 4;i++){
			pt1.put(answerId1[i]);
			pt1.putString(wedQuest.getAnswerById(answerId1[i]));
		}
		if(ObjectAccessor.getPlayer(player1.id)!=null){
			ObjectAccessor.getPlayer(player1.id).session.send(pt1);
		}
		int[] answerId2 = randomId();
		Packet pt2 = new Packet(OpCode.WEDDING_QUESTION1_SERVER);
		pt2.putInt(OpCode.WEDDING_QUESTION1_CLIENT);
		pt2.putInt(wedQuest.id);
		pt2.putString(MessageFormat.format("{0}本次问题:{1}/5  当前轮次:{2}/4", wedQuest.question, (questionNum+1),weddingInstance.answerNum.get(player2.id)==null?1:(weddingInstance.answerNum.get(player2.id)+1)));
		for(int i = 0;i < 4;i++){
			pt2.put(answerId2[i]);
			pt2.putString(wedQuest.getAnswerById(answerId2[i]));
		}
		if(ObjectAccessor.getPlayer(player2.id)!=null){
			ObjectAccessor.getPlayer(player2.id).session.send(pt2);
		}
		this.currentQuestionId = wedQuest.id;
		lastAnswerTime = System.currentTimeMillis();
	}
	
	/**处理结果**/
	public void processResult(boolean result){
		questionNum++;
		if(result){
			rightNum++;
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player1.id,MessageFormat.format(peony.Messages.STRING_01855, questionNum));
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player2.id,MessageFormat.format(peony.Messages.STRING_01855, questionNum));
		}else{
			errorNum++;
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player1.id, MessageFormat.format(peony.Messages.STRING_01856, questionNum));
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player2.id, MessageFormat.format(peony.Messages.STRING_01856, questionNum));
		}
		if(questionNum >= 5){
			this.state = END;
			questionOver(true);
		}else{
			sendQuestion();
		}
	}
	
	/**答题结束时的处理 1:一方拒绝答题  2:答题时间到**/
	public void questionOver(boolean isNeedDelete){
		if(questionNum >= 0){
			Player p1 = ObjectAccessor.getPlayer(player1.id);
			Player p2 = ObjectAccessor.getPlayer(player2.id);
			if(p1 != null){
				Packet pt = new Packet(OpCode.WEDDING_QUESTION_END_SERVER);
				int player1Exp = p1.level * 500;
				int needAddexp1 =  player1Exp * (rightNum * 2 + errorNum);
				if(p1.id == weddingInstance.banLangId || p1.id == weddingInstance.banNiangId)
					needAddexp1 = needAddexp1 * 2;
				PlayerTransaction tx = p1.newTransaction("WEDDINGQUESTION");
				p1.addExp(needAddexp1, tx, true);
				tx.commit();
				Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player1.id, MessageFormat.format(peony.Messages.STRING_01857, needAddexp1));
				p1.session.send(pt);
				int num = 0;
				if(weddingInstance.answerNum.get(p1.id) == null){
					num = 1;
				}else{
					num = weddingInstance.answerNum.get(p1.id);
					num++;
				}
				weddingInstance.answerNum.put(p1.id, (Integer)num);
			}
			if(p2 != null){
				Packet pt = new Packet(OpCode.WEDDING_QUESTION_END_SERVER);
				int player2Exp = p2.level * 500;
				int needAddexp2 =  player2Exp * (rightNum * 2 + errorNum);
				if(p1.id == weddingInstance.banLangId || p1.id == weddingInstance.banNiangId)
					needAddexp2 = needAddexp2 * 2;
				PlayerTransaction tx = p2.newTransaction("WEDDINGQUESTION");
				p2.addExp(needAddexp2, tx, true);
				tx.commit();
				Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player2.id,  MessageFormat.format(peony.Messages.STRING_01857, needAddexp2));
				p2.session.send(pt);
				int num = 0;
				if(weddingInstance.answerNum.get(p2.id) == null){
					num = 1;
				}else{
					num = weddingInstance.answerNum.get(p2.id);
					num++;
				}
				weddingInstance.answerNum.put(p2.id, (Integer)num);
			}
		}
		initData();
		if(isNeedDelete){
			weddingInstance.answerQues.remove(this);
		}
	}
	
	/***更新  检测答题是否过期*/
	public void update(){
		if(state != END){
			//未过期
			if(System.currentTimeMillis() - lastAnswerTime < WeddingService.QUESTION_WAIT_TIME)
				return;
			//过期 未结束
			processResult(false);
		}
	}
	
	/**是否包含某玩家**/
	public boolean isContainPlayer(int playerId){
		if(player1.id == playerId || player2.id == playerId){
			return true;
		}
		return false;
	}
	
	/**答案随机发送顺序**/
	public int[] randomId(){
		int[] answerId = new int[]{0,1,2,3};
		for(int i = 0;i < 4;i++){
			int index = random.nextInt(4);
			int temp = answerId[index];
			answerId[index] = answerId[i];
			answerId[i] = temp;
		}
		return answerId;
	}
	
	/**回答问题**/
	public void answerQuestion(int questionId,int answer) throws MarriageException{
		//完成答题
		if(questionAnswer1 != -1&&questionAnswer2== -1){
			questionAnswer2 = answer;
			if(questionAnswer1 == questionAnswer2){
				processResult(true);
			}else{
				processResult(false);
			}
			initData();
			return;
		}
		//第一次解答
		if(questionAnswer1 == -1&&questionAnswer2== -1){
			questionAnswer1 = answer;
		}
	}
	
	/**拒绝答题**/
	public void refuseQuestion(int playerId){
		this.state = END;
		Player p = ObjectAccessor.getPlayer(playerId);
		p.pool.setLong(WeddingService.QUESTION_REFUSE_TIME, System.currentTimeMillis());
		questionOver(true);
	}
	
	/**初始化答题数据**/
	public void initData(){
		currentQuestionId = -1;//当前答题序号
		questionAnswer1 = -1;//每道题中第一个人的应答
		questionAnswer2 = -1;//每道题中第二个人的应答
	}
}
