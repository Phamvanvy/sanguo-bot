package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class RenameMessage extends AbstractMessage {
	
	protected String oldName;
	protected String newName;
	
	public RenameMessage(int serial,String oldName,String newName){
		super(GameAccountMessageType.RENAME,serial);
		this.oldName = oldName;
		this.newName = newName;
	}
	
	public RenameMessage(String oldName,String newName){
		super(GameAccountMessageType.RENAME);
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
