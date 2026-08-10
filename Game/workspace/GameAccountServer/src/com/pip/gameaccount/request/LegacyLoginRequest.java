package com.pip.gameaccount.request;

import com.pip.net.ISession;
import com.pip.net.SessionRequest;

public class LegacyLoginRequest extends SessionRequest {
	
	public LegacyLoginRequest(int id,String serverId){
		super(id,RequestType.LEGACY_LOGIN,serverId);
	}
}
