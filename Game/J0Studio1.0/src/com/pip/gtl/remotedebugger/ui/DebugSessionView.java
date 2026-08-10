package com.pip.gtl.remotedebugger.ui;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.part.ViewPart;

import com.pip.gtl.remotedebugger.*;
import com.pip.j0ide.Activator;
import com.pip.j0ide.Application;
import com.pip.j0ide.editors.EditAndGotoJob;
import com.pip.j0ide.editors.GTLEditor;
import com.swtdesigner.ResourceManager;

public class DebugSessionView extends ViewPart {
	private Action pushETFAction;
	private Action reportAction;
	private Action stepOutAction;
	private Action stepOverAction;
	private Action refreshAction;
	private Action stopAction;
	private Action stepAction;
	private Action pauseAction;
	private Action runAction;
	private IAction stepOutAction2, stepOverAction2, stopAction2, stepAction2, pauseAction2, runAction2;
	
	class DebugSessionTreeContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		private GTLDebugManager debugManager;
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			debugManager = (GTLDebugManager)newInput;
		}
		
		public void dispose() {
		}
		
		public Object[] getElements(Object inputElement) {
			return debugManager.getSessions();
		}
		
		public Object[] getChildren(Object parentElement) {
			GTLDebugSession session = (GTLDebugSession)parentElement;
			return session.getCallStack();
		}
		
		public Object getParent(Object element) {
			if (element instanceof CallStackItem) {
				return ((CallStackItem)element).parent;
			}
			return null;
		}
		
		public boolean hasChildren(Object element) {
			return element instanceof GTLDebugSession;
		}
	}
	
	class DebugSessionTreeLabelProvider extends LabelProvider {
		public String getText(Object element) {
			String name;
			if (element instanceof GTLDebugSession) {
				name = ((GTLDebugSession)element).getDebugInfo().mainFile;
				String lastForderName = "dataui\\gtl";
				int p = name.indexOf(lastForderName);
				if(p>=0){
					name = name.substring(p+lastForderName.length()+1);//有"\", 去掉1位
				}
			} else {
				name = element.toString();
			}
			return name;
		}
		
		public Image getImage(Object element) {
			if (element instanceof GTLDebugSession) {
				return Activator.getDefault().getImageRegistry().get("thread");
			} else if (element instanceof CallStackItem) {
				return Activator.getDefault().getImageRegistry().get("callstackitem");
			} else {
				return null;
			}
		}
	}
	
	private Tree tree;
	private TreeViewer viewer;
	public static final String ID = "com.pip.gtl.remotedebugger.ui.DebugSessionView"; //$NON-NLS-1$

	/**
	 * Create contents of the view part
	 * @param parent
	 */
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());

		viewer = new TreeViewer(container, SWT.BORDER);
		viewer.setContentProvider(new DebugSessionTreeContentProvider());
		viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
		viewer.setLabelProvider(new DebugSessionTreeLabelProvider());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				debugSelectionChanged();
			}
		});
		tree = viewer.getTree();

		createActions();
		initializeToolBar();
		initializeMenu();
		
		GTLDebugManager dm = GTLDebugServer.getInstance().getDebugManager();
		dm.setDebugSessionView(this);
		viewer.setInput(dm);
		viewer.expandAll();
	}

	/**
	 * Create the actions
	 */
	private void createActions() {

		runAction = new Action("Run") {
			public void run() {
				onRun();
			}
		};
		runAction.setEnabled(false);
		runAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/run.gif"));

		pauseAction = new Action("Pause") {
			public void run() {
				onPause();
			}
		};
		pauseAction.setEnabled(false);
		pauseAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/pause.gif"));

		stepAction = new Action("Step") {
			public void run() {
				onStep();
			}
		};
		stepAction.setEnabled(false);
		stepAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/step.gif"));

		stopAction = new Action("Stop") {
			public void run() {
				onStop();
			}
		};
		stopAction.setEnabled(false);
		stopAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/stop.gif"));

		refreshAction = new Action("Refresh") {
			public void run() {
				refresh();
			}
		};
		refreshAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/refresh.gif"));

		stepOverAction = new Action("Step Over") {
			public void run() {
				onStepOver();
			}
		};
		stepOverAction.setEnabled(false);
		stepOverAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/stepover.gif"));

		stepOutAction = new Action("Step Out") {
			public void run() {
				onStepOut();
			}
		};
		stepOutAction.setEnabled(false);
		stepOutAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/stepout.gif"));

        reportAction = new Action("Report", IAction.AS_CHECK_BOX) {
            public void run() {
                onShowReport();
            }
        };
        reportAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/palette.gif"));
        
        pushETFAction = new Action("Reload ETF") {
        	public void run() {
        		onPushETF();
        	}
        };
        pushETFAction.setEnabled(false);
        pushETFAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/pushetf.gif"));
        
		WorkbenchWindow win = (WorkbenchWindow)getSite().getWorkbenchWindow();
		MenuManager menu = win.getMenuManager();
		IContributionItem[] items = menu.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i] instanceof MenuManager && ((MenuManager)items[i]).getMenuText().equals("&Debug")) {
				MenuManager mm = (MenuManager)items[i];
				ActionContributionItem action = (ActionContributionItem)mm.getItems()[3];
				runAction2 = action.getAction();
				action = (ActionContributionItem)mm.getItems()[4];
				stepAction2 = action.getAction();
				action = (ActionContributionItem)mm.getItems()[5];
				stepOverAction2 = action.getAction();
				action = (ActionContributionItem)mm.getItems()[6];
				stepOutAction2 = action.getAction();
				action = (ActionContributionItem)mm.getItems()[7];
				pauseAction2 = action.getAction();
				action = (ActionContributionItem)mm.getItems()[8];
				stopAction2 = action.getAction();
			}
		}
	}

	/**
	 * Initialize the toolbar
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();

		toolbarManager.add(runAction);

		toolbarManager.add(stepAction);

		toolbarManager.add(stepOverAction);

		toolbarManager.add(stepOutAction);

		toolbarManager.add(pauseAction);

		toolbarManager.add(stopAction);

		toolbarManager.add(refreshAction);

		toolbarManager.add(reportAction);
		
		toolbarManager.add(pushETFAction);
	}

	/**
	 * Initialize the menu
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
	}

	public void setFocus() {
		tree.setFocus();
	}

	public void refresh() {
		Object oldSel = getSelectedObject();
		viewer.refresh();
		if (oldSel != null) {
			// restore selection
			setSelectedObject(oldSel);
		}
		debugSelectionChanged();
		IEditorPart activeEditor = getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
		if (activeEditor instanceof GTLEditor) {
			((GTLEditor)activeEditor).refresh();
		}
		viewer.expandAll();
	}
	
	public void refresh(Object newSel) {
		viewer.refresh();
		if (newSel != null) {
		    setSelectedObject(newSel);
		}
		debugSelectionChanged();
		IEditorPart activeEditor = getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
		if (activeEditor instanceof GTLEditor) {
			((GTLEditor)activeEditor).refresh();
		}
		viewer.expandAll();
	}
	
	public Object getSelectedObject() {
		IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
		if (sel.isEmpty()) {
			return null;
		}
		return sel.getFirstElement();
	}
	
	public void setSelectedObject(Object obj) {
		StructuredSelection sel = new StructuredSelection(obj);
		viewer.setSelection(sel, true);
	}
	
	private void debugSelectionChanged() {
		GTLDebugManager dm = GTLDebugServer.getInstance().getDebugManager();
		Object selObj = getSelectedObject();
		if (selObj == null) {
			dm.setActiveSession(-1);
			stopAction.setEnabled(false);
			stepAction.setEnabled(false);
			stepOutAction.setEnabled(false);
			stepOverAction.setEnabled(false);
			pauseAction.setEnabled(false);
			runAction.setEnabled(false);
			stopAction2.setEnabled(false);
			stepAction2.setEnabled(false);
			stepOutAction2.setEnabled(false);
			stepOverAction2.setEnabled(false);
			pauseAction2.setEnabled(false);
			runAction2.setEnabled(false);
			pushETFAction.setEnabled(false);
			return;
		}
		GTLDebugSession session = null;
		if (selObj instanceof GTLDebugSession) {
			session = (GTLDebugSession)selObj;
			dm.setActiveSession(session);
		} else if (selObj instanceof CallStackItem) {
			CallStackItem item = (CallStackItem)selObj;
			dm.setActiveSession(item.parent);
			new EditAndGotoJob(item.file, item.line, false).run();
			session = item.parent;
		}
		session = dm.getActiveSessionObj();
		if (session != null) {
			stopAction.setEnabled(true);
			stepAction.setEnabled(session.getVMStatus() == GTLDebugSession.DEBUG_BREAKED);
			stepOverAction.setEnabled(session.getVMStatus() == GTLDebugSession.DEBUG_BREAKED);
			stepOutAction.setEnabled(session.getVMStatus() == GTLDebugSession.DEBUG_BREAKED);
			pauseAction.setEnabled(session.getVMStatus() == GTLDebugSession.DEBUG_RUNNING);
			runAction.setEnabled(session.getVMStatus() == GTLDebugSession.DEBUG_BREAKED);
			stopAction2.setEnabled(true);
			stepAction2.setEnabled(session.getVMStatus() == GTLDebugSession.DEBUG_BREAKED);
			stepOverAction2.setEnabled(session.getVMStatus() == GTLDebugSession.DEBUG_BREAKED);
			stepOutAction2.setEnabled(session.getVMStatus() == GTLDebugSession.DEBUG_BREAKED);
			pauseAction2.setEnabled(session.getVMStatus() == GTLDebugSession.DEBUG_RUNNING);
			runAction2.setEnabled(session.getVMStatus() == GTLDebugSession.DEBUG_BREAKED);
			pushETFAction.setEnabled(session.getVM().getETFFile() != null);
		} else {
			stopAction.setEnabled(false);
			stepAction.setEnabled(false);
			stepOutAction.setEnabled(false);
			stepOverAction.setEnabled(false);
			pauseAction.setEnabled(false);
			runAction.setEnabled(false);
			stopAction2.setEnabled(false);
			stepAction2.setEnabled(false);
			stepOutAction2.setEnabled(false);
			stepOverAction2.setEnabled(false);
			pauseAction2.setEnabled(false);
			runAction2.setEnabled(false);
			pushETFAction.setEnabled(false);
		}
	}
	
	public void activate() {
	    try {
    	    IWorkbenchWindow window = getSite().getWorkbenchWindow();
    	    window.getActivePage().activate(this);
    	    window.getShell().forceActive();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public void onRun() {
		GTLDebugManager dm = GTLDebugServer.getInstance().getDebugManager();
		GTLDebugSession session = dm.getActiveSessionObj();
		if (session != null) {
			session.go();
		}
		refresh();
	}
	
	public void onPause() {
		GTLDebugManager dm = GTLDebugServer.getInstance().getDebugManager();
		GTLDebugSession session = dm.getActiveSessionObj();
		if (session != null) {
			session.pause();
		}
		refresh();
	}
	
	public void onStep() {
		GTLDebugManager dm = GTLDebugServer.getInstance().getDebugManager();
		GTLDebugSession session = dm.getActiveSessionObj();
		if (session != null) {
			session.step();
		}
		refresh();
	}
	
	public void onStop() {
		GTLDebugManager dm = GTLDebugServer.getInstance().getDebugManager();
		GTLDebugSession session = dm.getActiveSessionObj();
		if (session != null) {
			session.close();
		}
		refresh();
	}
	
	public void onStepOver() {
		GTLDebugManager dm = GTLDebugServer.getInstance().getDebugManager();
		GTLDebugSession session = dm.getActiveSessionObj();
		if (session != null) {
			session.stepOver();
		}
		refresh();
	}
	
	public void onStepOut() {
		GTLDebugManager dm = GTLDebugServer.getInstance().getDebugManager();
		GTLDebugSession session = dm.getActiveSessionObj();
		if (session != null) {
			session.stepOut();
		}
		refresh();
	}
	
	public void onShowReport() {
	    GTLDebugManager dm = GTLDebugServer.getInstance().getDebugManager();
        GTLDebugSession session = dm.getActiveSessionObj();
        if (session != null) {
        	session.toggleFuncReport(reportAction.isChecked());
        	System.out.println("脚本\t调用栈\t执行次数\t总指令数\t最大指令数\t最小指令数");
            Iterator<String> itor = session.funcStatistic.keySet().iterator();
            while (itor.hasNext()) {
                String name = itor.next();
                HashMap<String, int[]> map = session.funcStatistic.get(name);
                Iterator<String> itor2 = map.keySet().iterator();
                while (itor2.hasNext()) {
                    String cs = itor2.next();
                    int[] info = map.get(cs);
                    System.out.println(session.getDebugInfo().taskName + "\t" + cs + "\t" + info[0] + "\t" + info[1] + "\t" + info[2] + "\t" + info[3]);
                }
            }
            session.funcStatistic.clear();
        }
	}
	
	protected void onPushETF() {
		GTLDebugManager dm = GTLDebugServer.getInstance().getDebugManager();
		GTLDebugSession session = dm.getActiveSessionObj();
		if (session != null && session.getVM().getETFFile() != null) {
			session.getVM().reloadETF();
		}
	}
	
	public void locateLastEIP() {
		Object selObj = getSelectedObject();
		if (selObj == null) {
			return;
		}
		GTLDebugSession session = null;
		if (selObj instanceof GTLDebugSession) {
			session = (GTLDebugSession)selObj;
		} else if (selObj instanceof CallStackItem) {
			CallStackItem item = (CallStackItem)selObj;
			session = item.parent;
		}
		if (session != null) {
			Object[] info = session.getCurrentLine2();
			if (info != null) {
				new EditAndGotoJob((File)info[0], ((Integer)info[1]).intValue(), false).run();
			}
		}
	}
	
	public GTLDebugSession getCurrentSession() {
		Object selObj = getSelectedObject();
		if (selObj == null) {
			return null;
		}
		if (selObj instanceof GTLDebugSession) {
			return (GTLDebugSession)selObj;
		} else if (selObj instanceof CallStackItem) {
			CallStackItem item = (CallStackItem)selObj;
			return item.parent;
		} else {
			return null;
		}
	}
}
