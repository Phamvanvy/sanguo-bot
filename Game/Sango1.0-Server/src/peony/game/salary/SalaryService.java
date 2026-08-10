package peony.game.salary;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.GameObject;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.Unit;
import peony.game.attendant.Attendant;
import peony.game.instance.InstanceSweepService;
import peony.game.party.Party;
import peony.game.party.PartyMember;
import peony.game.party.PartyService;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.cards.CardService;
import peony.service.feast.FeastInstanceService;
import peony.service.gamble.GambleService;
import peony.service.pluginstance.MayDayFestivalService;
import peony.vm.ASMQuest;
import peony.vm.ASMQuestUtil;

/**
 * 工资系统
 * @author pmeng
 */
public class SalaryService implements Service,ServiceEventListener{

	protected Map<Integer,String> salaryTypes = new Hashtable<Integer,String>();//工资类型
	
	protected Map<Integer,Salary> salarys = new HashMap<Integer,Salary>();//具体工资
	
	public static final String PROPERTY_SALARY = "property_salary";//角色总工资
	
	public static final String PPOPERTY_SALARY_DAY = "property_salary_day";//角色日工资

	public static int SALARY_LIMIT = 1000;//总工资持有上限
	
	public static int SALARY_DAYLIMIT = 70;//工资日持有上线
	
	public static final String PROPERTY_SALARY_ONLINE = "salaryonline"; //在线一小时
	
	public static final String PROPERTY_SALARY_ONLLINE_TIME = "onlinetime";//记录在线时长
	
	public static final String PROPERTY_SALARY_ONLINE_CHECKTIME = "onlinechecktime";//记录在线时间时刻
	
	public static final String PROPERTY_SALARY_CHAT = "salarychat"; //聊天
	
	public static final String PROPERTY_SALARY_QUESTDAY = "salaryquestday";//每日任务
	
	public static final String PROPERTY_SALARY_KILL_BOSS = "salarykillboss";//击杀副本boss
	
	public static final String PROPERTY_SALARY_DOUZHEN = "salarydouzhen";//斗阵胜利
	
	public static final String PROPERTY_SALARY_ZHANCHANG = "salaryzhanchang";//战场胜利
	
	public static final String PROPERTY_SALARY_RONGYUTA = "salaryrongyuta";//荣誉塔
	
	public static final String PROPERTY_SALARY_FUBEN = "salaryrongfuben";//完成一次副本
	
	public static final String PROPERTY_SALARY_KILLENEMY = "salarykillenemy";//击杀敌国玩家
	
	public static final String PROPERTY_SALARY_WUCHAO = "salarywuchao";//乌巢战役
	
	public static final String PROPERTY_SALARY_KILLGUOGONG = "salarykillguogong";//击杀国公
	
	public static final String PROPERTY_SALARY_GUOZHAN = "salaryguozhan";//获得国战胜利
	
	public static final String PROPERTY_SALARY_JUNTUANZHAN = "salarytuanzhan";//获得军团战胜利
	
	public static final String PROPERTY_SALARY_BIWU = "salarybiwu";//参加一次比武大会
	
	public static final String PROPERTY_SALARY_FUMA = "salaryfuma";//参加一次驸马选举
	
	public static final String PROPERTY_SALARY_QUESTWEEK = "salaryquestweek";//完成每周任务
	
	public static final String PROPERTY_SALARY_KILLBEN = "salarykillben";//击杀董卓
	
	public static final String PROPERTY_SALARY_ROCKCARD = "salaryrockcard";//摇祈福数
	
	public static final String PROPERTY_SALARY_DAYQUEST10 = "salarydayquest10";//完成10个每日任务
	
	public static final String PROPERTY_SALARY_DAYQUEST20 = "salarydayquest20";//完成20个每日任务
	
	public static final String PROPERTY_SALARY_DAYQUEST30 = "salarydayquest30";//完成30个每日任务
	
//	public static final String PROPERTY_SALARY_MIDAUTUMN = "midautumnquest3";//完成3个中秋工资任务
	
	public static final String PROPERTY_SALARY_DAYQUESTCOUNT = "dayquestcount";//完成每日任务数
	
	public static final String PROPERTY_SALARY_INSTANCESWEEP = "salarysweep";//扫荡/完成副本
	
	public static final String PROPERTY_SALARY_SWEEPCOUNT = "sweepcount";//扫荡/完成副本数
	
	public static int SALARY_ROCKCARD = 6;//摇卡次数
	
	public static final String PROPERTY_SALARY_CONVOY = "salaryconvoy";//押镖
	
	protected OnlineSalary onlineSalary;
	
	protected ChatSalary chatSalary;
	
