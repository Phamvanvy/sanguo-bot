package com.pip.server.account;

import java.util.UUID;

public class UUIDSessionIdGenerator implements ISessionIdGenerator {

	public String getSessionId() {
		return UUID.randomUUID().toString();
	}

}
