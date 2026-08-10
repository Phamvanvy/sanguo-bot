package com.pip.image.workshop;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import com.pip.util.FileExtensionFilter;
import com.pip.util.Utils;

public class TileLibView extends ViewPart implements IDoubleClickListener, ISelectionChangedListener {

	public static final String ID = "com.pip.image.workshop.TileLibView"; //$NON-NLS-1$

	private Tree tree;
	private TreeViewer viewer;
	private static FileExtensionFilter imageFilter = new FileExtensionFilter(new String[] { "png", "gif", "pip", "p", "cts", "ldf" }, true);

	private Action refreshAction;
	private Action importAction;
	private Action openAction;
	private Action deleteAction;
	private Action chooseDirAction;
	private FileDialog importDialog;
	
	/**
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */
	static class ViewContentProvider implements ITreeContentProvider {
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}
		
		public Object[] getChildren(Object parentElement) {
			File f = (File)parentElement;
			if (f.isDirectory()) {
				return f.listFiles(imageFilter);
			} else {
				return new Object[0];
			}
		}

	    public Object getParent(Object element) {
	    	return ((File)element).getParentFile();
	    }

	    public boolean hasChildren(Object element) {
    		return ((File)element).isDirectory();
	    }
	    
	    public void dispose() {}

	    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	}


	static class ViewLabelProvider extends LabelProvider {
		public Image getImage(Object element) {
			if (((File)element).isDirectory()) {
				if (((File)element).getParentFile() == null) {
					return WorkshopPlugin.getDefault().getImageRegistry().get("disk");
				} else {
					return WorkshopPlugin.getDefault().getImageRegistry().get("folder");
				}
			} else {
				if (((File)element).getName().toLowerCase().endsWith(".png")) {
					return WorkshopPlugin.getDefault().getImageRegistry().get("systemimage");
				} else if (((File)element).getName().toLowerCase().endsWith(".p")) {
                    return WorkshopPlugin.getDefault().getImageRegistry().get("systemimage");
                } else if (((File)element).getName().toLowerCase().endsWith(".gif")) {
					return WorkshopPlugin.getDefault().getImageRegistry().get("systemimage");
				} else if (((File)element).getName().toLowerCase().endsWith(".pip")) {
					return WorkshopPlugin.getDefault().getImageRegistry().get("image");
				} else if (((File)element).getName().toLowerCase().endsWith(".cts")) {
				    return WorkshopPlugin.getDefault().getImageRegistry().get("animate");
                } else if (((File)element).getName().toLowerCase().endsWith(".ldf")) {
                    return WorkshopPlugin.getDefault().getImageRegistry().get("landform");
				} else {
					return WorkshopPlugin.getDefault().getImageRegistry().get("disk");
				}
			}
		}
	
		public String getText(Object element) {
			File f = (File)element;
			if (f.getParentFile() == null) {
				return f.getAbsolutePath();
			} else {
				return f.getName();
			}
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent);
		tree = viewer.getTree();
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(Settings.tileLibDir);
		viewer.addDoubleClickListener(this);
		viewer.addSelectionChangedListener(this);
		createActions();
		initializeToolBar();
		initializeMenu();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	private File getSelectedFile() {
		IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
		if (sel.isEmpty()) {
			return null;
		}
		return (File)sel.getFirstElement();
	}
	
	public void doubleClick(DoubleClickEvent event) {
		if (event.getViewer() == viewer) {
			File selFile = getSelectedFile();
			if (selFile.isDirectory()) {
				expandOrCollapseNode(selFile);
				return;
			}
			try {
				openFile2(selFile);
			} catch (Exception e) {
				e.printStackTrace();
				MessageDialog.openError(getSite().getShell(), "错误", e.toString());
			}
		}
	}
	
	private void expandOrCollapseNode(Object node) {
		if (viewer.getExpandedState(node)) {
			viewer.collapseToLevel(node, 1);
		} else {
			viewer.expandToLevel(node, 1);
		}
	}
	
	public void selectionChanged(SelectionChangedEvent event) {
		if (event.getSelectionProvider() == viewer) {
			File selFile = getSelectedFile();
			if (selFile == null) {
				viewer.getTree().setMenu(null);
				return;
			}
			MenuManager mgr = new MenuManager();

			if (selFile.isFile()) {
				mgr.add(openAction);
				mgr.add(deleteAction);
			} else {
				mgr.add(importAction);
			}
			mgr.add(refreshAction);
			
			Menu menu = mgr.createContextMenu(viewer.getTree());
			viewer.getTree().setMenu(menu);
			
			try {
				if (selFile.isFile()) {
					openFile(selFile);
				}
			} catch (Exception e) {
				e.printStackTrace();
				MessageDialog.openError(getSite().getShell(), "错误", e.toString());
			}
		}
	}
	private void createActions() {
		deleteAction = new Action("删除") {
			public void run() {
				File selFile = getSelectedFile();
				String msg = "你确定要删除文件" + selFile.getName() + "吗？";
				if (MessageDialog.openConfirm(getSite().getShell(), "确认", msg)) {
					selFile.delete();
					viewer.refresh(selFile.getParentFile());
				}
			}
		};

		openAction = new Action("打开") {
			public void run() {
				File selFile = getSelectedFile();
				try {
					openFile2(selFile);
				} catch (Exception e) {
					e.printStackTrace();
					MessageDialog.openError(getSite().getShell(), "错误", e.toString());
				}
			}
		};

		refreshAction = new Action("刷新") {
			public void run() {
				File selFile = getSelectedFile();
				if (selFile != null) {
					viewer.refresh(selFile);
				}
			}
		};

		importAction = new Action("导入图片") {
			public void run() {
				File rootDir = getSelectedFile();
				if (importDialog == null) {
					importDialog = new FileDialog(getSite().getShell(), SWT.OPEN | SWT.MULTI);
				}
				importDialog.setFilterExtensions(new String[] { "*.png", "*.gif", "*.*" });
				importDialog.setFilterNames(new String[] { "PNG图片文件(*.png)", "GIF图片文件(*.gif)", "所有文件(*.*)" });
				if (importDialog.open() != null) {
					String dir = importDialog.getFilterPath();
					String[] fileNames = importDialog.getFileNames();
					for (int i = 0; i < fileNames.length; i++) {
						File srcFile = new File(dir, fileNames[i]);
						File destFile = new File(rootDir, fileNames[i]);
						if (srcFile.equals(destFile)) {
							continue;
						}
						try {
							Utils.copyFile(srcFile, destFile);
						} catch (Exception e) {
							MessageDialog.openError(getSite().getShell(), "错误", e.toString());
						}
					}
					viewer.refresh(rootDir);
				}
			}
		};
		
		chooseDirAction = new Action("切换目录") {
			public void run() {
				DirectoryDialog dlg = new DirectoryDialog(getSite().getShell());
				dlg.setFilterPath(Settings.tileLibDir.getAbsolutePath());
				dlg.setText("选择目录");
				dlg.setMessage("请选择素材库根目录：");
				String newPath = dlg.open();
				if (newPath != null) {
				    setRootPath(new File(newPath));
				}
			}
		};
	}
	
	public void setRootPath(File path) {
	    Settings.tileLibDir = path;
	    viewer.setInput(path);
	    viewer.refresh();
	}
	
	private void openFile2(File file) throws Exception {
		IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path((file.getAbsolutePath())));
		IDE.openEditorOnFileStore(getSite().getWorkbenchWindow().getActivePage(), fileStore);
	}
	
	private void openFile(File file) throws Exception {
		try {
			getSite().getPage().showView(TileView.ID);
			TileView tv = (TileView)getSite().getPage().findView(TileView.ID);
			tv.openTileFile(file);
		} catch (Exception e) {
			MessageDialog.openError(getSite().getShell(), "错误", e.toString());
		}
	}
	
	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
	}
	
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();

		menuManager.add(chooseDirAction);
	}
}
