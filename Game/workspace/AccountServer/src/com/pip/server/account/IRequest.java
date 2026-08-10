package com.pip.server.account;

import com.pip.server.account.entity.Entity;

public interface IRequest {
	public ISource getSource();
	public int getRequestId();
	public Entity getExtendedProperties();
}
