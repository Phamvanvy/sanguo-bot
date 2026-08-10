package com.pip.image.workshop;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import sun.management.FileSystem;

import com.pip.image.workshop.editor.AnimateEditor;
import com.pip.image.workshop.editor.BodyDef;
import com.pip.image.workshop.editor.ImageEditor;
import com.pip.mapeditor.NewCellMapDialog;
import com.pip.mapeditor.data.CellMap;
import com.pip.mapeditor.data.MapFile;
import com.pip.util.FileExtensionFilter;
import com.pip.util.FileRenamer;
import com.pip.util.Utils;
import com.pipimage.image.EquipHookMap;
import com.pipimage.png.PngEncoder;

public class DirectoryView extends ViewPart implements IDoubleClickListener, ISelectionChangedListener {
	public static final String ID = "com.pip.image.workshop.directoryview";

	private Tree tree;
	private TreeViewer viewer;
	private static FileExtensionFilter imageFilter = new FileExtensionFilter(new String[] { "pip", "cts", "png", "gif", "map", "p", "cm", "ldf", "lfi", "hk", "eqp" }, true);

	private Action newAction;
	private Action newAnimateAction;
	private Action newMapAction;
	private Action newLandformAction;
	private Action newCellMapAction;
	private Action refreshAction;
	private Action setTileLibDirAction;
	private Action exploreAction;
	private Action editAction;
	private Action deleteAction;
	private Action renameFileAction;
	private Action newLibModeMapAction;
	private Action addBookmarkAction;
	private ArrayList<Action> bookmarks;

	private IAction newBodyAction;

	private IAction newEquipAction;

	private IAction equipPreViewAction;
	private IAction adjustEqpAction;
	
	private class GotoBookmarkAction extends Action {
		private File directory;
		
		public GotoBookmarkAction(File di) {
			super(di.getAbsolutePath());
			directory = di;
		}
		
		public void run() {
			if (directory.exists() && directory.isDirectory()) {
				viewer.expandToLevel(directory, 1);
				StructuredSelection sel = new StructuredSelection(directory);
				viewer.setSelection(sel);
			}
		}
	}

	/**
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */
	static class ViewContentProvider implements ITreeContentProvider {
		public Object[] getElements(Object inputElement) {
			return File.listRoots();
		}
		
