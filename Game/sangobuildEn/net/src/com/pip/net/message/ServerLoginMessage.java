package com.pip.net.message;

public class ServerLoginMessage extends AbstractMessage{
	
	protected String serverId;
	protected String password;
	
	public ServerLoginMessage(String serverId,String password){
		super(ServerMessageType.LOGIN);
		this.serverId = serverId;
		this.password = password;
	}
	
	public ServerLoginMessage(int serial,String serverId,String password){
		super(ServerMessageType.LOGIN,serial);
		this.serverId = serverId;
		this.password = password;
	}

	public String getServerId() {
		return serverId;
	}

	public String getPassword() {
		return password;
	}
}
