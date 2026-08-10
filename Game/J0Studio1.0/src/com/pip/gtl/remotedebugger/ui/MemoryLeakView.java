package com.pip.gtl.remotedebugger.ui;


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
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
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
import com.pip.j0ide.editors.GTLEditor;
import com.swtdesigner.ResourceManager;

public class MemoryLeakView extends ViewPart{
	
	public static final String ID = "com.pip.gtl.remotedebugger.ui.MemoryLeakView"; //$NON-NLS-1$
	
	class MemoryLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			try {
				return element.toString();
			} catch (Exception e) {
				return "";
			}
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	class MemoryContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return items.toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	private Text valueViewer;
	private TableViewer tableViewer;
	private Table table;
	
	private TableColumn addressColumn;
	
	private java.util.List<Integer> items = new ArrayList<Integer>();
	
	/**
	 * create contents of the view part
	 * @param parent
	 */
	public void createPartControl(Composite parent) {
		//页面总的container
		Composite pageContainer = new Composite(parent, SWT.NONE);
		pageContainer.setLayout(new FillLayout(SWT.VERTICAL));
		
		Composite container = new Composite(pageContainer, SWT.NONE);
		container.setLayout(new FillLayout());
		
		final SashForm sashForm = new SashForm(container, SWT.VERTICAL);
		
		tableViewer = new TableViewer(sashForm,SWT.FULL_SELECTION | SWT.BORDER);
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent arg0) {
				Object obj = getSelectedObject();
				if(obj == null){
					
				}else{
					int addr = ((Integer) obj).intValue();
					GTLDebugManager dm = GTLDebugServer.getInstance().getDebugManager();
					if (dm.getSessions().length == 0) {
						return;
					}
					dm.getSessions()[0].queryAllocTrace(addr);
				}
			}
		});
		
		tableViewer.setLabelProvider(new MemoryLabelProvider());
		tableViewer.setContentProvider(new MemoryContentProvider());
		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		
		addressColumn = new TableColumn(table, SWT.NONE);
		addressColumn.setAlignment(SWT.RIGHT);
		addressColumn.setWidth(45);
		addressColumn.setText("地址");
		
		valueViewer = new Text(sashForm, SWT.MULTI | SWT.BORDER | SWT.WRAP);
		valueViewer.addMouseListener(new MouseListener(){

			public void mouseDoubleClick(MouseEvent arg0) {
				System.out.println(valueViewer.getSelectionText());
				locateFile(valueViewer.getSelectionText());
			}

			public void mouseDown(MouseEvent arg0) {
			}

			public void mouseUp(MouseEvent arg0) {
			}
			
		});
		
		sashForm.setWeights(new int[] {3, 1 });
		
		createActions();
		initializeToolBar();
		initializeMenu();
		
		GTLDebugManager dm = GTLDebugServer.getInstance().getDebugManager();
		dm.setMemoryLeakView(this);
	}

	@Override
	public void setFocus() {
		table.setFocus();
	}

	public void createActions(){
	}
	
	/**
	 * Initialize the toolbar
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
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
	
	private static FilenameFilter gtlFilter = new FilenameFilter(){

		public boolean accept(File dir, String name) {
			return name.endsWith(".gtl") || (dir.isDirectory() && dir.getName().equalsIgnoreCase("cvs")==false);
		}
		
	};
	
	public void refresh() {
		tableViewer.setInput(this);
		tableViewer.refresh();
	}
	/**
	 * 这里需要修改
	 * @param items
	 */
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
	
	public void setItems(java.util.List<Integer> items) {
		this.items = items;
		refresh();
	}
	
	public static void forceShow(java.util.List<Integer> items) {
		GTLDebugServer.getInstance().getDebugManager().getDisplay().
			asyncExec(new ForceShowJob(items));
	}
	
	private static class ForceShowJob implements Runnable {
		private java.util.List<Integer> items;
		
		public ForceShowJob(java.util.List<Integer> items) {
			this.items = items;
		}
		
		public void run() {
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(MemoryLeakView.ID);
				MemoryLeakView mview = (MemoryLeakView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(MemoryLeakView.ID);
				mview.setItems(items);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
