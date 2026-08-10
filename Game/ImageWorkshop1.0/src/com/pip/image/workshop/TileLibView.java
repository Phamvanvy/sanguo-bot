package com.pip.image.workshop;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import com.pip.util.FileCopier;
import com.pip.util.FileExtensionFilter;
import com.pip.util.FileMover;
import com.pip.util.FileRemover;
import com.pip.util.FileRenamer;
import com.pip.util.SWTUtils;
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
	private Action renameAction;
	private Action exploreAction;
	private Action convertToCTSAction;
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
		setupDragAndDrop(viewer);
		
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
		if (event.getSelectionProvider() == viewer) {
			File selFile = getSelectedFile();
			if (selFile == null) {
				viewer.getTree().setMenu(null);
				return;
			}
			MenuManager mgr = new MenuManager();

			if (selFile.isFile()) {
				mgr.add(renameAction);
				mgr.add(deleteAction);
				if (ProjectView.canAutoCreateCTS(getSelectedFiles())) {
					mgr.add(convertToCTSAction);
				}
				mgr.add(new Separator());
				mgr.add(exploreAction);
			} else {
				mgr.add(importAction);
				mgr.add(deleteAction);
				mgr.add(new Separator());
				mgr.add(exploreAction);
			}
			mgr.add(refreshAction);
			
			Menu menu = mgr.createContextMenu(viewer.getTree());
			if (viewer.getTree().getMenu() != null) {
				viewer.getTree().getMenu().dispose();
			}
			viewer.getTree().setMenu(menu);
			
			try {
				if (selFile.isFile()) {
					openFile(selFile);
				} else {
					openDir(selFile);
				}
			} catch (Exception e) {
				SWTUtils.showError(getSite().getShell(), "错误", e);
			}
		}
	}
	private void createActions() {
		deleteAction = new Action("删除(&D)") {
			public void run() {
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
		};

		openAction = new Action("打开") {
			public void run() {
				File selFile = getSelectedFile();
				try {
					openFile2(selFile);
				} catch (Exception e) {
					SWTUtils.showError(getSite().getShell(), "错误", e);
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

		importAction = new Action("导入图片...") {
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
							SWTUtils.showError(getSite().getShell(), "错误", e);
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
		
		renameAction = new Action("重命名(&R)"){
			public void run(){
				final File file = getSelectedFile();
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
		
		exploreAction = new Action("浏览...") {
		    public void run() {
		        File file = getSelectedFile();
		        if (file == null) {
		        	return;
		        }
		        if (file.isFile()) {
		        	file = file.getParentFile();
		        }
                String cmd = "explorer.exe \"" + file.getAbsolutePath() + "\"";
                try {
                    Runtime.getRuntime().exec(cmd);
                } catch (Exception e) {
                }
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
					SWTUtils.showError(getSite().getShell(), "错误", e);
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
	
	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
	}
	
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();

		menuManager.add(chooseDirAction);
	}
	
	private boolean isOpened(File file) {
		IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path((file.getAbsolutePath())));
		FileStoreEditorInput input = new FileStoreEditorInput(fileStore);
		return getSite().getWorkbenchWindow().getActivePage().findEditor(input) != null;
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
}