	protected QuestSalary questSalary;
	
	protected KillBossSalary killBossSalary;
	
	protected DouZhenSalary douzhenSalary;
	
	protected SalaryZhanChang salaryZhanChang;
	
	protected RongYuTaSalary salaryRongYuTa;
	
//	protected FuBenSalary salaryFuBen;   //小副本被清除了
	
	protected KillEnemySalary salaryKillEnemy;
	
	protected WuChaoSalary salaryWuChao;
	
	protected KillGuoGongSalary salaryKillGuoGong;
	
	protected GuozhanSalary salaryGuozhan;
	
	protected JunTuanSalary salaryJunTuan;
	
	protected BiWuSalary salaryBiWu;
	
	protected FuMaSalary salaryFuMa;
	
	protected QuestWeekSalary salaryQuestWeek;
	
	protected KillBenSalary salaryKillBen;
	
	protected RockCardSalary salaryRockCard;
	
	protected ConvoySalary salaryConvoy;
	
	protected DayQuestTenSalary salaryDayQuestTen;
	
	protected DayQuestTwenSalary salaryDayQuestTwen;
	
	protected DayQuestThirSalary salaryDayQuestThir;
	
	protected InstanceSweepSalary salaryInstanceSweep;
	
//	protected MidAutumnSalary salaryMidAutomn;
	
	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("salary.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
	}
	
	@SuppressWarnings("unchecked")
	public void parse(Document doc){
		Element root = doc.getRootElement();
		if (root != null) {
			List salaryTypes = root.elements("salaryType");
			for (int i = 0; i < salaryTypes.size(); i++) {
				int typeId = Integer.parseInt(((Element) salaryTypes.get(i)).attributeValue("type"));
				String typeName = ((Element)salaryTypes.get(i)).attributeValue("name");
				this.salaryTypes.put(typeId, typeName);
				List<Salary> sal = ((Element)salaryTypes.get(i)).elements("salary");
				for(int j = 0;j < sal.size();j++){
					int salaryId = Integer.parseInt(((Element) sal.get(j)).attributeValue("salaryId"));
					String name = ((Element)sal.get(j)).attributeValue("salaryName");
					String dec = ((Element)sal.get(j)).attributeValue("salaryDec");
					int salary = Integer.parseInt(((Element) sal.get(j)).attributeValue("sal"));
					Salary sa = initSalatyItems(typeId,salaryId,name,dec,salary);
					salarys.put(salaryId, sa);
				}
			}
	    }
	}
	
	/** 获取玩家工资列表*/
	public void getSalaryList(Player p,int serial,Packet pt,int page,int count){
			pt.putInt(serial);
			List<Salary> salaryList = getSalarys(p,page,count);
			if(salarys.size()==0)
				return;
			if(page!=0 && salaryList.size()==0)
				return;
			pt.putInt(salaryList.size());
			for(Salary s:salaryList){
			    if(s.hasGetSalary(p))
			    	pt.putString(s.dec+"(已领取)");
			    else{
			    	if(s.salaryId==4){
			    		pt.putString(s.dec+"("+p.dayQuest+"/10)");
			    	}else if(s.salaryId==5){
			    		pt.putString(s.dec+"("+p.dayQuest+"/20)");
			    	}else if(s.salaryId==6){
			    		pt.putString(s.dec+"("+p.dayQuest+"/30)");
			    	}else {
				       pt.putString(s.dec);
			    	}
			    }
				pt.putInt(s.salary);
			}
			pt.putInt(salarys.size());
			pt.putInt(p.daySalary);
//			pt.putInt(SALARY_DAYLIMIT);
	}
	
	/** 工资列表排序*/
	private List<Salary> bubbleSalarys(Player p,List<Salary> list){
		int var = 0;
		int var1 = 0;
		List<Salary> tempList = new ArrayList<Salary>();
		Iterator<Salary> it = list.iterator();
		while(it.hasNext()){
			Salary s = it.next();
			if(s.hasGetSalary(p)){
				it.remove();
			    tempList.add(s);
			}
		}
		Salary[] salarytemps = new Salary[list.size()];
		salarytemps = list.toArray(salarytemps);
		for(int i=0;i<list.size();i++){
			for(int j=i+1;j<list.size();j++){
				Salary temp = null;
				var = salarytemps[i].salary;
				var1 = salarytemps[j].salary;
				if(var>var1){
					temp = salarytemps[i];
					salarytemps[i] = salarytemps[j];
					salarytemps[j] = temp;
				}
			}
		}
		list = new ArrayList<Salary>();
		for(int i=0;i<salarytemps.length;i++){
			list.add(salarytemps[i]);
		}
		for(Salary s : tempList){
			list.add(s);
		}
		return list;
	}
	
