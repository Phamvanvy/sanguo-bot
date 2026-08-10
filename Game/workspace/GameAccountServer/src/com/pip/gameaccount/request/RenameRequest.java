package com.pip.gameaccount.request;

import com.pip.net.SessionRequest;

public class RenameRequest extends SessionRequest {

	protected String oldName,newName;
	
	public RenameRequest(int id,String sessionId,String oldName,String newName) {
		super(id, RequestType.RENAME, sessionId);
		this.oldName = oldName;
		this.newName = newName;
	}

	public String getOldName() {
		return oldName;
	}

	public String getNewName() {
		return newName;
	}
}
