package com.pip.net;

public interface IMessage {
	short getCmd();
	int getSerial();
    boolean isReply();
	ISession getSource();
	void setSource(ISession session);
}