	/** 检测在线一小时*/
	public void checkOnlineOneHour(Player p){
		if(onlineSalary.hasGetSalary(p))
			return;
		long salaryTime = p.pool.getLong(PROPERTY_SALARY_ONLLINE_TIME, 0);
		salaryTime += System.currentTimeMillis() - p.pool.getLong(PROPERTY_SALARY_ONLINE_CHECKTIME, System.currentTimeMillis());
		if(salaryTime >= OnlineSalary.validTimeDuration)
			onlineSalary.receiveSalary(p);
		else {
			p.pool.setLong(PROPERTY_SALARY_ONLLINE_TIME, salaryTime);
			p.pool.setLong(PROPERTY_SALARY_ONLINE_CHECKTIME, System.currentTimeMillis());
		}
	}
	
	/** 获取工资分页列表 */
	public synchronized List<Salary> getSalarys(Player p,int startPage, int pageCount){
		List<Salary> list = new ArrayList<Salary>();
		List<Salary> sals = new ArrayList<Salary>();
		for(Integer i : salarys.keySet()){
			sals.add(salarys.get(i));
		}
		List<Salary> sal = bubbleSalarys(p,sals);
		int temp = 0;
		int startIndex = startPage * pageCount;
		int endIndex = startPage * pageCount + pageCount;
		for(Salary s:sal){
			if(temp>=startIndex && temp<endIndex){
				list.add(s);
				if(temp==endIndex-1)
					break;
			}
			temp++;
		}
		return list;
	}
	
	/** 初始化各类工资条目*/
	private Salary initSalatyItems(int typeId,int salaryId,String name,String dec,int salar){
		Salary salary = null;
		switch(salaryId){
			case 0:
				salary = new InstanceSweepSalary(typeId, salaryId, name, dec, salar);
				salaryInstanceSweep = (InstanceSweepSalary)salary;
				break;
		    case 1:
			    salary = new RockCardSalary(typeId,salaryId,name,dec,salar);
			    salaryRockCard = (RockCardSalary)salary;
			    break;
			case 2:
				salary = new ChatSalary(typeId, salaryId, name, dec, salar);
				chatSalary = (ChatSalary)salary;
				break;
			case 3:
				salary = new ConvoySalary(typeId,salaryId,name,dec,salar);
				salaryConvoy = (ConvoySalary)salary;
				break;
			case 4:
				salary = new DayQuestTenSalary(typeId,salaryId,name,dec,salar);
				salaryDayQuestTen = (DayQuestTenSalary)salary;
				break;
			case 5:
				salary = new DayQuestTwenSalary(typeId,salaryId,name,dec,salar);
				salaryDayQuestTwen = (DayQuestTwenSalary)salary;
				break;
			case 6:
				salary = new DayQuestThirSalary(typeId,salaryId,name,dec,salar);
				salaryDayQuestThir = (DayQuestThirSalary)salary;
				break;
//			case 7:
//				salary = new FuBenSalary(typeId, salaryId, name, dec, salar);
//				salaryFuBen = (FuBenSalary)salary;
//				break;
			case 8:
				salary = new KillBenSalary(typeId,salaryId,name,dec,salar);
				salaryKillBen = (KillBenSalary)salary;
				break;
			case 9:
				salary = new RongYuTaSalary(typeId, salaryId, name, dec, salar);
				salaryRongYuTa = (RongYuTaSalary)salary;
				break;
//			case 10:
//				salary = new MidAutumnSalary(typeId,salaryId,name,dec,salar);
//				salaryMidAutomn = (MidAutumnSalary)salary;
//				break;
//			case 2:
//				salary = new QuestSalary(typeId, salaryId, name, dec, salar);
//				questSalary = (QuestSalary)salary;
//				break;
//			case 3:
//				salary = new KillBossSalary(typeId, salaryId, name, dec, salar);
//				killBossSalary = (KillBossSalary)salary;
//				break;
//			case 4:
//				salary = new DouZhenSalary(typeId, salaryId, name, dec, salar);
//				douzhenSalary = (DouZhenSalary)salary;
//				break;
//			case 5:
//				salary = new SalaryZhanChang(typeId, salaryId, name, dec, salar);
//				salaryZhanChang = (SalaryZhanChang)salary;
//				break;

//			case 8:
//				salary = new KillEnemySalary(typeId, salaryId, name, dec, salar);
//				salaryKillEnemy = (KillEnemySalary)salary;
//				break;
//			case 9:
//				salary = new WuChaoSalary(typeId, salaryId, name, dec, salar);
//				salaryWuChao = (WuChaoSalary)salary;
//				break;
//			case 10:
//				salary = new GuozhanSalary(service, typeId, salaryId, name, dec, salar);
//				salaryGuozhan = (GuozhanSalary)salary;
//				break;
//			case 11:
//				salary = new KillGuoGongSalary(service, typeId, salaryId, name, dec, salar);
//				salaryKillGuoGong = (KillGuoGongSalary)salary;
//				break;
//			case 12:
//				salary = new JunTuanSalary(service, typeId, salaryId, name, dec, salar);
//				salaryJunTuan = (JunTuanSalary)salary;
//				break;
//			case 13:
//				salary = new BiWuSalary(service, typeId, salaryId, name, dec, salar);
//				salaryBiWu = (BiWuSalary)salary;
//				break;
//			case 14:
//				salary = new FuMaSalary(service, typeId, salaryId, name, dec, salar);
//				salaryFuMa = (FuMaSalary)salary;
//				break;
//			case 15:
//				salary = new QuestWeekSalary(service, typeId, salaryId, name, dec, salar);
//				salaryQuestWeek = (QuestWeekSalary)salary;
//				break;
		}
		return salary;
	}
	
