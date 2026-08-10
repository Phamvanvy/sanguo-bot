package peony.game.notification;

public interface BindCallback {
	void bindSuccess(String appId, String provider, int accountId,
			String token);
	
	void bindFail(String appId, String provider, int accountId,
			String token);
}
