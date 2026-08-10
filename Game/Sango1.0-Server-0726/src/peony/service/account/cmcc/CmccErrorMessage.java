package peony.service.account.cmcc;

import com.pip.net.message.ErrorMessage;

public class CmccErrorMessage extends ErrorMessage {
	
	protected String message;
	
	public CmccErrorMessage(int serial, int code, String message) {
		super(serial, code);
		this.message = message;
	}

	public String getMessage(){
		return message;
	}
}
