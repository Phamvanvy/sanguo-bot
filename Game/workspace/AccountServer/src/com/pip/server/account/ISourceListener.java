package com.pip.server.account;

public interface ISourceListener {
	public void sourceRegistered(ISource source);
	public void sourceUnregistered(ISource source,boolean normal);
}
