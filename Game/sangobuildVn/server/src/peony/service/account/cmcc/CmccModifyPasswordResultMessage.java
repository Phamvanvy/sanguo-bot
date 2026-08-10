package peony.service.account.cmcc;

import com.pip.net.message.gameaccount.ModifyPasswordOkMessage;

public class CmccModifyPasswordResultMessage extends ModifyPasswordOkMessage {
	
	protected boolean success;
	protected int playerId;
	protected String msg;


	public CmccModifyPasswordResultMessage(int serial,boolean success,int playerId,String msg) {
		super(serial);
		this.success = success;
		this.playerId = playerId;
		this.msg = msg;
	}


	public boolean isSuccess() {
		return success;
	}


	public int getPlayerId() {
		return playerId;
	}


	public String getMsg() {
		return msg;
	}

	
}