	public void shutdown() {
		
	}
	
	public int[] getEventTypes() {
		return new int[]{
			ServiceEvent.EVENT_PLAYER_FIRSTLOAD,
			ServiceEvent.EVENT_CHAT,
			ServiceEvent.EVENT_FINISH_QUEST,
//			ServiceEvent.EVENT_DOUZHEN_WIN,
//			ServiceEvent.EVENT_JOIN_BIWU,
//			ServiceEvent.EVENT_JOIN_SILI,
			ServiceEvent.EVENT_UNIT_DIE
//			ServiceEvent.EVENT_BATTLE_WIN
		};
	}
	
	public void handleEvent(ServiceEvent event) {
		switch(event.type){
//		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
//			processPlayerLoaded((Player)event.param1);
//			break;
		case ServiceEvent.EVENT_CHAT:
			chatSalary.processChat((Player)event.param1);
			break;
		case ServiceEvent.EVENT_FINISH_QUEST:
//			salaryFuBen.playerFinishQuest((Player)event.param1, (Integer)event.param2,(Integer)event.param3);
			salaryDayQuestTen.playerFinishQuest((Player)event.param1, (Integer)event.param2,(Integer)event.param3);
			salaryDayQuestTwen.playerFinishQuest((Player)event.param1, (Integer)event.param2,(Integer)event.param3);
			salaryDayQuestThir.playerFinishQuest((Player)event.param1, (Integer)event.param2,(Integer)event.param3);
//			salaryKillGuoGong.playerFinishQuest((Player)event.param1, (Integer)event.param2,(Integer)event.param3);
//			questSalary.playerFinishQuest((Player)event.param1, (Integer)event.param2,(Integer)event.param3);
//			salaryMidAutomn.playerFinishQuest((Player)event.param1, (Integer)event.param2,(Integer)event.param3);
			break;
		case ServiceEvent.EVENT_UNIT_DIE:
			salaryRongYuTa.processUnitDie((Unit)event.param1,(Unit)event.param2);
			salaryKillBen.processUnitDie((Unit)event.param1,(Unit)event.param2);
			salaryInstanceSweep.processUnitDie((Unit)event.param1,(Unit)event.param2);
//			killBossSalary.processUnitDie((Unit)event.param1,(Unit)event.param2);
//			salaryKillEnemy.processUnitDie((Unit)event.param1,(Unit)event.param2);
			break;
//		case ServiceEvent.EVENT_DOUZHEN_WIN:
//			douzhenSalary.processDouzhen((Integer)event.param1);
//			break;
//		case ServiceEvent.EVENT_BATTLE_WIN:
//			salaryZhanChang.processBattleWin((Integer)event.param1,(Integer) event.param2);
//			salaryGuozhan.processBattleWin((Integer)event.param1,(Integer) event.param2);
//			salaryJunTuan.processBattleWin((Integer)event.param1,(Integer) event.param2);
//			break;
//		case ServiceEvent.EVENT_JOIN_BIWU:
//			salaryBiWu.getSalary((Player)event.param1);
//			break;
//		case ServiceEvent.EVENT_JOIN_SILI:
//			salaryWuChao.receiveSalary((Player)event.param1);
//			break;
//		case ServiceEvent.EVENT_BIWU_ZHAOQIN:
//			salaryFuMa.getSalary((Player)event.param1);
//			break;
		}
	}
	
