package com.pip.dispatch;

import java.util.*;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.ByteBuffer;

public class Channel {

    private String name;

    private Set<IoSession> sessions = new HashSet<IoSession>();

    public Set<IoSession> getSessions() {
		return sessions;
	}

	public Channel(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void join(IoSession session){
        sessions.add(session);
    }

    public boolean removeSession(IoSession session){
        return sessions.remove(session);
    }

    public void broadcast(ByteBuffer buffer){
        for(IoSession session:sessions){
            session.write(buffer.duplicate());
        }
    }
}
