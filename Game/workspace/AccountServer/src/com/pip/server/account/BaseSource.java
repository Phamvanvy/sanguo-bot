package com.pip.server.account;

public abstract class BaseSource implements ISource{
	
	private SourceListenerSupport listenerSupport = new SourceListenerSupport();
	
	private String id;
	private String gameCode;
	private Status status;
	
	public BaseSource(String id,String gameCode){
		this.id = id;
		this.gameCode = gameCode;
	}
	
	
	
	public Status getStatus() {
		return status;
	}



	public void setStatus(Status status) {
		this.status = status;
	}



	public String getId() {
		return id;
	}



	public String getGameCode() {
		return gameCode;
	}



	public void addListener(ISourceListener listener){
		listenerSupport.add(listener);
	}
	
	public void removeListener(ISourceListener listener){
		listenerSupport.remove(listener);
	}
}