	/** 玩家登录时初始化工资状态 */
	public void processPlayerLoaded(Player p){
//		p.pool.setLong(PROPERTY_SALARY_ONLINE_CHECKTIME, System.currentTimeMillis());
		removeProperty(p);
		p.dayQuest = p.pool.getInt(PROPERTY_SALARY_DAYQUESTCOUNT, 0);
		p.dayInstance = p.pool.getInt(SalaryService.PROPERTY_SALARY_SWEEPCOUNT, 0);
		Calendar lastLogout = Calendar.getInstance();
		lastLogout.setTime(p.lastLogoutTime);
		int dd = (lastLogout.get(Calendar.YEAR)<<16)|lastLogout.get(Calendar.DAY_OF_YEAR);
		p.salaryDay = dd;
		if(dd != Time.day){
			p.salaryDay = Time.day;
			initSalary(p);
			p.initDaySalary();
			InstanceSweepService inService = Server.server.getServiceRegistry().getInstanceSweepService();
			inService.initInstanceTimes(p);
			MayDayFestivalService mdService = Server.server.getServiceRegistry().getMayDayFestivalService();
			mdService.initEnterTime(p);
			CardService cardService = Server.server.getServiceRegistry().getCardService();
			cardService.initExpAdded(p);
			p.pool.setInt(Player.PROPERTY_READBOOK_COUNT, 0);
			GambleService gs = Server.server.getServiceRegistry().getGambleService();
			gs.initGambleCount(p);
			p.pool.setInt(FeastInstanceService.PROPERTY_FEAST_DAYCOUNT,0);
			p.pool.setInt(Player.PROPERTY_XUANWUSHI_DAY, 0);
			p.pool.setInt(Player.PROPERTY_XUANWUSHI_SALARYDAY, 0);
			p.pool.setInt(Player.PROPERTY_XUANWUSHI_BOSSDAY, 0);
			p.pool.setInt(Player.PROPERTY_XUANWUSHI_QUESTDAY, 0);
		}else{
			Server.server.getServiceRegistry().getCardService().processRanking(p);
			p.readBookCount = p.pool.getInt(Player.PROPERTY_READBOOK_COUNT, 0);
		}
	}
	
	/**
	 * 玩家隔天登录或在线换天时初始化工资状态
	 */
	public synchronized void initSalary(Player p){
		for(Salary s:salarys.values()){
			s.initRecordSalary(p);
		}
	}
	
	/**
	 * 移除过期工资属性值
	 */
	public void removeProperty(Player p){
		p.pool.remove(PROPERTY_SALARY_ONLLINE_TIME);
		p.pool.remove(PROPERTY_SALARY_ONLINE_CHECKTIME);
		p.pool.remove(PROPERTY_SALARY_ONLINE);
		p.pool.remove(PROPERTY_SALARY_DOUZHEN);
		p.pool.remove(PROPERTY_SALARY_QUESTDAY);
		p.pool.remove(PROPERTY_SALARY_KILL_BOSS);
		p.pool.remove(PROPERTY_SALARY_KILLENEMY);
		p.pool.remove(PROPERTY_SALARY_WUCHAO);
		p.pool.remove(PROPERTY_SALARY_ZHANCHANG);
	}
	
	/**
	 * 玩家下线保存属性
	 */
	public void saveProperty(Player p){
		p.pool.setInt(PROPERTY_SALARY_SWEEPCOUNT, p.dayInstance);
		p.pool.setInt(PROPERTY_SALARY_DAYQUESTCOUNT, p.dayQuest);
	}
	
	/**
	 * 处理摇卡工资
	 */
	public void processRockCardSalary(Player p){
		if(p!=null){
			salaryRockCard.processRockCardSalary(p);
		}
	}
	
	/**
	 * 处理押镖工资
	 */
	public void processConvoySalary(Player p){
		if(p!=null){
			salaryConvoy.processConvoySalary(p);
		}
	}
	
	/**
	 * 处理扫荡副本工资
	 */
	public void processSweepSalary(Player p){
		try{
			if(p!=null){
				salaryInstanceSweep.processSweep(p);
			}
		}catch(Exception e){
			
		}
	}

}


/** 取得一次斗阵胜利*/
class DouZhenSalary extends Salary{

	public DouZhenSalary(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		playerProperty(SalaryService.PROPERTY_SALARY_DOUZHEN);
	}
	
	public void init(Player p) {
		
	}
	
	public void update(Player p) {
		
	}
	
	public void processDouzhen(int partId){
		PartyService ps = Server.server.getServiceRegistry().getPartyService();
		Party party = ps.getPartyById(partId);
		if(party == null||party.members.size() == 0)
			return;
		for(PartyMember pm:party.members){
			Player p = pm.player;
			if(p==null)
				continue;
			receiveSalary(p);
		}
	}
}

