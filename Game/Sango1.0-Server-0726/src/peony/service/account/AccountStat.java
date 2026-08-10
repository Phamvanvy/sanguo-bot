package peony.service.account;

/**
 * 帐号统计信息，包括帐号的累计在线时长和累计下线时长，用于实现防沉迷系统。
 * @author lighthu
 */
public class AccountStat {
	/**
	 * 帐号ID。
	 */
	public int id;
	/**
	 * 当前登录状态。
	 */
	public boolean logined;
	/**
	 * 是否18岁以上。
	 */
	public boolean eighteen;
	/**
	 * 上次登录时间（如果当前已登录，保存本次登录时间）。
	 */
	public long lastLoginTime;
	/**
	 * 上次登出时间。
	 */
	public long lastLogoutTime;
	/**
	 * 累计在线时长（如果当前已登录，不包括从本地登录到现在的时间）。
	 */
	public long onlineTime;
	/**
	 * 累计休息时间（此时间在登录时结算）。
	 */
	public long restTime;
	
	public void logined() {
		if (!logined) {
			logined = true;
			restTime += System.currentTimeMillis() - lastLogoutTime;
			lastLoginTime = System.currentTimeMillis();
			if (restTime > 5 * 60 * 60 * 1000L) {
				// 累计下线满5小时，清除累计在线时长数据
				restTime = 0;
				onlineTime = 0;
			}
		}
	}
	
	public void logouted() {
		if (logined) {
			logined = false;
			onlineTime = System.currentTimeMillis() - lastLoginTime;
			lastLogoutTime = System.currentTimeMillis();
		}
	}
	
	/**
	 * 判断是否在疲劳状态。
	 * @return 0 - 不疲劳，1 - 疲劳，2 - 非常疲劳
	 */
	public int getTireState() {
		if (eighteen) {
			return 0;
		}
		long otime = onlineTime + (logined ? (System.currentTimeMillis() - lastLoginTime) : 0);
		if (otime < 3 * 60 * 60 * 1000L) {
			return 0;
		} else if (otime < 5 * 60 * 60 * 1000L) {
			return 1;
		} else {
			return 2;
		}
	}
	
	/**
	 * 保存为字符串，以保存到bdb中。
	 * @return
	 */
	public String save() {
		return lastLoginTime + "," + lastLogoutTime + "," + onlineTime + "," + restTime; 
	}
	
	/**
	 * 从保存的字符串中装载数据。
	 */
	public void load(String value) {
		String[] secs = value.split(",");
		if (secs.length >= 4) {
			try {
				lastLoginTime = Long.parseLong(secs[0]);
				lastLogoutTime = Long.parseLong(secs[1]);
				onlineTime = Long.parseLong(secs[2]);
				restTime = Long.parseLong(secs[3]);
			} catch (Exception e) {
			}
		}
	}
}
