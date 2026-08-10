package com.pip.gameaccount;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.pip.net.ISession;

public class SessionService implements ISessionService{
	
	protected Map<String,ISession> name2sessions = new HashMap<String,ISession>();
	protected ReadWriteLock lock = new ReentrantReadWriteLock();
	
	public void addSession(ISession session) {
		lock.writeLock().lock();
		try {
			name2sessions.put(session.getId(), session);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public ISession getSession(String name) {
		lock.readLock().lock();
		try {
			return name2sessions.get(name);
		} finally{
			lock.readLock().unlock();
		}
	}

	public void removeSession(ISession session) {
		lock.writeLock().lock();
		try {
			ISession old = name2sessions.get(session.getId());
			if (old == session) {
				name2sessions.remove(session.getId());
			}
		} finally{
			lock.writeLock().unlock();
		}
	}

	public ISession[] getSessions() {
	    lock.readLock().lock();
	    try {
	        Object[] arr = name2sessions.values().toArray();
	        ISession[] ret = new ISession[arr.length];
	        System.arraycopy(arr, 0, ret, 0, arr.length);
	        return ret;
	    } finally {
	        lock.readLock().unlock();
	    }
	}
}
