package com.pip.itimes.server.stage;

/**
 * @author Lelonte
 * @version 1.0
 */
public class RoarEffect extends Effect {
	
	private String channel;
	private String message;
	
	public RoarEffect() {
	}
	
	public String getMessage() {
	    return message;
	}
	
	public void setChannel(String channel) {
	    this.channel = channel;
	}
	
	public void setMessage(String message) {
	    this.message = message;
	}
	
	public String getChannel() {
	    return channel;
	}
	
	public byte getType(){
	    return 63;
	}
}
