package com.pip.j0ide;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import com.pip.gtl.codegen.GTLFunctionCallGenerator;
import com.pip.gtl.codegen.syscall.SystemFunctionManager;
import com.pip.gtl.preprocess.GTLPreProcessor;
import com.pip.gtleditor.util.EditUtils;
import com.pip.j0ide.data.Model;
import com.pip.j0ide.data.ProjectData;
import com.pip.j0ide.data.Variable;
import com.pip.j0ide.editors.ChooseModelDialog;
import com.pip.j0ide.editors.GTLEditor;
import com.pip.uieditor.editor.FrameEditor;
import com.pip.uieditor.editor.NewUIDialog;
import com.pip.uieditor.model.Screen;
import com.pip.uieditor.model.persist.PersistMapping;
import com.pip.uieditor.model.util.ScreenModelWriter;
import com.pip.util.FileExtensionFilter;
import com.pip.util.Utils;

public class DirectoryView extends ViewPart implements IDoubleClickListener, ISelectionChangedListener {
	private Action newGTLAction;
	private Action newUIAction;
	private Action newDirAction;
	private Action compileDirAction;
	private Action generateAllUICodeAction;
	private Action refreshAction;
	private Action expandAllAction;
	private Action switchProjectAction;
	private Tree tree;
	private Action editAction;
	private Action deleteAction;
	private Action newModelAction;
	private SearchInFilesDialog searchInFilesDialog;
	private MenuManager projectHistoryMenu;
	public static final String ID = "com.pip.j0ide.directoryview";

	private TreeViewer viewer;
	private static FileExtensionFilter gtlFilter = new FileExtensionFilter(new String[] { "gtl", "h", "properties", "ui" }, true);
	static class ViewContentProvider implements ITreeContentProvider {
		private ProjectData project;
		
		public Object[] getElements(Object inputElement) {
			return ProjectData.TYPE_NAMES;
		}
		
