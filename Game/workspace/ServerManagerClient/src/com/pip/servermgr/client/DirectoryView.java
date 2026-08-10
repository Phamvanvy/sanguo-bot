package com.pip.servermgr.client;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.pip.servermgr.data.Configuration;
import com.pip.servermgr.data.HttpUtils;
import com.pip.servermgr.data.IServerStatusListener;
import com.pip.servermgr.data.Product;
import com.pip.servermgr.data.Server;
import com.pip.servermgr.data.ServerGroup;
import com.pip.servermgr.data.SynchronizeThread;

public class DirectoryView extends ViewPart implements IServerStatusListener {
	TreeViewer treeViewer;
	private Tree tree;
	private Display display;
	private static final String ROOT = "root";
	private Action refreshAction;
	private Action openAllAction;
	private Action startAllAction;
	private Action stopAllAction;
	private Action syncAllAction;
	private boolean disposed = false;
	private static DirectoryView instance;
	
	class TreeContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		
		public void dispose() {}
		
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}
		
		public Object[] getChildren(Object parentElement) {
			if (parentElement == ROOT) {
				return Configuration.products;
			} else if (parentElement instanceof Product) {
				return ((Product)parentElement).servers;
			} else if (parentElement instanceof ServerGroup) {
				return ((ServerGroup)parentElement).servers;
			} else {
				return new Object[0];
			}
		}
		
		public Object getParent(Object element) {
			if (element instanceof Product) {
				return ROOT;
			} else if (element instanceof ServerGroup) {
				return ((ServerGroup)element).parent;
			} else if (element instanceof Server) {
				return ((Server)element).parent;
			} else {
				return null;
			}
		}
		
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}
	}

	class TreeLabelProvider extends LabelProvider {
		public String getText(Object element) {
			String ret = super.getText(element);
			if (element instanceof ServerGroup) {
				if (AsyncExecuteThread.instance.isOwnerExists(element)) {
					ret += "(正在同步)";
				}
			}
			return ret;
		}
		
		public Image getImage(Object element) {
			if (element instanceof Product) {
				return ClientPlugin.getDefault().getImageRegistry().get("product");
			} else if (element instanceof ServerGroup) {
				return ClientPlugin.getDefault().getImageRegistry().get("servergroup");
			} else if (element instanceof Server) {
				Server server = (Server)element;
				if (server.statusTime == -1) {
					return ClientPlugin.getDefault().getImageRegistry().get("server");
				} else if (server.isServerOn()) {
					return ClientPlugin.getDefault().getImageRegistry().get("server_on");
				} else {
					return ClientPlugin.getDefault().getImageRegistry().get("server_off");
				}
			} else {
				return null;
			}
		}
	}
	
	public static final String ID = "com.pip.servermgr.client.directoryview";

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		instance = this;
		treeViewer = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		treeViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				StructuredSelection sel = (StructuredSelection)event.getSelection();
				if (sel.isEmpty()) {
					return;
				}
				Object selObj = sel.getFirstElement();
				ProductFileView fileView = (ProductFileView)getSite().getWorkbenchWindow().getActivePage().findView(ProductFileView.ID);
				if (selObj instanceof Product) {
					fileView.setProduct((Product)selObj);
				} else if (selObj instanceof ServerGroup) {
					fileView.setProduct(((ServerGroup)selObj).parent);
				} else if (selObj instanceof Server) {
					fileView.setProduct(((Server)selObj).parent.parent);
				}
			}
		});
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {
				StructuredSelection sel = (StructuredSelection)event.getSelection();
				if (sel.isEmpty()) {
					return;
				}
				Object selObj = sel.getFirstElement();
				if (selObj instanceof Product) {
					if (treeViewer.getExpandedState(selObj)) {
						treeViewer.collapseToLevel(selObj, 1);
					} else {
						treeViewer.expandToLevel(selObj, 2);
					}
				} else if (selObj instanceof ServerGroup) {
					edit((ServerGroup)selObj);
				} else if (selObj instanceof Server) {
					edit(((Server)selObj).parent);
				}
			}
		});
		treeViewer.setLabelProvider(new TreeLabelProvider());
		tree = treeViewer.getTree();
		treeViewer.setContentProvider(new TreeContentProvider());
		treeViewer.setInput(ROOT);
		initializeToolBar();
		initailizeMenu();
		
		display = getSite().getShell().getDisplay();
		SynchronizeThread.instance.addListener(this);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
	}
	
	private Object[] getSelectedObjects() {
		IStructuredSelection sel = (IStructuredSelection)treeViewer.getSelection();
		return sel.toArray();
	}
	
	private Object[] getSelectedObjects2() {
		IStructuredSelection sel = (IStructuredSelection)treeViewer.getSelection();
		Object[] objs = sel.toArray();
		List<Object> allList = new ArrayList<Object>();
		for (Object obj : objs) {
			allList.add(obj);
		}
		List<Object> ret = new ArrayList<Object>();
		for (Object obj : objs) {
			Object parent = ((TreeContentProvider)treeViewer.getContentProvider()).getParent(obj);
			if (parent == null || allList.indexOf(parent) == -1) {
				ret.add(obj);
			}
		}
		objs = new Object[ret.size()];
		ret.toArray(objs);
		return objs;
	}
	
	/**
	 * 取得所有选中的服务器组。如果一个产品被选中，则下属的所有服务器组都被选中。
	 * @return
	 */
	private ServerGroup[] getSelectedServerGroups() {
		List<ServerGroup> ret = new ArrayList<ServerGroup>();
		Object[] sels = getSelectedObjects();
		for (Object sel : sels) {
			if (sel instanceof ServerGroup) {
				if (ret.indexOf(sel) == -1) {
					ret.add((ServerGroup)sel);
				}
			} else if (sel instanceof Product) {
				Product prod = (Product)sel;
				for (ServerGroup sg : prod.servers) {
					if (ret.indexOf(sg) == -1) {
						ret.add(sg);
					}
				}
			}
		}
		ServerGroup[] ret2 = new ServerGroup[ret.size()];
		ret.toArray(ret2);
		return ret2;
	}
	
	/**
	 * 取得所有选中的服务器。如果一个产品或服务器组被选中，则下属的所有服务器都被选中。
	 * @return
	 */
	private Server[] getSelectedServers() {
		List<Server> ret = new ArrayList<Server>();
		Object[] sels = getSelectedObjects();
		for (Object sel : sels) {
			if (sel instanceof Server) {
				if (ret.indexOf(sel) == -1) {
					ret.add((Server)sel);
				}
			} else if (sel instanceof ServerGroup) {
				ServerGroup sg = (ServerGroup)sel;
				for (Server s : sg.servers) {
					if (ret.indexOf(s) == -1) {
						ret.add(s);
					}
				}
			} else if (sel instanceof Product) {
				Product prod = (Product)sel;
				for (ServerGroup sg : prod.servers) {
					for (Server s : sg.servers) {
						if (ret.indexOf(s) == -1) {
							ret.add(s);
						}
					}
				}
			}
		}
		Server[] ret2 = new Server[ret.size()];
		ret.toArray(ret2);
		return ret2;
	}

	private void initailizeMenu() {
		refreshAction = new Action("刷新(&R)") {
			public void run() {
				Object[] objs = getSelectedObjects2();
				for (Object obj : objs) {
					refresh(obj);
				}
			}
		};
		
		openAllAction = new Action("全部打开(&O)") {
			public void run() {
				for (ServerGroup sg : getSelectedServerGroups()) {
					edit(sg);
				}
			}
		};

		startAllAction = new Action("全部启动(&S)") {
			public void run() {
				final Server[] servers = getSelectedServers();
				StringBuilder sb = new StringBuilder();
				sb.append("是否确定启动下列所有服务器？");
				for (Server server : servers) {
					if (server.statusTime == -1 || !server.canStart()) {
						String msg = server.getFullName() + "当前状态不允许启动，操作取消。";
						MessageDialog.openError(getSite().getShell(), "错误", msg);
						return;
					}
					sb.append("\n" + server.getFullName());
				}
				if (MessageDialog.openConfirm(getSite().getShell(), "全部启动", sb.toString())) {
					new Thread() {
						public void run() {
							for (Server server : servers) {
								updateStatusBar("正在请求启动：" + server.getFullName());
								try {
									HttpUtils.executeShell(server.getShellScript(), "start", false, true);
								} catch (Exception e) {
									e.printStackTrace();
									MessageDialog.openError(getSite().getShell(), "错误", e.toString());
								}
							}
							updateStatusBar("");
						}
					}.start();
				}
			}
		};

		stopAllAction = new Action("全部停止(&T)") {
			public void run() {
				final Server[] servers = getSelectedServers();
				StringBuilder sb = new StringBuilder();
				sb.append("是否确定停止下列所有服务器？");
				for (Server server : servers) {
					if (server.statusTime == -1 || !server.canStop()) {
						String msg = server.getFullName() + "当前状态不允许停止，操作取消。";
						MessageDialog.openError(getSite().getShell(), "错误", msg);
						return;
					}
					sb.append("\n" + server.getFullName());
				}
				if (MessageDialog.openConfirm(getSite().getShell(), "全部停止", sb.toString())) {
					new Thread() {
						public void run() {
							for (Server server : servers) {
								updateStatusBar("正在请求停止：" + server.getFullName());
								try {
									HttpUtils.executeShell(server.getShellScript(), "stop", false, true);
								} catch (Exception e) {
									e.printStackTrace();
									MessageDialog.openError(getSite().getShell(), "错误", e.toString());
								}
							}
							updateStatusBar("");
						}
					}.start();
				}
			}
		};

		syncAllAction = new Action("全部同步(&N)") {
			public void run() {
				ServerGroup[] serverGroups = getSelectedServerGroups();
				StringBuilder sb = new StringBuilder();
				sb.append("是否确定同步下列所有服务器？");
				for (ServerGroup sg : serverGroups) {
					String fullName = sg.parent.name + "-" + sg.name;
					sb.append("\n" + fullName);
				}
				if (MessageDialog.openConfirm(getSite().getShell(), "全部同步", sb.toString())) {
					for (ServerGroup sg : serverGroups) {
						String[] syncToken = new String[] { sg.getPath() + "/data.sh", "upload" };
						AsyncExecuteThread.instance.addRequest(sg, syncToken);
					}
				}
			}
		};

		MenuManager mgr = new MenuManager();
		mgr.add(refreshAction);
		if (Configuration.allowModify) {
			mgr.add(openAllAction);
			mgr.add(startAllAction);
			mgr.add(stopAllAction);
			mgr.add(syncAllAction);
		}
		Menu menu = mgr.createContextMenu(tree);
		tree.setMenu(menu);
	}
	
	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
	}
	
	private void edit(ServerGroup group) {
		try {
			getSite().getWorkbenchWindow().getActivePage().openEditor(new ServerGroupInput(group), ServerGroupEditor.ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void refresh(Object selObj) {
		if (selObj instanceof Product) {
			Product product = (Product)selObj;
			for (int i = 0; i < product.servers.length; i++) {
				refresh(product.servers[i]);
			}
		} else if (selObj instanceof ServerGroup) {
			ServerGroup group = (ServerGroup)selObj;
			for (int i = 0; i < group.servers.length; i++) {
				refresh(group.servers[i]);
			}
		} else if (selObj instanceof Server) {
			SynchronizeThread.instance.sync((Server)selObj, true);
		}
	}

	public void onError(Server server, Exception ex) {
		if (!disposed) {
			display.asyncExec(new ServerStatusHandler(server, ex));
		}
	}

	public void statusChanged(Server server) {
		if (!disposed) {
			display.asyncExec(new ServerStatusHandler(server));
		}
	}
	
	public void statusChanged(ServerGroup serverGroup) {
		if (!disposed) {
			display.asyncExec(new ServerStatusHandler(serverGroup));
		}
	}
	
	private class ServerStatusHandler implements Runnable {
		private Object s;  // Server or ServerGroup
		private Exception e;
		
		public ServerStatusHandler(Object s) {
			this.s = s;
		}
		
		public ServerStatusHandler(Object s, Exception e) {
			this.s = s;
			this.e = e;
		}
		
		public void run() {
			if (e != null) {
				MessageDialog.openError(getSite().getShell(), "刷新错误", s.toString() + ":" + e.toString());
			} else {
				treeViewer.refresh(s);
			}
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		disposed = true;
		SynchronizeThread.instance.removeListener(this);
	}
	
	public void updateStatusBar(final String msg) {
		display.asyncExec(new Runnable() {
			public void run() {
				IStatusLineManager statusLine = DirectoryView.this.getViewSite().getActionBars().getStatusLineManager();
				statusLine.setMessage(msg);
			}
		});
	}
	
	public static void updateStatusBarStatic(String msg) {
		try {
			instance.updateStatusBar(msg);
		} catch (Exception e) {
		}
	}
	
	public void showError(final String error) {
		display.asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(getSite().getShell(), "错误", error);
			}
		});
	}
	
	public static DirectoryView getInstance() {
		return instance;
	}
}
