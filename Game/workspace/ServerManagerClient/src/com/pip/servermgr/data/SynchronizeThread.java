package com.pip.servermgr.data;

import java.io.*;
import java.util.*;

import com.pip.servermgr.client.DirectoryView;

/**
 * 服务器状态刷新请求队列。
 * @author lighthu
 */
public class SynchronizeThread {
	/**
	 * 只有一个请求队列，所有服务器状态刷新操作都要它来完成。
	 */
	public static SynchronizeThread instance;
	static {
		instance = new SynchronizeThread();
		instance.start();
	}
	
	// 刷新任务队列，越前面的优先级越高
	private List<Object[]> taskList = new ArrayList<Object[]>();
	// 状态监听者
	private List<IServerStatusListener> listeners = new ArrayList<IServerStatusListener>();
	
	/**
	 * 增加一个服务器到刷新队列里。
	 * @param server 要刷新状态的服务器
	 * @param forceUpdate 是否强制刷新，强制刷新的优先级比非强制刷新的要高
	 */
	public void sync(Server server, boolean forceUpdate) {
		synchronized(taskList) {
			if (forceUpdate) {
				taskList.add(0, new Object[] { server, Boolean.TRUE });
				for (int i = 1; i < taskList.size(); i++) {
					if (taskList.get(i)[0] == server) {
						taskList.remove(i);
						break;
					}
				}
			} else {
				for (int i = 0; i < taskList.size(); i++) {
					if (taskList.get(i)[0] == server) {
						return;
					}
				}
				taskList.add(new Object[] { server, Boolean.FALSE });
			}
			taskList.notifyAll();
		}
	}
	
	public void start() {
	    for (int i = 0; i < 10; i++) {
	        new WorkerThread().start();
	    }
	}
	
	/**
	 * 添加状态变化监听者。
	 * @param l
	 */
	public void addListener(IServerStatusListener l) {
		synchronized (listeners) {
			listeners.add(l);
		}
	}
	
	/**
	 * 删除状态变化监听者。
	 * @param l
	 */
	public void removeListener(IServerStatusListener l) {
		synchronized (listeners) {
			listeners.remove(l);
		}
	}
	
	/**
	 * 请求线程。
	 */
	class WorkerThread extends Thread {
    	public void run() {
    		while (true) {
    			Object[] task;
    			synchronized(taskList) {
    				if (taskList.size() == 0) {
    					try {
    						taskList.wait();
    					} catch (Exception e) {
    					}
    				}
    				if (taskList.size() > 0) {
    					task = taskList.remove(0);
    				} else {
    					continue;
    				}
    			}
    			Server nextServer = (Server)task[0];
    			boolean forceUpdate = ((Boolean)task[1]).booleanValue();
    			
    			// 到服务器请求下载服务器状态，刷新成功后通知所有监听者，出错也通知
    			DirectoryView.updateStatusBarStatic("正在刷新状态：" + nextServer.getFullName());
    			try {
    				String ret = HttpUtils.executeShell(nextServer.getShellScript(), "status", true, forceUpdate);
    				nextServer.updateStatus(ret);
    				fireStatusChanged(nextServer);
    			} catch (Exception e) {
    				nextServer.setError();
    				fireError(nextServer, e);
    			}
    			DirectoryView.updateStatusBarStatic("");
    		}
    	}
	}
	
	// 发出状态变化通知
	private void fireStatusChanged(Server server) {
		Object[] arr;
		synchronized(listeners) {
			arr = listeners.toArray();
		}
		for (int i = 0; i < arr.length; i++) {
			try {
				((IServerStatusListener)arr[i]).statusChanged(server);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	// 发出错误通知
	private void fireError(Server server, Exception ex) {
		Object[] arr;
		synchronized(listeners) {
			arr = listeners.toArray();
		}
		for (int i = 0; i < arr.length; i++) {
			try {
				((IServerStatusListener)arr[i]).onError(server, ex);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