		public ViewContentProvider(ProjectData proj) {
			this.project = proj;
		}
		
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ProjectData) {
				return ProjectData.TYPE_NAMES;
			} else if (parentElement instanceof String) {
				if (parentElement == ProjectData.TYPE_GLOBAL) {
                    return new Object[0];
                } else if (parentElement == ProjectData.TYPE_MODEL) {
					return project.getModels();
				} else if (parentElement == ProjectData.TYPE_SCRIPT) {
					File[] files = project.getGTLDir().listFiles(gtlFilter);
					File[] linkFiles = null;
					int size = files.length;
					if(project.getLinkDir() != null) {
						linkFiles = project.getLinkDir().listFiles(gtlFilter);
						if(linkFiles != null) {
							size += linkFiles.length;							
						}
					}
					
					File[] ret = new File[size];
					System.arraycopy(files, 0, ret, 0, files.length);
					if(linkFiles != null) {
						System.arraycopy(linkFiles, 0, ret, files.length, linkFiles.length);						
					}
					return ret;
				}
	    	} else if (parentElement instanceof File) {
	    		File f = (File)parentElement;
	    		if (f.isDirectory()) {
	    		    File[] tmp = f.listFiles(gtlFilter);
	    		    Arrays.sort(tmp);
	    			return tmp;
	    		} else {
	    			return new Object[0];
	    		}
	    	}
	    	return new Object[0];
		}

	    public Object getParent(Object element) {
	    	if (element instanceof ProjectData) {
	    		return null;
	    	} else if (element instanceof String) {
	    		return null;
	    	} else if (element instanceof Model) {
	    		return ProjectData.TYPE_MODEL;
	    	} else if (element instanceof File) {
	    		File f = (File)element;
	    		if (f.equals(project.getGTLDir())) {
	    			return ProjectData.TYPE_SCRIPT;
	    		} else {
	    			return f.getParentFile();
	    		}
            } else {
	    		return null;
	    	}
	    }

	    public boolean hasChildren(Object element) {
	    	if ((element instanceof ProjectData) ||
	    		(element instanceof String && element != ProjectData.TYPE_GLOBAL && element != ProjectData.TYPE_PROJ_CONFIG)) {
	    		return true;
	    	} else if (element instanceof File) {
	    		return ((File)element).isDirectory();
	    	} else {
	    		return false;
	    	}
	    }
	    
	    public void dispose() {}

	    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	}
	
	static class ViewLabelProvider extends LabelProvider {
		public Image getImage(Object element) {
			String imageName = null;
			if (element instanceof String) {
				if (element == ProjectData.TYPE_GLOBAL) {
                    imageName = "model";
                } else if (element == ProjectData.TYPE_MODEL) {
					imageName = "model";
				} else if (element == ProjectData.TYPE_SCRIPT) {
					imageName = "gtls";
				} else if(element == ProjectData.TYPE_REVISION){
					imageName = "model";
				} else if(element == ProjectData.TYPE_PROJ_CONFIG){
					imageName = "model";
				}
			} else if (element instanceof Model) {
				imageName = "model";
			} else if (element instanceof File) {
				File f = (File)element;
				if (f.isDirectory()) {
					imageName = "folder";
				} else {
					imageName = "gtl";
				}
			}
			if (imageName != null) {
				return Activator.getDefault().getImageRegistry().get(imageName);
			} else {
				return null;
			}
		}
	
		public String getText(Object element) {
			if (element == null) {
				return "";
			}
			if (element instanceof File) {
				return ((File)element).getName();
			} else {
				return element.toString();
			}
		}
	}
	
	private class ProjectShortcutAction extends Action {
	    private String path;

	    public ProjectShortcutAction(String path) {
	        super(path);
	        this.path = path;
	    }
	    
        public void run() {
            if (!getSite().getWorkbenchWindow().getActivePage().closeAllEditors(true)) {
                return;
            }
            switchProjectImpl(path);
        }
    }

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent);
		tree = viewer.getTree();

		final DragSource treeDragSource = new DragSource(tree, DND.DROP_COPY);
		treeDragSource.addDragListener(new DragSourceAdapter() {
			public void dragStart(DragSourceEvent event) {
			    Object[] sels = getSelectedObjects();
			    if (sels.length == 0) {
			        event.doit = false;
			    } else {
			        event.doit = false;
			        for (int i = 0; i < sels.length; i++) {
	                    if (sels[i] instanceof Model) {
	                        event.doit = true;
	                        break;
	                    }
			        }
			    }
			}
			
			public void dragSetData(DragSourceEvent event) {
			    Object[] sels = getSelectedObjects();
			    StringBuffer buf = new StringBuffer();
			    for (int i = 0; i < sels.length; i++) {
			        if (i > 0) {
			            buf.append("\n");
			        }
			        if (sels[i] instanceof Model) {
			            buf.append("Model:" + ((Model)sels[i]).id);
	                }
			    }
			    event.data = buf.toString();
			}
			
			public void dragFinished(DragSourceEvent event) {
			}
		});
		treeDragSource.setTransfer(new Transfer[] {TextTransfer.getInstance()});

		final DropTarget treeDropTarget = new DropTarget(tree, DND.DROP_COPY);
		treeDropTarget.addDropListener(new DropTargetAdapter() {
			public void dragEnter(DropTargetEvent event) {
			}
			public void dragLeave(DropTargetEvent event) {
			}
			public void dragOperationChanged(DropTargetEvent event) {
			}
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_NONE | DND.FEEDBACK_SCROLL;
				event.detail = DND.DROP_NONE;
				if (event.item != null) {
					TextTransfer textTransfer = TextTransfer.getInstance();
					String data = (String)textTransfer.nativeToJava(event.currentDataType);
					if (data == null) {
						return;
					}
					TreeItem titem = (TreeItem)event.item;
					Object targetObj = titem.getData();
					String[] items = data.split("\n");
					for (int i = 0; i < items.length; i++) {
    					if (items[i].startsWith("Model:")) {
    					}
					}
				}
			}
			public void drop(DropTargetEvent event) {
				if (event.data == null || event.item == null) {
					return;
				}
				TreeItem titem = (TreeItem)event.item;
				Object targetObj = titem.getData();
				String data = (String)event.data;
				String[] items = data.split("\n");
				boolean needSaveProject = false;
				boolean needRefreshTarget = false;
                for (int kk = 0; kk < items.length; kk++) {
                    data = items[kk];
    				if (data.startsWith("Model:")) {
    				}
                }
                if (needSaveProject) {
                    save();
                }
                if (needRefreshTarget) {
                    viewer.refresh(targetObj);
                }
			}
			public void dropAccept(DropTargetEvent event) {
			}
		});
		treeDropTarget.setTransfer(new Transfer[] {TextTransfer.getInstance()});
		
		viewer.setContentProvider(new ViewContentProvider(Application.getInstance().getProjectData()));
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(Application.getInstance().getProjectData());
		viewer.addDoubleClickListener(this);
		viewer.addSelectionChangedListener(this);
		createActions();
		initializeToolBar();
		initializeMenu();
	}
	
	private void save() {
		try {
			Application.getInstance().getProjectData().save();
		} catch (Exception e) {
			MessageDialog.openError(getSite().getShell(), "保存数据失败", e.toString());
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
	
	public void refreshAll() {
		this.viewer.refresh();
	}
	
	private Object[] getSelectedObjects() {
	    IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
        return sel.toArray();
	}
	
	public void doubleClick(DoubleClickEvent event) {
		if (event.getViewer() == viewer) {
			Object selObj = getSelectedObject();
			if (selObj == null) {
			    return;
			}
			if (selObj instanceof Model || selObj instanceof File) {
				if (selObj instanceof File && ((File)selObj).isDirectory()) {
					expandOrCollapseNode(selObj);
					return;
				}
				try {
					editObject(selObj);
				} catch (Exception e) {
					e.printStackTrace();
					MessageDialog.openError(getSite().getShell(), "错误", e.toString());
				}
			} else if (selObj instanceof String) {
				if (selObj == ProjectData.TYPE_GLOBAL || selObj == ProjectData.TYPE_REVISION || selObj == ProjectData.TYPE_PROJ_CONFIG) {
					try {
						editObject(selObj);
					} catch (Exception e) {
						e.printStackTrace();
						MessageDialog.openError(getSite().getShell(), "错误", e.toString());
					}
				} else {
					expandOrCollapseNode(selObj);
				}
			} else {
				expandOrCollapseNode(selObj);
			}
		}
	}
	
	private void expandAll(Object node) {
		viewer.expandToLevel(node, 100);
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
		    Object selObj = getSelectedObject();
		    if (selObj == null) {
		        viewer.getTree().setMenu(null);
		        return;
			}
			MenuManager mgr = new MenuManager();
			if (selObj instanceof String) {
				if (selObj == ProjectData.TYPE_MODEL) {
					mgr.add(newModelAction);
				} else if (selObj == ProjectData.TYPE_SCRIPT) {
					mgr.add(newGTLAction);
					mgr.add(newUIAction);
					mgr.add(compileDirAction);
					mgr.add(newDirAction);
				} else if (selObj == ProjectData.TYPE_GLOBAL) {
                    mgr.add(editAction);
				}
			} else {
				if (selObj instanceof File) {
					File f = (File)selObj;
					if (f.isFile()) {
						mgr.add(editAction);
						mgr.add(deleteAction);
					} else {
						mgr.add(newGTLAction);
						mgr.add(newUIAction);
						mgr.add(compileDirAction);
						mgr.add(generateAllUICodeAction);
					}
				} else {
					mgr.add(editAction);
					mgr.add(deleteAction);
				}
			}
			if (viewer.isExpandable(selObj)) {
				mgr.add(expandAllAction);
				mgr.add(refreshAction);
			}
			
			Menu menu = mgr.createContextMenu(viewer.getTree());
			viewer.getTree().setMenu(menu);
		}
	}
	
	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
	}
	private void createActions() {
		newModelAction = new Action("新建机型") {
			public void run() {
				Model model = Application.getInstance().getProjectData().newModel();
				viewer.refresh(ProjectData.TYPE_MODEL);
				save();
				try {
					editObject(model);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		deleteAction = new Action("删除") {
			public void run() {
				Object[] selObjs = getSelectedObjects();
				for (int i = 0; i < selObjs.length; i++) {
				    Object selObj = selObjs[i];
    				if (selObj instanceof Model) {
    					Model model = (Model)selObj;
    					String msg = "你确定要删除机型[" + model.title + "]吗？";
    					if (MessageDialog.openConfirm(getSite().getShell(), "确认", msg)) {
    						stopEdit(selObj);
    						Application.getInstance().getProjectData().deleteModel(model);
    						viewer.refresh();
    						save();
    					}
    				} else if (selObj instanceof File) {
    					File f = (File)selObj;
    					String msg = "你确定要删除文件" + f.getName() + "吗？";
    					if (MessageDialog.openConfirm(getSite().getShell(), "确认", msg)) {
    						stopEdit(selObj);
    						f.delete();
    						if (f.getParentFile().equals(Application.getInstance().getProjectData().getGTLDir())) {
    							viewer.refresh(ProjectData.TYPE_SCRIPT);
    						} else {
    							viewer.refresh(f.getParentFile());
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
					editObject(selObj);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		switchProjectAction = new Action("打开项目...") {
			public void run() {
				switchProject();
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
		
		expandAllAction = new Action("全部展开") {
			public void run() {
				Object selObj = getSelectedObject();
				if (selObj != null) {
					expandAll(selObj);
				}
			}
		};

		newGTLAction = new Action("新建游戏脚本") {
			public void run() {
				Object selObj = getSelectedObject();
				File newFile;
				if (selObj instanceof String) {
					newFile = newGTL(Application.getInstance().getProjectData().getGTLDir());
				} else {
					newFile = newGTL((File)selObj);
				}
				viewer.refresh(selObj);
				try {
					if (newFile != null) {
						//初始化脚本默认函数
						EditUtils.createEmptyTaskUI(newFile);
						editObject(newFile);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		newUIAction = new Action("新建UI") {
			public void run() {
				Object selObj = getSelectedObject();
				File newFile = null;
				if (selObj instanceof String) {
					newFile = newUI(Application.getInstance().getProjectData().getGTLDir());
				} else if(selObj instanceof File) {
					newFile = newUI((File)selObj);
				}
				viewer.refresh(selObj);
				try {
					if (newFile != null) {
						//初始化脚本默认函数
						editObject(newFile);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		newDirAction = new Action("新建文件夹") {
			public void run() {
				Object selObj = getSelectedObject();
//				File newFile;
				if (selObj instanceof String) {
//					newFile = 
						newDir(Application.getInstance().getProjectData().getGTLDir());
				} else {
//					newFile = 
						newDir((File)selObj);
				}
				viewer.refresh(selObj);
			}
		};
		
		compileDirAction = new Action("编译所有脚本") {
		    public void run() {
                Object selObj = getSelectedObject();
                File dir;
                if (selObj instanceof String) {
                    dir = Application.getInstance().getProjectData().getGTLDir();
                } else {
                    dir = (File)selObj;
                }
                
                // 保存所有GTL
                IEditorPart[] editors = getSite().getPage().getDirtyEditors();
                for (int i = 0; i < editors.length; i++) {
                    if (editors[i] instanceof GTLEditor) {
                        getSite().getPage().saveEditor(editors[i], false);
                    }
                }

                // 查找所有需要编译的文件
                List<File> allFiles = new ArrayList<File>();
                findAllGTL(dir, allFiles);
                if (allFiles.size() == 0) {
                	String msg = "没有找到需要编译的文件.\n" +
                			"请检查<全局变量><GTLVersion>设置是否正确.\n" +
                			"如果没有此变量,请新建,并设置为脚本里的VERSION值.";
                    MessageDialog.openInformation(getSite().getShell(), "信息", msg);
                    return;
                }
                
                // 选择目标机型和目录
                ChooseModelDialog modelDlg = new ChooseModelDialog(getSite().getShell());
                modelDlg.setDefaultPath(dir.getAbsolutePath());
                if (modelDlg.open() != ChooseModelDialog.OK) {
                    return;
                }
                String outputPath = modelDlg.getOutputPath();
                File targetDir = dir;
                if (outputPath.length() > 0) {
                    targetDir = ChooseModelDialog.resolveFile(targetDir, outputPath);
                }
                Model[] targetModels = modelDlg.getChoosenModels();
                boolean force = modelDlg.isForceCompile();
                boolean returnCheck = modelDlg.isForceReturnCheck();
                
                // 显示并清空Output窗口
                try {
        			getSite().getWorkbenchWindow().getActivePage().showView(ErrorsView.ID);
        			Application.getInstance().getErrorsView().clear();
            		getSite().getWorkbenchWindow().getActivePage().showView(ConsoleView.ID);
                    Application.getInstance().getConsole().clear();
                } catch (Exception e) {
                	e.printStackTrace();
                }
                //拷贝.info
                try {
                	modelDlg.copyDotInfo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                File[] fileArr = new File[allFiles.size()];
                allFiles.toArray(fileArr);
                GTLEditor.CompilerJob job = new GTLEditor.CompilerJob(fileArr, targetModels, targetDir, Integer.parseInt(Settings.compileThreadCount), force, returnCheck);
                ProgressMonitorDialog progress = new ProgressMonitorDialog(getSite().getShell());
                progress.setCancelable(true);
                try {
                    progress.run(true, true, job);
                } catch (Exception e) {
                    e.printStackTrace();
                }
		    }
		};
		
		generateAllUICodeAction = new Action("生成所有UI代码") {
			public void run() {
				String msg = "你确定要重新生成所有UI代码吗？之前的UI代码将全部被覆盖呦！！！";
				if (MessageDialog.openConfirm(getSite().getShell(), "确认", msg)) {
					
	                Object selObj = getSelectedObject();
	                File dir;
	                if (selObj instanceof String) {
	                    dir = Application.getInstance().getProjectData().getGTLDir();
	                } else {
	                    dir = (File)selObj;
	                }
	                ArrayList<File> uiFiles = new ArrayList<File>();
	                findAllUIFiles(dir, uiFiles);
	                if (uiFiles.size() == 0) {
	                	msg = "没有找到UI文件";
	                    MessageDialog.openInformation(getSite().getShell(), "信息", msg);
	                } else{
	                    FrameEditor.GenerateCodeJob job = new FrameEditor.GenerateCodeJob(uiFiles);
	                    ProgressMonitorDialog progress = new ProgressMonitorDialog(getSite().getShell());
	                    progress.setCancelable(true);
	                    try{
	                    	progress.run(true, true, job);
	                    } catch(Exception e) {
	                    	e.printStackTrace();
	                    }
	                    return;
	                }               
				}
			}
		};
	}
	
	private void findAllGTL(File dir, List<File> output) {
		String projectGTLVersion = "1";
		for(Variable var:Application.getInstance().getProjectData().variables){
			if(var.name.equals("GTLVersion")){
				projectGTLVersion = var.value;
				break;
			}
		}
	    File[] files = dir.listFiles();
	    for (int i = 0; i < files.length; i++) {
	        if (files[i].isDirectory()) {
	            findAllGTL(files[i], output);
	        } else if (files[i].isFile() && files[i].getName().toLowerCase().endsWith(".gtl")) {
	            try {
	                String content = Utils.loadFileContent(files[i]);
	                boolean match = false;
	                BufferedReader br = new BufferedReader(new StringReader(content));
	                String line;
	                while ((line = br.readLine()) != null) {
	                	line = line.trim();
	                	if (!line.contains("VERSION")) {
	                		continue;
	                	}
	                	if (line.matches("\\s*VERSION\\s*" + projectGTLVersion + "\\s*;.*")) {
	                		match = true;
	                		break;
	                	}
	                }
	                if (match) {
	                    output.add(files[i]);
	                } else {
	                	System.out.println("Project GTL Verion "+projectGTLVersion+" miss math:"+files[i].getName());
	                }
	            } catch (Exception e) {
	            }
	        }
	    }
	}
	
	private void findAllUIFiles(File dir, ArrayList<File> output) {
	    File[] files = dir.listFiles();
	    for (int i = 0; i < files.length; i++) {
	        if (files[i].isDirectory()) {
	        	findAllUIFiles(files[i], output);
	        } else if (files[i].isFile() && files[i].getName().toLowerCase().endsWith(".ui")) {
	        	output.add(files[i]);
	        }
	    }		
	}
	
	private void stopEdit(Object obj) {
		IEditorPart editor = null;
		if (obj instanceof Model) {
			editor = getSite().getWorkbenchWindow().getActivePage().findEditor(new ModelInput((Model)obj));
		} else if (obj instanceof File) {
			try {
				IEditorReference[] editors = getSite().getWorkbenchWindow().getActivePage().getEditorReferences();
				for (int i = 0; i < editors.length; i++) {
					if (!(editors[i].getEditorInput() instanceof FileStoreEditorInput)) {
						continue;
					}
					FileStoreEditorInput input = (FileStoreEditorInput)editors[i].getEditorInput();
					URI url = input.getURI();
					String editFile = Utils.urlToPath(url);
					if (obj.equals(new File(editFile))) {
						editor = editors[i].getEditor(true);
						break;
					}
				}
			} catch (Exception e) {
			}
        } 
		if (editor != null) {
			getSite().getWorkbenchWindow().getActivePage().closeEditor(editor, false);
		}
	}
	
	private File newUI(File parent) {
		NewUIDialog dlg = new NewUIDialog(getSite().getShell());
		if(dlg.open() == NewUIDialog.OK) {
			String fileName = dlg.getUIName();
			if(!fileName.toLowerCase().endsWith(".ui")) {
				fileName += ".ui";
			}
			File newfile = new File(parent, fileName);
			try{
				if (!newfile.createNewFile()) {
					MessageDialog.openError(getSite().getShell(), "错误", fileName + "已经存在了。");
				}
				FileOutputStream fos = new FileOutputStream(newfile);
				try {
					int[] screenSize = dlg.getScreenSize();
					new ScreenModelWriter().write(fos, new Screen(screenSize[0], screenSize[1]), PersistMapping.getDefault());
				} catch (Exception e) {
					e.printStackTrace();
				} finally{
					fos.close();
				}
				return newfile;
			} catch(Exception e) {
				MessageDialog.openError(getSite().getShell(), "错误", "创建文件" + fileName + "失败。");
			}
		}
		return null;
	}
	
	private File newGTL(File parent) {
		InputDialog dlg = new InputDialog(getSite().getShell(), "新建游戏脚本", "请输入文件名：", "new", new IInputValidator() {
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
			if (!newname.toLowerCase().endsWith(".gtl") && !newname.toLowerCase().endsWith(".h") ) {
				newname += ".gtl";
			}
			File newfile = new File(parent, newname);
			try {
				if (!newfile.createNewFile()) {
					MessageDialog.openError(getSite().getShell(), "错误", newname + "已经存在了。");
					throw new Exception("文件名重复");
				}
				return newfile;
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), "错误", "创建文件" + newname + "失败。");
			}
		}
		return null;
	}
	private File newDir(File parent) {
		InputDialog dlg = new InputDialog(getSite().getShell(), "新建文件夹", "请输入文件夹名：", "new", new IInputValidator() {
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
			File newfile = new File(parent, newname);
			try {
				if (!newfile.mkdir()) {
					MessageDialog.openError(getSite().getShell(), "错误", newname + "已经存在了。");
					throw new Exception("文件名重复");
				}
				return newfile;
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), "错误", "创建文件" + newname + "失败。");
			}
		}
		return null;
	}
	
	private void switchProject() {
		if (!getSite().getWorkbenchWindow().getActivePage().closeAllEditors(true)) {
			return;
		}
		ProjectData prj = Application.getInstance().getProjectData();
		DirectoryDialog dlg = new DirectoryDialog(getSite().getShell());
		dlg.setFilterPath(prj.getBaseDir().getAbsolutePath());
		dlg.setText("选择目录");
		dlg.setMessage("请选择项目目录：");
		String newPath = dlg.open();
		if (newPath != null) {
		    switchProjectImpl(newPath);
		}
	}
	
	private void switchProjectImpl(String newPath) {
	    ProjectData prj = Application.getInstance().getProjectData();
        Settings.changeWorkingDir(newPath);
        refreshProjectHistoryMenu();
        try {
            prj.load(new File(newPath));
            
            File linkDir = Application.getInstance().getProjectData().getLinkDir();
        	if (linkDir != null) {
        		SystemFunctionManager.configure(new File[] {
        				new File(Settings.workingDir, "gtl/functions.properties"),
        				new File(linkDir, "core_functions.properties")
        		});
        	} else {
        		SystemFunctionManager.configure(new File[] {
        				new File(Settings.workingDir, "gtl/functions.properties")
        		});
        	}
        } catch (Exception e) {
            MessageDialog.openError(getSite().getShell(), "载入数据错误", e.toString());
        }
        viewer.refresh();
        getSite().getShell().setText("J0集成开发工具 - "+Settings.workingDir);
	}
	
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
		IToolBarManager barMngr = getViewSite().getActionBars().getToolBarManager();
		IAction action = new Action("CollapseAll"){
			public void run(){
				viewer.collapseAll();
			}
		};
		action.setImageDescriptor(Activator.imageDescriptorFromPlugin(PlatformUI.PLUGIN_ID,"/icons/full/elcl16/collapseall.gif"));
		barMngr.add(action);
		
		IAction link = new Action("关联至编辑器"){
			public void run(){
				link2editor();
			}
		};
		link.setImageDescriptor(Activator.imageDescriptorFromPlugin("org.eclipse.ui.workbench.texteditor","/icons/full/elcl16/link_to_editor.gif"));
		barMngr.add(link);

		menuManager.add(newModelAction);
		menuManager.add(switchProjectAction);
		
		projectHistoryMenu = new MenuManager("打开过的项目...");
        refreshProjectHistoryMenu();
		menuManager.add(projectHistoryMenu);
	}
	
	protected void link2editor() {
		IWorkbenchWindow win= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (win != null) {
			IWorkbenchPage page= win.getActivePage();
			if (page != null) {
				IEditorPart part= page.getActiveEditor();
				IEditorInput input = part.getEditorInput();
				if(input instanceof FileStoreEditorInput){
					FileStoreEditorInput fileStoreEditorInput = (FileStoreEditorInput)input;
					System.out.println("DirectoryView.link2editor()"+fileStoreEditorInput.getURI());
					File f = new File(fileStoreEditorInput.getURI());
					this.viewer.setSelection(new StructuredSelection(f));
				}
			}
		}
			
	}

	private void refreshProjectHistoryMenu() {
	    projectHistoryMenu.removeAll();
	    for (String s : Settings.projectHistory) {
	        projectHistoryMenu.add(new ProjectShortcutAction(s));
	    }
	}
	
	private void editObject(Object obj) throws Exception {
		if (obj instanceof Model) {
			getSite().getWorkbenchWindow().getActivePage().openEditor(new ModelInput((Model)obj), ModelEditor.ID);
		} else if (obj instanceof File) {
			IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path(((File)obj).getAbsolutePath()));
			IDE.openEditorOnFileStore(getSite().getWorkbenchWindow().getActivePage(), fileStore);
		} else if (obj == ProjectData.TYPE_GLOBAL) {
            getSite().getWorkbenchWindow().getActivePage().openEditor(new GlobalVarInput(Application.getInstance().getProjectData()), GlobalVarEditor.ID);
        } else if(obj == ProjectData.TYPE_REVISION){
        	getSite().getWorkbenchWindow().getActivePage().openEditor(new RevisionInput(Application.getInstance().getProjectData()), RevisionEditor.ID);
        } else if(obj == ProjectData.TYPE_PROJ_CONFIG){
        	getSite().getWorkbenchWindow().getActivePage().openEditor(new RevisionInput(Application.getInstance().getProjectData()), LinkDirEditor.ID);
        }
	}
	
	private void openFile(File file) throws Exception {
		IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path((file.getAbsolutePath())));
		IDE.openEditorOnFileStore(getSite().getWorkbenchWindow().getActivePage(), fileStore);
	}
	
	public void refreshNode(Object node) {
	    viewer.refresh(node);
	}
	
	public void searchGTL() {
		File baseDir = Application.getInstance().getProjectData().getGTLDir();
		File linkDir = Application.getInstance().getProjectData().getLinkDir();
		SearchGTLDialog dlg = new SearchGTLDialog(getSite().getShell(), baseDir, linkDir);
		if (dlg.open() == SearchGTLDialog.OK) {
			try {
				openFile(dlg.getSelectedFile());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void searchInFiles() {
		if (searchInFilesDialog == null) {
			searchInFilesDialog = new SearchInFilesDialog(getSite().getShell());
		}
		if (searchInFilesDialog.open() == Dialog.OK) {
			String st = searchInFilesDialog.getSearchText();
			boolean mc = searchInFilesDialog.isMatchCase();
			boolean mw = searchInFilesDialog.isMatchWholeWord();
			boolean ur = searchInFilesDialog.isUseRegExp();
			if (st.length() == 0) {
			    return;
			}
			SearchResultView.searchResults.clear();
			File baseDir = Application.getInstance().getProjectData().getGTLDir();
			List<File> stack = new ArrayList<File>();
			stack.add(baseDir);
			if(Application.getInstance().getProjectData().getLinkDir() != null) {
				stack.add(Application.getInstance().getProjectData().getLinkDir());				
			}
			while (stack.size() > 0) {
				File f = stack.remove(0);
				File[] ff = f.listFiles();
				for (File fff : ff) {
					if (fff.isFile() && (fff.getName().toLowerCase().endsWith(".gtl") || fff.getName().toLowerCase().endsWith(".h"))) {
						performSearch(fff, st, mc, mw, ur);
					} else if (fff.isDirectory()) {
						stack.add(fff);
					}
				}
			}
			SearchResultView view = (SearchResultView)getSite().getWorkbenchWindow().getActivePage().findView(SearchResultView.ID);
			if (view == null) {
				try {
					view = (SearchResultView)getSite().getWorkbenchWindow().getActivePage().showView(SearchResultView.ID);
				} catch (Exception e) {
				}
			}
			if (view != null) {
				view.refresh();
				getSite().getWorkbenchWindow().getActivePage().activate(view);
			}
		}
	}
	
	private void performSearch(File file, String search, boolean matchCase, boolean matchWholeWord, boolean useRegExp) {
		try {
			String[] lines = GTLPreProcessor.read(file);
			if (useRegExp) {
    			String patternStr = search;
    			if (matchWholeWord) {
    				patternStr = "\\b" + patternStr + "\\b";
    			}
    			Pattern pat = Pattern.compile(patternStr, matchCase ? Pattern.DOTALL : (Pattern.DOTALL | Pattern.CASE_INSENSITIVE));
    			for (int i = 0; i < lines.length; i++) {
    				Matcher mat = pat.matcher(lines[i]);
    				while (mat.find()) {
    					SearchResultView.SearchResult sr = new SearchResultView.SearchResult();
    					sr.file = file;
    					sr.lineNo = i;
    					sr.column = mat.start();
    					sr.length = mat.end() - mat.start();
    					sr.lineContent = lines[i];
    					SearchResultView.searchResults.add(sr);
    				}
    			}
			} else {
			    if (!matchCase) {
			        search = search.toLowerCase();
			    }
			    for (int i = 0; i < lines.length; i++) {
			        int index;
			        if (matchCase) {
			            index = lines[i].indexOf(search);
			        } else {
			            index = lines[i].toLowerCase().indexOf(search);
			        }
			        if (index != -1) {
                        SearchResultView.SearchResult sr = new SearchResultView.SearchResult();
                        sr.file = file;
                        sr.lineNo = i;
                        sr.column = index;
                        sr.length = search.length();
                        sr.lineContent = lines[i];
                        SearchResultView.searchResults.add(sr);
			        }
                }
			}
		} catch (Exception e) {
		}
	}
}