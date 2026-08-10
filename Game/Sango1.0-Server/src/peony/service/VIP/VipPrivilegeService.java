package peony.service.VIP;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import peony.game.Gain;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.PropertyPool;
import peony.game.Server;
import peony.game.SetFindPathCall;
import peony.game.Time;
import peony.game.VMap;
import peony.game.changed.ChangedItem;
import peony.service.CycleInstanceMapManager;
import peony.service.MonthlyPayService;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.account.AccountProperty;
import peony.service.account.AccountPropertyDao;
import peony.service.activity.OldPlayerReturnNewActivity;
import peony.service.award.AwardService;



public class VipPrivilegeService implements Service,ServiceEventListener{
	
	private static final Logger log = Logger.getLogger(VipPrivilegeService.class);
	
	public static String PROPERTY_VIP_CHARGELEVEL = "vipchargelevel";//vip等级
	public static String PROPERTY_VIP_IBUYVALUE = "vipibuyvalue";//活动开启后第一次登录记录消费金额
	public static String PROPERTY_CYCLEINSTANCE_DAY = "getcycleinstanceday";
	public static Date START_TIME;
	
	public static float QUEST_UPRATIO = 0.1f;//任务达人：完成任何任务所获得的经验和战功都将提升10%
	public static float HORSE_UPRATIO = 0.5f;//坐骑通过打怪所获经验速度提升50%。
	public static int NATIONCHAT_UPLIMIT = 30;//每日国聊次数上限增加30条。
	public static float BOOK_DECTIME_RATIO = 0.1f; //读书缩短10%的时间
	public static float HORSEFIX_RATIO = 0.1f;//坐骑合成所消耗的战功减少10%
	public static int ROCKCARD_UPLIMIT = 100;//每日战功摇卡的上限从50升至100
	//七级以上特权
	public static float STARPROMOTE_DECCREDIT_RATIO = 0.1f;//一键升星中的一次冲星和多次冲星分别都减少10%的战功
	
	public static int[] CHARGEVALUE_PIP = {0,100,500,1000,2000,5000,10000,20000,50000,100000,200000,500000,1000000};//PIP的VIP等级对应的充值金额
	public static int[] CHARGEVALUE_TW = {0,50,300,600,1200,3000,6000,12000,30000,60000,120000,300000,600000};//TAIWAN的VIP等级对应的充值金额
	
	public static String[] AUTO_PERFECT_NATURANENHANCE = {"力量","敏捷","体力","智力","生命","精力","暴击","命中","物闪","法闪","物攻","法攻","护甲","法防","","","","免暴"};
	
	public static int[] CYCLEINSTANCE_BUFFITEM = {0,0,4657,4657,4658,4658,4659,4659,4660,4660,4661,4661,4661,4661}; //荣誉塔buff物品id
	
	public static float[] VIP_OFFLINEEXP = {1,1,1,1,1,1,1.1f,1.2f,1.3f,1.4f,1.6f,1.8f,2.0f};	//离线经验增加幅度
	
	public static int[] VIP_REMOVEJEWEL = {3,3,3,3,3,3,4,4,5,5,6,6,7};   //免摘除符宝石等级
	
	public Map<Integer,AccountProperty> accountProperty = new HashMap<Integer,AccountProperty>();   //账号属性
	
