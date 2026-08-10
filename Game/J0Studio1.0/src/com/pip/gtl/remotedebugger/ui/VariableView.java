package com.pip.gtl.remotedebugger.ui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.part.ViewPart;

import com.pip.gtl.decompiler.ETFDebugInfo;
import com.pip.gtl.remotedebugger.*;
import com.pip.j0ide.Activator;
import com.pip.j0ide.Application;
import com.swtdesigner.ResourceManager;

public class VariableView extends ViewPart {
	private Text contentViewer;
	private Tree tree;
	private int treeSortType = SWT.NONE;
	private TreeViewer viewer;
	private int watchSortType = SWT.NONE;
	private Tree watchTree;
	private TreeViewer watchViewer;
	private GTLDebugSession session;
	private VariableContentProvider watchContentProvider;
	
	private class NameSorter extends ViewerSorter {
		private int dir;
		
		public NameSorter(int dir) {
			this.dir = dir;
		}
		
		public int compare(Viewer viewer, Object e1, Object e2) {
			int compValue;
			if (e1 instanceof VariableItem && e2 instanceof VariableItem) {
				String name1 = ((VariableItem)e1).name;
				String name2 = ((VariableItem)e2).name;
				compValue = name1.compareTo(name2);
			} else if (e1 instanceof WatchItem && e2 instanceof WatchItem) {
				String name1 = ((WatchItem)e1).name;
				String name2 = ((WatchItem)e2).name;
				compValue = name1.compareTo(name2);
			} else {
				compValue = 0;
			}
			if (dir == SWT.UP) {
				return compValue;
			} else {
				return -compValue;
			}
		}
	}
	
	private static class WatchItem {
		public String name;
		
		public WatchItem(String n) {
			name = n;
		}
		
		public String toString() {
			return name;
		}
	}
	
	class VariableContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		private Set<String> watchVars = null;
		
		public VariableContentProvider(boolean watch) {
			if (watch) {
				watchVars = new HashSet<String>();
			}
		}
		
		public void addWatch(String var) {
			watchVars.add(var);
		}
		
