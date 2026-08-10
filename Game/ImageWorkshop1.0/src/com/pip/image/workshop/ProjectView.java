package com.pip.image.workshop;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.jdom.Element;

import com.pip.image.workshop.editor.ImageViewer;
import com.pip.mapeditor.NewMapFileDialog;
import com.pip.mapeditor.data.MapFile;
import com.pip.mapeditor.data.ProjectOwner;
import com.pip.mapeditor.data.ProjectParser;
import com.pip.mapeditor.data.TileSet;
import com.pip.util.FileCopier;
import com.pip.util.FileExtensionFilter;
import com.pip.util.FileMover;
import com.pip.util.FileRemover;
import com.pip.util.FileRenamer;
import com.pip.util.SWTUtils;
import com.pip.util.Utils;
import com.pipimage.data.ImageDescription;
import com.pipimage.data.TileInfo2;
import com.pipimage.image.LandformImage;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFramePiece;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageData;
import com.pipimage.png.PngEncoder;

public class ProjectView extends ViewPart implements IDoubleClickListener, ISelectionChangedListener {

	public static final String ID = "com.pip.image.workshop.ProjectView"; //$NON-NLS-1$

	@Override
	public void dispose() {
		super.dispose();
		ProjectOwner.disposeAll();
	}
	private TreeViewer viewer;
	private static FileExtensionFilter imageFilter = new FileExtensionFilter(new String[] { "png", "gif", "pip", "p", "cts", "ldf", "map" }, true);

	private Action setAsGroupDir;
	private Action cancelGroupDir;
	private Action newDirAction;
	private Action newPipImageAction;
	private Action newAnimateAction;
	private Action newMapAction;
	private Action newLandformAction;
	private Action refreshAction;
	private Action exploreAction;
//	private Action importAction;
	private Action openAction;
	private Action deleteAction;
	private Action chooseDirAction;
	private Action updateFileMapAction;
	private Action convertToCTSAction;
//	private FileDialog importDialog;
	private static Shell shell;
	public int activeResHashCode;
	private Action newPrjAction;
	private Action setPrjDirAction;
	private Action renameAction;
	private Action propertyAction;
	
	// 增加目录同步功能
	private Action createCopyAction;
	private Action adjustColorAction;
	private Action syncAction;
	
	/**
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */
	static class PrgViewContentProvider implements ITreeContentProvider {
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}
		
		public Object[] getChildren(Object parentElement) {
			File f;
			boolean isPrjDir = false;
			if(parentElement instanceof Element){
				Element el = (Element) parentElement;
				if(isPrjEl(el)){
					f = new File(el.getAttributeValue("dir"));
					if(f.getName().equals("data")){
						isPrjDir = true;
					}
				}else{
					return el.getChildren().toArray();
				}
			}else{
				f = (File)parentElement;
			}
			if (f.isDirectory()) {
				if(isPrjDir){
					//filtering folders
					File libDir = new File(f, "pipLib");
					if(libDir.exists()==false){
						libDir.mkdir();
					}
					return new File[]{libDir};
				}else{
					return f.listFiles(imageFilter);
				}
			} else {
//				return new Object[0];
				try {
					return ProjectParser.getFileRefList(f.getAbsolutePath());
				} catch (Exception e) {
					e.printStackTrace();
					return new Object[0];
				}
			}
		}

	    public Object getParent(Object element) {
//	    	Element el = (Element) element;
//	    	return el.getParent();
	    	return ((File)element).getParentFile();
	    }

	    public boolean hasChildren(Object element) {
	    	File f = null;
	    	if(element instanceof Element){
		    	Element el = (Element) element;
		    	if(isPrjEl(el) ){
		    		String dir = el.getAttributeValue("dir");
		    		f = new File(dir);
		    		return true;
		    	}else{
		    		return el.getChildren().size()>0;
		    	}
	    	}else if(element instanceof File){
	    		f = (File) element;
	    		if(f.isFile()){
	    			return true;
	    		}else{
	    			
	    		}
	    	}else{
	    		return false;
	    	}
	    	return f.isDirectory()&&f.list().length>0;
	    }
	    
