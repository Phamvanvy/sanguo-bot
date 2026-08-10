package com.pip.net;

import java.net.SocketAddress;

import org.apache.mina.common.IoSession;

public class AcceptSession implements ISession {
	
	protected String id;
	protected IoSession session;
	protected boolean valid = false;
	protected long lastMessageTime;
	
	public AcceptSession(IoSession session){
		this.session = session;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public String getId(){
		return id;
	}

	public boolean isConnected() {
		if(session==null)
			return false;
		else{
			return session.isConnected();
		}
	}

	public boolean isValid() {
		return valid;
	}
	
	public void setValid(boolean valid){
		this.valid = valid;
	}

	public void send(IMessage message) {
		if(session!=null&&session.isConnected()){
			session.write(message);
		}
	}
	
	public void close(){
		if(session!=null&&session.isConnected()){
			session.close();
		}
	}
	
	public SocketAddress getRemoteAddress(){
		if(session!=null){
			return session.getRemoteAddress();
		}
		return null;
	}
	
	public boolean equals(ISession session){
		if(this==session)
			return true;
		return id.equals(session.getId());
	}

	public long getLastMessageTime() {
		return lastMessageTime;
	}

	public void setLastMessageTime(long lastMessageTime) {
		this.lastMessageTime = lastMessageTime;
	}

	public boolean timeout() {
		return System.currentTimeMillis() > lastMessageTime + 300000L;
	}
}