/** 在线一小时*/
class OnlineSalary extends Salary{
	
	public static long validTimeDuration = 60 * 60*1000L; //
	
	public OnlineSalary(int salaryTypeId,int salaryId,String name,String dec,int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_ONLINE);
	}
	
	public void init(Player p){
		p.pool.setLong(SalaryService.PROPERTY_SALARY_ONLLINE_TIME, 0);
		p.pool.setLong(SalaryService.PROPERTY_SALARY_ONLINE_CHECKTIME, System.currentTimeMillis());
	}

	public void update(Player p) {
	}
}

/** 发起聊天*/
class ChatSalary extends Salary{

	public ChatSalary(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_CHAT);
	}
	
	public void init(Player p) {
		
	}
	
	public void update(Player p) {
		
	}
	
	public void processChat(Player p){
		if(p!=null){
			receiveSalary(p);
		}
	}
}

/** 每日任务*/
class QuestSalary extends Salary{
	
	public QuestSalary(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_QUESTDAY);
	}
	
	public void init(Player p) {
		
	}
	
	public void update(Player p) {
		
	}
	
	public void playerFinishQuest(Player p,int questId,int branch){
		if(p!=null){
			ASMQuest quest=ASMQuestUtil.getQuest(questId);
			if(quest.getGameQuest().getRepeatType() == 3)
				receiveSalary(p);
		}
	}
}

/**  击杀一次副本的最终BOSS*/
class KillBossSalary extends Salary{
	
	int[] bossIds = new int[]{
			4001795,  
			3162115,
			1769474,
			5509157,
			4005891,
			4669443,
			1773570,
			5513228,
			6422532
	};
	
	public KillBossSalary(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_KILL_BOSS);
	}
	
	public void init(Player p) {
		
	}
	
	public void update(Player p) {
		
	}
	
	public void processUnitDie(Unit u1,Unit u2){
		for(int id:bossIds){
			if(u1.id == id){
				Player p = ObjectAccessor.getPlayer(u2.id);
				if(p!=null){
					receiveSalary(p);
				}
			}
		}
	}
}

/**  获得一次战场胜利*/
class SalaryZhanChang extends Salary{
	
	public SalaryZhanChang(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_ZHANCHANG);
	}
	
	public void init(Player p) {
		
	}
	
	public void update(Player p) {
		
	}
	
	public void processBattleWin(int playerId,int typeId){
		Player p = ObjectAccessor.getPlayer(playerId);
		if(p != null && typeId == 1){
			receiveSalary(p);
		}
	}
}

/**  挑战荣誉塔 */
class RongYuTaSalary extends Salary{
	
	int[] bossIds = new int[]{
			8454145,
			8454155,
			8454165,
			8454175,
			8454185,
			8454195,
			8454205,
			8454215,
			8454225,
			
			8458241,
			8458251,
			8458261,
			8458271,
			8458281,
			8458291,
			8458301,
			
			8462337,
			8462347,
			8462357,
			8462367,
			8462377,
			8462387,
			8462397,
			
			8466433,
			8466443,
			8466453,
			8466463,
			8466473,
			8466483,
			8466493,
			
			
			
			
	};
	
	public RongYuTaSalary(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_RONGYUTA);
	}
	
	public void init(Player p) {
		
	}
	
	public void update(Player p) {
		
	}
	
	public void processUnitDie(Unit u1,Unit u2){
		for(int id:bossIds){
			if(u1.id == id){
				Player p = null;
				if(u2.type == GameObject.TYPE_PLAYER)
				    p = ObjectAccessor.getPlayer(u2.id);
				else if(u2.type == GameObject.TYPE_ATTENDANT){
					Attendant att =(Attendant)u2;
					p = att.owner;
				}
				if(p!=null){
					receiveSalary(p);
				}
			}
		}
	}
}

/** 趣味副本*/
class FuBenSalary extends Salary{
	
	int[] questIds = new int[]{
			2844,2845,2846,2847,2859,2813,2834,2835,2849,2873,2862	
	};
	
	public FuBenSalary(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_FUBEN);
	}
	
	public void init(Player p) {
		
	}
	
	public void update(Player p) {
		
	}
	
	public void playerFinishQuest(Player p,int questId,int branch){
		if(p!=null){
			for(int id:questIds){
				if(id == questId){
					receiveSalary(p);
				}
			}
		}
	}
}

/** 
 * 击杀一位敌国玩家
 */
class KillEnemySalary extends Salary{
	
