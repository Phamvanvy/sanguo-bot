package com.pip.gtl.remotedebugger;

import java.net.Socket;
import java.util.*;
import java.io.*;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.pip.gtl.decompiler.ETFDebugInfo;
import com.pip.gtl.remotedebugger.ui.DebugSessionView;
import com.pip.gtl.remotedebugger.ui.MemoryView;
import com.pip.gtl.remotedebugger.ui.VariableView;
import com.pip.gtleditor.IGTLDebugManager;
import com.pip.j0ide.Settings;

public class GTLDebugManager implements IGTLDebugManager {
	public static class Breakpoint {
		public File file;
		public int lineNo;
		
		public Breakpoint(File f, int l) {
			file = f;
			lineNo = l;
		}
		
		public int hashCode() {
			return file.hashCode() + lineNo;
		}
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Breakpoint)) {
				return false;
			}
			Breakpoint bo = (Breakpoint)o;
			return file.equals(bo.file) && lineNo == bo.lineNo;
		}
	}
	
	protected ArrayList<GTLDebugSession> sessions;
	protected HashMap<Breakpoint, Breakpoint> breakpoints;
	protected int activeSession;
	protected Display display;
	protected DebugSessionView debugSessionView;
	protected VariableView variableView;
	protected MemoryView memoryView;
	
	
	public Display getDisplay() {
		return display;
	}

	public void setDisplay(Display display) {
		this.display = display;
	}

	public DebugSessionView getDebugSessionView() {
		return debugSessionView;
	}

	public void setDebugSessionView(DebugSessionView debugSessionView) {
		this.debugSessionView = debugSessionView;
	}

	public VariableView getVariableView() {
		return variableView;
	}

	public void setVariableView(VariableView variableView) {
		this.variableView = variableView;
	}
	
	public MemoryView getMemoryView() {
		return memoryView;
	}
	
	public void setMemoryView(MemoryView memoryView) {
		this.memoryView = memoryView;
	}

	public synchronized GTLDebugSession[] getSessions() {
		GTLDebugSession[] ret = new GTLDebugSession[sessions.size()];
		sessions.toArray(ret);
		return ret;
	}
	
	public synchronized GTLDebugSession findSession(String name) {
	    for (GTLDebugSession session : sessions) {
	        if (session.getDebugInfo().taskName.equals(name)) {
	            return session;
	        }
	    }
	    return null;
	}

	public GTLDebugManager() {
		sessions = new ArrayList<GTLDebugSession>();
		breakpoints = new HashMap<Breakpoint, Breakpoint>();
		activeSession = -1;
	}
	
	public int getActiveSession() {
		return activeSession;
	}

	public void setActiveSession(int activeSession) {
		if (this.activeSession != activeSession) {
			this.activeSession = activeSession;
			display.asyncExec(new RefreshVariableViewJob());
		}
	}
	
	public synchronized void setActiveSession(GTLDebugSession sess) {
		for (int i = 0; i < sessions.size(); i++) {
			if (sessions.get(i) == sess) {
				this.setActiveSession(i);
				return;
			}
		}
		this.setActiveSession(-1);
	}
	
	public synchronized GTLDebugSession getActiveSessionObj() {
		if (activeSession >= 0 && activeSession < sessions.size()) {
			return sessions.get(activeSession);
		} else {
			return null;
		}
	}

	public int[] getHightlightLines(String file) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		File findFile = new File(file);
		GTLDebugSession as = getActiveSessionObj();
		if (as != null) {
		    Object[] line = as.getCurrentLine();
			if (line != null && findFile.equals(line[0])) {
				list.add((Integer)line[1]);
			}
		}
		Object[] arr = breakpoints.keySet().toArray();
		for (int i = 0; i < arr.length; i++) {
			Breakpoint bp = (Breakpoint)arr[i];
			if (findFile.equals(bp.file)) {
				list.add(bp.lineNo);
			}
		}
		int[] ret = new int[list.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = list.get(i);
		}
		return ret;
	}

	public boolean isActiveLine(String file, int lineNum) {
		File findFile = new File(file);
		GTLDebugSession as = getActiveSessionObj();
		if (as != null) {
			Object[] line = as.getCurrentLine();
			if (line != null && findFile.equals(line[0]) && lineNum == ((Integer)line[1]).intValue()) {
				return true;
			}
		}
		return false;
	}

	public boolean isBreakpoint(String file, int lineNum) {
		Breakpoint bp = new Breakpoint(new File(file), lineNum);
		return breakpoints.containsKey(bp);
	}
	
	public boolean isBreakpoint(File file, int lineNum) {
		Breakpoint bp = new Breakpoint(file, lineNum);
		return breakpoints.containsKey(bp);
	}

	public void toggleBreakpoint(String file, int lineNum) {
		Breakpoint bp = new Breakpoint(new File(file), lineNum);
		boolean add = true;
		if (breakpoints.containsKey(bp)) {
			breakpoints.remove(bp);
			add = false;
		} else {
			breakpoints.put(bp, bp);
		}
		for (int i = 0; i < sessions.size(); i++) {
			int realLine = sessions.get(i).getLineAt(bp.file, bp.lineNo);
			if (realLine != -1) {
				sessions.get(i).notifyBreakPointChanged(realLine, add);
			}
			String[] libs = sessions.get(i).getDebugInfo().libraries;
            for (int j = 0; libs != null && j < libs.length; j++) {
                GTLDebugSession ss = findSession(libs[j]);
                if (ss == null) {
                    continue;
                }
                realLine = ss.getLineAt(bp.file, bp.lineNo);
                if (realLine == -1) {
                    continue;
                }
                int[] info = ss.getBreakpointInfoAt(realLine);
                if (info != null) {
                    info[0] |= (j + 1) << 12;
                    sessions.get(i).notifyBreakPointChanged(info, add);
                }
            }
		}
	}
	
	public synchronized void newSession(Socket sock) {
		try {
			GTLDebugSession session = new GTLDebugSession(sock, this);
			sessions.add(session);
			session.start();
			
			Object[] bps = this.breakpoints.keySet().toArray();
			for (int i = 0; i < bps.length; i++) {
				Breakpoint bp = (Breakpoint)bps[i];
				int line = session.getLineAt(bp.file, bp.lineNo);
				if (line != -1) {
				    session.notifyBreakPointChanged(line, true);
				    continue;
				}
				String[] libs = session.getDebugInfo().libraries;
				for (int j = 0; libs != null && j < libs.length; j++) {
				    GTLDebugSession ss = findSession(libs[j]);
				    if (ss == null) {
				        continue;
				    }
				    line = ss.getLineAt(bp.file, bp.lineNo);
				    if (line == -1) {
				        continue;
				    }
				    int[] info = ss.getBreakpointInfoAt(line);
				    if (info != null) {
				        info[0] |= (j + 1) << 12;
				        session.notifyBreakPointChanged(info, true);
				    }
				}
			}
			
			session.go();
			session.startThread();
		} catch (Exception e) {
			try {
				sock.close();
			} catch (Exception e1) {
			}
			e.printStackTrace();
		}
		display.asyncExec(new RefreshDebugSessionViewJob(null));
	}
	
	public void sessionStatusChanged(GTLDebugSession session, int newState) {
		synchronized (this) {
			if (newState == GTLDebugSession.DEBUG_INIT) {
				// back to init means the session is closed
				for (int i = 0; i < sessions.size(); i++) {
					if (sessions.get(i) == session) {
						sessions.remove(i);
						if (activeSession == i) {
							setActiveSession(-1);
						}
						display.asyncExec(new RefreshDebugSessionViewJob(null));
						return;
					}
				}
			} else if (newState == GTLDebugSession.DEBUG_BREAKED) {
				GTLDebugSession cs = this.getActiveSessionObj();
				if (cs == null || cs.getVMStatus() != GTLDebugSession.DEBUG_BREAKED) {
					// if current session is not in break status, switch to new session
					for (int i = 0; i < sessions.size(); i++) {
						if (sessions.get(i) == session) {
							setActiveSession(i);
							display.asyncExec(new ActivateDebugSessionViewJob());
							break;
						}
					}
				}
			}
		}
		display.asyncExec(new RefreshDebugSessionViewJob(null));
	}
	
	public void callStackChanged(GTLDebugSession session) {
		CallStackItem[] stack = session.getCallStack();
		if (stack != null && stack.length > 0) {
			display.asyncExec(new RefreshDebugSessionViewJob(stack[0]));
		} else {
			display.asyncExec(new RefreshDebugSessionViewJob(null));
		}
	}
	
	public void staticHeapSyncOver(GTLDebugSession session) {
		if (session == getActiveSessionObj()) {
			display.asyncExec(new RefreshVariableViewJob());
		}
	}
	
	public void showAllocTrace(CallStackItem[] items) {
		display.asyncExec(new ShowAllocTraceJob(items));
	}
	
	private class RefreshDebugSessionViewJob implements Runnable {
		private Object focusObj;
		
		public RefreshDebugSessionViewJob(Object newFocus) {
			focusObj = newFocus;
		}
		
		public void run() {
			getDebugSessionView().refresh(focusObj);
		}
	}

   private class ActivateDebugSessionViewJob implements Runnable {
        public void run() {
            getDebugSessionView().activate();
        }
    }

	private class RefreshVariableViewJob implements Runnable {
		public void run() {
			if (getVariableView() != null) {
				getVariableView().refresh();
			}
			if (getMemoryView() != null) {
				getMemoryView().refresh();
			}
		}
	}
	
	private class ShowAllocTraceJob implements Runnable {
		private CallStackItem[] items;
		
		public ShowAllocTraceJob(CallStackItem[] ii) {
			items = ii;
		}
		
		public void run() {
			getMemoryView().showAllocTrace(items);
		}
	}
}
