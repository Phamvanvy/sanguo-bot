package peony.service.account.cmcc;

import com.pip.net.message.AbstractMessage;

public class CmccPushDownloadMessage extends AbstractMessage {

    /**
     * 通知世界服务器用户需要通过卓望平台下载客户端。
     * userId			String			用户ID
     * accountId		int				帐号ID
     * playerId			int				角色ID
     * url				String			下载地址
     */
	
	protected String userId;
	protected int accountId;
	protected int playerId;
	protected String url;
	
	public CmccPushDownloadMessage(int serial,String userId,int accountId,int playerId,String url) {
		super(CmccMessageType.PUSH_DOWNLOAD, serial);
		this.userId = userId;
		this.accountId = accountId;
		this.playerId = playerId;
		this.url = url;
	}

	public String getUserId() {
		return userId;
	}

	public int getAccountId() {
		return accountId;
	}

	public int getPlayerId() {
		return playerId;
	}

	public String getUrl() {
		return url;
	}

}