	public KillEnemySalary(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_KILLENEMY);
	}
	
	public void init(Player p) {
	}
	
	public void update(Player p) {
	}
	
	protected void processUnitDie(Unit u1,Unit u2){
		//杀人
		if(u1==null||u2==null)
			return;
		if(u1.type==GameObject.TYPE_PLAYER&&u2.type==GameObject.TYPE_PLAYER){
			if(u1.faction!=u2.faction){
				if(Math.abs(u1.level - u2.level) < 11){
					if(ObjectAccessor.getPlayer(u2.id)==null)
						return;
					receiveSalary(ObjectAccessor.getPlayer(u2.id));
				}
			}
		}
	}
}

/**  乌巢战役 */
class WuChaoSalary extends Salary{
	
	public WuChaoSalary(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_WUCHAO);
	}
	
	public void init(Player p) {
		
	}
	
	public void update(Player p) {
		
	}
}

class KillGuoGongSalary extends Salary{
	
	int[] questIds = new int[]{
			1503,1504,1502	
	};
	
	public KillGuoGongSalary(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_KILLGUOGONG);
	}
	
	public void init(Player p) {
		
	}
	
	public void update(Player p) {
		
	}
	
	public void playerFinishQuest(Player p,int questId,int branch){
		if(p!=null){
			for(int id:questIds){
				if(id == questId){
					receiveSalary(p);
				}
			}
		}
	}
}

class GuozhanSalary extends Salary{
	
	public GuozhanSalary(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_GUOZHAN);
	}
	
	public void init(Player p) {
		
	}
	
	public void update(Player p) {
		
	}
	
	public void processBattleWin(int playerId,int typeId){
		Player p = ObjectAccessor.getPlayer(playerId);
		if(p != null && typeId == 0){
			receiveSalary(p);
		}
	}
}

class JunTuanSalary extends Salary{
	
	public JunTuanSalary(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_JUNTUANZHAN);
	}
	
	public void init(Player p) {
		
	}
	
	public void update(Player p) {
		
	}
	
	public void processBattleWin(int playerId,int typeId){
		Player p = ObjectAccessor.getPlayer(playerId);
		if(p != null && typeId == 2){
			receiveSalary(p);
		}
	}
}

class BiWuSalary extends Salary{
	
	public BiWuSalary(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_BIWU);
	}
	
	public void init(Player p) {
		
	}
	
	public void update(Player p) {
		
	}
}

class FuMaSalary extends Salary{
	
	public FuMaSalary(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_FUMA);
	}
	
	public void init(Player p) {
		
	}
	
	public void update(Player p) {
		
	}
}

class QuestWeekSalary extends Salary{
	
	public QuestWeekSalary(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_QUESTWEEK);
	}
	
	public void init(Player p) {
		
	}
	
	public void update(Player p) {
		
	}
}

/** 扫荡副本 */
class InstanceSweepSalary extends Salary {
	int[] bossIds = new int[]{
			1769474,
			1773570,
			3162115,
			4669443,
			4001795,
			4005891,
			5509157,
			5513227
	};
	public InstanceSweepSalary(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_INSTANCESWEEP);
	}
	
    public void init(Player p) {
		p.dayInstance=0;
		p.pool.setInt(SalaryService.PROPERTY_SALARY_SWEEPCOUNT, 0);
	}
	
	public void update(Player p) {
		
	}

	public void processUnitDie(Unit u1,Unit u2){
		for(int id:bossIds){
			if(u1.id == id){
				Player p = null;
				if(u2.type == GameObject.TYPE_PLAYER){
					p = ObjectAccessor.getPlayer(u2.id);
				}else if(u2.type == GameObject.TYPE_ATTENDANT){
					Attendant att = (Attendant)u2;
					p = att.owner;
				}
				if(p!=null){
					p.dayInstance++;
					p.pool.setInt(SalaryService.PROPERTY_SALARY_SWEEPCOUNT, p.dayInstance);
					if(p.dayInstance>=3){
						if(p.party != null){
							for(PartyMember player:p.party.members){
								if(player.player!=null){
								    receiveSalary(player.player);
								}
							}
						}else{
							receiveSalary(p);
						}
					}
				}
			}
		}
	}
	
    public void processSweep(Player p){
		p.dayInstance++;
		p.pool.setInt(SalaryService.PROPERTY_SALARY_SWEEPCOUNT, p.dayInstance);
		if(p.dayInstance>=3){
			receiveSalary(p);
		}
	}
}


