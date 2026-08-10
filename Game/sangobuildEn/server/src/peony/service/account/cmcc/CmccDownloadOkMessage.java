package peony.service.account.cmcc;

import com.pip.net.message.AbstractMessage;

public class CmccDownloadOkMessage extends AbstractMessage {

	protected String userId;
	
	public CmccDownloadOkMessage(String userId) {
		super(CmccMessageType.DOWNLOADOK);
		this.userId = userId;
	}

	public String getUserId(){
		return userId;
	}
}
