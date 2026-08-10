package peony.service.activity;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import peony.game.DayListener;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemTemplate;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.PropertyPool;
import peony.game.Server;
import peony.game.Time;
import peony.game.buff.Buff;
import peony.game.buff.BuffUtil;
import peony.game.chat.ChatService;
import peony.game.mail.MailService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.VIP.VipPrivilegeService;
import peony.service.account.AccountProperty;

import com.pip.sanguo.data.Card;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.OperationStatus;

public class TenthAnniversaryService implements Service , ServiceEventListener,DayListener{
	private static final Logger log = Logger.getLogger(TenthAnniversaryService.class);
	
	/**职业技能卡片ID*/
	public static int[] clazzCardId={
		4229,4230,4231,
		4232,4233,4234,
		4235,4236,4237,
		4238,4239,4240
	};
	
	/***
	 * 每天奖励物品ID
	 * 物品ID，数量
	 */
	public static int rewardItemIds_Day[][]={
		{1337,1,4809,5},
		{4809,10},
		{4135,1,4809,15},
	};
	/***
	 * 每周奖励物品ID
	 */
	public static int rewardItemIds_Week[][]={
		{1615,1},
		{1615,2},
		{1616,1},
	};
	
//	public Map<Integer,AccountProperty> accountProperty = new HashMap<Integer,AccountProperty>();   //账号属性
	
	//dayType
	public static final int DAYTYPE_DAY=1;
	public static final int DAYTYPE_WEEK=2;
	
	//工资类型
	public static final int TYPE_SALARY3=1;
	public static final int TYPE_SALARY30=2;
	public static final int TYPE_SALARY70=3;
	
	//当天工资档
	public static final int DAY_SALARY_3 = 3;
	public static final int DAY_SALARY_30 = 30;
	public static final int DAY_SALARY_70 = 70;
	
	//周期为7天
	public static final int CYCLEDAYS=7;
	
	//限制参加活动的等级>=55
	public static int LIMITLEVEL=55;
	
	//每天的奖励各类数量
	public static int REWARDCOUNT_DAY=3;
	
	//每周的奖励各类数量
	public static int REWARDCOUNT_WEEK=3;
	
	//周奖励的次数
	public static final int WEEKREWARDCOUNT_3=5;
	public static final int WEEKREWARDCOUNT_30=5;
	public static final int WEEKREWARDCOUNT_70=5;
	
	/**活动开始时间*/
	public int startDay;
	public int weekFlag;
	
	public static final String DAYSALARY="TENTHANNIVERSARY_DAYSALARY";
	
	
	
	public static final String DAYTIME="TENTHANN_DAY";
	
	public static final String WEEKFLAG="TENTHANN_WEEKFLAG";//周参加的标志
	
	public static final String PATCH_CLEARWEEKFLAG="TENTHANN_CLEARWEEKFLAG";//清除上周参加状态
	
	public String getDaySalary(int daySalary){
		return "DAYSALARY"+daySalary;
	}

	//每周工资档
	public String getWeekSalary(int daySalary){
		return "WEEKSALARY"+daySalary;
	}
	

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
		Time.addDayListener(this);
		loadDayFromBDB();
		if(startDay==0){//第一次启动时保存一下活动开始日期
			startDay=Time.day;
			saveCurrentCWeek(Time.day);
		}
		loadWeekFlagBDB();
		if(weekFlag<3){
			weekFlag=3;
			saveWeekFlag(3);
		}
		if(weekFlag==0){
			saveWeekFlag(1);
		}
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}
	
	public AccountProperty getAccountPropFromDB(int accountId){
		AccountProperty ap = Server.server.getServiceRegistry().getDbService().accountPropertyDao.getAccountPropertyByAcc(accountId);
		if(ap == null){
			ap = new AccountProperty();
			ap.setAccountId(accountId);
			PropertyPool pool = new PropertyPool();
			ap.setPool(pool);
		}
		return ap;
	}

