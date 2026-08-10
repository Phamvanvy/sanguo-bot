package com.pip.servermgr.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.pip.servermgr.data.HttpUtils;
import com.pip.servermgr.data.ServerGroup;

/**
 * 异步按队列执行远程shell调用。
 * @author light.hu
 */
public class AsyncExecuteThread extends Thread {
	public static AsyncExecuteThread instance = new AsyncExecuteThread();
	private List<Object> taskOwner = new ArrayList<Object>();
	private List<String[]> queue = new ArrayList<String[]>();
	private Display display;
	
	public AsyncExecuteThread() {
		display = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay();
		start();
	}
	
	public synchronized void addRequest(Object owner, String[] request) {
		if (isRequestExists(request)) {
			return;
		}
		taskOwner.add(owner);
		queue.add(request);
		if (owner instanceof ServerGroup) {
			DirectoryView.getInstance().statusChanged((ServerGroup)owner);
		}
		notifyAll();
	}
	
	public synchronized boolean isRequestExists(String[] request) {
		for (String[] arr : queue) {
			if (Arrays.equals(arr, request)) {
				return true;
			}
		}
		return false;
	}
	
	public synchronized boolean isOwnerExists(Object owner) {
		for (Object o : taskOwner) {
			if (o == owner) {
				return true;
			}
		}
		return false;
	}
	
	public void run() {
		while (true) {
			String[] req = null;
			synchronized(this) {
				if (queue.size() > 0) {
					req = queue.get(0);
				} else {
					try {
						wait(10000L);
					} catch (Exception e) {
					}
				}
			}
			if (req != null) {
				DirectoryView.updateStatusBarStatic("正在同步数据：" + req[0] + " " + req[1]);
				try {
					HttpUtils.executeShell(req[0], req[1], false, true);
				} catch (Exception e) {
					e.printStackTrace();
					display.asyncExec(new ShowError(e.toString()));
				}
				DirectoryView.updateStatusBarStatic("");
				synchronized(this) {
					queue.remove(0);
					Object owner = taskOwner.remove(0);
					if (owner instanceof ServerGroup) {
						DirectoryView.getInstance().statusChanged((ServerGroup)owner);
					}
				}
			}
		}
	}
	
	private static class ShowError implements Runnable {
		private String message;
		
		public ShowError(String m) {
			message = m;
		}
		
		public void run() {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "错误", message);
		}
	}
}
