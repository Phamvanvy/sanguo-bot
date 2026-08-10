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
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
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
import com.pip.mapeditor.NewMapFileDialog;
import com.pip.mapeditor.data.CellMap;
import com.pip.mapeditor.data.MapFile;
import com.pip.util.FileCopier;
import com.pip.util.FileExtensionFilter;
import com.pip.util.FileMover;
import com.pip.util.FileRemover;
import com.pip.util.FileRenamer;
import com.pip.util.Utils;
import com.pipimage.image.EquipHookMap;
import com.pipimage.image.PipParticleEffectSet;
import com.pipimage.png.PngEncoder;

public class DirectoryView extends ViewPart implements IDoubleClickListener, ISelectionChangedListener {
	public static final String ID = "com.pip.image.workshop.directoryview";

	private Tree tree;
	private TreeViewer viewer;
	private static FileExtensionFilter imageFilter = new FileExtensionFilter(new String[] { "pip", "cts", "png", "gif", "map", "p", "cm", "ldf", "hk", "eqp", "pef" }, true);

	private Action newImageAction;
	private Action newDirAction;
	private Action newAnimateAction;
	private Action newMapAction;
	private Action newLandformAction;
	private Action newCellMapAction;
	private Action newEffectAction;
	private Action refreshAction;
	private Action setTileLibDirAction;
	private Action exploreAction;
	private Action editAction;
	private Action deleteAction;
	private Action renameFileAction;
	private Action addBookmarkAction;
	private Action convertToCTSAction;
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
				} else if (((File)element).getName().toLowerCase().endsWith(".pef")) {
					return WorkshopPlugin.getDefault().getImageRegistry().get("animate");
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
		setupDragAndDrop(viewer);
		
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
	
	public Object getSelectedObject() {
		IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
		if (sel.isEmpty()) {
			return null;
		}
		return sel.getFirstElement();
	}
	
	/**
	 * 取得选中的文件列表。
	 * @return 如果选中内容包括非文件内容，或者选中内容为空，返回null；选中的文件中，如果同时包含一个目录和此目录下的子目录或文件，则只返回父目录。
	 */
	public String[] getSelectedFiles() {
		IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
		if (sel.isEmpty()) {
			return null;
		}
		ArrayList<String> retList = new ArrayList<String>();
		for (Object obj : sel.toArray()) {
			if (!(obj instanceof File)) {
				return null;
			}
			String s1 = ((File)obj).getAbsolutePath();
			boolean ignore = false;
			for (int i = 0; i < retList.size(); i++) {
				String s2 = retList.get(i);
				if (s1.startsWith(s2)) {
					// 子目录或文件，不加入列表
					ignore = true;
					break;
				} else if (s2.startsWith(s1)) {
					// 父目录来了，删除旧记录
					retList.remove(i);
					i--;
				}
			}
			if (!ignore) {
				retList.add(s1);
			}
		}
		String[] ret = new String[retList.size()];
		retList.toArray(ret);
		return ret;
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
				if (f.getName().endsWith(".cts")) {
					mgr.add(newBodyAction);
					mgr.add(newEquipAction);
					mgr.add(newEffectAction);
					mgr.add(new Separator());
				} else if (f.getName().endsWith(".hk")) {
					mgr.add(equipPreViewAction);
					mgr.add(new Separator());
				}
				mgr.add(renameFileAction);
				mgr.add(deleteAction);
				if (ProjectView.canAutoCreateCTS(getSelectedFiles())) {
					mgr.add(convertToCTSAction);
				}
			} else {
				Settings.workingDir = f;
				mgr.add(newDirAction);
				mgr.add(newImageAction);
				mgr.add(newMapAction);
				mgr.add(newAnimateAction);
				mgr.add(newLandformAction);
				mgr.add(newCellMapAction);
				mgr.add(new Separator());
				mgr.add(setTileLibDirAction);
				mgr.add(deleteAction);
			}
			mgr.add(new Separator());
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
		
		deleteAction = new Action("删除(&D)") {
			public void run() {
				String[] selFiles = getSelectedFiles();
				if (selFiles == null) {
					return;
				}
				String msg = "你确定要删除选中的" + selFiles.length + "个文件吗？";
				if (MessageDialog.openConfirm(getSite().getShell(), "确认", msg)) {
					for (String p : selFiles) {
						stopEdit(new File(p));
					}
					try {
						FileRemover.remove(selFiles);
					} catch (Exception e) {
						MessageDialog.openError(getSite().getShell(), "错误", e.toString());
					}
					for (String p : selFiles) {
						viewer.refresh(new File(p).getParentFile());
					}
				}
			}
		};
		renameFileAction = new Action("重命名(&R)"){
			public void run(){
				final File file = (File)getSelectedObject();
				final String defaultName = (file).getName();
				
				if (isOpened(file)) {
					MessageDialog.openInformation(getSite().getShell(), "提示", "此文件已打开，请先关闭编辑界面重试。");
					return;
				} else {
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
						if (newName.equals(defaultName)) {
							return;
						}
						if (!Utils.getSuffix(newName).equals(Utils.getSuffix(defaultName))) {
							MessageDialog.openInformation(getSite().getShell(), "提示", "不能修改文件扩展名。");
							return;
						}
						String fromFullPath = file.getAbsolutePath();
						boolean ret = FileRenamer.rename(fromFullPath, newName);
						if (ret) {
							viewer.refresh(file.getParentFile());
						} else {
							MessageDialog.openInformation(getSite().getShell(), "提示", "修改失败!");
							return;
						}
						
						// 如果修改的是hk，那么提示是否同步修改同名的cts；如果修改的是cts，提示是否同步修改hk。
						if (fromFullPath.toLowerCase().endsWith(".cts")) {
							String hkFullName = Utils.replaceSuffix(fromFullPath, "hk");
							File hkFile = new File(hkFullName);
							File hkNewFile = new File(hkFile.getParentFile(), Utils.replaceSuffix(newName, "hk"));
							if (hkFile.exists() && !hkNewFile.exists()) {
								String msg = "此目录下有一个同名的hk文件，是否要把此hk文件的名称也同时修改？";
								if (MessageDialog.openConfirm(getSite().getShell(), "提示", msg)) {
									ret = FileRenamer.rename(hkFullName, Utils.replaceSuffix(newName, "hk"));
									if (ret) {
										viewer.refresh(hkFile.getParentFile());
									} else {
										MessageDialog.openInformation(getSite().getShell(), "提示", "修改失败!");
									}
								}
							}
						} else if (fromFullPath.toLowerCase().endsWith(".hk")) {
							String ctsFullName = Utils.replaceSuffix(fromFullPath, "cts");
							File ctsFile = new File(ctsFullName);
							File ctsNewFile = new File(ctsFile.getParentFile(), Utils.replaceSuffix(newName, "cts"));
							if (ctsFile.exists() && !ctsNewFile.exists()) {
								String msg = "此目录下有一个同名的cts文件，是否要把此cts文件的名称也同时修改？";
								if (MessageDialog.openConfirm(getSite().getShell(), "提示", msg)) {
									ret = FileRenamer.rename(ctsFullName, Utils.replaceSuffix(newName, "cts"));
									if (ret) {
										viewer.refresh(ctsFile.getParentFile());
									} else {
										MessageDialog.openInformation(getSite().getShell(), "提示", "修改失败!");
									}
								}
							}
						}
					}
				}
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

		newDirAction = new Action("新建文件夹") {
			public void run() {
				InputDialog dlg = new InputDialog(getSite().getShell(), "新建文件夹", "请输入文件夹名称：", "新建文件夹", new IInputValidator() {
				public String isValid(String newText) {
						if (newText.trim().length() == 0) {
							return "目录名不能为空。";
						} else {
							return null;
						}
					}
				});
				if (dlg.open() != InputDialog.OK) {
					return;
				}
				String dirName = dlg.getValue();
				Object selFile = getSelectedObject();
				if (selFile instanceof File && ((File)selFile).isDirectory()) {
					File newDir = new File((File)selFile, dirName);
					if (newDir.exists() == false) {
						if (newDir.mkdir()) {
							viewer.refresh(selFile);
							viewer.expandToLevel(selFile, 1);
						}
					} else {
						MessageDialog.openError(getSite().getShell(), "错误", "文件夹" + dirName + "已经存在了。");
					}
				}
			}
		};
		
		newImageAction = new Action("新建图片") {
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
				viewer.expandToLevel(selObj, 1);
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
		
		newEffectAction = new Action("新建粒子效果文件"){
			public void run() {
				Object selObj = getSelectedObject();
				File f = (File)selObj;
				File effectFile = newEffectFile(f);
				if (effectFile == null){
					return;
				}
				try {
					openFile(effectFile);
				}catch(Exception e){
					MessageDialog.openError(getSite().getShell(), "错误", e.toString());
					e.printStackTrace();
				}
				viewer.refresh(f.getParentFile());
			}
		};
		
		convertToCTSAction = new Action("转换为动画文件") {
			public void run() {
				// 把选中的PNG/GIF文件转换为动画文件，每个PNG/GIF文件一帧。
				String[] files = getSelectedFiles();
				try {
					ProjectView.autoCreateCTS(files);
					viewer.refresh(new File(files[0]).getParentFile());
				} catch (Exception e) {
					MessageDialog.openError(getSite().getShell(), "错误", e.toString());
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
	
	protected File newEffectFile(File ctsFile) {
		String defaultName = ctsFile.getName().replace(".cts", ".pef");
		InputDialog dlg = new InputDialog(getSite().getShell(), "新建粒子效果文件", "请输入粒子效果文件名：", defaultName, new IInputValidator() {
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
			if (!newname.toLowerCase().endsWith(".pef")) {
				newname += ".pef";
			}
			File newfile = new File(ctsFile.getParentFile(), newname);
			try {
				if (!newfile.createNewFile()) {
					MessageDialog.openError(getSite().getShell(), "错误", newname + "已经存在了。");
					return null;
				}
				PipParticleEffectSet effectSet = new PipParticleEffectSet();
				effectSet.setOriginalFile(newfile);
				effectSet.setAnimateFile(ctsFile);
				effectSet.save(newfile);
				return newfile;
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), "错误", "创建文件" + newname + "失败。");
			}
		}
		return null;
	}

	public static void stopEdit(Object obj) {
		if (obj instanceof File && ((File)obj).isDirectory()) {
			File[] files = ((File)obj).listFiles();
			for (File f : files) {
				stopEdit(f);
			}
		} else {
	        IEditorPart editor = null;
	        IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path((((File)obj)).getAbsolutePath()));
	        FileStoreEditorInput input = new FileStoreEditorInput(fileStore);
	        editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findEditor(input);
	        if (editor != null) {
	            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(editor, false);
	        }
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
    	NewMapFileDialog dlg = new NewMapFileDialog(getSite().getShell(), parent);
    	if (dlg.open() == Dialog.OK) {
			try {
				File newfile = new File(parent, dlg.fileName);
				if (!newfile.createNewFile()) {
					MessageDialog.openError(getSite().getShell(), "错误", dlg.fileName + "已经存在了。");
					return null;
				}
				MapFile map = new MapFile();
				map.isLibMode = dlg.libMode;
				map.setBlurTileWidth(dlg.tileSize);
				map.setBlurTileHeight(dlg.tileSize);
				map.save(newfile);
				return newfile;
			} catch (Exception e) {
				e.printStackTrace();
				MessageDialog.openError(getSite().getShell(), "错误", "创建文件" + dlg.fileName + "失败。\n"+e.toString());
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
		IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path((file.getAbsolutePath())));
		IDE.openEditorOnFileStore(getSite().getWorkbenchWindow().getActivePage(), fileStore);
	}
	
	private boolean isOpened(File file) {
		IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path((file.getAbsolutePath())));
		FileStoreEditorInput input = new FileStoreEditorInput(fileStore);
		return getSite().getWorkbenchWindow().getActivePage().findEditor(input) != null;
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
	
	/*
     * 设置拖动支持。
     */
    private void setupDragAndDrop(TreeViewer v) {
        Tree tree = v.getTree();
        final DragSource treeDragSource = new DragSource(tree, DND.DROP_MOVE | DND.DROP_COPY);
        treeDragSource.addDragListener(new DragSourceAdapter() {
            /**
             * 判断是否允许拖动。选择的任意文件都可以拖动
             */
            public void dragStart(DragSourceEvent event) {
            	String[] sels = getSelectedFiles();
            	if (sels == null) {
                    event.doit = false;
                } else {
                    event.doit = true;
                }
            }

            /**
             * 设置拖动数据，是文件名的数组。
             */
            public void dragSetData(DragSourceEvent event) {
                String[] sels = getSelectedFiles();
                event.data = sels;
            }

            public void dragFinished(DragSourceEvent event) {
            }
        });
        treeDragSource.setTransfer(new Transfer[] { FileTransfer.getInstance() });

        final DropTarget treeDropTarget = new DropTarget(tree, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
        treeDropTarget.addDropListener(new DropTargetAdapter() {
        	private int currentDragOp = DND.DROP_MOVE;
        	
            public void dragEnter(DropTargetEvent event) {
            	currentDragOp = event.detail;
            	if (currentDragOp == DND.DROP_DEFAULT) {
            		currentDragOp = DND.DROP_MOVE;
            	}
            }

            public void dragLeave(DropTargetEvent event) {
            }

            public void dragOperationChanged(DropTargetEvent event) {
            	currentDragOp = event.detail;
            }

            /**
             * 检查当前目标是否允许拖放。允许拖动到一个新分类中（加到最后），或者拖动到一个指定对象（插入到前面）。
             */
            public void dragOver(DropTargetEvent event) {
                event.feedback = DND.FEEDBACK_NONE | DND.FEEDBACK_SCROLL | DND.FEEDBACK_EXPAND | DND.FEEDBACK_SELECT;
                event.detail = DND.DROP_NONE;
                if (event.item != null) {
                	TreeItem titem = (TreeItem) event.item;
                    Object targetObj = titem.getData();
                    if (targetObj instanceof File && ((File)targetObj).isDirectory()) {
                        event.detail = currentDragOp;
                    } else {
                    	event.detail = DND.DROP_NONE;
                    }
                } else {
                	event.detail = DND.DROP_NONE;
                }
            }

            /**
             * 拖动结束。
             */
            public void drop(DropTargetEvent event) {
            	if (event.data == null || event.item == null) {
                    return;
                }
                TreeItem titem = (TreeItem) event.item;
                Object targetObj = titem.getData();
                String[] sources = (String[])event.data;
                if (targetObj instanceof File && ((File)targetObj).isDirectory()) {
                	if (event.detail == DND.DROP_COPY) {
                		try {
							FileCopier.copy(sources, (File)targetObj);
						} catch (Exception e) {
							e.printStackTrace();
							MessageDialog.openError(getSite().getShell(), "错误", e.toString());
						}
						viewer.refresh((File)targetObj);
                	} else if (event.detail == DND.DROP_MOVE) {
                		for (String p : sources) {
                			stopEdit(new File(p));
						}
						try {
							FileMover.move(sources, (File)targetObj);
						} catch (Exception e) {
							MessageDialog.openError(getSite().getShell(), "错误", e.toString());
						}
						for (String p : sources) {
							viewer.refresh(new File(p).getParentFile());
						}
						viewer.refresh((File)targetObj);
                	}
                }
            }

            public void dropAccept(DropTargetEvent event) {
            }
        });
        treeDropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance() });
    }
}
