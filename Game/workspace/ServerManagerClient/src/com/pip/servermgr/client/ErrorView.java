package com.pip.servermgr.client;

import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.pip.servermgr.data.*;

public class ErrorView extends ViewPart implements IServerStatusListener {
	class ErrorServerTableContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return errorServers.toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class ErrorServerTableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			Server server = (Server)element;
			if (columnIndex == 0) {
				return server.parent.parent.toString();
			} else if (columnIndex == 1) {
				return server.parent.toString();
			} else {
				return server.toString();
			}
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	private TableViewer tableViewer;
	private Table table;
	private Display display;
	private Vector<Server> errorServers = new Vector<Server>();
	private boolean disposed = false;
	

	
	public static final String ID = "com.pip.servermgr.client.ErrorView";

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		tableViewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.BORDER);
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {
				StructuredSelection sel = (StructuredSelection)tableViewer.getSelection();
				if (!sel.isEmpty()) {
					edit((Server)sel.getFirstElement());
				}
			}
		});
		tableViewer.setContentProvider(new ErrorServerTableContentProvider());
		tableViewer.setLabelProvider(new ErrorServerTableLabelProvider());
		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		tableViewer.setInput(this);

		final TableColumn productColumn = new TableColumn(table, SWT.NONE);
		productColumn.setWidth(93);
		productColumn.setText("产品");

		final TableColumn regionColumn = new TableColumn(table, SWT.NONE);
		regionColumn.setWidth(75);
		regionColumn.setText("分区");

		final TableColumn serverColumn = new TableColumn(table, SWT.NONE);
		serverColumn.setWidth(284);
		serverColumn.setText("服务");
		initializeToolBar();

		display = getSite().getShell().getDisplay();
		SynchronizeThread.instance.addListener(this);
		refreshList();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
	}
	
	private void refreshList() {
		errorServers.clear();
		for (Product pro : Configuration.products) {
			for (ServerGroup group : pro.servers) {
				for (Server server : group.servers) {
					if (server.statusTime != -1 && !server.isServerOn()) {
						errorServers.add(server);
					}
				}
			}
		}
		tableViewer.refresh();
	}
	
	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
	}
	
	private void edit(Server server) {
		try {
			getSite().getWorkbenchWindow().getActivePage().openEditor(new ServerGroupInput(server.parent), ServerGroupEditor.ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void onError(Server server, Exception ex) {
		if (!disposed) {
			display.asyncExec(new Runnable() {
				public void run() {
					refreshList();
				}
			});
		}
	}

	public void statusChanged(Server server) {
		if (!disposed) {
			display.asyncExec(new Runnable() {
				public void run() {
					refreshList();
				}
			});
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		disposed = true;
		SynchronizeThread.instance.removeListener(this);
	}
}
