package com.pip.net.message;

public class ServerLoginOkMessage extends AbstractMessage{
	public ServerLoginOkMessage(int serial){
		super(ServerMessageType.LOGINOK,serial,true);
	}
}