		public void removeWatch(String var) {
			watchVars.remove(var);
		}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			session = (GTLDebugSession)newInput;
		}
		public void dispose() {
		}
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}
		public Object[] getChildren(Object parentElement) {
			if (parentElement == null) {
				return new Object[0];
			}
			if (parentElement instanceof GTLDebugSession) {
				if (watchVars == null) {
					return ((GTLDebugSession)parentElement).getVariables();
				} else {
					Object[] ret = new Object[watchVars.size()];
					String[] watchNames = new String[watchVars.size()];
					watchVars.toArray(watchNames);
					Arrays.sort(watchNames);
					VariableItem[] allVars = ((GTLDebugSession)parentElement).getVariables();
					for (int i = 0; i < ret.length; i++) {
						ret[i] = new WatchItem(watchNames[i]);
						for (int j = 0; j < allVars.length; j++) {
							if (allVars[j].name.equals(watchNames[i])) {
								ret[i] = allVars[j];
								break;
							}
						}
					}
					return ret;
				}
			} else if (parentElement instanceof VariableItem) {
				VariableItem var = (VariableItem)parentElement;
				Object value = getVariableValue(session, var);
				if (value == null || value instanceof Exception) {
					return new Object[0];
				}
				ETFDebugInfo.StructDef stdef = (ETFDebugInfo.StructDef)session.getDebugInfo().structDefs.get(var.typeName);
				if (stdef != null) {
					if ((var.type & 16) > 0 || var.type == 9 || var.type == 10) {
						// 结构体的数组/Vector/Hashtable
						Object[] arr = (Object[])value;
						int len = arr.length;
						if (var.type == 10) {
							len /= 2;
						}
						VariableItem[] ret = new VariableItem[len];
						for (int i = 0; i < len; i++) {
							ret[i] = new VariableItem();
							ret[i].parent = var;
							ret[i].variableType = VariableItem.TYPE_ARRAYMEMBER;
							if (var.type == 9 || var.type == 10) {
								ret[i].type = 4;
							} else {
								ret[i].type = var.type & 0x0F;
							}
							ret[i].name = "[" + i + "]";
							ret[i].typeName = var.typeName;
							if (var.type == 10) {
								ret[i].address = i * 2 + 1;
							} else {
								ret[i].address = i;
							}
						}
						return ret;
					} else {
						// 普通结构体，这是value应该是一个int[]，查找运行时信息
						try {
							stdef = session.getRuntimeType(stdef, ((int[])value)[0]);
						} catch (Exception e) {
						}
						VariableItem[] ret = new VariableItem[stdef.members.size()];
						for (int i = 0; i < ret.length; i++) {
							ETFDebugInfo.VariableDef member = (ETFDebugInfo.VariableDef)stdef.members.get(i);
							ret[i] = new VariableItem();
							ret[i].parent = var;
							ret[i].variableType = VariableItem.TYPE_MEMBER;
							ret[i].type = member.type;
							ret[i].name = member.name;
							ret[i].typeName = member.typeName;
							ret[i].address = member.address;
						}
						return ret;
					}
				} else if (value instanceof Object[]) {
					Object[] arr = (Object[])value;
					int len = arr.length;
					if (var.type == 10) {
						len /= 2;
					}
					VariableItem[] ret = new VariableItem[len];
					for (int i = 0; i < len; i++) {
						ret[i] = new VariableItem();
						ret[i].parent = var;
						ret[i].variableType = VariableItem.TYPE_ARRAYMEMBER;
						if (var.type == 9 || var.type == 10) {
							ret[i].type = 4;
						} else {
							ret[i].type = var.type & 0x0F;
						}
						ret[i].name = "[" + i + "]";
						ret[i].typeName = var.typeName;
						if (var.type == 10) {
							ret[i].address = i * 2 + 1;
						} else {
							ret[i].address = i;
						}
					}
					return ret;
				} else if (watchVars != null) {
					Object[] ret = new Object[watchVars.size()];
					String[] watchNames = new String[watchVars.size()];
					watchVars.toArray(watchNames);
					Arrays.sort(watchNames);
					for (int i = 0; i < ret.length; i++) {
						ret[i] = new WatchItem(watchNames[i]);
					}
					return ret;
				} else {
					return new Object[0];
				}
			} else {
				return new Object[0];
			}
		}
		public Object getParent(Object element) {
			if (element != null && element instanceof VariableItem) {
				return ((VariableItem)element).parent;
			} else {
				return null;
			}
		}
		public boolean hasChildren(Object element) {
			if (element == null) {
				return false;
			}
			if (element instanceof GTLDebugSession) {
				return true;
			} else if (element instanceof VariableItem) {
				VariableItem var = (VariableItem)element;
				Object value = getVariableValue(session, var);
				if (value == null || value instanceof Exception) {
					return false;
				}
				ETFDebugInfo.StructDef stdef = (ETFDebugInfo.StructDef)session.getDebugInfo().structDefs.get(var.typeName);
				if (stdef != null) {
					return true;
				} else if (value instanceof Object[]) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}
	class VariableNameLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return element.toString();
		}
		public Image getImage(Object element) {
			if (element == null || !(element instanceof VariableItem)) {
				return null;
			}
			VariableItem var = (VariableItem)element;
			switch (var.variableType) {
				//0 - 全局变量, 1 - 参数, 2 - 局部变量, 3 - 成员变量, 4 - 数组成员
			case VariableItem.TYPE_GLOBAL:
				return Activator.getDefault().getImageRegistry().get("globalvariable");
			case VariableItem.TYPE_LOCAL:
			case VariableItem.TYPE_PARAM:
				return Activator.getDefault().getImageRegistry().get("localvariable");
			case VariableItem.TYPE_MEMBER:
				return Activator.getDefault().getImageRegistry().get("member");
			}
			return null;
		}
	}
	class VariableValueLabelProvider extends CellLabelProvider {
		public void update(ViewerCell cell) {
			if (cell.getElement() instanceof VariableItem) {
				VariableItem var = (VariableItem)cell.getElement();
				cell.setText(getVariableString(session, var));
			} else {
				cell.setText("<not available>");
			}
		}
	}
	class VariableValueEditingSupport extends EditingSupport {
		public VariableValueEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}
		
		protected boolean canEdit(Object element) {
			// 判断一个变量是否允许编辑
			if (element instanceof VariableItem) {
				VariableItem var = (VariableItem)element;
				if (var.parent == null) {
					if (var.variableType == VariableItem.TYPE_GLOBAL || var.variableType == VariableItem.TYPE_LOCAL ||
							var.variableType == VariableItem.TYPE_PARAM) {
						// 0 - 全局变量, 1 - 参数, 2 - 局部变量
						if (var.type < 4 || var.type == 11) {
							// 整型或者字符串
							return true;
						}
					}
				} else if (var.variableType == VariableItem.TYPE_MEMBER) {
					// 成员变量
					if (var.type < 4 || var.type == 11) {
						return true;
					}
				} else if (var.variableType == VariableItem.TYPE_ARRAYMEMBER) {
					// 数组元素
					if (var.parent.type == 16 || var.parent.type == 17 || var.parent.type == 18 ||
							var.parent.type == 19 || var.parent.type == 27) {
						return true;
					}
				}
			}
			return false;
		}

		protected CellEditor getCellEditor(Object element) {
			return this.getViewer().getCellEditors()[1];
		}

		protected Object getValue(Object element) {
			if (element instanceof VariableItem) {
				VariableItem var = (VariableItem)element;
				return getVariableString(session, var);
			} else {
				return "";
			}
		}

		protected void setValue(Object element, Object value) {
			if (element instanceof VariableItem) {
				VariableItem var = (VariableItem)element;
				String oldValue = getVariableString(session, var);
				if (oldValue.equals(value)) {
					return;
				}
				if (var.parent == null) {
					if (var.variableType == VariableItem.TYPE_GLOBAL || var.variableType == VariableItem.TYPE_LOCAL ||
							var.variableType == VariableItem.TYPE_PARAM) {
						// 0 - 全局变量, 1 - 参数, 2 - 局部变量
						if (var.type < 4) {
							try {
								modifyInt(var.address, Integer.parseInt((String)value));
							} catch (Exception e) {
							}
						} else if (var.type == 11) {
							modifyString(getVariableIntValue(session, var), (String)value);
						}
					}
				} else if (var.variableType == VariableItem.TYPE_MEMBER) {
					// 成员变量
					if (var.type < 4) {
						try {
							modifyIntMember(getVariableIntValue(session, var.parent), var.address, Integer.parseInt((String)value));
						} catch (Exception e) {
						}
					} else if (var.type == 11) {
						modifyString(getVariableIntValue(session, var), (String)value);
					}
				} else if (var.variableType == VariableItem.TYPE_ARRAYMEMBER) {
					// 数组元素
					if (var.type < 4) {
						try {
							modifyIntMember(getVariableIntValue(session, var.parent), var.address, Integer.parseInt((String)value));
						} catch (Exception e) {
						}
					} else if (var.type == 11) {
						modifyString(getVariableIntValue(session, var), (String)value);
					}
				}
			}
		}
	}
	private Action refreshAction;
	public static final String ID = "com.pip.gtl.remotedebugger.ui.VariableView"; //$NON-NLS-1$
	private TreeViewerColumn varNameColumn;
	private TreeViewerColumn watchNameColumn;

	/**
	 * Create contents of the view part
	 * @param parent
	 */
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());

		final SashForm sashForm = new SashForm(container, SWT.VERTICAL);
		final SashForm sashForm2 = new SashForm(sashForm, SWT.VERTICAL);

		viewer = new TreeViewer(sashForm2, SWT.FULL_SELECTION | SWT.BORDER);
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				Object node = getSelectedObject();
				if (node == null) {
					return;
				}
				if (viewer.getExpandedState(node)) {
					viewer.collapseToLevel(node, 1);
				} else {
					viewer.expandToLevel(node, 1);
				}

			}
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object obj = getSelectedObject();
				if (obj == null) {
					contentViewer.setText("");
				} else {
					VariableItem var = (VariableItem)obj;
					int intValue = getVariableIntValue(session, var);
					String hex = Integer.toHexString(intValue).toUpperCase();
					while (hex.length() < 8) {
						hex = "0" + hex;
					}
					hex = "0x" + hex;
					int addr;
					if (session.getDebugInfo().languageVersion < 2) {
						addr = intValue & 0x1FFF;
					} else {
						addr = intValue & 0xFFFF;
					}
					contentViewer.setText(getVariableIntValue(session, var) + "(" + hex + ")\n" +
							addr + "\n" +
							getVariableString(session, var));
				}
			}
		});
		tree = viewer.getTree();
		tree.setHeaderVisible(true);

		varNameColumn = new TreeViewerColumn(viewer, SWT.NONE);
		TreeViewerColumn valueColumn = new TreeViewerColumn(viewer, SWT.NONE);
		varNameColumn.getColumn().setWidth(125);
		varNameColumn.getColumn().setText("变量名");
		valueColumn.getColumn().setWidth(125);
		valueColumn.getColumn().setText("值");
		varNameColumn.getColumn().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				try {
					if (treeSortType == SWT.NONE) {
						tree.setSortColumn(varNameColumn.getColumn());
						tree.setSortDirection(SWT.UP);
						viewer.setSorter(new NameSorter(SWT.UP));
						treeSortType = SWT.UP;
					} else if (treeSortType == SWT.UP) {
						tree.setSortColumn(varNameColumn.getColumn());
						tree.setSortDirection(SWT.DOWN);
						viewer.setSorter(new NameSorter(SWT.DOWN));
						treeSortType = SWT.DOWN;
					} else if (treeSortType == SWT.DOWN) {
						tree.setSortColumn(null);
						tree.setSortDirection(SWT.NONE);
						viewer.setSorter(null);
						treeSortType = SWT.NONE;
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		});

		viewer.setContentProvider(new VariableContentProvider(false));
		viewer.setLabelProvider(new VariableNameLabelProvider());
		valueColumn.setLabelProvider(new VariableValueLabelProvider());
		valueColumn.setEditingSupport(new VariableValueEditingSupport(viewer));
		TextCellEditor ed = new TextCellEditor(tree, SWT.BORDER);
		viewer.setCellEditors(new CellEditor[] { null, ed });
		
		watchViewer = new TreeViewer(sashForm2, SWT.FULL_SELECTION | SWT.BORDER);
		watchViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				Object node = getSelectedObject2();
				if (node == null) {
					return;
				}
				if (viewer.getExpandedState(node)) {
					viewer.collapseToLevel(node, 1);
				} else {
					viewer.expandToLevel(node, 1);
				}

			}
		});
		watchViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object obj = getSelectedObject2();
				if (obj == null) {
					contentViewer.setText("");
				} else if (obj instanceof VariableItem) {
					VariableItem var = (VariableItem)obj;
					int intValue = getVariableIntValue(session, var);
					String hex = Integer.toHexString(intValue).toUpperCase();
					while (hex.length() < 8) {
						hex = "0" + hex;
					}
					hex = "0x" + hex;
					int addr;
					if (session.getDebugInfo().languageVersion < 2) {
						addr = intValue & 0x1FFF;
					} else {
						addr = intValue & 0xFFFF;
					}
					contentViewer.setText(getVariableIntValue(session, var) + "(" + hex + ")\n" +
							addr + "\n" +
							getVariableString(session, var));
				}
			}
		});
		watchTree = watchViewer.getTree();
		watchTree.setHeaderVisible(true);

		watchNameColumn = new TreeViewerColumn(watchViewer, SWT.NONE);
		valueColumn = new TreeViewerColumn(watchViewer, SWT.NONE);
		watchNameColumn.getColumn().setWidth(125);
		watchNameColumn.getColumn().setText("变量名");
		valueColumn.getColumn().setWidth(125);
		valueColumn.getColumn().setText("值");
		watchNameColumn.getColumn().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				try {
					if (watchSortType == SWT.NONE) {
						watchTree.setSortColumn(watchNameColumn.getColumn());
						watchTree.setSortDirection(SWT.UP);
						watchViewer.setSorter(new NameSorter(SWT.UP));
						watchSortType = SWT.UP;
					} else if (watchSortType == SWT.UP) {
						watchTree.setSortColumn(watchNameColumn.getColumn());
						watchTree.setSortDirection(SWT.DOWN);
						watchViewer.setSorter(new NameSorter(SWT.DOWN));
						watchSortType = SWT.DOWN;
					} else if (watchSortType == SWT.DOWN) {
						watchTree.setSortColumn(null);
						watchTree.setSortDirection(SWT.NONE);
						watchViewer.setSorter(null);
						watchSortType = SWT.NONE;
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		});

		watchContentProvider = new VariableContentProvider(true);
		watchViewer.setContentProvider(watchContentProvider);
		watchViewer.setLabelProvider(new VariableNameLabelProvider());
		valueColumn.setLabelProvider(new VariableValueLabelProvider());
		valueColumn.setEditingSupport(new VariableValueEditingSupport(watchViewer));
		ed = new TextCellEditor(watchTree, SWT.BORDER);
		watchViewer.setCellEditors(new CellEditor[] { null, ed });
		
		contentViewer = new Text(sashForm, SWT.MULTI | SWT.BORDER | SWT.WRAP);
		sashForm2.setWeights(new int[] { 1, 1 });
		sashForm.setWeights(new int[] { 4, 1 });

		createActions();
		initializeToolBar();
		initializeMenu();

		GTLDebugManager dm = GTLDebugServer.getInstance().getDebugManager();
		dm.setVariableView(this);
	}

	/**
	 * Create the actions
	 */
	private void createActions() {

		refreshAction = new Action("Refresh") {
			public void run() {
				refresh();
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
		
		// 创建变量列表菜单
        MenuManager mgr = new MenuManager();
        mgr.add(new Action("Add Watch") {
            public void run() {
                addWatch();
            }
        });
        tree.setMenu(mgr.createContextMenu(tree));	
		
        // 创建监视列表菜单
        mgr = new MenuManager();
        mgr.add(new Action("Remove Watch") {
            public void run() {
                removeWatch();
            }
        });
        watchTree.setMenu(mgr.createContextMenu(watchTree));	
	}

	public void setFocus() {
		tree.setFocus();
	}
	
	public void refresh() {
		GTLDebugManager dm = GTLDebugServer.getInstance().getDebugManager();
		viewer.setInput(dm.getActiveSessionObj());
		viewer.refresh();
		watchViewer.setInput(dm.getActiveSessionObj());
		watchViewer.refresh();
	}

	public Object getSelectedObject() {
		IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
		if (sel.isEmpty()) {
			return null;
		}
		return sel.getFirstElement();
	}
	
	public Object getSelectedObject2() {
		IStructuredSelection sel = (IStructuredSelection)watchViewer.getSelection();
		if (sel.isEmpty()) {
			return null;
		}
		return sel.getFirstElement();
	}
	
	protected void addWatch() {
		Object obj = getSelectedObject();
		if (obj != null && obj instanceof VariableItem) {
			watchContentProvider.addWatch(((VariableItem)obj).name);
			watchViewer.refresh();
		}
	}
	
	public void addWatch(String name) {
		watchContentProvider.addWatch(name);
		watchViewer.refresh();
	}
	
	protected void removeWatch() {
		Object obj = getSelectedObject2();
		if (obj != null) {
			if (obj instanceof VariableItem) {
				watchContentProvider.removeWatch(((VariableItem)obj).name);
				watchViewer.refresh();
			} else if (obj instanceof WatchItem) {
				watchContentProvider.removeWatch(((WatchItem)obj).name);
				watchViewer.refresh();
			}
		}
	}

	public static int getVariableIntValue(GTLDebugSession session, VariableItem item) {
	    if (item.parent == null) {
	        return session.getVM().memLoad(item.address);
	    } else {
	        Object parvar = getVariableValue(session, item.parent);
            if (parvar instanceof Exception || parvar == null) {
                return 0;
            }
            if (item.variableType == VariableItem.TYPE_MEMBER) { // 成员变量
                int value = ((int[])parvar)[item.address & 0x3FFFFFFF];
                return value;
            } else {  // 对象数组成员
            	int addr = getVariableIntValue(session, item.parent);
            	if (session.getDebugInfo().languageVersion >= 2) {
            		// 使用全局堆
            		return addr | (item.address << 16) | 0x20000000;
            	} else {
            		return addr | (item.address << 12) | 0x02000000;
            	}
            }
	    }
	}
	
	public static Object getVariableValue(GTLDebugSession session, VariableItem item) {
		try {
			if (item.parent == null) {
				return session.getVM().getVariableValue(item.address);
			} else {
				Object parvar = getVariableValue(session, item.parent);
				if (parvar instanceof Exception) {
					throw (Exception)parvar;
				}
				if (item.variableType == VariableItem.TYPE_MEMBER) { // 成员变量
					int value = ((int[])parvar)[item.address & 0x3FFFFFFF];
					if ((item.address & 0x40000000) > 0) {
						return session.getVM().followPointer(value);
					} else {
						return String.valueOf(value);
					}
				} else {  // 对象数组成员
					return ((Object[])parvar)[item.address];
				}
			}
		} catch (Exception e) {
			return e;
		}
	}
	
	public static String getVariableString(GTLDebugSession session, VariableItem var) {
		if (var.parent != null && var.parent.type == 10) {
			// Hashtable的值需要特殊处理
			Object[] arr = (Object[])getVariableValue(session, var.parent);
			Object key = arr[var.address - 1];
			Object value = arr[var.address];
			if (var.typeName.length() > 0) {
				// 如果本身是结构，则值不显示
				return printObjectWithQuote(key) + "=" + var.typeName;
			} else {
				return printObjectWithQuote(key) + "=" + printObjectWithQuote(value);
			}
		}
		Object value = getVariableValue(session, var);
		if (value == null) {
			return "null";
		}
		if (value instanceof Exception) {
			return value.toString();
		}
		if (var.type == 9) {
			if (var.typeName.length() > 0) {
				return "Vector<" + var.typeName +">";
			} else {
				return "Vector";
			}
		} else if (var.type == 10) {
			if (var.typeName.length() > 0) {
				return "Hashtable<" + var.typeName + ">";
			} else {
				return "Hashtable";
			}
		} else if (var.type == 25) {
			return "Vector[]";
		} else if (var.type == 26) {
			return "Hashtable[]";
		} else if (var.typeName.length() > 0) {
			if ((var.type & 16) > 0) {
				return var.typeName + "[]";
			} else {
				// 普通结构，查找运行时信息
				try {
					ETFDebugInfo.StructDef st = (ETFDebugInfo.StructDef)session.getDebugInfo().structDefs.get(var.typeName);
					st = session.getRuntimeType(st, ((int[])value)[0]);
					return st.name;
				} catch (Exception e) {
					return var.typeName;
				}
			}
		}
		return printObject(value);
	}
	
	public static String printObjectWithQuote(Object o) {
		if (o == null) {
			return "null";
		}
		if (o instanceof String) {
			return "\"" + printObject(o) + "\"";
		} else {
			return printObject(o);
		}
	}

    public static String printObject(Object o) {
    	if (o == null) {
    		return "null";
    	}
    	if (o instanceof boolean[]) {
    		return printBooleans((boolean[])o);
    	} else if (o instanceof byte[]) {
    		return printBytes((byte[])o);
    	} else if (o instanceof short[]) {
    		return printShorts((short[])o);
    	} else if (o instanceof int[]) {
    		return printInts((int[])o);
    	} else if (o instanceof String) {
    		return (String)o;
    	} else if (o instanceof Object[]) {
    		return printObjects((Object[])o);
    	} else {
    		return o.toString();
    	}
    }

    private static String printBooleans(boolean[] arr) {
    	StringBuffer buf = new StringBuffer();
    	buf.append("{ ");
    	for (int i = 0; i < arr.length; i++) {
    		if (i > 0) {
    			buf.append(",");
    		}
    		buf.append(arr[i]);
    	}
    	buf.append(" }");
    	return buf.toString();
    }

    private static String printBytes(byte[] arr) {
    	StringBuffer buf = new StringBuffer();
    	buf.append("{ ");
    	for (int i = 0; i < arr.length; i++) {
    		if (i > 0) {
    			buf.append(",");
    		}
    		buf.append("0x");
    		buf.append(Integer.toHexString(arr[i] & 0xFF));
    		if (buf.length() > 4000) {
    		    break;
    		}
    	}
    	buf.append(" }");
    	return buf.toString();
    }

    private static String printShorts(short[] arr) {
    	StringBuffer buf = new StringBuffer();
    	buf.append("{ ");
    	for (int i = 0; i < arr.length; i++) {
    		if (i > 0) {
    			buf.append(",");
    		}
    		buf.append(arr[i]);
    	}
    	buf.append(" }");
    	return buf.toString();
    }

    private static String printInts(int[] arr) {
    	StringBuffer buf = new StringBuffer();
    	buf.append("{ ");
    	for (int i = 0; i < arr.length; i++) {
    		if (i > 0) {
    			buf.append(",");
    		}
    		buf.append(arr[i]);
    	}
    	buf.append(" }");
    	return buf.toString();
    }

    private static String printObjects(Object[] arr) {
    	StringBuffer buf = new StringBuffer();
    	buf.append("{ ");
    	for (int i = 0; i < arr.length; i++) {
    		if (i > 0) {
    			buf.append(",");
    		}
    		if (arr[i] instanceof String) {
    			buf.append("\"");
    		}
    		buf.append(printObject(arr[i]));
    		if (arr[i] instanceof String) {
    			buf.append("\"");
    		}
    	}
    	buf.append(" }");
    	return buf.toString();
    }
    
    private void modifyInt(int address, int value) {
    	session.modify(0, address, 0, value, null);
    	session.syncState(false);
    }
    
    private void modifyIntMember(int ptr, int index, int value) {
    	session.modify(1, ptr, index, value, null);
    	session.syncState(false);
    }
    
    private void modifyString(int ptr, String value) {
    	session.modify(2, ptr, 0, 0, value);
    	session.syncState(false);
    }
}
