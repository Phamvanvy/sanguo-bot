package peony.service.activity;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.changed.ChangedItem;
import peony.game.mail.MailService;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.account.ChargeActivityService;
import peony.service.account.FirstCharge;
import peony.service.sleepycat.SleepyCatService;

import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;

/**
 * @author bqzhang
 *	新区活动
 */
public class NewServerAreaActivity implements IActivityImpl, ServiceEventListener {
	protected Activity act;
	
	/** 新区统计天数 15天 */
	protected static int LASTTIME = 15*24*60*60*1000;
	
	/** 活动结束剩余天数 */
	public static int lastDays = 15;
	
	/** 是否统计数据 */
	public static boolean isRunTotal = false;
	
	/** 是否可以获取奖励 */
	public static boolean ableGetReward = false;
	
	// 上次保存的时间
	protected int lastSaveTime;
	
	public NewServerAreaActivity(Activity act){
		this.act = act;
	}
	
	public void startup() throws Exception {
		isRunTotal = true;
		ChargeActivityService  service = Server.server.getServiceRegistry().getChargeActivityService();
		service.setNewAreaEnd(false);
		timeHandler();
		Server.server.getEventManager().registerListener(this);
		changedPlayerState(true);
		long lastTimes = act.schedule.stopTime.getTime() - System.currentTimeMillis();
		lastDays = (int)(lastTimes/(24*60*60*1000))+1;
		if(lastDays < 0){
			lastDays = 0;
		}
	}
	
	public void clear() {
	}

	public Activity getActivity() {
		return act;
	}
	
	public String getDBName() {
		return "NEWSERVERAREAACTIVITY";
	}
	
	public void load() {
		
	}

	public void save() {
		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		Database db = null;
		try {
			db = dbservice.openDatabase(getDBName());
			DatabaseEntry keyEntry = new DatabaseEntry();
			DatabaseEntry dataEntry = new DatabaseEntry();
			StringBinding.stringToEntry(String.valueOf(0), keyEntry);
			StringBinding.stringToEntry(String.valueOf(ableGetReward), dataEntry);
			db.put(null, keyEntry, dataEntry);
		} catch (Exception e) {
		} finally {
			if (db != null) {
				try {
					db.close();
				} catch (Exception e) {
				}
			}
		}
		lastSaveTime = Time.currTime;
	}

	public void shutdown() {
		changedPlayerState(false);
		Server.server.getEventManager().unregisterListener(this);
	}

