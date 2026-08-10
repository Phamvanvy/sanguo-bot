package peony.game.notification;

public interface PushCallback {
	void pushSuccess(String appId, String provider, int accountId,
			String token, String message);
	
	void pushFail(String appId, String provider, int accountId,
			String token, String message);
	
}