		public Object[] getChildren(Object parentElement) {
			File f = (File)parentElement;
			if (f.isDirectory()) {
			    File[] tmp = f.listFiles(imageFilter);
			    Arrays.sort(tmp);
				return tmp;
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
				if (((File)element).getName().toLowerCase().endsWith(".pip")) {
					return WorkshopPlugin.getDefault().getImageRegistry().get("image");
				} else if (((File)element).getName().toLowerCase().endsWith(".cts")) {
					return WorkshopPlugin.getDefault().getImageRegistry().get("animate");
				} else if (((File)element).getName().toLowerCase().endsWith(".gif")) {
					return WorkshopPlugin.getDefault().getImageRegistry().get("systemimage");
				} else if (((File)element).getName().toLowerCase().endsWith(".png")) {
					return WorkshopPlugin.getDefault().getImageRegistry().get("systemimage");
				} else if (((File)element).getName().toLowerCase().endsWith(".p")) {
                    return WorkshopPlugin.getDefault().getImageRegistry().get("systemimage");
                } else if (((File)element).getName().toLowerCase().endsWith(".map")) {
					return WorkshopPlugin.getDefault().getImageRegistry().get("map");
				} else if (((File)element).getName().toLowerCase().endsWith(".cm")) {
                    return WorkshopPlugin.getDefault().getImageRegistry().get("cellmap");
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
		viewer.setInput(File.listRoots());
		viewer.addDoubleClickListener(this);
		viewer.addSelectionChangedListener(this);
		createActions();
		initializeToolBar();
		initializeMenu();
		
		if (Settings.workingDir.exists() && Settings.workingDir.isDirectory()) {
			viewer.expandToLevel(Settings.workingDir, 1);
			StructuredSelection sel = new StructuredSelection(Settings.workingDir);
			viewer.setSelection(sel);
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	private Object getSelectedObject() {
		IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
		if (sel.isEmpty()) {
			return null;
		}
		return sel.getFirstElement();
	}
	
	public void doubleClick(DoubleClickEvent event) {
		if (event.getViewer() == viewer) {
			IStructuredSelection sel = (IStructuredSelection)event.getSelection();
			if (sel.isEmpty()) {
				return;
			}
			Object selObj = sel.getFirstElement();
			if (((File)selObj).isDirectory()) {
				expandOrCollapseNode(selObj);
				return;
			}
			try {
				openFile((File)selObj);
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
			IStructuredSelection sel = (IStructuredSelection)event.getSelection();
			if (sel.isEmpty()) {
				viewer.getTree().setMenu(null);
				return;
			}
			Object selObj = sel.getFirstElement();
			MenuManager mgr = new MenuManager();

			File f = (File)selObj;
			if (f.isFile()) {
				mgr.add(editAction);
				mgr.add(deleteAction);
				mgr.add(renameFileAction);
				if(f.getName().endsWith(".cts")){
					mgr.add(newBodyAction);
					mgr.add(newEquipAction);
				}else if(f.getName().endsWith(".hk")){
					mgr.add(equipPreViewAction);
				} else if(f.getName().endsWith(".eqp")) {
					mgr.add(adjustEqpAction);
				}
			} else {
				mgr.add(newAction);
				mgr.add(newAnimateAction);
				mgr.add(newLibModeMapAction);
				mgr.add(newMapAction);
				mgr.add(newLandformAction);
				mgr.add(newCellMapAction);
				mgr.add(setTileLibDirAction);
				Settings.workingDir = f;
			}
			mgr.add(exploreAction);
			mgr.add(refreshAction);
			
			Menu menu = mgr.createContextMenu(viewer.getTree());
			viewer.getTree().setMenu(menu);
		}
	}
	private void createActions() {
		newBodyAction = new Action("新建素体文件"){
			public void run() {
				Object selObj = getSelectedObject();
				File f = (File)selObj;
				File bodyFile = newBodyFile(f);
				if(bodyFile == null){
					return;
				}
				try{
					openFile(bodyFile);
				}catch(Exception e){
					MessageDialog.openError(getSite().getShell(), "错误", e.toString());
					e.printStackTrace();
				}
				viewer.refresh(f.getParentFile());
			}
		};
		newEquipAction = new Action("新建装配文件"){
			public void run() {
				Object selObj = getSelectedObject();
				File f = (File)selObj;
				File eqpFile = newEquipFile(f);
				if(eqpFile != null){
					try{
						openFile(eqpFile);
					}catch(Exception e){
						MessageDialog.openError(getSite().getShell(), "错误", e.toString());
						e.printStackTrace();
					}
					viewer.refresh(f.getParentFile());
				}
			}
		};
		equipPreViewAction = new Action("装配预览"){
			public void run(){
				Object selObj = getSelectedObject();
				File f = (File)selObj;
				File bodyFile = new File(f.getParentFile(), f.getName().replace(".hk", ".pre"));
				if(bodyFile != null){
					try{
						openFile(bodyFile);
					}catch(Exception e){
						MessageDialog.openError(getSite().getShell(), "错误", e.toString());
						e.printStackTrace();
					}
				}
			}
		};
		
		adjustEqpAction = new Action("校正装备文件"){
			public void run(){
				try{
					adjustEqp();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		};
		
		deleteAction = new Action("删除") {
			public void run() {
				Object selObj = getSelectedObject();
				File f = (File)selObj;
				String msg = "你确定要删除文件" + f.getName() + "吗？";
				if (MessageDialog.openConfirm(getSite().getShell(), "确认", msg)) {
					stopEdit(selObj);
					f.delete();
					// 如果是CTS文件，删除伴生的CTN文件
					if (f.getName().toLowerCase().endsWith(".cts")) {
						String ctnFile = f.getAbsolutePath();
						ctnFile = ctnFile.substring(0, ctnFile.length() - 1) + "n";
						new File(ctnFile).delete();
					} else if(f.getName().toLowerCase().endsWith(".hk")) {
						String hkFile = f.getAbsolutePath() + "c";
						new File(hkFile).delete();
					} else if(f.getName().toLowerCase().endsWith(".eqp")) {
						String eqpFile = f.getAbsolutePath() + "c";
						new File(eqpFile).delete();
					}
					viewer.refresh(f.getParentFile());
				}
			}
		};
		renameFileAction = new Action("重命名"){
			public void run(){
				final File file = (File)getSelectedObject();
				final String defaultName = (file).getName();
				
				if(supportRename(defaultName)==false){
					MessageDialog.openInformation(getSite().getShell(), "提示", "不支持的文件类型(支持pip,cts,hk,eqp)");
					return;
				}
				int openedEditorCnt = getSite().getWorkbenchWindow().getActivePage().getEditorReferences().length;
				if(openedEditorCnt>0){
					MessageDialog.openInformation(getSite().getShell(), "提示", "文件重命名可能会影响多个文件,请先关闭所有打开的编辑界面(快捷键Ctr+shift+w)");
					return;
				}else{
					InputDialog dlg = new InputDialog(getSite().getShell(), "输入", "请输入新文件名:", defaultName, new IInputValidator() {
						public String isValid(String newText) {
							if (newText.trim().length() == 0) {
								return "文件名不能为空。";
							} else {
								String toFullPath = file.getParent()+File.separator+newText.trim();
								if(new File(toFullPath).exists()){
									return "文件已存在";
								}
								return null;
							}
						}
					});
					if (dlg.open() == InputDialog.OK) {					
						String newName = dlg.getValue().trim();
						if(supportRename(newName)==false){
							MessageDialog.openInformation(getSite().getShell(), "提示", "不支持的文件类型(支持pip,cts,hk,eqp)");
							return;
						}
						if(newName.equals(defaultName)){
							return;
						}
						String fromFullPath = file.getAbsolutePath();
						String toFullPath = file.getParent()+File.separator+newName;
						boolean ret = FileRenamer.rename(fromFullPath, toFullPath);
						if(ret){
							viewer.refresh(file.getParentFile());
						}else{
							MessageDialog.openInformation(getSite().getShell(), "提示", "修改失败!");
						}
					}
				}
			}

			private boolean supportRename(String defaultName) {
				for(String type:new String[]{".cts", ".pip", ".hk", ".eqp"}){
					if(defaultName.endsWith(type)){
						return true;
					}
				}
				return false;
			}
		};

		editAction = new Action("编辑") {
			public void run() {
				Object selObj = getSelectedObject();
				try {
					openFile((File)selObj);
				} catch (Exception e) {
					e.printStackTrace();
					MessageDialog.openError(getSite().getShell(), "错误", e.toString());
				}
			}
		};

		refreshAction = new Action("刷新") {
			public void run() {
				Object selObj = getSelectedObject();
				if (selObj != null) {
					viewer.refresh(selObj);
				}
			}
		};
		
		setTileLibDirAction = new Action("设置为贴图素材库目录") {
		    public void run() {
		        Object selObj = getSelectedObject();
                if (selObj != null && selObj instanceof File && ((File)selObj).isDirectory()) {
                    Settings.tileLibDir = (File)selObj;
                    TileLibView tlv = (TileLibView)getSite().getWorkbenchWindow().getActivePage().findView(TileLibView.ID);
                    if (tlv != null) {
                        tlv.setRootPath(Settings.tileLibDir);
                    }
                }
		    }
		};
		
		exploreAction = new Action("浏览...") {
		    public void run() {
		        Object selObj = getSelectedObject();
                if (selObj != null && selObj instanceof File) {
                	File f = (File)selObj;
                	if(f.isFile()){
                		f = f.getParentFile();
                	}
                    String cmd = "explorer.exe \"" + f.getAbsolutePath() + "\"";
                    try {
                        Runtime.getRuntime().exec(cmd);
                    } catch (Exception e) {
                    }
                }
		    }
		};

		newAction = new Action("新建图片") {
			public void run() {
				Object selObj = getSelectedObject();
				File file = newImage((File)selObj);
				viewer.refresh(selObj);
				try {
					if (file != null) {
						openFile(file);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		newAnimateAction = new Action("新建动画") {
			public void run() {
				Object selObj = getSelectedObject();
				File file = newAnimate((File)selObj);
				viewer.refresh(selObj);
				try {
					if (file != null) {
						openFile(file);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		newMapAction = new Action("新建地图") {
			public void run() {
				Object selObj = getSelectedObject();
				File file = newMap((File)selObj);
				viewer.refresh(selObj);
				try {
					if (file != null) {
						openFile(file);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		newLibModeMapAction = new Action("新建地图(库模式)") {
			public void run() {
				Object selObj = getSelectedObject();
				File file = newMap((File)selObj,true);
				viewer.refresh(selObj);
				try {
					if (file != null) {
						openFile(file);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
        newLandformAction = new Action("新建地形") {
            public void run() {
                Object selObj = getSelectedObject();
                File file = newLandform((File)selObj);
                viewer.refresh(selObj);
                try {
                    if (file != null) {
                        openFile(file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
		
		newCellMapAction = new Action("新建格点地图") {
            public void run() {
                Object selObj = getSelectedObject();
                File file = newCellMap((File)selObj);
                viewer.refresh(selObj);
                try {
                    if (file != null) {
                        openFile(file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

		addBookmarkAction = new Action("添加书签") {
			public void run() {
				File selFile = (File)getSelectedObject();
				if (selFile.exists() && selFile.isDirectory() && !Settings.bookmarks.contains(selFile)) {
					Settings.bookmarks.add(selFile);
					IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
					Action action = new GotoBookmarkAction(selFile);
					menuManager.add(action);
				}
			}
		};

	}
	
	protected File newEquipFile(File ctsFile) {
		String defaultName = ctsFile.getName().replace(".cts", ".eqp");
		InputDialog dlg = new InputDialog(getSite().getShell(), "新建装配文件", "请输入装配文件名：", defaultName, new IInputValidator() {
			public String isValid(String newText) {
				if (newText.trim().length() == 0) {
					return "文件名不能为空。";
				} else {
					return null;
				}
			}
		});
		if (dlg.open() == InputDialog.OK) {
			String newname = dlg.getValue();
			if (!newname.toLowerCase().endsWith(".eqp")) {
				newname += ".eqp";
			}
			File newfile = new File(ctsFile.getParentFile(), newname);
			try {
				if (!newfile.createNewFile()) {
					MessageDialog.openError(getSite().getShell(), "错误", newname + "已经存在了。");
					return null;
				}
				EquipHookMap eqp2hk = new EquipHookMap();
				eqp2hk.setEquipCtsName(ctsFile.getName());
				eqp2hk.save(newfile.getAbsolutePath(), null, null);
				return newfile;
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), "错误", "创建文件" + newname + "失败。");
				e.printStackTrace();
			}
		}
		return null;
	}

	protected File newBodyFile(File ctsFile) {
		String defaultName = ctsFile.getName().replace(".cts", ".hk");
		InputDialog dlg = new InputDialog(getSite().getShell(), "新建素体文件", "请输入素体文件名：", defaultName, new IInputValidator() {
			public String isValid(String newText) {
				if (newText.trim().length() == 0) {
					return "文件名不能为空。";
				} else {
					return null;
				}
			}
		});
		if (dlg.open() == InputDialog.OK) {
			String newname = dlg.getValue();
			if (!newname.toLowerCase().endsWith(".hk")) {
				newname += ".hk";
			}
			File newfile = new File(ctsFile.getParentFile(), newname);
			try {
				if (!newfile.createNewFile()) {
					MessageDialog.openError(getSite().getShell(), "错误", newname + "已经存在了。");
					return null;
				}
				BodyDef bodyDef = new BodyDef();
				bodyDef.ctsFile = ctsFile.getName();
				bodyDef.save(null, newfile.getAbsolutePath());
				return newfile;
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), "错误", "创建文件" + newname + "失败。");
			}
		}
		return null;
	}

	private void stopEdit(Object obj) {
        IEditorPart editor = null;
        IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path((((File)obj)).getAbsolutePath()));
        FileStoreEditorInput input = new FileStoreEditorInput(fileStore);
        editor = getSite().getWorkbenchWindow().getActivePage().findEditor(input);
        if (editor != null) {
            getSite().getWorkbenchWindow().getActivePage().closeEditor(editor, false);
        }
	}
	
	private File newImage(File parent) {
		InputDialog dlg = new InputDialog(getSite().getShell(), "新建图片", "请输入图片文件名：", "newimage", new IInputValidator() {
			public String isValid(String newText) {
				if (newText.trim().length() == 0) {
					return "文件名不能为空。";
				} else {
					return null;
				}
			}
		});
		if (dlg.open() == InputDialog.OK) {
			String newname = dlg.getValue();
			if (!newname.toLowerCase().endsWith(".pip")) {
				newname += ".pip";
			}
			File newfile = new File(parent, newname);
			try {
				if (!newfile.createNewFile()) {
					MessageDialog.openError(getSite().getShell(), "错误", newname + "已经存在了。");
				}
				return newfile;
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), "错误", "创建文件" + newname + "失败。");
			}
		}
		return null;
	}

    private File newLandform(File parent) {
        InputDialog dlg = new InputDialog(getSite().getShell(), "新建地形", "请输入地形文件名：", "newlandform", new IInputValidator() {
            public String isValid(String newText) {
                if (newText.trim().length() == 0) {
                    return "文件名不能为空。";
                } else {
                    return null;
                }
            }
        });
        if (dlg.open() == InputDialog.OK) {
            String newname = dlg.getValue();
            if (!newname.toLowerCase().endsWith(".ldf")) {
                newname += ".ldf";
            }
            File newfile = new File(parent, newname);
            try {
                if (!newfile.createNewFile()) {
                    MessageDialog.openError(getSite().getShell(), "错误", newname + "已经存在了。");
                }
                return newfile;
            } catch (Exception e) {
                MessageDialog.openError(getSite().getShell(), "错误", "创建文件" + newname + "失败。");
            }
        }
        return null;
    }

    private File newAnimate(File parent) {
		InputDialog dlg = new InputDialog(getSite().getShell(), "新建动画", "请输入动画文件名：", "newanimate", new IInputValidator() {
			public String isValid(String newText) {
				if (newText.trim().length() == 0) {
					return "文件名不能为空。";
				} else {
					return null;
				}
			}
		});
		if (dlg.open() == InputDialog.OK) {
			String newname = dlg.getValue();
			if (!newname.toLowerCase().endsWith(".cts")) {
				newname += ".cts";
			}
			File newfile = new File(parent, newname);
			try {
				if (!newfile.createNewFile()) {
					MessageDialog.openError(getSite().getShell(), "错误", newname + "已经存在了。");
				}
				return newfile;
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), "错误", "创建文件" + newname + "失败。");
			}
		}
		return null;
	}
    private File newMap(File parent) {
    	return newMap(parent, false);
    }
	private File newMap(File parent,boolean libmode) {
	    InputDialog dlg = new InputDialog(getSite().getShell(), "新建地图", "请输入地图文件名：", "newmap", new IInputValidator() {
            public String isValid(String newText) {
                if (newText.trim().length() == 0) {
                    return "文件名不能为空。";
                } else {
                    return null;
                }
            }
        });
        if (dlg.open() == InputDialog.OK) {
            String newname = dlg.getValue();
            if (!newname.toLowerCase().endsWith(".map")) {
                newname += ".map";
            }
            
			try {
				File newfile = new File(parent, newname);
				if (!newfile.createNewFile()) {
					MessageDialog.openError(getSite().getShell(), "错误", newname + "已经存在了。");
				}
				MapFile map = new MapFile();
				if(libmode){
					map.isLibMode = true;
				}
				map.save(newfile);
				return newfile;
			} catch (Exception e) {
				e.printStackTrace();
				MessageDialog.openError(getSite().getShell(), "错误", "创建文件" + newname + "失败。\n"+e.toString());
			}
		}
		return null;
	}
	
	private File newCellMap(File parent) {
        NewCellMapDialog dlg = new NewCellMapDialog(getSite().getShell());
        if (dlg.open() == NewCellMapDialog.OK) {
            String newname = dlg.getFileName();
            if (!newname.toLowerCase().endsWith(".cm")) {
                newname += ".cm";
            }
            try {
                File newfile = new File(parent, newname);
                if (!newfile.createNewFile()) {
                    MessageDialog.openError(getSite().getShell(), "错误", newname + "已经存在了。");
                }
                CellMap map = new CellMap(dlg.getMapWidth(), dlg.getMapHeight(), dlg.getCellDepth());
                map.save(newfile);
                return newfile;
            } catch (Exception e) {
                MessageDialog.openError(getSite().getShell(), "错误", "创建文件" + newname + "失败。");
            }
        }
        return null;
    }
	
	private void openFile(File file) throws Exception {
		String path = file.getAbsolutePath();
		IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path((file.getAbsolutePath())));
		IDE.openEditorOnFileStore(getSite().getWorkbenchWindow().getActivePage(), fileStore);
	}
	
	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
	}
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();

		menuManager.add(addBookmarkAction);
		for (int i = 0; i < Settings.bookmarks.size(); i++) {
			Action action = new GotoBookmarkAction(Settings.bookmarks.get(i));
			menuManager.add(action);
		}
	}

	public void adjustEqp() {
		Object el = getSelectedObject();
		if (el instanceof File) {
			adjustEqp(((File)el).getAbsolutePath(), false);
		}
	}
	public void adjustEqp(String equipFilePath, boolean silent){
		File f = new File(equipFilePath);
			if (!f.isFile()) {
				if(!silent)
				MessageDialog.openError(getSite().getShell(), "自动校正错误", "请在目录中选择一个装配文件。");
				return;
			}
			if (!f.getName().endsWith(".eqp")) {
				if(!silent)
				MessageDialog.openError(getSite().getShell(), "自动校正错误", "请在目录中选择一个装配文件。");
				return;
			}
			
			try {
				FileInputStream fis = new FileInputStream(f);
				DataInputStream dis = new DataInputStream(fis);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(bos);
				String prefix = "../../../";
				if(f.getParentFile().getName().matches("\\d+")){
					prefix = "../../";
				}
				try {
					String equipCtsName = dis.readUTF();
					dos.writeUTF("equ.cts");
					int cnt = dis.readByte();
					dos.writeByte(cnt);
					for (int i = 0; i < cnt; i++) {
						String hkName = dis.readUTF();
						dos.writeUTF(prefix + "atk.hk");
						int hookId = dis.readByte();
						dos.writeByte(hookId);
						int frameCnt = dis.readShort();
						dos.writeShort(frameCnt);
						for (int j = 0; j < frameCnt; j++) {
							int mid = dis.readByte();
							dos.writeByte(mid);
						}
					}
				} finally {
					dis.close();
					dos.flush();
				}
				Utils.saveFileData(f, bos.toByteArray());
				if(!silent)
				MessageDialog.openInformation(getSite().getShell(), "成功", "自动校正操作成功。");
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), "错误", e.toString());
				return;
			}
	}
}