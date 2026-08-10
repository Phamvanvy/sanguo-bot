package peony.service.account;

import peony.common.ClientSessionAsyncCall;
import peony.net.ClientSession;

import com.pip.net.IMessage;
import com.pip.net.uwap2.mina.UWAPData;

public abstract class AccountAsyncCall extends ClientSessionAsyncCall {

	protected IMessage message;
	protected UWAPData data;
	
	public AccountAsyncCall(ClientSession session){
		super(session);
	}
	
	public void setSuccess(boolean success){
		this.success = success;
	}
	
	
	public void setMessage(IMessage message){
		this.message = message;
	}
	
	public void setUWAPData(UWAPData data){
		this.data = data;
	}
}