//	/**
//	 * 添加帐号属性
//	 * @param accountId
//	 * @param ap
//	 */
//	public synchronized void addAccountProperty(int accountId,AccountProperty ap){
//		accountProperty.put(accountId, ap);
//	}
//	
//	public synchronized AccountProperty getAccountProperty(int accountId){
//		if(accountProperty.containsKey(accountId)){
//			return accountProperty.get(accountId);
//		}
//		return null;
//	}
	
	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_SALARY_ADD,//工资增加事件
				ServiceEvent.EVENT_PLAYER_FIRSTLOAD,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_SALARY_ADD://工资增加事件
			processSalary((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD://firstload事件
			clearDaySalary((Player)event.param1);
			break;
		}
	}
	
	/***
	 * 处理过天清除信息
	 */
	public synchronized void clearDaySalary(Player player){
		VipPrivilegeService vipService = Server.server.getServiceRegistry().getVipPrivilegeService();
		AccountProperty ap=vipService.getAccountProperty(player.accountId);
		if(player!=null&&ap!=null){
			int oldDayTime=ap.pool.getInt(DAYTIME, 0);
			if(oldDayTime==0||Time.day>oldDayTime){//第一次或是第二天时存储当天的时间,并清除前一天的数据
				ap.pool.setInt(DAYTIME, Time.day);
				String flag3=getDaySalary(DAY_SALARY_3);
				String flag30=getDaySalary(DAY_SALARY_30);
				String flag70=getDaySalary(DAY_SALARY_70);
				ap.pool.setInt(flag3, 0);
				ap.pool.setInt(flag30, 0);
				ap.pool.setInt(flag70, 0);
				ap.pool.setInt(DAYSALARY, 0);
			}
			if(/*ap.pool.getInt(WEEKFLAG,0)!=0&&*/ap.pool.getInt(WEEKFLAG,0)!=weekFlag){
				ap.pool.setInt(WEEKFLAG,weekFlag);
				String flag3=getWeekSalary(DAY_SALARY_3);
				String flag30=getWeekSalary(DAY_SALARY_30);
				String flag70=getWeekSalary(DAY_SALARY_70);
				ap.pool.setInt(flag3, 0);
				ap.pool.setInt(flag30, 0);
				ap.pool.setInt(flag70, 0);
			}
		}
	}
	
	public synchronized void processSalary(Player player){
		if(player!=null){
			if(player.level<LIMITLEVEL){//55级以下不能参加此活动
				return;
			}
			if(weekFlag>=4){
				return;
			}
			VipPrivilegeService vipService = Server.server.getServiceRegistry().getVipPrivilegeService();
			int currDaySalary=player.getPlayerDaySalary();
			StringBuffer sb=new StringBuffer();
			sb.append("[TENTHANNIVERSARYSERVICEDSALARYCHANGE]CURRDAYSALARY["+currDaySalary+"]");
			AccountProperty prop=vipService.getAccountProperty(player.accountId);
			if(prop!=null){
				//每周参加的标志
				if(prop.pool.getInt(WEEKFLAG,0)==0){
					prop.pool.setInt(WEEKFLAG,weekFlag);
				}
				if(currDaySalary>prop.pool.getInt(DAYSALARY, 0)){
					prop.pool.setInt(DAYSALARY, currDaySalary);
				}
				String flag3_Day=getDaySalary(DAY_SALARY_3);
				String flag30_Day=getDaySalary(DAY_SALARY_30);
				String flag70_Day=getDaySalary(DAY_SALARY_70);
				String flag3_Week=getWeekSalary(DAY_SALARY_3);
				String flag30_Week=getWeekSalary(DAY_SALARY_30);
				String flag70_Week=getWeekSalary(DAY_SALARY_70);
				ChatService chat=Server.server.getServiceRegistry().getChatService();
				sb.append("DAY_SALARY_3_OLDVALUE["+prop.pool.getInt(flag3_Day,0)+"]");
				if(currDaySalary>=DAY_SALARY_3&&prop.pool.getInt(flag3_Day,0)==0){//如果没有完成当日3工资
					prop.pool.setInt(flag3_Day, 1);
					sb.append("WEEK_3OLD["+prop.pool.getInt(flag3_Week,0)+"]");
					if(prop.pool.getInt(flag3_Week,0)>-1){
						prop.pool.setInt(flag3_Week, prop.pool.getInt(flag3_Week, 0)+1);
					}
					chat.sendPrivateMessage(player.id, MessageFormat.format("您今日工资数达到了{0}，恭喜您获得周年庆活动奖励，请到活动界面领取奖励。",currDaySalary));
					sb.append("WEEK_3NEW["+prop.pool.getInt(flag3_Week,0)+"]");
					if(prop.pool.getInt(flag3_Week,0)==WEEKREWARDCOUNT_3){
						chat.sendPrivateMessage(player.id, MessageFormat.format("您在活动期间内工资数累积{0}次达到{1}，恭喜您获得周年庆活动奖励，请到活动界面领取奖励。",WEEKREWARDCOUNT_3,DAY_SALARY_3));
					}
				}
				sb.append("DAY_SALARY_3_NEWVALUE["+prop.pool.getInt(flag3_Day,0)+"]");
				sb.append("DAY_SALARY_30_OLDVALUE["+prop.pool.getInt(flag30_Day,0)+"]");
				if(currDaySalary>=DAY_SALARY_30&&prop.pool.getInt(flag30_Day,0)==0){
					prop.pool.setInt(flag30_Day, 1);
					sb.append("WEEK_30OLD["+prop.pool.getInt(flag30_Week,0)+"]");
					if(prop.pool.getInt(flag30_Week,0)>-1){
						prop.pool.setInt(flag30_Week, prop.pool.getInt(flag30_Week, 0)+1);
					}
					chat.sendPrivateMessage(player.id, MessageFormat.format("您今日工资数达到了{0}，恭喜您获得周年庆活动奖励，请到活动界面领取奖励。",currDaySalary));
					sb.append("WEEK_30NEW["+prop.pool.getInt(flag30_Week,0)+"]");
					if(prop.pool.getInt(flag30_Week,0)==WEEKREWARDCOUNT_30){
						chat.sendPrivateMessage(player.id, MessageFormat.format("您在活动期间内工资数累积{0}次达到{1}，恭喜您获得周年庆活动奖励，请到活动界面领取奖励。",WEEKREWARDCOUNT_30,DAY_SALARY_30));
					}
				}
				sb.append("DAY_SALARY_30_NEWVALUE["+prop.pool.getInt(flag30_Day,0)+"]");
				sb.append("DAY_SALARY_70_OLDVALUE["+prop.pool.getInt(flag70_Day,0)+"]");
				if(currDaySalary>=DAY_SALARY_70&&prop.pool.getInt(flag70_Day,0)==0){
					prop.pool.setInt(flag70_Day, 1);
					sb.append("WEEK_70OLD["+prop.pool.getInt(flag70_Week,0)+"]");
					if(prop.pool.getInt(flag70_Week,0)>-1){
						prop.pool.setInt(flag70_Week, prop.pool.getInt(flag70_Week, 0)+1);
					}
					sb.append("WEEK_70NEW["+prop.pool.getInt(flag70_Week,0)+"]");
					chat.sendPrivateMessage(player.id, MessageFormat.format("您今日工资数达到了{0}，恭喜您获得周年庆活动奖励，请到活动界面领取奖励。",currDaySalary));
					if(prop.pool.getInt(flag70_Week,0)==WEEKREWARDCOUNT_70){
						chat.sendPrivateMessage(player.id, MessageFormat.format("您在活动期间内工资数累积{0}次达到{1}，恭喜您获得周年庆活动奖励，请到活动界面领取奖励。",WEEKREWARDCOUNT_70,DAY_SALARY_70));
					}
				}
				sb.append("DAY_SALARY_70_NEWVALUE["+prop.pool.getInt(flag70_Day,0)+"]");
			}
			log.info(sb.toString());
		}
	}


	public void dayChanged() {
		//1.过天清除当天数据.每天凌晨重新计算
		for (Player p : ObjectAccessor.players.values()) {
			try {
				if(p!=null){
					AccountProperty ap = Server.server.getServiceRegistry().getVipPrivilegeService().getAccountProperty(p.accountId);
					//清除在线时每天的奖励记录
					if(ap!=null){
						ap.pool.setInt(DAYTIME, Time.day);
						String flag3=getDaySalary(DAY_SALARY_3);
						String flag30=getDaySalary(DAY_SALARY_30);
						String flag70=getDaySalary(DAY_SALARY_70);
						log.info("[DAYCHANGECLEARDAYSALARY]ACC["+p.accountId+"]DAY_SALARY_3["+ap.pool.getInt(flag3, 0)
								+"]DAY_SALARY30["+ap.pool.getInt(flag30, 0)
								+"]DAY_SALARY70["+ap.pool.getInt(flag70, 0)+"]"
								);
						ap.pool.setInt(flag3, 0);
						ap.pool.setInt(flag30, 0);
						ap.pool.setInt(flag70, 0);
						ap.pool.setInt(DAYSALARY, 0);
					}
				}
			}catch(Exception e){
			}
		}
		if(Time.day-CYCLEDAYS>=startDay&&startDay!=0){
			startDay=0;
			saveCurrentCWeek(0);
			if(weekFlag==3){
				weekFlag=4;
				saveWeekFlag(4);
			}
			//清除在线玩家的奖励状态
			for (Player p : ObjectAccessor.players.values()) {
				try {
					if(p!=null){
						AccountProperty ap = Server.server.getServiceRegistry().getVipPrivilegeService().getAccountProperty(p.accountId);
						//清除在线时每天的奖励记录
						if(ap!=null){
							String flag3=getWeekSalary(DAY_SALARY_3);
							String flag30=getWeekSalary(DAY_SALARY_30);
							String flag70=getWeekSalary(DAY_SALARY_70);
							log.info("[DAYCHANGECLEARWEEKSALARY]ACC["+p.accountId+"]WEEK_SALARY_3["+ap.pool.getInt(flag3, 0)
									+"]WEEK_SALARY30["+ap.pool.getInt(flag30, 0)
									+"]WEEK_SALARY70["+ap.pool.getInt(flag70, 0)
									);
							ap.pool.setInt(flag3, 0);
							ap.pool.setInt(flag30, 0);
							ap.pool.setInt(flag70, 0);
							ap.pool.setInt(DAYSALARY, 0);
						}
					}
				}catch(Exception e){
				}
			}
		}
	}
	
	public void loadDayFromBDB(){
		try {
			Database db = Server.server.getServiceRegistry().getSleepyCatService().tenthAnniversaryServiceDB;
			Cursor cursor = null;
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            try {
	            cursor = db.openCursor(null, new CursorConfig());
	            while (cursor.getNext(keyEntry, dataEntry, null) != OperationStatus.NOTFOUND) {
	            	String key=StringBinding.entryToString(keyEntry);
	            	if(key!=null&&key.equals("TENTHANNIVERSARYSERVICESTARTDAY")){
	            		int value = IntegerBinding.entryToInt(dataEntry);
	            		startDay = (value==0?0:value);
	            	}
	            }
	        } finally {
	            if (cursor != null) {
	                try {
	                    cursor.close();
	                } catch (Exception e) {
	                }
	            }
	        }
		} catch (Exception e) {
			startDay=0;
		} 
	}
	public void loadWeekFlagBDB(){
		try {
			Database db = Server.server.getServiceRegistry().getSleepyCatService().tenthAnniversaryServiceDB;
			Cursor cursor = null;
			DatabaseEntry keyEntry = new DatabaseEntry();
			DatabaseEntry dataEntry = new DatabaseEntry();
			try {
				cursor = db.openCursor(null, new CursorConfig());
				while (cursor.getNext(keyEntry, dataEntry, null) != OperationStatus.NOTFOUND) {
					String key=StringBinding.entryToString(keyEntry);
					if(key!=null&&key.equals("TENTHANNIVERSARYSERVICESTARTDAYFLAG")){
						int value = IntegerBinding.entryToInt(dataEntry);
						weekFlag = (value==0?1:value);
					}
				}
			} finally {
				if (cursor != null) {
					try {
						cursor.close();
					} catch (Exception e) {
					}
				}
			}
		} catch (Exception e) {
			weekFlag=1;
		} 
	}
	
	public void saveWeekFlag(int flag){
		Database db = Server.server.getServiceRegistry().getSleepyCatService().tenthAnniversaryServiceDB;
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		StringBinding.stringToEntry("TENTHANNIVERSARYSERVICESTARTDAYFLAG", key);
		IntegerBinding.intToEntry(weekFlag, data);
		try {
			db.put(null, key, data);
		} catch (DatabaseException e) {
		}
	}
	
	public void saveCurrentCWeek(int startDay){
		Database db = Server.server.getServiceRegistry().getSleepyCatService().tenthAnniversaryServiceDB;
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		StringBinding.stringToEntry("TENTHANNIVERSARYSERVICESTARTDAY", key);
		IntegerBinding.intToEntry(startDay, data);
		try {
			db.put(null, key, data);
		} catch (DatabaseException e) {
		}
	}
	
	/***
	 * 10周年庆奖励信息
	 */
	public synchronized void getReWardInfo(Packet packet, ClientSession session){
		int serial =packet.getInt();
		Player p=(Player)session.getClient();
		if(p!=null){
			Packet pt = new Packet(OpCode.TENTHANNIVERSARY_INFO_SERVER);
			pt.putInt(serial);
			VipPrivilegeService vipService = Server.server.getServiceRegistry().getVipPrivilegeService();
			AccountProperty ap=vipService.getAccountProperty(p.accountId);
			int daySalary=(ap==null?0:ap.pool.getInt(DAYSALARY, 0));
			pt.putInt(daySalary);
			if(ap!=null){
				//1.当天数据
				pt.put(REWARDCOUNT_DAY);
				int itemCount=0;
				for(int rewardCount=0;rewardCount<REWARDCOUNT_DAY;rewardCount++){
					if(rewardCount==0){
						int daySalary3=ap.pool!=null?ap.pool.getInt(getDaySalary(DAY_SALARY_3), 0):0;
						pt.putShort(daySalary3);
						pt.putShort(DAY_SALARY_3);
						itemCount=rewardItemIds_Day[TYPE_SALARY3-1].length/2;
						pt.put(itemCount);
						for(int i=0;i<itemCount;i++){
							int id=rewardItemIds_Day[TYPE_SALARY3-1][i*2];
							int count=rewardItemIds_Day[TYPE_SALARY3-1][i*2+1];
							GameItem item=ObjectAccessor.createGameItem(id);
							pt.putUTF(item!=null?item.template.name:"");
							String bindDesc="";
							if(item.template.bindType==ItemTemplate.BIND_REWARD){
								bindDesc="（拾取绑定）";
							}else if(item.template.bindType==ItemTemplate.BIND_USED){
								bindDesc="（使用绑定）";
							}
							pt.putUTF((item!=null?item.template.desc:"")+bindDesc);
							pt.putInt(item.template.id);
							pt.put((byte)item.template.showImage);
							pt.putShort(((item.template.showType<<2|item.template.bindType)&0xFFFF));
							pt.put(item.template.quality);
							pt.put(count);
						}
					}else if(rewardCount==1){
						int daySalary30=ap.pool!=null?ap.pool.getInt(getDaySalary(DAY_SALARY_30), 0):0;
						pt.putShort(daySalary30);
						pt.putShort(DAY_SALARY_30);
						itemCount=rewardItemIds_Day[TYPE_SALARY30-1].length/2+3;//加3张技能卡
						pt.put(itemCount);
						for(int i=p.clazz*3;i<p.clazz*3+3;i++){
							int id=clazzCardId[i];
							int count=1;
							GameItem item=ObjectAccessor.createGameItem(id);
							pt.putUTF(item!=null?item.template.name:"");
							Card cd = Server.server.getServiceRegistry().getCardService().getCardByItemId(item.template.id);
							Buff skillBuff=BuffUtil.createBuff(cd.buff2Id, 1, p, p, 0);
							Buff skillBuff12=BuffUtil.createBuff(cd.buff2Id, 12, p, p, 0);
							String desc="1级效果：";
							if(skillBuff!=null){
								desc=desc+skillBuff.getDesc();
								desc=desc+"\n12级效果："+skillBuff12.getDesc();
							}
							pt.putUTF(desc);
							pt.putInt(item.template.id);
							pt.put((byte)item.template.showImage);
							pt.putShort(((item.template.showType<<2|item.template.bindType)&0xFFFF));
							pt.put(item.template.quality);
							pt.put(count);
						}
						for(int i=0;i<itemCount-3;i++){
							int id=rewardItemIds_Day[TYPE_SALARY30-1][i*2];
							int count=rewardItemIds_Day[TYPE_SALARY30-1][i*2+1];
							GameItem item=ObjectAccessor.createGameItem(id);
							pt.putUTF(item!=null?item.template.name:"");
							String bindDesc="";
							if(item.template.bindType==ItemTemplate.BIND_REWARD){
								bindDesc="（拾取绑定）";
							}else if(item.template.bindType==ItemTemplate.BIND_USED){
								bindDesc="（使用绑定）";
							}
							pt.putUTF((item!=null?item.template.desc:"")+bindDesc);
							pt.putInt(item.template.id);
							pt.put((byte)item.template.showImage);
							pt.putShort(((item.template.showType<<2|item.template.bindType)&0xFFFF));
							pt.put(item.template.quality);
							pt.put(count);
						
						}
					}else if(rewardCount==2){
						int daySalary70=ap.pool!=null?ap.pool.getInt(getDaySalary(DAY_SALARY_70), 0):0;
						pt.putShort(daySalary70);
						pt.putShort(DAY_SALARY_70);
						itemCount=rewardItemIds_Day[TYPE_SALARY70-1].length/2;
						pt.put(itemCount);
						for(int i=0;i<itemCount;i++){
							int id=rewardItemIds_Day[TYPE_SALARY70-1][i*2];
							int count=rewardItemIds_Day[TYPE_SALARY70-1][i*2+1];
							GameItem item=ObjectAccessor.createGameItem(id);
							pt.putUTF(item!=null?item.template.name:"");
							String bindDesc="";
							if(item.template.bindType==ItemTemplate.BIND_REWARD){
								bindDesc="（拾取绑定）";
							}else if(item.template.bindType==ItemTemplate.BIND_USED){
								bindDesc="（使用绑定）";
							}
							pt.putUTF((item!=null?item.template.desc:"")+bindDesc);
							pt.putInt(item.template.id);
							pt.put((byte)item.template.showImage);
							pt.putShort(((item.template.showType<<2|item.template.bindType)&0xFFFF));
							pt.put(item.template.quality);
							pt.put(count);
						}
					}
				}
				pt.put(REWARDCOUNT_WEEK);
				for(int j=0;j<REWARDCOUNT_WEEK;j++){
					if(j==0){
						//2.本周数据
						int weekSalary3=ap.pool!=null?ap.pool.getInt(getWeekSalary(DAY_SALARY_3), 0):0;
						pt.putShort(weekSalary3>=WEEKREWARDCOUNT_3?WEEKREWARDCOUNT_3:weekSalary3);
						pt.putShort(WEEKREWARDCOUNT_3);
						itemCount=rewardItemIds_Week[TYPE_SALARY3-1].length/2;
						pt.put(itemCount);
						for(int i=0;i<itemCount;i++){
							int id=rewardItemIds_Week[TYPE_SALARY3-1][i*2];
							int count=rewardItemIds_Week[TYPE_SALARY3-1][i*2+1];
							GameItem item=ObjectAccessor.createGameItem(id);
							pt.putUTF(item!=null?item.template.name:"");
							String bindDesc="";
							if(item.template.bindType==ItemTemplate.BIND_REWARD){
								bindDesc="（拾取绑定）";
							}else if(item.template.bindType==ItemTemplate.BIND_USED){
								bindDesc="（使用绑定）";
							}
							pt.putUTF((item!=null?item.template.desc:"")+bindDesc);
							pt.putInt(item.template.id);
							pt.put((byte)item.template.showImage);
							pt.putShort(((item.template.showType<<2|item.template.bindType)&0xFFFF));
							pt.put(item.template.quality);
							pt.put(count);
						}
					}else if(j==1){
						int weekSalary30=ap.pool!=null?ap.pool.getInt(getWeekSalary(DAY_SALARY_30), 0):0;
						pt.putShort(weekSalary30>=WEEKREWARDCOUNT_30?WEEKREWARDCOUNT_30:weekSalary30);
						pt.putShort(WEEKREWARDCOUNT_30);
						itemCount=rewardItemIds_Week[TYPE_SALARY30-1].length/2;
						pt.put(itemCount);
						for(int i=0;i<itemCount;i++){
							int id=rewardItemIds_Week[TYPE_SALARY30-1][i*2];
							int count=rewardItemIds_Week[TYPE_SALARY30-1][i*2+1];
							GameItem item=ObjectAccessor.createGameItem(id);
							pt.putUTF(item!=null?item.template.name:"");
							String bindDesc="";
							if(item.template.bindType==ItemTemplate.BIND_REWARD){
								bindDesc="（拾取绑定）";
							}else if(item.template.bindType==ItemTemplate.BIND_USED){
								bindDesc="（使用绑定）";
							}
							pt.putUTF((item!=null?item.template.desc:"")+bindDesc);
							pt.putInt(item.template.id);
							pt.put((byte)item.template.showImage);
							pt.putShort(((item.template.showType<<2|item.template.bindType)&0xFFFF));
							pt.put(item.template.quality);
							pt.put(count);
						}
					}else if(j==2){
						int weekSalary70=ap.pool!=null?ap.pool.getInt(getWeekSalary(DAY_SALARY_70), 0):0;
						pt.putShort(weekSalary70>=WEEKREWARDCOUNT_70?WEEKREWARDCOUNT_70:weekSalary70);
						pt.putShort(WEEKREWARDCOUNT_70);
						itemCount=rewardItemIds_Week[TYPE_SALARY70-1].length/2;
						pt.put(itemCount);
						for(int i=0;i<itemCount;i++){
							int id=rewardItemIds_Week[TYPE_SALARY70-1][i*2];
							int count=rewardItemIds_Week[TYPE_SALARY70-1][i*2+1];
							GameItem item=ObjectAccessor.createGameItem(id);
							pt.putUTF(item!=null?item.template.name:"");
							String bindDesc="";
							if(item.template.bindType==ItemTemplate.BIND_REWARD){
								bindDesc="（拾取绑定）";
							}else if(item.template.bindType==ItemTemplate.BIND_USED){
								bindDesc="（使用绑定）";
							}
							pt.putUTF((item!=null?item.template.desc:"")+bindDesc);
							pt.putInt(item.template.id);
							pt.put((byte)item.template.showImage);
							pt.putShort(((item.template.showType<<2|item.template.bindType)&0xFFFF));
							pt.put(item.template.quality);
							pt.put(count);
						}
					}
				}
			}
			p.send(pt);
		}
	}
	
	/**
	 * 发送奖励
	 */
	public synchronized void sendReWard(Packet packet, ClientSession session){
		int serial = packet.getInt();
		Player p=(Player)session.getClient();
		int dayOrWeek=packet.getByte();
		int type=packet.getByte();
		if(p!=null){
			if(p.level<LIMITLEVEL){//55级以下不能参加此活动
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TENTHANNIVERSARY_GETEWARD_CLIENT, MessageFormat.format("{0}级以上才能领取奖励，勇士还是去尽快提升等级吧！",LIMITLEVEL));
				return;
			}
			if(weekFlag>=4){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TENTHANNIVERSARY_GETEWARD_CLIENT, "活动已结束！");
				return;
			}
			Packet pt = new Packet(OpCode.TENTHANNIVERSARY_GETREWARD_SERVER);
			pt.putInt(serial);
			pt.put(dayOrWeek);
			pt.put(type);
			ChatService chat=Server.server.getServiceRegistry().getChatService();
			VipPrivilegeService vipService = Server.server.getServiceRegistry().getVipPrivilegeService();
			AccountProperty ap=vipService.getAccountProperty(p.accountId);
			if(ap!=null&&ap.pool!=null){
				if(dayOrWeek==DAYTYPE_DAY){//每天奖励发放
					switch(type){
					case TYPE_SALARY3:
					case TYPE_SALARY30:
					case TYPE_SALARY70:
						int flag=0;
						if(type==TYPE_SALARY3){
							flag=ap.pool.getInt(getDaySalary(DAY_SALARY_3),0);
						}else if(type==TYPE_SALARY30){
							flag=ap.pool.getInt(getDaySalary(DAY_SALARY_30),0);
						}else if(type==TYPE_SALARY70){
							flag=ap.pool.getInt(getDaySalary(DAY_SALARY_70),0);
						}
						if(flag==1){//有奖励需要发放
//							int sendcount0=0;
							int sendcount1=0;
							for(int i=0;i<rewardItemIds_Day[type-1].length;i+=2){
								int id=rewardItemIds_Day[type-1][i];
								int count=rewardItemIds_Day[type-1][i+1];
								GameItem rewardItem=ObjectAccessor.createGameItem(id);
								if(rewardItem!=null){
									PlayerTransaction tx = p.newTransaction("TENTHANNIVERSARYREWARDTOBAG_DAY");
									try {
										p.bag.addGameItemComplete(rewardItem, count, tx, true);
										tx.commit();
//										if(sendcount0==0){
//											sendcount0=1;
//											chat.sendPrivateMessage(p.id, "您领取的周年庆活动奖励已发送到背包，请及时查收。");
//										}
									} catch (NoEnoughSpaceException e) {
										tx.rollback();
										if(sendcount1==0){
											sendcount1=1;
											chat.sendPrivateMessage(p.id, "由于您的背包已满，您领取的周年庆活动奖励由飞鸽发送，请及时查收。");
										}
										MailService service = Server.server.getServiceRegistry().getMailService();
										service.sendSystemMail(p.id, peony.Messages.STRING_00004, "周年庆活动奖励", "这是您领取的周年庆活动奖励，请及时收取附件。", 0, rewardItem, count, "TENTHANNIVERSARYREWARDTOMAIL_DAY");
									}
								}
							}
//							sendcount0=0;
							sendcount1=0;
							if(type==TYPE_SALARY30){//给技能卡
								for(int i=p.clazz*3;i<p.clazz*3+3;i++){
									int id=clazzCardId[i];
									int count=1;
									GameItem rewardItem=ObjectAccessor.createGameItem(id);
									if(rewardItem!=null){
										PlayerTransaction tx = p.newTransaction("TENTHANNIVERSARYREWARDTOBAG_DAY_CARD");
										try {
											p.bag.addGameItemComplete(rewardItem, count, tx, true);
											tx.commit();
//											if(sendcount0==0){
//												sendcount0=1;
//												chat.sendPrivateMessage(p.id, "您领取的周年庆活动奖励已发送到背包，请及时查收。");
//											}
										} catch (NoEnoughSpaceException e) {
											tx.rollback();
											if(sendcount1==0){
												sendcount1=1;
												chat.sendPrivateMessage(p.id, "由于您的背包已满，您领取的周年庆活动奖励由飞鸽发送，请及时查收。");
											}
											MailService service = Server.server.getServiceRegistry().getMailService();
											service.sendSystemMail(p.id, peony.Messages.STRING_00004, "周年庆活动奖励", "这是您领取的周年庆活动奖励，请及时收取附件。", 0, rewardItem, count, "TENTHANNIVERSARYREWARDTOMAIL_DAY_CARD");
										}
									}
								}
							}
							if(type==TYPE_SALARY3){
								ap.pool.setInt(getDaySalary(DAY_SALARY_3),-1);
							}else if(type==TYPE_SALARY30){
								ap.pool.setInt(getDaySalary(DAY_SALARY_30),-1);
							}else if(type==TYPE_SALARY70){
								ap.pool.setInt(getDaySalary(DAY_SALARY_70),-1);
							}
						}else if(flag==0){//未完成
							ErrorHandler.sendErrorMessage(session, serial, OpCode.TENTHANNIVERSARY_GETEWARD_CLIENT, "达到工资数量要求后才能领取奖励。");
							return;
						}else if(flag==-1){//奖励已经发放
							ErrorHandler.sendErrorMessage(session, serial, OpCode.TENTHANNIVERSARY_GETEWARD_CLIENT, "每日只奖励一次，不能重复领取。");
							return;
						}
						break;
					default:
						ErrorHandler.sendErrorMessage(session, serial, OpCode.TENTHANNIVERSARY_GETEWARD_CLIENT, "无此奖励");
						return;
					}
				}else if(dayOrWeek==DAYTYPE_WEEK){//周奖励发放
					switch(type){
					case TYPE_SALARY3:
					case TYPE_SALARY30:
					case TYPE_SALARY70:
						int flag=0;
						if(type==TYPE_SALARY3){
							if(ap.pool.getInt(getWeekSalary(DAY_SALARY_3),0)==-1){
								flag=-1;
							}else{
								flag=ap.pool.getInt(getWeekSalary(DAY_SALARY_3),0)>=WEEKREWARDCOUNT_3?1:0;
							}
						}else if(type==TYPE_SALARY30){
							if(ap.pool.getInt(getWeekSalary(DAY_SALARY_30),0)==-1){
								flag=-1;
							}else{
								flag=ap.pool.getInt(getWeekSalary(DAY_SALARY_30),0)>=WEEKREWARDCOUNT_30?1:0;
							}
						}else if(type==TYPE_SALARY70){
							if(ap.pool.getInt(getWeekSalary(DAY_SALARY_70),0)==-1){
								flag=-1;
							}else{
								flag=ap.pool.getInt(getWeekSalary(DAY_SALARY_70),0)>=WEEKREWARDCOUNT_70?1:0;
							}
						}
						if(flag==1){//有奖励需要发放
//							int sendcount0=0;
							int sendcount1=0;
							for(int i=0;i<rewardItemIds_Week[type-1].length;i+=2){
								int id=rewardItemIds_Week[type-1][i];
								int count=rewardItemIds_Week[type-1][i+1];
								GameItem rewardItem=ObjectAccessor.createGameItem(id);
								if(rewardItem!=null){
									PlayerTransaction tx = p.newTransaction("TENTHANNIVERSARYREWARDTOBAG_WEEK");
									try {
										p.bag.addGameItemComplete(rewardItem, count, tx, true);
										tx.commit();
//										if(sendcount0==0){
//											chat.sendPrivateMessage(p.id, "您领取的周年庆活动奖励已发送到背包，请及时查收。");
//											sendcount0=1;
//										}
									} catch (NoEnoughSpaceException e) {
										tx.rollback();
										if(sendcount1==0){
											chat.sendPrivateMessage(p.id, "由于您的背包已满，您领取的周年庆活动奖励由飞鸽发送，请及时查收。");
											sendcount1=1;
										}
										MailService service = Server.server.getServiceRegistry().getMailService();
										service.sendSystemMail(p.id, peony.Messages.STRING_00004, "周年庆活动奖励", "这是您领取的周年庆活动奖励，请及时收取附件。", 0, rewardItem, count, "TENTHANNIVERSARYREWARDTOMAIL_WEEK");
									}
								}
							}
							if(type==TYPE_SALARY3){
								ap.pool.setInt(getWeekSalary(DAY_SALARY_3),-1);
							}else if(type==TYPE_SALARY30){
								ap.pool.setInt(getWeekSalary(DAY_SALARY_30),-1);
							}else if(type==TYPE_SALARY70){
								ap.pool.setInt(getWeekSalary(DAY_SALARY_70),-1);
							}
						}else if(flag==0){//未完成
							int days=0;
							int salaryValue=0;
							if(type==TYPE_SALARY3){
								days=WEEKREWARDCOUNT_3;
								salaryValue=DAY_SALARY_3;
							}else if(type==TYPE_SALARY30){
								days=WEEKREWARDCOUNT_30;
								salaryValue=DAY_SALARY_30;
							}else if(type==TYPE_SALARY70){
								days=WEEKREWARDCOUNT_70;
								salaryValue=DAY_SALARY_70;
							}
							ErrorHandler.sendErrorMessage(session, serial, OpCode.TENTHANNIVERSARY_GETEWARD_CLIENT, MessageFormat.format("累计{0}天达到{1}工资后才能领取奖励。",days,salaryValue));
							return;
						}else if(flag==-1){//奖励已经发放
							ErrorHandler.sendErrorMessage(session, serial, OpCode.TENTHANNIVERSARY_GETEWARD_CLIENT, "奖励不能重复领取");
							return;
						}
						break;
					default:
						ErrorHandler.sendErrorMessage(session, serial, OpCode.TENTHANNIVERSARY_GETEWARD_CLIENT, "无此奖励");
						return;
					}
				}else{
					ErrorHandler.sendErrorMessage(session, serial, OpCode.TENTHANNIVERSARY_GETEWARD_CLIENT, "请求类型错误");
					return;
				}
			}
			p.send(pt);
		}
	}
}