	    public void dispose() {}

	    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	}


	static class PrgViewLabelProvider extends LabelProvider {
		public Image getImage(Object element) {
			String name ;
			if(element instanceof Element){
				Element el = (Element) element;
				name = el.getName();
			}else if(element instanceof File){
				File f = (File)element; 
				if(f.isDirectory()){
					if(checkGroupDir(f,true)){
						return WorkshopPlugin.getDefault().getImageRegistry().get("mainGroup");
					}else if(checkGroupDir(f, false)){
						return WorkshopPlugin.getDefault().getImageRegistry().get("groupFolder");
					}else if (MirrorData.isMirrorDir(f)) {
						return WorkshopPlugin.getDefault().getImageRegistry().get("groupFolder");
					} else {
						return WorkshopPlugin.getDefault().getImageRegistry().get("folder");
					}
				}
				name = ((File)element).getName();
			}else{
				name = element.toString();
			}
			if(name.equals("project")){
				return WorkshopPlugin.getDefault().getImageRegistry().get("grid");
			}else if(name.equals("lib")){
				return WorkshopPlugin.getDefault().getImageRegistry().get("tiles");
			}else{
//				name = el.getAttributeValue("file").toLowerCase();
				if (name.endsWith(".png")) {
					return WorkshopPlugin.getDefault().getImageRegistry().get("systemimage");
				} else if (name.endsWith(".p")) {
                    return WorkshopPlugin.getDefault().getImageRegistry().get("systemimage");
                } else if (name.endsWith(".gif")) {
					return WorkshopPlugin.getDefault().getImageRegistry().get("systemimage");
				} else if (name.endsWith(".pip")) {
					return WorkshopPlugin.getDefault().getImageRegistry().get("image");
				} else if (name.endsWith(".cts")) {
				    return WorkshopPlugin.getDefault().getImageRegistry().get("animate");
				} else if (name.endsWith(".map")) {
					return WorkshopPlugin.getDefault().getImageRegistry().get("map");
				} else if (name.endsWith(".ldf")) {
                    return WorkshopPlugin.getDefault().getImageRegistry().get("landform");
				} else {
					return WorkshopPlugin.getDefault().getImageRegistry().get("disk");
				}
			}
		}
	
		public String getText(Object element) {
			if(element instanceof Element){
				return ((Element)element).getAttributeValue("name");
			}else if(element instanceof File){
				return ((File)element).getName();
			}else{
				return element.toString();
			}
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent);
		viewer.setContentProvider(new PrgViewContentProvider());
		viewer.setLabelProvider(new PrgViewLabelProvider());
		viewer.setInput(Settings.projects);
		createProjectOwners();
		viewer.addDoubleClickListener(this);
		viewer.addSelectionChangedListener(this);
		viewer.expandToLevel(2);
		setupDragAndDrop(viewer);
		
		createActions();
		initializeToolBar();
		initializeMenu();
	}

	private void createProjectOwners() {
		for(Element el:(List<Element>)Settings.projects.getChildren()){
			prjEl2prjOwner.put(el, ProjectOwner.find(el.getAttributeValue("dir"), true));
		}
	}
	public static boolean checkGroupDir(File f,boolean checkMain) {	
		for(ProjectOwner prjOwner:prjEl2prjOwner.values()){
			try {
				if(f.getAbsolutePath().startsWith(prjOwner.getPrjDataPath()) && prjOwner.checkGroupDir(f,checkMain)){
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	private static HashMap<Element,ProjectOwner> prjEl2prjOwner = new HashMap<Element, ProjectOwner>();
	private ProjectOwner getSelectedPrjOwner(){
		TreeSelection treeSel = (TreeSelection) viewer.getSelection();
		Element prjEl = (Element) treeSel.getPaths()[0].getFirstSegment();
		return prjEl2prjOwner.get(prjEl);
	}
	private Object getSelectedObj(){
		IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
		if (sel.isEmpty()) {
			return null;
		}
		return sel.getFirstElement();
	}
	private Element getSelectedElement(){
		IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
		if (sel.isEmpty()) {
			return null;
		}
		return (Element) sel.getFirstElement();
	}
	private File getSelectedFile() {
		IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
		if (sel.isEmpty()) {
			return null;
		}
		Object obj = sel.getFirstElement();
		if(obj instanceof File){//file element
			return (File)sel.getFirstElement();
		}else if(obj instanceof Element){//project element
			return new File(getSelectedPrjOwner().getPrjDataPath());
		}else{//files' ref element is string
			return null;
		}
	}
	
	public void doubleClick(DoubleClickEvent event) {
		if (event.getViewer() == viewer) {
			Object obj = getSelectedObj();
			File selFile = null;
			if(obj instanceof File){
				selFile = (File)obj;
			}else if(obj instanceof String){
				selFile = new File(getSelectedPrjOwner().getPrjDataPath(), (String)obj);
			}
			if(selFile==null){
				return;
			}
			if (selFile.isDirectory()) {
				expandOrCollapseNode(selFile);
				return;
			}
			try {
				openFileEditor(selFile);
			} catch (Exception e) {
				SWTUtils.showError(getSite().getShell(), "错误", e);
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
		//event == null -> manually call to refresh menu
		if (event==null || event.getSelectionProvider() == viewer) {
			Object el = getSelectedObj();
			MenuManager mgr = new MenuManager();

			if (isPrjEl(el)) {
				mgr.add(newPrjAction);
				mgr.add(newDirAction);
				mgr.add(new Separator());
				mgr.add(propertyAction);
				mgr.add(setPrjDirAction);
				mgr.add(renameAction);
				mgr.add(deleteAction);
				mgr.add(new Separator());
				mgr.add(exploreAction);
			} else if (el instanceof File) {
				File selFile = (File)el;
				if (selFile.isFile()) {
					mgr.add(renameAction);
					mgr.add(deleteAction);
					if (canAutoCreateCTS(getSelectedFiles())) {
						mgr.add(convertToCTSAction);
					}
				} else if (selFile.isDirectory()) {
					mgr.add(newDirAction);
					mgr.add(newPipImageAction);
					mgr.add(newMapAction);
					mgr.add(newAnimateAction);
					mgr.add(newLandformAction);
					mgr.add(new Separator());
					mgr.add(updateFileMapAction);
					mgr.add(deleteAction);
					mgr.add(createCopyAction);
					mgr.add(adjustColorAction);
					if (MirrorData.isMirrorDir(selFile)) {
						mgr.add(syncAction);
					}
				}
				mgr.add(new Separator());
				mgr.add(exploreAction);
			}
			mgr.add(refreshAction);
			
			Menu menu = mgr.createContextMenu(viewer.getTree());
			if (viewer.getTree().getMenu() != null) {
				viewer.getTree().getMenu().dispose();
			}
			viewer.getTree().setMenu(menu);
			
			if(el instanceof File){//preview
				try {
					File selFile = (File)el;
					if (selFile.isFile() && selFile.getName().endsWith(".lfi")==false && selFile.getName().endsWith(".map")==false) {
						openFile(selFile);
					} else if (selFile.isDirectory()) {
						openDir(selFile);
					}
				} catch (Exception e) {
					SWTUtils.showError(getSite().getShell(), "错误", e);
				}
			}
		}
	}
	/**
	 * 生成切换至分组的子菜单。
	 * @param mgr
	 * @param mapFired 此菜单是否是给单个map文件的。是则执行切换后会刷新打开的编辑器。
	 */
	private void makeSwitchGroupSubmenu(MenuManager mgr, final boolean mapFired) {
		ProjectOwner prjOwner = getSelectedPrjOwner();
		File pipLibDir = new File(prjOwner.getPrjDataPath(),prjOwner.getLibDirName());
		if(pipLibDir.exists()==false){
			return;
		}
		List<String> grpNames;
		String mapCurGroup = null;
		try {
			grpNames = prjOwner.getSwitchGroupNames(pipLibDir, !mapFired);
			if(mapFired){
				mapCurGroup = prjOwner.getMapCurGroup(getSelectedFile());
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.reportError("获取可切换分组时出现错误:\n"+e);
			return;
		}
		if(grpNames.size()>0){
			MenuManager subMgr = new MenuManager("切换至分组");
			for(final String name:grpNames){
				Action act = new Action(name){
					public void run(){
						if(mapFired){
							doSwitchToGroup(name,true);
						}else{
							doSwitchToGroup(name,false);
						}
					}
				}; 
				subMgr.add(act);
				if(mapFired && name.equals(mapCurGroup)){
					act.setEnabled(false);
				}
			}
			mgr.add(subMgr);
		}
	}

	protected void doSwitchToGroup(String toGroupName, boolean mapFired) {
		ProjectOwner owner = getSelectedPrjOwner();
		String curGroupName = owner.getCurGroupName();
		if(curGroupName!=null){
			//遍历当前组里的图片和动画文件,并将引用他们的地图里的引用值切换
			String libDir = owner.getPrjDataPath()+owner.getLibDirName();
			File curDir = new File(libDir, curGroupName);
			File destDir = new File(libDir, toGroupName);
			try {
				if(mapFired){
					owner.replaceHashCodeRef(getSelectedFile(),curDir,destDir);
//					owner.setCurGroupName(toGroupName);//切换单个地图，不更改owner的当前组
//					viewer.refresh(new File(libDir));

				}else{
					owner.replaceHashCodeRef(curDir, destDir);
					owner.setCurGroupName(toGroupName);
					viewer.refresh(getSelectedFile());
				}
				selectionChanged(null);
			} catch (Exception e) {
				e.printStackTrace();
				reportError("切换分组时出现错误:\n"+e);
			}
		}
	}

	/**
	 * 生成克隆至分组的子菜单
	 * @param mgr
	 */
	private void makeGroupSubmenu(MenuManager mgr) {
		List<String> grpNames = getSelectedPrjOwner().getGroupDirNames();
		if(grpNames!=null && grpNames.size()>0){
			String tag = "data"+File.separator+"pipLib"+File.separator;
			String newFilePath = getSelectedFile().getAbsolutePath();
			int idx = newFilePath.indexOf(tag) + tag.length();
			String postfix = newFilePath.substring(idx);
			MenuManager subMgr = new MenuManager("克隆至分组");
			for(final String name:grpNames){
				if(postfix.startsWith(name)){
					continue;
				}
				subMgr.add(new Action(name){
					public void run(){
						doMakeGroupCopy(name);
					}
				});
			}
			mgr.add(subMgr);
		}else{
			
		}
	}

	/**
	 * 克隆当前选中的文件至指定的分组.<p>
	 * 如果是地形文件（ldf），还会拷贝相应的lfi文件.
	 * @param groupName
	 */
	protected void doMakeGroupCopy(String groupName) {
		File curFile = getSelectedFile();
		doMakeGroupCopy(groupName, curFile);
		if(curFile.getName().toLowerCase().endsWith(".ldf")){
			curFile = new File(curFile.getParentFile(),curFile.getName().replaceAll("\\.ldf$", "\\.lfi"));
			doMakeGroupCopy(groupName, curFile);
		}
	}
	/**
	 * 克隆当前指定的文件至指定的分组
	 * @param groupName
	 * @param srcFile
	 */
	protected void doMakeGroupCopy(String groupName, File srcFile) {
		String newFilePath = srcFile.getAbsolutePath();
		String tag = "data"+File.separator+"pipLib"+File.separator;
		int idx = newFilePath.indexOf(tag) + tag.length();
		String prefix = newFilePath.substring(0,idx);
		String postfix = newFilePath.substring(idx);
		postfix = postfix.substring(postfix.indexOf(File.separator));
		newFilePath = prefix + groupName + postfix;
		File destFile = new File(newFilePath);
		destFile.getParentFile().mkdirs();
		if(srcFile.equals(destFile)){//源和目标相同
			return;
		}
		if(destFile.exists()){
			reportError("目标文件已经存在");
			return;
		}
		try {
			Utils.copyFile(srcFile, destFile);
		} catch (IOException e) {
			e.printStackTrace();
			reportError("克隆组文件时出现错误:\n"+e);
		}
	}

	private boolean clipboardHasFile() {
		Clipboard sysc = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable clipT = sysc.getContents(null);
		if (clipT != null) {
			if (clipT.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				Object ob = null;
				try {
					ob = clipT.getTransferData(DataFlavor.javaFileListFlavor);
					List fileList = (List) ob;
					if ((fileList != null) && (fileList.size() > 0)) {
						return true;
					}
				}catch(Exception e){
					return false;
				}
			}
		}
		return false;
	}
	public static boolean isPrjEl(Object obj){
		return obj instanceof Element && ((Element)obj).getName().equals("project");
	}

	//	private Action link2curMapAction;
	public static String getInput(String title, String tip){
		InputDialog dlg = new InputDialog(shell, title,tip,null,null);
		if(dlg.open()==InputDialog.OK && dlg.getValue().equals("")==false){
			return dlg.getValue();
		}else{
			return null;
		}
	}
	
	private void createActions() {
		shell = getSite().getShell();
		
		cancelGroupDir = new Action("取消组文件夹"){
			public void run(){
				doRemoveGroup();
			}
		};
		setAsGroupDir = new Action("设为组文件夹"){
			public void run(){
				doSetAsGroup();
			}
		};
		newDirAction = new Action("新建文件夹"){
			public void run(){
				doCreateDir();
			}
		};
		updateFileMapAction = new Action("更新文件映射"){
			public void run(){
				doUpdateFileMap();
			}
		};
//		link2curMapAction = new Action("链接至当前地图"){
//			public void run(){
//				Element el = getSelectedElement();
//				String mapFilePath;// = "Areas\\10\\game.map";
//				IEditorPart editor = getSite().getPage().getActiveEditor();
//				if (editor != null && editor instanceof MapEditor) {
//					String prjDir = prjEl2prjOwner.get(el).getPrjDataPath();
//					MapEditor me = (MapEditor) editor;
//					mapFilePath = me.getFilePath().replace(prjDir+File.separator, "");
//					ProjectParser.linkMap(el, mapFilePath );
//					viewer.refresh(el);
//				}
//			}
//		};
		propertyAction = new Action("属性"){
			public void run(){
				Element el = getSelectedElement();
				if(isPrjEl(el)){
					String msg = "项目路径:"+el.getAttributeValue("dir");
					MessageDialog.openInformation(shell, "项目属性", msg );
				}
			}
		};
		renameAction = new Action("重命名(&R)"){
			public void run(){
				Object el = getSelectedObj();
				if (isPrjEl(el)) {
					String input = getInput("重命名", "请输入新名称:");
					if(input!=null){
						Element el1 = getSelectedElement();
						el1.getAttribute("name").setValue(input);
						viewer.refresh(el1);
					}
				} else {
					final File file = (File)getSelectedObj();
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
			}
		};
	
		setPrjDirAction = new Action("设置路径"){
			public void run(){
				doSetPrjDir();
			}
		};
		newPrjAction = new Action("新建项目"){
			public void run(){
				doCreateProject();
			}
		};
		deleteAction = new Action("删除") {
			public void run() {
				doDelete();
			}
		};

		openAction = new Action("打开") {
			public void run() {
				File selFile = getSelectedFile();
				try {
					openFileEditor(selFile);
				} catch (Exception e) {
					SWTUtils.showError(getSite().getShell(), "错误", e);
				}
			}
		};

		refreshAction = new Action("刷新") {
			public void run() {
				doRefresh();
			}
		};
		exploreAction = new Action("浏览...") {
		    public void run() {
		    	Object el = getSelectedObj();
		    	if (el == null) {
		    		return;
		    	}
		    	File dir;
		    	if (isPrjEl(el)) {
		    		dir = new File(((Element)el).getAttributeValue("dir"));
				} else if (el instanceof File) {
					dir = (File)el;
					if (dir.isFile()) {
						dir = dir.getParentFile();
					}
		    	} else {
		    		return;
		    	}
                String cmd = "explorer.exe \"" + dir.getAbsolutePath() + "\"";
                try {
                    Runtime.getRuntime().exec(cmd);
                } catch (Exception e) {
                }
		    }
		};
		newPipImageAction = new Action("新建图片") {
			public void run() {
				Object selObj = getSelectedFile();
				File file = newImage((File)selObj);
				viewer.refresh(selObj);
				try {
					if (file != null) {
						openFileEditor(file);
					}
				} catch (Exception e) {
					SWTUtils.showError(getSite().getShell(), "错误", e);
				}
			}
		};
		
		newAnimateAction = new Action("新建动画") {
			public void run() {
				Object selObj = getSelectedFile();
				File file = newAnimate((File)selObj);
				viewer.refresh(selObj);
				try {
					if (file != null) {
						openFileEditor(file);
					}
				} catch (Exception e) {
					SWTUtils.showError(getSite().getShell(), "错误", e);
				}
			}
		};
		
		newMapAction = new Action("新建地图") {
			public void run() {
				Object selObj = getSelectedFile();
				File file = newMap((File)selObj);
				viewer.refresh(selObj);
				viewer.expandToLevel(selObj, 1);
				try {
					if (file != null) {
						openFileEditor(file);
					}
				} catch (Exception e) {
					SWTUtils.showError(getSite().getShell(), "错误", e);
				}
			}
		};
        newLandformAction = new Action("新建地形") {
            public void run() {
                Object selObj = getSelectedFile();
                File file = newLandform((File)selObj);
                viewer.refresh(selObj);
                try {
                    if (file != null) {
                    	openFileEditor(file);
                    }
                } catch (Exception e) {
                	SWTUtils.showError(getSite().getShell(), "错误", e);
                }
            }
        };
		
//		importAction = new Action("导入图片") {
//			public void run() {
//				File rootDir = getSelectedFile();
//				if (importDialog == null) {
//					importDialog = new FileDialog(getSite().getShell(), SWT.OPEN | SWT.MULTI);
//				}
//				importDialog.setFilterExtensions(new String[] { "*.png", "*.gif", "*.*" });
//				importDialog.setFilterNames(new String[] { "PNG图片文件(*.png)", "GIF图片文件(*.gif)", "所有文件(*.*)" });
//				if (importDialog.open() != null) {
//					String dir = importDialog.getFilterPath();
//					String[] fileNames = importDialog.getFileNames();
//					for (int i = 0; i < fileNames.length; i++) {
//						File srcFile = new File(dir, fileNames[i]);
//						File destFile = new File(rootDir, fileNames[i]);
//						if (srcFile.equals(destFile)) {
//							continue;
//						}
//						try {
//							Utils.copyFile(srcFile, destFile);
//						} catch (Exception e) {
//							MessageDialog.openError(getSite().getShell(), "错误", e.toString());
//						}
//					}
//					viewer.refresh(rootDir);
//				}
//			}
//		};
		
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
		
		convertToCTSAction = new Action("转换为动画文件") {
			public void run() {
				// 把选中的PNG/GIF文件转换为动画文件，每个PNG/GIF文件一帧。
				String[] files = getSelectedFiles();
				try {
					autoCreateCTS(files);
					viewer.refresh(new File(files[0]).getParentFile());
				} catch (Exception e) {
					SWTUtils.showError(getSite().getShell(), "错误", e);
				}
			}
		};
		
		createCopyAction = new Action("创建镜像目录...") {
			public void run() {
				// 为当前选中的目录创建一个镜像目录，镜像目录和原目录除了名字不同以外，里面的内容完全相同
				onCreateCopy();
			}
		};
		
		adjustColorAction = new Action("调整所有图片颜色...") {
			public void run() {
				// 搜索目录中所有pip文件，并且合并起来放到一个大图片中，准备批量调整颜色
				try {
					onAdjustColor();
				} catch (Exception e) {
					SWTUtils.showError(getSite().getShell(), "错误", e);
				}
			}
		};
		
		syncAction = new Action("和主目录同步") {
			public void run() {
				// 让镜像目录和主目录同步所有修改
				onSync();
			}
		};
	}
	protected void doRemoveGroup() {
		try {
			getSelectedPrjOwner().removeGroupDir(getSelectedFile());
			selectionChanged(null);
			viewer.refresh(getSelectedFile().getParentFile());
		} catch (Exception e) {
			e.printStackTrace();
			reportError("取消组文件夹设置时出现错误:\n"+e);
		}
	}

	protected void doCreateProject() {
		String name = getInput("新建项目", "请输入项目名称:");
		if(name!=null){
			for(Object elem:Settings.projects.getChildren()){
				if(((Element)elem).getAttributeValue("name").equals(name)){
					MessageDialog.openError(shell, "错误", "已经有同名项目存在.");
					return;
				}
			}
			Element el = new Element("project");
			el.addAttribute("name", name);
			el.addAttribute("dir", "");
			Settings.projects.addContent(el);
			prjEl2prjOwner.put(el, ProjectOwner.find("", true));
			viewer.refresh(Settings.projects);
		}		
	}

	protected void doSetPrjDir() {
		DirectoryDialog dlg = new DirectoryDialog(shell);
		dlg.setFilterPath("e:\\workspace\\Xiyou-Editor1.0\\");
		dlg.setText("选择目录");
		dlg.setMessage("请选择项目对应的数据编辑器的data目录:");
		String newPath = dlg.open();
		if (newPath != null) {
			for(Object elem:Settings.projects.getChildren()){
				if(((Element)elem).getAttributeValue("dir").equals(newPath)){
					MessageDialog.openError(shell, "错误", "项目>"+((Element)elem).getAttributeValue("name")+"<已经设定到了该目录");
					return;
				}
			}
		    setPrjPath(newPath);
		}		
	}

	protected void doRefresh() {
		Object obj = getSelectedObj();
		if(isPrjEl(obj)){
			Element el = getSelectedElement();
			String path = el.getAttributeValue("dir");
			if(path.indexOf("data")>0){
				getSelectedPrjOwner().refreshFileMap(el.getAttributeValue("dir")+"\\pipLib",true);//);
				getSelectedPrjOwner().refreshRefs(el.getAttributeValue("dir")+"\\pipLib");
			}
		}
		File selFile = getSelectedFile();
		if (selFile != null) {
			viewer.refresh(selFile);
		}else{
		}		
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
	
	protected void doDelete() {
		Object obj = getSelectedObj();
		if(isPrjEl(obj)){
			Element el = getSelectedElement();
			boolean ret = MessageDialog.openConfirm(shell, "确认", "确认删除项目>"+el.getAttributeValue("name")+"<吗?\n"
					+"磁盘文件需要手动删除.");
			if(ret){
				Settings.projects.getMixedContent().remove(el);
				viewer.refresh(Settings.projects);
			}
			return;
		}
		String[] selFiles = getSelectedFiles();
		if (selFiles == null) {
			return;
		}
		String msg = "你确定要删除选中的" + selFiles.length + "个文件吗？";
		if (MessageDialog.openConfirm(getSite().getShell(), "确认", msg)) {
			for (String p : selFiles) {
				DirectoryView.stopEdit(new File(p));
			}
			try {
				FileRemover.remove(selFiles);
			} catch (Exception e) {
				SWTUtils.showError(getSite().getShell(), "错误", e);
			}
			for (String p : selFiles) {
				viewer.refresh(new File(p).getParentFile());
			}
		}
	}
	
	private void doRemoveMapRef(File mapSrcFile){
		MapFile mapFile = new MapFile();
		ProjectOwner owner = getSelectedPrjOwner();
		try {
			mapFile.load(mapSrcFile);
			for(TileSet ts:mapFile.getLandforms()){
				owner.removeAnimateRef(ts.hashCode, mapSrcFile.getAbsolutePath());
			}
			if(mapFile.animateList!=null){
				for(PipAnimateSet pas:mapFile.animateList){
					owner.removeAnimateRef(pas.hashCode, mapSrcFile.getAbsolutePath());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			reportError("删除地图引用记录时出现错误:\n"+e);
		}
	}
	private void reportError(String string) {
		MessageDialog.openError(shell, "错误", string);
	}

	protected void doSetAsGroup() {
		try{
			File curDir = getSelectedFile();
			if(getSelectedPrjOwner().checkGroupDir(curDir)==true){
				return;
			}
			getSelectedPrjOwner().addGroupDir(curDir);
			selectionChanged(null);
			viewer.refresh(curDir.getParentFile());
		}catch(Exception e){
			SWTUtils.showError(getSite().getShell(), "错误", "设置组文件夹时出现错误。", e);
		}
	}

	protected void doCreateDir() {
		String dirName = getInput("新建文件夹", "请输入文件夹名称:");
		if(dirName == null){
			return;
		}
		File selFile = getSelectedFile();
		File newDir = null;
		if(selFile.isDirectory()){
		}else{
			selFile = selFile.getParentFile();
		}
		newDir = new File(selFile, dirName);
		if(newDir.exists()==false){
			if(newDir.mkdir()){
				viewer.refresh(selFile);
				viewer.expandToLevel(selFile, 1);
			}
		}else{
			MessageDialog.openError(shell, "错误", "文件夹" + dirName + "已经存在了。");
		}
	}

	protected void doCopy() {
		File srcFile = getSelectedFile();
		if(srcFile!=null){
			org.eclipse.swt.dnd.Clipboard cb = new org.eclipse.swt.dnd.Clipboard(Display.getDefault());
			Transfer[] dataTypes = new Transfer[]{FileTransfer.getInstance()};
			String[] data = new String[]{srcFile.getAbsolutePath()};
			try{
				cb.setContents(new Object[]{data}, dataTypes);
				cb.dispose();
			}catch( Exception e){
				e.printStackTrace();
			}
		}
	}

	protected void doUpdateFileMap() {
		File dir = getSelectedFile();
		if(dir != null && dir.isDirectory()){
			getSelectedPrjOwner().refreshFileMap(dir.getAbsolutePath(), false);
		}
	}

	protected void doPaste() {
		Clipboard sysc = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable clipT = sysc.getContents(null);
		if (clipT != null) {
			if (clipT.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				Object ob = null;
				try {
					ob = clipT.getTransferData(DataFlavor.javaFileListFlavor);
					List fileList = (List) ob;
					if ((fileList != null) && (fileList.size() > 0)) {
						for (int k = 0; k < fileList.size(); k++) {
							String src = ((File) fileList.get(k)).getAbsolutePath();
							System.out.println(src);
							File srcF = new File(src);
							File f = getSelectedFile();
							if(f.isFile()){
								f = f.getParentFile();
							}
							File destF = new File(f.getAbsolutePath(), srcF.getName());
							Utils.copyFile(srcF, destF);
							viewer.refresh(getSelectedFile());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected void setPrjPath(String newPath) {
		Element el = getSelectedElement();
		el.getAttribute("dir").setValue(newPath);
		prjEl2prjOwner.get(el).setPrjDataPath(newPath);
		prjEl2prjOwner.get(el).refreshFileMap(newPath, true);
		viewer.refresh(el);
	}

	public void setRootPath(File path) {
	    Settings.tileLibDir = path;
	    viewer.setInput(path);
	    viewer.refresh();
	}
	
	
	/**
	 * show in preview page
	 * @param file
	 * @throws Exception
	 */
	private void openFile(File file) throws Exception {
		try {
			getSite().getPage().showView(TileView.ID);
			TileView tv = (TileView)getSite().getPage().findView(TileView.ID);
			tv.openTileFile(file);
		} catch (Exception e) {
			SWTUtils.showError(getSite().getShell(), "错误", e);
		}
	}
	
	private void openDir(File dir) throws Exception {
		try {
			getSite().getPage().showView(TileView.ID);
			TileView tv = (TileView)getSite().getPage().findView(TileView.ID);
			tv.openDir(dir);
		} catch (Exception e) {
			SWTUtils.showError(getSite().getShell(), "错误", e);
		}
	}
	
	/**
	 * open edit page
	 * @param file
	 * @throws Exception
	 */
	private void openFileEditor(File file) throws Exception {
		IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path((file.getAbsolutePath())));
		IDE.openEditorOnFileStore(getSite().getWorkbenchWindow().getActivePage(), fileStore);
	}
	
	private void initializeToolBar() {
//		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
	}
	
	private void initializeMenu() {
//		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();

//		menuManager.add(chooseDirAction);
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
                getSelectedPrjOwner().regFile(newfile.getAbsolutePath());
                return newfile;
            } catch (Exception e) {
                MessageDialog.openError(getSite().getShell(), "错误", "创建文件" + newname + "失败。");
            }
        }
        return null;
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
				getSelectedPrjOwner().regFile(newfile.getAbsolutePath());
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
				getSelectedPrjOwner().regFile(newfile.getAbsolutePath());
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
				SWTUtils.showError(getSite().getShell(), "错误", "创建文件" + dlg.fileName + "失败。", e);
			}
		}
		return null;
	}

	private boolean isOpened(File file) {
		IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path((file.getAbsolutePath())));
		FileStoreEditorInput input = new FileStoreEditorInput(fileStore);
		return getSite().getWorkbenchWindow().getActivePage().findEditor(input) != null;
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
                            SWTUtils.showError(getSite().getShell(), "错误", e);
                        }
                        viewer.refresh((File)targetObj);
                    } else if (event.detail == DND.DROP_MOVE) {
                        for (String p : sources) {
                            DirectoryView.stopEdit(new File(p));
                        }
                        try {
                            FileMover.move(sources, (File)targetObj);
                        } catch (Exception e) {
                        	SWTUtils.showError(getSite().getShell(), "错误", e);
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
    
    /**
     * 检查一组图片文件是否能合并到一个动画文件里。这些文件必须都是图片文件，并且必须都在同一个目录里。
     * @param imageFiles
     * @return
     */
    public static boolean canAutoCreateCTS(String[] imageFiles) {
    	if (imageFiles == null || imageFiles.length == 0) {
    		return false;
    	}
    	File parent = new File(imageFiles[0]).getParentFile();
    	for (String path : imageFiles) {
    		File f = new File(path);
    		if (!f.getParentFile().equals(parent)) {
    			return false;
    		}
    		if (!f.isFile()) {
    			return false;
    		}
    		if (f.getName().toLowerCase().endsWith(".gif") || f.getName().toLowerCase().endsWith(".png")) {
    			continue;
    		} else {
    			return false;
    		}
    	}
    	return true;
    }
    
    /**
     * 把同一目录下的多个图片文件(PNG/GIF)自动放到一个CTS文件里。CTS文件和新创建的PIP文件名都和第一个图片文件
     * 相同。每个图片创建一帧和一个动画序列。
     * @param imageFiles 所有图片文件的全路径
     * @throws Exception
     */
    public static void autoCreateCTS(String[] imageFiles) throws Exception {
    	// 首先检查目标文件是否存在
    	String pipFileName = Utils.replaceSuffix(imageFiles[0], "pip");
    	String ctsFileName = Utils.replaceSuffix(imageFiles[0], "cts");
    	if (new File(pipFileName).exists()) {
    		throw new Exception(pipFileName + "已存在。");
    	}
    	if (new File(ctsFileName).exists()) {
    		throw new Exception(ctsFileName + "已存在。");
    	}
    	
    	// 首先把所有的图片加入到一个pip文件里去，每个文件一帧，如果65536色不够，则使用真彩色
    	PipImage pimg = new PipImage();
    	try {
	    	pimg.setSupportColorOp(false);
	    	pimg.setSupportMoreColors(true);
	    	pimg.setMergeMode(false);
	    	boolean oldFlag = PipImage.limitSize;
	    	PipImage.limitSize = false;
	    	for (String ifile : imageFiles) {
	    		Image newImg = new Image(PlatformUI.getWorkbench().getDisplay(), ifile);
	    		int[][] idata = ImageViewer.getImageData(newImg, newImg.getBounds());
	    		pimg.addFrame(idata);
	    		newImg.dispose();
	    	}
	    	PipImage.limitSize = oldFlag;
    	} catch (Exception e) {
    		pimg = new PipImage();
    		pimg.setSupportColorOp(false);
	    	pimg.setSupportMoreColors(false);
	    	pimg.setMergeMode(false);
	    	pimg.setTrueColor(true);
	    	boolean oldFlag = PipImage.limitSize;
	    	PipImage.limitSize = false;
	    	for (String ifile : imageFiles) {
	    		Image newImg = new Image(PlatformUI.getWorkbench().getDisplay(), ifile);
	    		int[][] idata = ImageViewer.getImageData(newImg, newImg.getBounds());
	    		pimg.addFrame(idata);
	    		newImg.dispose();
	    	}
	    	PipImage.limitSize = oldFlag;
    	}
    	pimg.save(new File(pipFileName));
    	
    	// 创建一个cts文件，每一帧图片对应一个动画帧和一个动画序列
    	PipAnimateSet pas = new PipAnimateSet();
    	pas.addSourceFile(new File(pipFileName).getName(), pimg);
    	for (int i = 0; i < pimg.getFrameCount(); i++) {
    		// 创建帧
    		PipAnimateFrame f = pas.addFrame(String.valueOf(i));
    		PipAnimateFramePiece p = f.addPiece(0, i);
    		p.setDx(-pimg.getImageData(i).getWidth() / 2);
    		p.setDy(-pimg.getImageData(i).getHeight() / 2);
    		
    		// 创建动画序列
    		PipAnimate a = pas.addAnimate(String.valueOf(i));
    		a.addFrame(i);
    	}
    	pas.save(new File(ctsFileName), true);
    	String ctnFileName = Utils.replaceSuffix(ctsFileName, "ctn");
    	pas.save(new File(ctnFileName), false);
    }
    
    /*
     * 为当前选中的目录在其父目录下创建一个镜像目录。镜像目录里所有内容和原目录完全相同。
     */
    protected void onCreateCopy() {
    	// 选择一个新目录
    	File dir = getSelectedFile();
    	if (!dir.isDirectory()) {
    		return;
    	}
    	InputDialog dlg = new InputDialog(getSite().getShell(), "输入", "请输入镜像目录名：", dir.getName(), new IInputValidator() {
    		public String isValid(String value) {
    			if (value.trim().length() == 0) {
    				return "请输入合法的目录名。";
    			}
    			if (new File(getSelectedFile().getParentFile(), value.trim()).exists()) {
    				return "此目录已存在。";
    			}
    			return null;
    		}
    	});
    	if (dlg.open() != InputDialog.OK) {
    		return;
    	}
    	
    	// 创建目标目录
    	File targetDir = new File(dir.getParentFile(), dlg.getValue().trim());
    	if (!targetDir.mkdir()) {
    		MessageDialog.openError(getSite().getShell(), "错误", "创建目录失败。");
    		return;
    	}
    	
        // 创建镜像设置文件并执行同步操作
        try {
	        MirrorData mdata = new MirrorData(dir, targetDir);
	        mdata.sync();
        } catch (Exception e) {
        	SWTUtils.showError(getSite().getShell(), "错误", "创建镜像中发生错误。", e);
        }
        
        viewer.refresh(dir.getParentFile());
    }
    
    /*
     * 使选中的镜像目录和源目录做一次同步。
     */
    protected void onSync() {
    	File dir = getSelectedFile();
    	if (!dir.isDirectory()) {
    		return;
    	}
    	if (!new File(dir, "mirror.xml").exists()) {
    		// 不是镜像目录
    		MessageDialog.openError(getSite().getShell(), "错误", "你选择的目录不是一个镜像目录。");
    		return;
    	}
    	
    	// 执行同步操作
    	try {
	        MirrorData mdata = new MirrorData(null, dir);
	        mdata.sync();
        } catch (Exception e) {
        	SWTUtils.showError(getSite().getShell(), "错误", "同步过程中发生错误。", e);
        }
    	
    	viewer.refresh(dir);
    }

	/*
	 * 搜索目录中所有pip文件，并且合并起来放到一个大图片中，准备批量调整颜色
	 */
	protected void onAdjustColor() throws Exception {
		File dir = getSelectedFile();
    	if (!dir.isDirectory()) {
    		return;
    	}
    
    	// 搜索出所有pip文件和ldf文件
    	Set<String> fileSet = new HashSet<String>();
    	Utils.findFilesInDir(dir, ".pip", fileSet);
    	Utils.findFilesInDir(dir, ".ldf", fileSet);
    	
    	// 检查目标目录是否是一个镜像目录或其子目录
    	File mirrorDir = dir;
    	while (mirrorDir != null && !MirrorData.isMirrorDir(mirrorDir)) {
    		mirrorDir = mirrorDir.getParentFile();
    		if (mirrorDir.getName().equals("data")) {
    			mirrorDir = null;
    			break;
    		}
    	}
    	
    	if (mirrorDir != null) {
        	// 如果是镜像目录，检查目标目录中有哪些文件在做镜像之后已经被修改过
    		MirrorData mdata = new MirrorData(null, mirrorDir);
    		Set<String> modifySet = new HashSet<String>();
    		for (String p : fileSet) {
    			String rpath = Utils.getRelatePath(p, mirrorDir.getAbsolutePath());
    			int oldV = mdata.getFileVersion(rpath);
    			int newV = MirrorData.getFileCRCVersion(new File(p));
    			if (newV != oldV) {
    				modifySet.add(p);
    			}
    		}
    		
    		// 如果有文件被修改过，提示是否一起做调色，还是只调没有修改过的文件
    		if (modifySet.size() > 0) {
    			String msg = "有" + modifySet.size() + "个文件在上次同步后被修改，可能已经调过色了。这些文件是否需要参与本次调色？";
    			if (!MessageDialog.openQuestion(getSite().getShell(), "确认", msg)) {
    				for (String p : modifySet) {
    					fileSet.remove(p);
    				}
    			}
    		}
    	}
    	
    	if (fileSet.size() == 0) {
    		MessageDialog.openInformation(getSite().getShell(), "信息", "没有找到需要调色的文件。");
    		return;
    	}
    	
    	// 终于可以调色啦，把所有的pip和ldf的帧合并成一个大图片，导出成一个png文件和一个.s文件。
    	FileDialog dlg = new FileDialog(getSite().getShell(), SWT.SAVE);
    	dlg.setText("请选择保存中间结果的图片文件");
        dlg.setFilterExtensions(new String[] { "*.png" });
        dlg.setFilterNames(new String[] { "PNG文件(*.png)" });
        String picFile = dlg.open();
        if (picFile == null) {
            return;
        }
        
        List<PipImage> images = new ArrayList<PipImage>();
        List<String> imageFiles = new ArrayList<String>();
        for (String p : fileSet) {
        	if (p.toLowerCase().endsWith(".ldf")) {
        		LandformImage img = new LandformImage();
        		img.load(p);
        		images.add(img);
        	} else {
        		PipImage img = new PipImage();
        		img.load(p);
        		images.add(img);
        	}
        	imageFiles.add(p);
        }
        
        Point[] pos = getBestLayout(images);
        
        ImageData bufferData = new ImageData(pos[0].x, pos[0].y, 32, new PaletteData(0x0000FF00, 0x00FF0000, 0xFF000000));
        bufferData.alphaData = new byte[pos[0].x * pos[0].y];
        int index = 0;
        for (PipImage img : images) {
	        for (int i = 0; i < img.getImgCount(); i++, index++) {
	            Image frameImg = img.getImageDraw(i).createSWTImage(getSite().getShell().getDisplay(), 0);
	            ImageData fdata = frameImg.getImageData();
	            for (int y = 0; y < fdata.height; y++) {
	            	for (int x = 0; x < fdata.width; x++) {
	            		int tx = pos[index + 1].x + x;
	            		int ty = pos[index + 1].y + y;
	            		int value = fdata.getPixel(x, y);
	            		bufferData.alphaData[ty * bufferData.width + tx] = (byte)(value & 0xFF);
	            		int sp = ty * bufferData.bytesPerLine + tx * 4;
	            		bufferData.data[sp++] = (byte)((value >> 24) & 0xFF);
	            		bufferData.data[sp++] = (byte)((value >> 16) & 0xFF);
	            		bufferData.data[sp++] = (byte)((value >> 8) & 0xFF);
	            		bufferData.data[sp++] = (byte)(value & 0xFF);
	            	}
	            }
	            frameImg.dispose();
	        }
        }
            
        // Write .png file
        Image tmpImg = new Image(getSite().getShell().getDisplay(), bufferData);
        PngEncoder enc = new PngEncoder(tmpImg);
        FileOutputStream fos = new FileOutputStream(picFile);
        enc.encode32(fos, false);
        fos.close();
        tmpImg.dispose();
            
        // Write .s file
        int lastPos = picFile.lastIndexOf('.');
        String sfile = picFile.substring(0, lastPos) + ".s";
        ImageDescription imageDesc = new ImageDescription();
        imageDesc.type = ImageDescription.VERSION_4;
        index = 0;
        for (PipImage img : images) {
	        for (int i = 0; i < img.getImgCount(); i++, index++) {
	        	TileInfo2 ti2 = new TileInfo2();
	            ti2.x = pos[index + 1].x;
	            ti2.y = pos[index + 1].y;
	            ti2.width = img.getImageData(i).getWidth();
	            ti2.height = img.getImageData(i).getHeight();
	            imageDesc.tileList2.add(ti2);
	        }
        }
        imageDesc.save(new File(sfile));
        
        // 提示对目标文件进行调色，然后继续
        String msg = "中间文件已保存，请用绘图软件对其进行颜色处理，然后点击OK继续。\n注意：颜色处理完成之前，请不要点击OK。";
        MessageDialog.openInformation(getSite().getShell(), "信息", msg);
        while (!MessageDialog.openQuestion(getSite().getShell(), "确认", "对中间文件的颜色处理已经完成了吗？")) {
        }
        
        // OK，我们继续，读取PNG图片中的内容更新所有的pip
        ImageDescription id = new ImageDescription();
        id.load(new File(sfile));
        index = 0;
        Object[] tiles = id.getTileList();
        Image newImg = new Image(getSite().getShell().getDisplay(), picFile);
        for (int iid = 0; iid < images.size(); iid++) {
        	PipImage img = images.get(iid);
        	int fcount = img.getImgCount();
        	
        	// 清空数据
        	img.getImagePalettes().clear();
        	img.getImageDatas().clear();
        	
        	// 读取调色板
        	boolean oldColorSetting = img.isSupportMoreColors();
        	img.setSupportMoreColors(true);
        	PipImage.initPalette(img, new File(picFile));
        	
        	// 读取帧数据
	        for (int i = 0; i < fcount; i++, index++) {
	        	TileInfo2 info = (TileInfo2)tiles[index];
	        	int[][] rawData = ImageViewer.getImageData(newImg, new Rectangle(info.x, info.y, info.width, info.height));
	        	img.addFrame(rawData);
	        	if ((info.param & ImageDescription.T_HORIZONTAL) > 0) {
	        		img.getImageData(img.getImgCount() - 1).hflip();
	            }
	            if ((info.param & ImageDescription.T_VERTICAL) > 0) {
	            	img.getImageData(img.getImgCount() - 1).vflip();
	            }
	        }
	        
	        // 优化调色板
	        int[] unused = img.getNonUsedColors();
            if (unused.length != 0) {
            	img.deleteColors(unused);
            }
            
            // 保存图片
            img.setSupportMoreColors(oldColorSetting);
            img.save(new File(imageFiles.get(iid)));
        }
        newImg.dispose();
        
        // 处理完成
        MessageDialog.openInformation(getSite().getShell(), "信息", "恭喜，操作全部成功！");
	}
	
	// 计算多个PIP合在一张大图的布局。
	private Point[] getBestLayout(List<PipImage> imgs) {
		int w = 0, h = 0;
		int count = 0;
		for (PipImage img : imgs) {
			for (int i = 0; i < img.getImgCount(); i++, count++) {
				PipImageData data = img.getImageData(i);
				w += data.getWidth() + 2;
				if (data.getHeight() + 2 > h) {
					h = data.getHeight() + 2;
				}
			}
		}
		if (w / h > 3) {
			w = (int)(w / Math.sqrt(w / (h * 3)));
		}
		Point[] ret = new Point[count + 1];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = new Point(0, 0);
		}
		int rw = 0, lh = 0, dx = 0, dy = 0;
		int index = 0;
		for (PipImage img : imgs) {
			for (int i = 0; i < img.getImgCount(); i++, index++) {
				PipImageData data = img.getImageData(i);
				if (dx != 0 && dx + data.getWidth() + 2 > w) {
					dx = 0;
					dy += lh;
					lh = 0;
					i--;
					index--;
					continue;
				} else {
					ret[index + 1].x = dx;
					ret[index + 1].y = dy;
					dx += data.getWidth() + 2;
					if (lh < data.getHeight() + 2) {
						lh = data.getHeight() + 2;
					}
					if (dx > rw) {
						rw = dx;
					}
				}
			}
		}
		ret[0].x = rw;
		ret[0].y = dy + lh;
		return ret;
	}
}
