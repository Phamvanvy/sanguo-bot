package com.pip.gtl.remotedebugger.ui;

import java.awt.List;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import com.pip.gtl.remotedebugger.CallStackItem;
import com.pip.gtl.remotedebugger.GTLDebugManager;
import com.pip.gtl.remotedebugger.GTLDebugServer;
import com.pip.gtl.remotedebugger.GTLDebugSession;
import com.pip.j0ide.Activator;
import com.pip.j0ide.Application;
import com.pip.j0ide.ConsoleView;
import com.pip.j0ide.editors.GTLEditor;
import com.swtdesigner.ResourceManager;

public class MemoryView extends ViewPart {
	private GTLDebugSession session;
	
	private class NumberSorter extends ViewerSorter {
		private int dir;
		
		public NumberSorter(int dir) {
			this.dir = dir;
		}
		
		public int compare(Viewer viewer, Object e1, Object e2) {
			int n1 = ((Integer)e1).intValue();
			int n2 = ((Integer)e2).intValue();
			if (table.getSortColumn() == refCountColumn) {
				n1 = session.getVM().refCount[n1];
				n2 = session.getVM().refCount[n2];
			} else if (table.getSortColumn() == leakColumn) {
				n1 = isLeak(n1) ? 0 : 1;
				n2 = isLeak(n2) ? 0 : 1;
			}
			if (dir == SWT.UP) {
				if (n1 < n2) {
					return -1;
				} else if (n1 == n2) {
					return 0;
				} else {
					return 1;
				}
			} else {
				if (n1 < n2) {
					return 1;
				} else if (n1 == n2) {
					return 0;
				} else {
					return -1;
				}
			}
		}
	}
	
