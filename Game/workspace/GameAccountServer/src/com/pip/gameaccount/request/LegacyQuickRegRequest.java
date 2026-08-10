package com.pip.gameaccount.request;

import com.pip.net.ISession;
import com.pip.net.SessionRequest;

public class LegacyQuickRegRequest extends SessionRequest {
	
	public LegacyQuickRegRequest(int id,String serverId){
		super(id,RequestType.LEGACY_QUICKREG,serverId);
	}
}
