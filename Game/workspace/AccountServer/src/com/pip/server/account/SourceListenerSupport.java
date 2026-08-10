package com.pip.server.account;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SourceListenerSupport {
	public List<ISourceListener> listeners = new CopyOnWriteArrayList<ISourceListener>();
	
	public void add(ISourceListener listener){
		listeners.add(listener);
	}
	
	public void remove(ISourceListener listener){
		listeners.remove(listener);
	}
	
	public void fireSourceRegistered(ISource source){
		for(ISourceListener l:listeners){
			l.sourceRegistered(source);
		}
	}
	
	public void fireSourceUnRegistered(ISource source,boolean normal){
		for(ISourceListener l:listeners){
			l.sourceUnregistered(source, normal);
		}
	}
}
