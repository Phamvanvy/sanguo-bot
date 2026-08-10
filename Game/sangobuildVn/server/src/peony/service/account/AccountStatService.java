package peony.service.account;

import java.util.concurrent.ConcurrentHashMap;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import peony.game.Player;
import peony.game.Server;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

/**
 * 管理帐号在线时长统计信息，处理防沉迷系统。
 * @author lighthu
 */
public class AccountStatService implements Service, ServiceEventListener {
	private ConcurrentHashMap<Integer, AccountStat> accounts = new ConcurrentHashMap<Integer, AccountStat>();
	
	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}
	
	public void shutdown() {
		for (AccountStat as : accounts.values()) {
			as.logouted();
			saveAccountStat(as);
		}
		accounts.clear();
	}
	
	public int[] getEventTypes() {
		return new int[] {
				ServiceEvent.EVENT_PLAYER_LOGOUTED,
				ServiceEvent.EVENT_PLAYER_LOGINED,
		};
	}
	
	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_PLAYER_LOGOUTED:
			logouted((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_LOGINED:
			logined((Player)event.param1);
			break;
		}
	}
	
	/**
	 * 帐号登录通知。
	 * @param id
	 * @param eighteen
	 */
	public void logined(Player p) {
		AccountStat as = getAccountStat(p.accountId);
		
		// 根据account判断的realPhone判断是否需要做防沉迷检查（"1"表示18岁以下）
		as.eighteen = true;
		if (p.session != null && p.session.getIdentity() != null 
				&& p.session.getIdentity() instanceof Account) {
			Account acc = (Account)p.session.getIdentity();
			as.eighteen = !"1".equals(acc.getRealPhone());
		}
		
		as.logined();
		saveAccountStat(as);
		accounts.put(as.id, as);
	}
	
	/**
	 * 帐号登出通知。
	 * @param id
	 */
	public void logouted(Player p) {
		AccountStat as = accounts.get(p.accountId);
		if (as != null) {
			as.logouted();
			saveAccountStat(as);
			accounts.remove(as.id);
		}
	}
	
	/**
	 * 判断是否在疲劳状态。
	 * @return 0 - 不疲劳，1 - 疲劳，2 - 非常疲劳
	 */
	public int getTireState(int id) {
		AccountStat as = accounts.get(id);
		if (as == null) {
			return 0;
		} else {
			return as.getTireState();
		}
	}
	
	/**
	 * 把一个帐号的统计信息保存起来。
	 * @param acc
	 */
	private void saveAccountStat(AccountStat acc) {
		Database db = Server.server.getServiceRegistry().getSleepyCatService().accountStatDB;
		DatabaseEntry key = new DatabaseEntry();
		IntegerBinding.intToEntry(acc.id, key);
		DatabaseEntry data = new DatabaseEntry();
		StringBinding.stringToEntry(acc.save(), data);
		try {
			db.put(null, key, data);
		} catch (DatabaseException e) {
		}
	}
	
	/**
	 * 尝试查找一个保存的帐号信息，如果找不到，创建一个新的。
	 * @param id
	 * @return
	 */
	private AccountStat getAccountStat(int id) {
		Database db = Server.server.getServiceRegistry().getSleepyCatService().accountStatDB;
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		IntegerBinding.intToEntry(id, key);
		try {
			if (db.get(null, key, data, LockMode.DEFAULT)== OperationStatus.SUCCESS) {
				String value = StringBinding.entryToString(data);
				AccountStat ret = new AccountStat();
				ret.id = id;
				ret.logined = false;
				ret.load(value);
				return ret;
			}
		} catch (DatabaseException e) {
		}
		
		// 创建
		AccountStat ret = new AccountStat();
		ret.id = id;
		ret.logined = false;
		ret.lastLoginTime = System.currentTimeMillis();
		ret.lastLogoutTime = System.currentTimeMillis();
		ret.onlineTime = 0;
		ret.restTime = 0;
		saveAccountStat(ret);
		return ret;
	}
}