	public void timeHandler(){
		//15天以后执行
		Server.server.scheduExec.schedule(new Runnable(){
			public void run() {
				isRunTotal = false;
				ableGetReward = true;
				try{
					ChargeActivityService  service = Server.server.getServiceRegistry().getChargeActivityService();
					service.setNewAreaEnd(true);
					Iterator<Player> it = ObjectAccessor.players.values().iterator();
					while(it.hasNext()){
						Player p = it.next();
						if(p != null){
							accountPlayReward(p);
						}
					}
				}catch(Exception e){
				}
			}
		}, act.schedule.stopTime.getTime()-System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}
	
	
	//活动结束后结算所有在线玩家的奖励(不在线的等登陆后再结算)
	public static void accountPlayReward(Player p){
		//可以领取奖励并且还未领取
		if(ChargeActivityService.newAreaActEnd && p.pool.getInt(Player.PROPERTY_NEWAREAACT_ISGET, 0) == 0){
			//10%战功消耗返还
			int payedCredit = p.pool.getInt(Player.PROPERTY_NEWAREAACT_CREDIT, 0);
			int credit = payedCredit / 10;
			if (credit > 0) {
				PlayerTransaction tx = p.newTransaction("NEWAREAACT");
				p.addCredit(credit, tx, true);
				tx.commit();
			}
			p.pool.setInt(Player.PROPERTY_NEWAREAACT_CREDIT, 0);
			//10颗对应宝石光效等级奖励
			int itemID = -1;
			int totalStar = p.equipments.getFlashLevel();
			if(totalStar > 0){
				totalStar += 2;
			}
			if(totalStar == 4){
				itemID = 1615;	//4级宝石如意袋
			}else if(totalStar == 5){
				itemID = 1616;	//5级宝石如意袋
			}else if(totalStar == 6){
				itemID = 1617;	//6级宝石如意袋
			}else if(totalStar >= 7){
				itemID = 1618;	//7级宝石如意袋
			}
			if(itemID >= 0){
				GameItem item = ObjectAccessor.createGameItem(itemID);
				if (item != null) {
					MailService service = Server.server.getServiceRegistry().getMailService();
					service.sendSystemMail(p.id, "系统", "新区活动奖励", "恭喜您已经获得新区活动奖励，快快去镶嵌上这些宝石让自己更强力吧。", 0, item, ChargeActivityService.REWARD_JEWEL_CNT, "NEWAREAACTIVITY");
					String msg = MessageFormat.format("{0}从新区活动中获得{1}!/-6", p.name, item.template.name);
				    Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, msg);
				}
			}
			p.pool.setInt(Player.PROPERTY_NEWAREAACT_ISGET, 1);
			p.pool.setInt(Player.PROPERTY_NEWAREAACT_CREDIT, 0);
			
			ChargeActivityService  chargeService = Server.server.getServiceRegistry().getChargeActivityService();

			int actState = 0;
			FirstCharge firstCharge = chargeService.getFirstCharge(p.accountId, false);
			if (firstCharge != null) {
				//首冲奖励
				if(!firstCharge.hasGetFirstGift(ChargeActivityService.PROPERTY_FIRSTCHARGE_CHARGEANDREWARD)) {
					actState |= ChargeActivityService.FIRST_STATE;
				}
				//累充奖励
				if(!chargeService.hasGetMulGift(p.accountId)) {
					actState |= ChargeActivityService.MUL_STATE;
				}
			}else{
				actState |= ChargeActivityService.FIRST_STATE;
				actState |= ChargeActivityService.MUL_STATE;
			}
			p.addIntPropertyChangedItem(ChangedItem.ACTIVITY_STATE, actState, false, true);
		}
	}
	
	public int[] getEventTypes() {
		return new int[]{
			ServiceEvent.EVENT_CREDIT_DEC,
			ServiceEvent.EVENT_CHANGEDAY_THREE
		};
	}
	
	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_CREDIT_DEC:
			proPlayerCredit((Player)event.param1, (Integer)event.param2);
			break;
		case ServiceEvent.EVENT_CHANGEDAY_THREE:
			dayChanged();
			break;
		}
	}
	
	public void proPlayerCredit(Player p, int credit){
		if(p != null){
			if(isRunTotal){
				int payedCredit = p.pool.getInt(Player.PROPERTY_NEWAREAACT_CREDIT, 0);
				p.pool.setInt(Player.PROPERTY_NEWAREAACT_CREDIT, credit+payedCredit);
			}
		}
	}
	
	public void dayChanged() {
		lastDays--;
		if(lastDays < 0){
			lastDays = 0;
		}
	}
	
	//改变玩家新区活动状态（0-关闭，1开启）
	public void changedPlayerState(boolean isActStart){
		try{
			ChargeActivityService  service = Server.server.getServiceRegistry().getChargeActivityService();
			Iterator<Player> it = ObjectAccessor.players.values().iterator();
			while(it.hasNext()){
				Player p = it.next();
				if(p != null){
					int actState = 0;
					FirstCharge firstCharge = service.getFirstCharge(p.accountId, false);
					if (firstCharge != null) {
						//首冲奖励
						if(!firstCharge.hasGetFirstGift(ChargeActivityService.PROPERTY_FIRSTCHARGE_CHARGEANDREWARD)) {
							actState |= ChargeActivityService.FIRST_STATE;
						}
						//累充奖励
						if(!service.hasGetMulGift(p.accountId)) {
							actState |= ChargeActivityService.MUL_STATE;
						}
					}else{
						actState |= ChargeActivityService.FIRST_STATE;
						actState |= ChargeActivityService.MUL_STATE;
					}
					if(isActStart){
						actState |= ChargeActivityService.NEW_STATE;
					}
					p.addIntPropertyChangedItem(ChangedItem.ACTIVITY_STATE, actState, false, true);
				}
			}
		}catch(Exception e){
		}
	}
	
}
