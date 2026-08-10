package com.pip.net.message;

import com.pip.net.IMessage;
import com.pip.net.ISession;

public class AbstractMessage implements IMessage {
	
	protected ISession session;
	protected short cmd;
	protected int serial;
	protected boolean isReply;
	
	static int static_serial = 0;
	
	public AbstractMessage(short cmd,int serial,boolean isReply){
		this.cmd = cmd;
		this.serial = serial;
		this.isReply = isReply;
	}
	
	public AbstractMessage(short cmd,int serial){
		this(cmd,serial,false);
	}
	
	
	public AbstractMessage(short cmd){
		this(cmd,getIncrementSerial());
	}
	

	public boolean isReply(){
		return isReply;
	}
	
	public int getSerial(){
		return serial;
	}
	
	public short getCmd() {
		return cmd;
	}

	public ISession getSource() {
		return session;
	}

	public void setSource(ISession session) {
		this.session = session;
	}
	
	public static final synchronized int getIncrementSerial(){
		static_serial ++;
		if(static_serial<0)
			static_serial = 1;
		return static_serial;
	}

}