/** 击杀董卓*/
class KillBenSalary extends Salary {
	int[] bossIds = new int[]{
			6422532
	};
	public KillBenSalary(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_KILLBEN);
	}
	
    public void init(Player p) {
		
	}
	
	public void update(Player p) {
		
	}

	public void processUnitDie(Unit u1,Unit u2){
		for(int id:bossIds){
			if(u1.id == id){
				Player p = null;
				if(u2.type == GameObject.TYPE_PLAYER){
					p = ObjectAccessor.getPlayer(u2.id);
				}else if(u2.type == GameObject.TYPE_ATTENDANT){
					Attendant att = (Attendant)u2;
					p = att.owner;
				}
				if(p!=null){
					if(p.party != null){
						for(PartyMember player:p.party.members){
							if(player.player!=null){
							    receiveSalary(player.player);
							}
						}
					}else{
						receiveSalary(p);
					}
				}
			}
		}
	}
	
}

/** 摇卡工资*/
class RockCardSalary extends Salary{
	
	public RockCardSalary(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_ROCKCARD);
	}
	
	public void processRockCardSalary(Player p){
		if(p!=null){
			if(p.rockCardCount>=SalaryService.SALARY_ROCKCARD){
				receiveSalary(p);
			}
		}
	}
	
	public void init(Player p) {
		
	}
	
	public void update(Player p) {
		
	}
}


/**押镖工资*/
class ConvoySalary extends Salary{
	
	public ConvoySalary(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_CONVOY);
	}
	
	public void processConvoySalary(Player p){
		if(p!=null){
			receiveSalary(p);
		}
	}
	
	public void init(Player p) {
		
	}
	
	public void update(Player p) {
		
	}
}

/**完成10次每日任务*/
class DayQuestTenSalary extends Salary{
	
	public DayQuestTenSalary(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_DAYQUEST10);
	}
	
	
	public void playerFinishQuest(Player p,int questId,int branch){
		if(p!=null){
			ASMQuest quest=ASMQuestUtil.getQuest(questId);
			if(quest.getGameQuest().getRepeatType() == 3){
				p.dayQuest++;
				p.pool.setInt(SalaryService.PROPERTY_SALARY_DAYQUESTCOUNT, p.dayQuest);
				if(p.dayQuest >= 10){
				    receiveSalary(p);
				}
			}
		}
	}
	
	public void init(Player p) {
		p.dayQuest=0;
		p.pool.setInt(SalaryService.PROPERTY_SALARY_DAYQUESTCOUNT, 0);
	}
	
	public void update(Player p) {
		
	}
}

/**完成20次每日任务*/
class DayQuestTwenSalary extends Salary{
	
	public DayQuestTwenSalary(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_DAYQUEST20);
	}
	
	public void playerFinishQuest(Player p,int questId,int branch){
		if(p!=null){
			if(p.dayQuest >= 20){
			    receiveSalary(p);
			}
		}
	}
	
	public void init(Player p) {
		
	}
	
	public void update(Player p) {
		
	}
}

/**完成30次每日任务*/
class DayQuestThirSalary extends Salary{
	
	public DayQuestThirSalary(int salaryTypeId,
			int salaryId, String name, String dec, int salary) {
		super(salaryTypeId, salaryId, name, dec, salary);
		this.playerProperty(SalaryService.PROPERTY_SALARY_DAYQUEST30);
	}
	
	public void playerFinishQuest(Player p,int questId,int branch){
		if(p!=null){
			if(p.dayQuest >= 30){
			    receiveSalary(p);
			}
		}
	}
	
	public void init(Player p) {
		
	}
	
	public void update(Player p) {
		
	}
}
//class MidAutumnSalary extends Salary{
//
//	public MidAutumnSalary(int salaryTypeId, int salaryId, String name,
//			String dec, int salary) {
//		super(salaryTypeId, salaryId, name, dec, salary);
//		this.playerProperty(SalaryService.PROPERTY_SALARY_MIDAUTUMN);
//	}
//
//	public void init(Player p) {
//		if(p!=null){
//			p.midAutumnDayQuest=0;
//			p.pool.setInt(SalaryService.PROPERTY_SALARY_MIDAUTUMN, 0);
//		}
//	}
//
//	public void update(Player p) {
//		
//	}
//	public void playerFinishQuest(Player p,int questId,int branch){
//		if(p!=null){
//			ASMQuest quest=ASMQuestUtil.getQuest(questId);
//			if(quest.getGameQuest().getRepeatType() == 3&&
//					(questId==3590||questId==3593||questId==3594
//							||questId==3591||questId==3595
//							||questId==3597||questId==3596
//							||questId==3598||questId==3672)){
//				p.midAutumnDayQuest++;
//				p.pool.setInt(SalaryService.PROPERTY_SALARY_DAYQUESTCOUNT, p.midAutumnDayQuest);
//				if(p.midAutumnDayQuest >= 3){
//				    receiveSalary(p);
//				}
//			}
//		}
//	}
//}
