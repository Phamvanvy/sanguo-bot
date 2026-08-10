package com.pip.image.workshop;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
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
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.jdom.Element;

import com.pip.mapeditor.data.MapFile;
import com.pip.mapeditor.data.ProjectOwner;
import com.pip.mapeditor.data.ProjectParser;
import com.pip.mapeditor.data.TileSet;
import com.pip.util.FileExtensionFilter;
import com.pip.util.Utils;
import com.pipimage.image.PipAnimateSet;

public class ProjectView extends ViewPart implements IDoubleClickListener, ISelectionChangedListener {

	public static final String ID = "com.pip.image.workshop.ProjectView"; //$NON-NLS-1$

	@Override
	public void dispose() {
		super.dispose();
		ProjectOwner.disposeAll();
	}
	private TreeViewer viewer;
	private static FileExtensionFilter imageFilter = new FileExtensionFilter(new String[] { "png", "gif", "pip", "p", "cts", "ldf", "lfi", "map" }, true);

	private Action setAsGroupDir;
	private Action cancelGroupDir;
	private Action newDirAction;
	private Action newPipImageAction;
	private Action newLibModeMapAction;
	private Action newAnimateAction;
	private Action newMapAction;
	private Action newLandformAction;
	private Action refreshAction;
	private Action exploreAction;
//	private Action importAction;
	private Action openAction;
	private Action deleteAction;
	private Action chooseDirAction;
	private Action copyAction;
	private Action pasteAction;
	private Action updateFileMapAction;
//	private FileDialog importDialog;
	
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
					}else{
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
		createActions();
		initializeToolBar();
		initializeMenu();
	}

	private void createProjectOwners() {
		for(Element el:(List<Element>)Settings.projects.getChildren()){
			prjEl2prjOwner.put(el, ProjectOwner.find(el.getAttributeValue("dir")));
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
		//event == null -> manually call to refresh menu
		if (event==null || event.getSelectionProvider() == viewer) {
			Object el = getSelectedObj();
			MenuManager mgr = new MenuManager();

			if(isPrjEl(el)){
				mgr.add(setPrjDirAction);
				mgr.add(newPrjAction);
				mgr.add(deleteAction);
				mgr.add(propertyAction);
				mgr.add(renameAction);
				Element prjEl = getSelectedElement();
				String prjDir = prjEl.getAttributeValue("dir"); 
				if(prjDir.equals("")==false&&prjDir.endsWith("data")==false){
					mgr.add(newDirAction);
				}
			}else if(el instanceof File){
				File selFile = (File)el;
				if (selFile.isFile()) {
					mgr.add(openAction);
					mgr.add(copyAction);
					mgr.add(deleteAction);
					mgr.add(newDirAction);
					mgr.add(pasteAction);
					if(selFile.getName().toLowerCase().endsWith(".map")){
						makeSwitchGroupSubmenu(mgr,true);
					}else{
						makeGroupSubmenu(mgr);
					}
				} else if(selFile.isDirectory()){
					if(selFile.getName().equals("pipLib")){
						mgr.add(exploreAction);
						mgr.add(newDirAction);
						makeSwitchGroupSubmenu(mgr,false);
					}else {
						mgr.add(newPipImageAction);
						mgr.add(newMapAction);
						mgr.add(newLibModeMapAction);
						mgr.add(newAnimateAction);
						mgr.add(newLandformAction);
						mgr.add(newDirAction);
						mgr.add(new Separator());
						mgr.add(updateFileMapAction);
						mgr.add(exploreAction);
						mgr.add(pasteAction);
						mgr.add(deleteAction);
					}
					//folder under pipLib, if it's not group DIR, add setTo menu item
					if(selFile.getParentFile().getName().equals("pipLib")){
						if(getSelectedPrjOwner().checkGroupDir(selFile)==false){
							mgr.add(setAsGroupDir);
						}else if(getSelectedPrjOwner().checkGroupDir(selFile, true)==false){
							mgr.add(cancelGroupDir);
						}
					}
//					mgr.add(importAction);
				}
				if(clipboardHasFile()==false){
					pasteAction.setEnabled(false);
				}else{
					pasteAction.setEnabled(true);
				}
			}
			mgr.add(refreshAction);
			
			Menu menu = mgr.createContextMenu(viewer.getTree());
			viewer.getTree().setMenu(menu);
			
			if(el instanceof File){//preview
				try {
					File selFile = (File)el;
					if (selFile.isFile() && selFile.getName().endsWith(".lfi")==false && selFile.getName().endsWith(".map")==false) {
						openFile(selFile);
					}
				} catch (Exception e) {
					e.printStackTrace();
					MessageDialog.openError(getSite().getShell(), "错误", e.toString());
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
	public int activeResHashCode;
	public static boolean isPrjEl(Object obj){
		return obj instanceof Element && ((Element)obj).getName().equals("project");
	}
	private static Shell shell;
	private Action newPrjAction;
	private Action setPrjDirAction;
	private Action renameAction;
	private Action propertyAction;
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
		copyAction = new Action("复制"){
			public void run(){
				doCopy();
			}
		};
		updateFileMapAction = new Action("更新文件映射"){
			public void run(){
				doUpdateFileMap();
			}
		};
		pasteAction = new Action("粘贴"){
			public void run(){
				doPaste();
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
		renameAction = new Action("重命名"){
			public void run(){
				String input = getInput("重命名", "请输入新名称:");
				if(input!=null){
					Element el = getSelectedElement();
					el.getAttribute("name").setValue(input);
					viewer.refresh(el);
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
					e.printStackTrace();
					MessageDialog.openError(getSite().getShell(), "错误", e.toString());
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
		    	File dir = getSelectedFile();
		    	if(dir != null && dir.isDirectory()){
                    String cmd = "explorer.exe \"" + dir.getAbsolutePath() + "\"";
                    try {
                        Runtime.getRuntime().exec(cmd);
                    } catch (Exception e) {
                    }
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
					e.printStackTrace();
					MessageDialog.openError(getSite().getShell(), "错误", e.toString());
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
					e.printStackTrace();
					MessageDialog.openError(getSite().getShell(), "错误", e.toString());
				}
			}
		};
		
		newMapAction = new Action("新建地图") {
			public void run() {
				Object selObj = getSelectedFile();
				File file = newMap((File)selObj);
				viewer.refresh(selObj);
				try {
					if (file != null) {
						openFileEditor(file);
					}
				} catch (Exception e) {
					e.printStackTrace();
					MessageDialog.openError(getSite().getShell(), "错误", e.toString());
				}
			}
		};
		newLibModeMapAction = new Action("新建地图(库模式)") {
			public void run() {
				Object selObj = getSelectedFile();
				File file = newMap((File)selObj,true);
				viewer.refresh(selObj);
				viewer.expandToLevel(selObj, 1);
				try {
					if (file != null) {
						openFileEditor(file);
					}
				} catch (Exception e) {
					e.printStackTrace();
					MessageDialog.openError(getSite().getShell(), "错误", e.toString());
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
                    e.printStackTrace();
                    MessageDialog.openError(getSite().getShell(), "错误", e.toString());
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
			prjEl2prjOwner.put(el, ProjectOwner.find(""));
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
			}
		}
		File selFile = getSelectedFile();
		if (selFile != null) {
			viewer.refresh(selFile);
		}else{
		}		
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
		}
		File f = getSelectedFile();
		if(f!=null){// && f.isFile()){
			if(f.isDirectory() && f.list().length>0){
				MessageDialog.openInformation(shell, "提示", "该文件夹不为空.不能删除");
				return;
			}else if(f.isFile()){
				try{
					if(ProjectParser.getFileRefList(f.getAbsolutePath()).length>0){
						MessageDialog.openInformation(shell, "提示", "该文件已被其他文件引用.不能删除");
						return;
					}
				}catch(Exception e){
					reportError("检查文件是否被引用时出现错误:\n"+e);
				}
			}
			String msg = "确定删除>"+f.getName()+"<吗?";
			if(MessageDialog.openConfirm(shell, "确认", msg)==false){
				return;
			}
			if(f.getAbsolutePath().endsWith(".map")){
				doRemoveMapRef(f);
			}
			boolean deled = f.delete();
			if(deled && getSelectedPrjOwner().checkGroupDir(f)){
				try {
					getSelectedPrjOwner().removeGroupDir(f);
				} catch (Exception e) {
					e.printStackTrace();
					reportError(e.toString());
				}
			}
			//BUG, f may directly under data directory, and then it's parent is Project Element, not File
			viewer.refresh(f.getParentFile());
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
			e.printStackTrace();
			MessageDialog.openError(shell, "错误", "设置组文件夹时出现错误:\n"+e.toString());
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
			}else{
				
			}
		}else{
			MessageDialog.openError(shell, "错误", "文件夹:"+dirName+" 已经存在.");
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
			MessageDialog.openError(getSite().getShell(), "错误", e.toString());
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
}
