package peony.game.notification;

/**
 * DeviceToken记录
 * @author Jeffrey
 *
 */
public class NotificationToken {
	
	//账号Id
	public int accountId;
	//发送push的渠道号
	public String provider;
	//机器的token
	public String deviceToken;
	
	public long lastLogoutTime;
	
	public NotificationToken(int accountId, String provider, String deviceToken, long lastLogoutTime) {
		if(provider == null || deviceToken == null)
			throw new IllegalArgumentException();
		this.accountId = accountId;
		this.provider = provider;
		this.deviceToken = deviceToken;
		this.lastLogoutTime = lastLogoutTime;
	}
}