	class MemoryLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			try {
				if (columnIndex == 0) {
					return "";
				} else if (columnIndex == 1) {
					return element.toString();
				} else if (columnIndex == 2) {
					int addr = ((Integer)element).intValue();
					Object obj = session.getVM().dynamicHeap[addr];
					return VariableView.printObject(obj);
				} else if (columnIndex == 3) {
					int addr = ((Integer)element).intValue();
					int refCount = session.getVM().refCount[addr];
					return String.valueOf(refCount);
				} else {
					return "";
				}
			} catch (Exception e) {
				return "";
			}
		}
		public Image getColumnImage(Object element, int columnIndex) {
			try {
				if (columnIndex == 0) {
					int addr = ((Integer)element).intValue();
					if (session.getVM().useFlag[addr]) {
						return Activator.getDefault().getImageRegistry().get("usedslot");
					}
				} else if (columnIndex == 4) {
					int addr = ((Integer)element).intValue();
					if (isLeak(addr)) {
						return Activator.getDefault().getImageRegistry().get("leakslot");
					}
				}
			} catch (Exception e) {
			}
			return null;
		}
	}
	class MemoryContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			if (inputElement == null || session.getVM().dynamicHeap == null) {
				return new Object[0];
			}
			int len = session.getVM().dynamicHeap.length;
			Object[] ret = new Object[len];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = new Integer(i);
			}
			return ret;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			session = (GTLDebugSession)newInput;
		}
	}
	private Text valueViewer;
	private TableViewer tableViewer;
	private Table table;
	private Action refreshAction;
	private TableColumn currentSortColumn;
	private int currentSortDir;
	public static final String ID = "com.pip.gtl.remotedebugger.ui.MemoryView"; //$NON-NLS-1$

	/**
	 * Create contents of the view part
	 * @param parent
	 */
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());

		final SashForm sashForm = new SashForm(container, SWT.VERTICAL);

		tableViewer = new TableViewer(sashForm, SWT.FULL_SELECTION | SWT.BORDER);
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				Object obj = getSelectedObject();
				if (obj == null) {
				} else {
					int addr = ((Integer)obj).intValue();
					session.queryAllocTrace(addr);
				}
			}
		});
		tableViewer.setLabelProvider(new MemoryLabelProvider());
		tableViewer.setContentProvider(new MemoryContentProvider());
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object obj = getSelectedObject();
				if (obj == null) {
					valueViewer.setText("");
				} else {
					MemoryLabelProvider mlp = (MemoryLabelProvider)tableViewer.getLabelProvider();
					valueViewer.setText(mlp.getColumnText(obj, 2));
				}
			}
		});
		table = tableViewer.getTable();
		table.setHeaderVisible(true);

		final TableColumn useFlagColumn = new TableColumn(table, SWT.NONE);
		useFlagColumn.setAlignment(SWT.CENTER);
		useFlagColumn.setWidth(20);

		addressColumn = new TableColumn(table, SWT.NONE);
		addressColumn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (currentSortColumn == addressColumn) {
					currentSortDir = currentSortDir == SWT.UP ? SWT.DOWN : SWT.UP;
					table.setSortDirection(currentSortDir);
					tableViewer.setSorter(new NumberSorter(currentSortDir));
				} else {
					currentSortColumn = addressColumn;
					table.setSortColumn(addressColumn);
					currentSortDir = SWT.UP;
					table.setSortDirection(currentSortDir);
					tableViewer.setSorter(new NumberSorter(currentSortDir));
				}
			}
		});
		addressColumn.setAlignment(SWT.RIGHT);
		addressColumn.setWidth(45);
		addressColumn.setText("地址");

		final TableColumn valueColumn = new TableColumn(table, SWT.NONE);
		valueColumn.setWidth(210);
		valueColumn.setText("值");

		refCountColumn = new TableColumn(table, SWT.NONE);
		refCountColumn.setWidth(45);
		refCountColumn.setText("引用");
		refCountColumn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (currentSortColumn == refCountColumn) {
					currentSortDir = currentSortDir == SWT.UP ? SWT.DOWN : SWT.UP;
					table.setSortDirection(currentSortDir);
					tableViewer.setSorter(new NumberSorter(currentSortDir));
				} else {
					currentSortColumn = refCountColumn;
					table.setSortColumn(refCountColumn);
					currentSortDir = SWT.UP;
					table.setSortDirection(currentSortDir);
					tableViewer.setSorter(new NumberSorter(currentSortDir));
				}
			}
		});
		
		leakColumn = new TableColumn(table, SWT.NONE);
		leakColumn.setAlignment(SWT.CENTER);
		leakColumn.setWidth(20);
		leakColumn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (currentSortColumn == leakColumn) {
					currentSortDir = currentSortDir == SWT.UP ? SWT.DOWN : SWT.UP;
					table.setSortDirection(currentSortDir);
					tableViewer.setSorter(new NumberSorter(currentSortDir));
				} else {
					currentSortColumn = leakColumn;
					table.setSortColumn(leakColumn);
					currentSortDir = SWT.UP;
					table.setSortDirection(currentSortDir);
					tableViewer.setSorter(new NumberSorter(currentSortDir));
				}
			}
		});
		
		currentSortColumn = addressColumn;
		currentSortDir = SWT.UP;
		table.setSortColumn(addressColumn);
		table.setSortDirection(currentSortDir);
		tableViewer.setSorter(new NumberSorter(currentSortDir));
		
		valueViewer = new Text(sashForm, SWT.MULTI | SWT.BORDER | SWT.WRAP);
		valueViewer.addMouseListener(new MouseListener(){

			public void mouseDoubleClick(MouseEvent arg0) {
				System.out.println(valueViewer.getSelectionText());
				locateFile(valueViewer.getSelectionText());
			}

			public void mouseDown(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void mouseUp(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		sashForm.setWeights(new int[] {3, 1 });
		
		createActions();
		initializeToolBar();
		initializeMenu();
		
		GTLDebugManager dm = GTLDebugServer.getInstance().getDebugManager();
		dm.setMemoryView(this);
	}

	protected void locateFile(String selectionText) {
		String[] sec = selectionText.split(":");
		if(sec.length!=2 || sec[0].endsWith(".gtl")==false){
			return;
		}
		File baseDir = Application.getInstance().getProjectData().getGTLDir();
		File errorFile = searchFile(baseDir, sec[0]);
		if(errorFile == null){
			errorFile = searchFile(Application.getInstance().getProjectData().linkDir, sec[0]);
		}
		if(errorFile == null){
			return;
		}
		int errorLine = Integer.parseInt(sec[1]) - 1;
		try {
			IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path(((File)errorFile).getAbsolutePath()));
			IEditorPart ed = IDE.openEditorOnFileStore(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), fileStore);
			if (ed != null && ed instanceof GTLEditor) {
				if (errorLine < 0) {
					errorLine = 0;
				}
				((GTLEditor)ed).jumpToLine(errorLine);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	private static FilenameFilter gtlFilter = new FilenameFilter(){

		public boolean accept(File dir, String name) {
			return name.endsWith(".gtl") || (dir.isDirectory() && dir.getName().equalsIgnoreCase("cvs")==false);
		}
		
	};
	private TableColumn addressColumn;
	private TableColumn refCountColumn;
	private TableColumn leakColumn;
	
	private File searchFile(File file, String string) {
		File ret = null;
		if(file.isDirectory()){
			for(File dir:file.listFiles(gtlFilter)){
				ret = searchFile(dir, string);
				if(ret != null){
					break;
				}
			}
		}else if(file.isFile()){
			if(file.getName().equalsIgnoreCase(string)){
				ret = file;
			}
//			else{
//				System.out.println(file.getName());	
//			}
		}
		return ret;
	}

	/**
	 * Create the actions
	 */
	private void createActions() {

		refreshAction = new Action("Refresh") {
			public void run() {
				forceRefresh();
			}
		};
		refreshAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/refresh.gif"));
		// Create the actions
	}

	/**
	 * Initialize the toolbar
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();

		toolbarManager.add(refreshAction);
	}

	/**
	 * Initialize the menu
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
	}

	public Object getSelectedObject() {
		IStructuredSelection sel = (IStructuredSelection)tableViewer.getSelection();
		if (sel.isEmpty()) {
			return null;
		}
		return sel.getFirstElement();
	}

	public void setFocus() {
		table.setFocus();
	}

	/*
	 * 强制刷新内存视图，这个操作会重新计算引用计数。
	 */
	private void forceRefresh() {
		GTLDebugManager dm = GTLDebugServer.getInstance().getDebugManager();
		GTLDebugSession session = dm.getActiveSessionObj();
		if (session == null) {
			return;
		}
		if (session.getDebugInfo().languageVersion < 2) {
			// 非全局动态堆，只需要分析当前session的引用计数即可
			session.clearRefCount();
			session.updateRefCount(session);
		} else {
			// 全局动态堆，所有其他session都需要更新
			session.clearRefCount();
			for (GTLDebugSession sess : dm.getSessions()) {
				if (sess.getVMStatus() != GTLDebugSession.DEBUG_BREAKED &&
						sess.getDebugInfo().fileVersion == 0) {
					// 这个session没有暂停，需要更新一次内存数据
					sess.syncState(true);
				}
				sess.updateRefCount(session);
			}
		}
		tableViewer.setInput(dm.getActiveSessionObj());
		tableViewer.refresh();
	}
	
	public void refresh() {
		GTLDebugManager dm = GTLDebugServer.getInstance().getDebugManager();
		tableViewer.setInput(dm.getActiveSessionObj());
		tableViewer.refresh();
	}
	
	public void showAllocTrace(CallStackItem[] items) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < items.length; i++) {
		    if (items[i] == null) {
		        buf.append("unknown");
		    } else {
		        buf.append(items[i].toString());
		    }
			buf.append("\n");
		}
		valueViewer.setText(buf.toString());
	}
	
	/*
	 * 检查一个单元是否泄漏。
	 */
	private boolean isLeak(int addr) {
		if (addr < 32) {
			// 临时对象
			return false;
		}
		if (!session.getVM().useFlag[addr]) {
			return false;
		}
		if (session.getVM().refCount[addr] > 0) {
			return false;
		}
		return true;
	}

	public void forceCheckLeak(GTLDebugSession sess) {
		int len = sess.getVM().dynamicHeap.length;
		java.util.List<Integer> leaks = new ArrayList<Integer>();
		for (int addr = 32; addr < len; addr++) {
			if (!sess.getVM().useFlag[addr]) {
				continue;
			}
			if (sess.getVM().refCount[addr] > 0) {
				continue;
			}
			leaks.add(addr);
		}
		MemoryLeakView.forceShow(leaks);
	}
}
