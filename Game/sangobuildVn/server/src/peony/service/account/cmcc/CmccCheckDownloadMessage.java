package peony.service.account.cmcc;

import com.pip.net.message.AbstractMessage;


//* userId			String			用户ID
//* accountId		int				请求帐号ID
//* playerId			int 			请求角色ID
//* jvmcode			String			客户端Java机型代码
public class CmccCheckDownloadMessage extends AbstractMessage {

	protected String userId;
	protected int accountId,playerId;
	protected String jvmcode;

	public CmccCheckDownloadMessage(String userId,int accountId,int playerId,String jvmcode) {
		super(CmccMessageType.CHECK_DOWNLOAD);
		this.userId = userId;
		this.accountId = accountId;
		this.playerId = playerId;
		this.jvmcode = jvmcode;
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

	public String getJvmcode() {
		return jvmcode;
	}
	
}