	public static int UPVIPLEVEL = 12;

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}
	
	public void startup() throws Exception {
		getStartDay();
		Server.server.getEventManager().registerListener(this);
	}
	

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_PLAYER_FIRSTLOAD,
				ServiceEvent.EVENT_CHARGE_SUCCESS,
				ServiceEvent.EVENT_MAP_PLAYER_ADDED,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_CHARGE_SUCCESS:
			processPlayerCharge((Player)event.param1, (Integer)event.param2);
			break;
		case ServiceEvent.EVENT_MAP_PLAYER_ADDED:
			processPlayerAddMap((VMap)event.param1,(Player)event.param2);
			break;
		}
	}
	
	protected void processPlayerAddMap(VMap map,Player p){
		if(p!=null){
			int mapId = CycleInstanceMapManager.mapId.get(p.clazz);
			if(map.getMapID() == mapId){
				 int itemId = VipPrivilegeService.getBuffItem(p.vipLevel);
			     if(itemId !=0){
			    	int lastGetDay = p.pool.getInt(VipPrivilegeService.PROPERTY_CYCLEINSTANCE_DAY,0);
				    if(lastGetDay != Time.day){
				    	PlayerTransaction tx = p.newTransaction("VIPCYCLEINSTANCE");
						GameItem item = ObjectAccessor.createGameItem(itemId);
					    try {
							p.bag.addGameItemComplete(item, 1, tx, true);
							tx.commit();
							p.pool.setInt(VipPrivilegeService.PROPERTY_CYCLEINSTANCE_DAY, Time.day);
						} catch (NoEnoughSpaceException e) {
							tx.rollback();
							Server.server.getServiceRegistry().getMailService().sendSystemMail(
								p.id, peony.Messages.STRING_00004, peony.Messages.STRING_01346, "vip荣誉塔物品", 0, item, 1, "VIPCYCLEINSTANCE");
						}
				    }
			    }
		    }
		}
	}
	
	protected void processPlayerCharge(Player p, int money){
		if(p!=null){
			if(!Server.server.revision.equals(Server.REVISION_TYPE_TW)){
				money*=10;
			}
			int oldCharge = p.chargeValue;
			int oldLevel = p.vipLevel;
			p.chargeValue += money;
			LogUtil.logVipExpChange(p, oldCharge, p.chargeValue, "VIPCHARGE");
			int newVipLevel = getVIPLevel(p.chargeValue);
			AccountProperty ap = getAccountProperty(p.accountId);
			int playerVipLevel = p.pool.getInt(VipPrivilegeService.PROPERTY_VIP_CHARGELEVEL, 0);
			if(newVipLevel>p.vipLevel){
				if(playerVipLevel<newVipLevel){
				     processReward(p, playerVipLevel,newVipLevel);
				     p.pool.setInt(VipPrivilegeService.PROPERTY_VIP_CHARGELEVEL, newVipLevel);
				}
				if(p.vipLevel<1 && newVipLevel>=1){
					processMonthPay(p,true);
				}
				if(p.vipLevel<3 && newVipLevel>=3){
					AwardService service = Server.server.getServiceRegistry().getAwardService();
					service.processCharge(p);
				}
				p.vipLevel = newVipLevel;
				ap.pool.setInt(VipPrivilegeService.PROPERTY_VIP_CHARGELEVEL,newVipLevel);
		    	p.addIntPropertyChangedItem(ChangedItem.VIP_LEVEL,newVipLevel,false,true);
		    	//通告VIP
		    	Server.server.getServiceRegistry().getSyncExecutorService().schedule(
						new SetFindPathCall(p.session, null, p));
		    	Server.server.getServiceRegistry().getVipPrivilegeService().addAccountProperty(p.accountId, ap);
		    	p.message(-1, MessageFormat.format("恭喜您，您的VIP等级提升到{0}级", newVipLevel), -1, -1);
		    	LogUtil.logVipLevelUp(p, oldLevel, newVipLevel, oldCharge, p.chargeValue, "VIPCHARGE");
			}
		}
	}
	
	public synchronized void processPlayerFirstLoad(Player player,AccountProperty ap){
		int ibuyValue = ap.pool.getInt(VipPrivilegeService.PROPERTY_VIP_IBUYVALUE,0);
	    player.chargeValue = Server.server.getServiceRegistry().getDbService().chargeDao.getTotalChargesAfter(player.accountId,VipPrivilegeService.START_TIME)*10;
	    if(Server.server.revision.equals(Server.REVISION_TYPE_TW)){
	    	player.chargeValue /= 10;
	    }
	    player.chargeValue += ibuyValue;
	    int newVipLevel = getVIPLevel(player.chargeValue);
	    int vipLevel = ap.pool.getInt(VipPrivilegeService.PROPERTY_VIP_CHARGELEVEL,0);
	    player.vipLevel = vipLevel;
	    int playerVipLevel = player.pool.getInt(VipPrivilegeService.PROPERTY_VIP_CHARGELEVEL, 0);
	    if(playerVipLevel<vipLevel){
	    	processReward(player, playerVipLevel,vipLevel);
	    	player.pool.setInt(VipPrivilegeService.PROPERTY_VIP_CHARGELEVEL, vipLevel);
	    	playerVipLevel = vipLevel;
	    }
	    if(newVipLevel >0 && newVipLevel!=vipLevel){
	    	ap.pool.setInt(VipPrivilegeService.PROPERTY_VIP_CHARGELEVEL,newVipLevel);
	    	if(newVipLevel>vipLevel){
	    		if(playerVipLevel<newVipLevel){
	    	         processReward(player, playerVipLevel,newVipLevel);
	    	         player.pool.setInt(VipPrivilegeService.PROPERTY_VIP_CHARGELEVEL, newVipLevel);
	    		}
	    	     if(vipLevel<1 && newVipLevel>=1){
					processMonthPay(player,false);
				 }
	    	}
	    	player.vipLevel = newVipLevel;
	    	player.addIntPropertyChangedItem(ChangedItem.VIP_LEVEL,newVipLevel,false,true);
	    	addAccountProperty(player.accountId,ap);
	    	//通告VIP
	    	Server.server.getServiceRegistry().getSyncExecutorService().schedule(
					new SetFindPathCall(player.session, null, player));
	    	LogUtil.logVipLevelUp(player, vipLevel, newVipLevel, player.chargeValue, player.chargeValue, "VIPLOGIN");
		}
	}
	
	public int getVIPReward(Player player,int level){
		int count = 0;
		if(level==1||level == 3||level == 5||level == 7||level == 9||level==11){
			count += 5;
		}
		if(level == 12){
			count += 10;
		}
		return count;
	}
	
	public void processReward(Player player,int oldValue,int newValue){
		int count = 0;
		for(int value=oldValue+1;value<=newValue;value++){
			count += getVIPReward(player,value);
		}
		player.bag.extend(player.bag.getAddedSize()+count, true);
	}
	
	/** 玩家vip等级扩展的背包数 */
	public int vipAddBagCount(Player p){
		int count = 0;
		if(p.vipLevel>0){
			for(int value=1;value<=p.vipLevel;value++){
				count += getVIPReward(p,value);
			}
		}
		return count;
	}
	
	public void processMonthPay(Player player,boolean change){
		MonthlyPayService service = Server.server.getServiceRegistry().getMonthlyPayService();
		String pool = service.getPoolByType(MonthlyPayService.MONTHPAY_TYPE_TELEPORT);
		if(player.pool.getLong(pool, 0)==0){
			player.pool.setLong(pool, 1);
			player.monthPay.put(MonthlyPayService.MONTHPAY_TYPE_TELEPORT, player.pool.getLong(pool,0l));
			if(change){
			    player.addIntPropertyChangedItem(ChangedItem.TIMEOUT,MonthlyPayService.MONTHPAY_TYPE_TELEPORT,false,true);
			}
		}
	}
	
    /** 提升完成任何任务所获得的经验和战功*/
	public static void promoteQuestValue(Player player,Gain gain){
		if(player.vipLevel>=1){
			int value = gain.getCredit();
			if(value>0){
			    int tempValue = (int)(value*(1+QUEST_UPRATIO));
			    gain.setCredit(tempValue);
			}
			value = gain.getExp();
			if(value>0){
				int tempValue = (int)(value*(1+QUEST_UPRATIO));
			    gain.setExp(tempValue);
			}
		}
	}
	
	/** 提升坐骑经验获得*/
	public static int promoteHorseExpGet(Player player,int addHorseExp){
		if(player.vipLevel>=1){
			addHorseExp = (int)(addHorseExp*(1+HORSE_UPRATIO));
		}
		return addHorseExp;
	}
	
	
	public static void getStartDay(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2013);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH, 22);
		cal.set(Calendar.HOUR_OF_DAY, 9);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		START_TIME = cal.getTime();
	}
	
	
	public int getVIPLevel(int chargeValue){
		if(chargeValue>0){
			if(Server.server.revision.equals(Server.REVISION_TYPE_TW)){
				for(int i=12;i>0;i--){
					if(chargeValue>=CHARGEVALUE_TW[i]){
						return i;
					}
				}
			}else{
				for(int i=12;i>0;i--){
					if(chargeValue>=CHARGEVALUE_PIP[i]){
						return i;
					}
				}
			}
		}
		return 0;
	}
	
	public static int getVIPValue(int vipLevel){
		if(Server.server.revision.equals(Server.REVISION_TYPE_TW)){
			if(vipLevel<12){
				return CHARGEVALUE_TW[vipLevel];
			}else{
				return CHARGEVALUE_TW[12];
			}
		}else{
			if(vipLevel<12){
				return CHARGEVALUE_PIP[vipLevel];
			}else{
				return CHARGEVALUE_PIP[12];
			}
		}
	}
	
	public static int getBuffItem(int vipLevel){
		return CYCLEINSTANCE_BUFFITEM[vipLevel];
	}
	
	public static int decHorseFixCredit(Player player,int creditValue){
		int ret = creditValue;
		if(player.vipLevel>=5)
			ret = (int) (creditValue*(1-HORSEFIX_RATIO));
		return ret;
	}
	
	public static void decStarPromoteCredit(Player player,int creditValue){
		if(player.vipLevel>=7){
			float rate = 1-STARPROMOTE_DECCREDIT_RATIO;
			creditValue *= rate;
		}
	}
	
	public synchronized AccountProperty getAccountProperty(int accountId){
		if(accountProperty.containsKey(accountId)){
			return accountProperty.get(accountId);
		}
		return null;
	}
	public AccountProperty getAccountPropertyFromDB(int accountId){
		AccountPropertyDao dao = Server.server.getServiceRegistry().getDbService().accountPropertyDao;
		List<AccountProperty> list = dao.getAccountProperty(accountId);
		boolean reCalcLevel = false;
		int level = 0;
		int hadOldPlayer = 0;
		if(list!=null && list.size()>1){
			StringBuilder sb = new StringBuilder();
			StringBuilder sb1 = new StringBuilder();
			int ibuyValue = 0;
			for(AccountProperty ap : list){
				if(ap!=null){
					int value = ap.pool.getInt(PROPERTY_VIP_IBUYVALUE, 0);
					sb.append(value+",");
					if(value>ibuyValue){
					    ibuyValue = ap.pool.getInt(PROPERTY_VIP_IBUYVALUE, 0);
					}
					int levelValue = ap.pool.getInt(PROPERTY_VIP_CHARGELEVEL, 0);
					sb1.append(levelValue+",");
					if(levelValue>level)
						level = levelValue;
					int hadOldPlayerValue = ap.pool.getInt(OldPlayerReturnNewActivity.oldPlayerPool, 0);
					sb1.append(hadOldPlayerValue+",");
					if(hadOldPlayerValue>hadOldPlayer)
						hadOldPlayer = hadOldPlayerValue;
				}
			}
			log.info("[VIPPRE]ACC["+accountId+"]IBUYVALUE["+ibuyValue+"]VALUES["+sb.toString()+"]LEVELS["+sb1.toString()+"]");
			int count = 0;
			for(AccountProperty ap : list){
				reCalcLevel = true;
				if(ap!=null){
					int value = ap.pool.getInt(PROPERTY_VIP_IBUYVALUE, 0);
					if(value<ibuyValue){
						dao.makeTransient(ap);
						log.info("[VIPREMOVE]ACC["+accountId+"]IBUYVALUE["+value+"]");
					}else if(value == ibuyValue){
						if(count!=0){
							dao.makeTransient(ap);
							log.info("[VIPREMOVE]ACC["+accountId+"]IBUYVALUE["+value+"]");
						}
						count++;
					}
				}
			}
			log.info("[VIPCOUNT]ACC["+accountId+"]COUNT["+count+"]");
		}
		AccountProperty ap = dao.getAccountPropertyByAcc(accountId);
		if(ap!=null && ap.pool.getInt(PROPERTY_VIP_CHARGELEVEL, 0)<level){
			ap.pool.setInt(PROPERTY_VIP_CHARGELEVEL, level);
		}
		if(ap!=null && ap.pool.getInt(OldPlayerReturnNewActivity.oldPlayerPool, 0)<hadOldPlayer){
			ap.pool.setInt(OldPlayerReturnNewActivity.oldPlayerPool, hadOldPlayer);
		}
		if(ap == null){
			ap = new AccountProperty();
			ap.setAccountId(accountId);
			PropertyPool pool = new PropertyPool();
			ap.setPool(pool);
			long ibuyValue = Server.server.getServiceRegistry().getDbService().ibuyDAO.getConsumeCount(accountId, VipPrivilegeService.START_TIME);
	    	if(ibuyValue>0){
	    		ibuyValue/=3600;
	    	}
	    	ap.pool.setInt(PROPERTY_VIP_IBUYVALUE, (int)ibuyValue);
	    	if(reCalcLevel){
	    		int chargeValue = Server.server.getServiceRegistry().getDbService().chargeDao.getTotalChargesAfter(accountId,VipPrivilegeService.START_TIME)*10;
    		    if(Server.server.revision.equals(Server.REVISION_TYPE_TW)){
    		    	chargeValue /= 10;
    		    }
    		    chargeValue += ibuyValue;
    		    int newVipLevel = getVIPLevel(chargeValue);
    		    ap.pool.setInt(PROPERTY_VIP_CHARGELEVEL, newVipLevel);    		    
	    	}
	    	Server.server.getServiceRegistry().getDbService().accountPropertyDao.newEntity(ap);
		}
		log.info("[VIP]ACC["+accountId+"]IBUYVALUE["+ap.pool.getInt(PROPERTY_VIP_IBUYVALUE, 0)+"]CHARGEVALUE["+ap.pool.getInt(PROPERTY_VIP_CHARGELEVEL, 0)+"]");
		return ap;
	}
	
	public synchronized void addAccountProperty(int accountId,AccountProperty ap){
		accountProperty.put(accountId, ap);
	}

}
