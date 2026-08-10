package com.pip.dispatch;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;

public interface Dispatcher {
    public void broadcast(ByteBuffer buffer);
    public void unRegisterClient(int sessionId);
    public IoSession getSession(int sessionId);
    public void shutdown();
}
