package com.pip.servermgr.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.action.Action;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import com.pip.servermgr.client.ClientPlugin;

import com.pip.servermgr.data.Configuration;
import com.pip.servermgr.data.HttpUtils;
import com.pip.servermgr.data.Product;
import com.swtdesigner.ResourceManager;

public class ProductFileView extends ViewPart {
	private Action uploadDirButton;
	private Action downloadAction;
	private Action refreshAction;
	class FileListLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			String[] line = (String[])element;
			return line[columnIndex];
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	
	class FileListContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			if (inputElement == null) {
				return new Object[0];
			}
			return ((List<String[]>)inputElement).toArray();
		}
		
		public void dispose() {}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	}

	public static final String ID = "com.pip.servermgr.client.ProductFileView"; //$NON-NLS-1$
	private TableViewer fileViewer;
	private Table fileTable;
	private Product product;
	private List<String[]> fileList;
	private static FileDialog fileDialog, saveDialog;
	private static DirectoryDialog dirDialog;

	/**
	 * Create contents of the view part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());

		fileViewer = new TableViewer(container, SWT.FULL_SELECTION | SWT.BORDER);
		fileViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {
				StructuredSelection sel = (StructuredSelection)event.getSelection();
				if (sel.isEmpty()) {
					return;
				}
				String[] selObj = (String[])sel.getFirstElement();
				if (Configuration.allowModify) {
					updateFile(selObj[0]);
				}
			}
		});
		fileViewer.setLabelProvider(new FileListLabelProvider());
		fileViewer.setContentProvider(new FileListContentProvider());
		fileTable = fileViewer.getTable();
		fileTable.setLinesVisible(true);
		fileTable.setHeaderVisible(true);

		final TableColumn nameColumn = new TableColumn(fileTable, SWT.NONE);
		nameColumn.setWidth(170);
		nameColumn.setText("文件名");

		final TableColumn sizeColumn = new TableColumn(fileTable, SWT.NONE);
		sizeColumn.setWidth(100);
		sizeColumn.setText("文件大小");

		final TableColumn timeColumn = new TableColumn(fileTable, SWT.NONE);
		timeColumn.setWidth(128);
		timeColumn.setText("更新时间");

		createActions();
		initializeToolBar();
		initializeMenu();
		
		// 初始化数据
		refreshFileList();
	}
	
	public void setProduct(Product product) {
		if (this.product != product) {
			this.product = product;
			refreshFileList();
		}
	}
	
	private void downloadFile(String name) {
		if (saveDialog == null) {
			saveDialog = new FileDialog(getSite().getShell(), SWT.SAVE);
		}
		saveDialog.setFileName(name);
		String localFile = saveDialog.open();
		if (localFile == null) {
			return;
		}
		File f = new File(localFile);
		if (f.exists() && f.isDirectory()) {
			MessageDialog.openError(getSite().getShell(), "错误", "目标路径指向一个文件夹。");
			return;
		}
		if (f.exists()) {
			if (!MessageDialog.openConfirm(getSite().getShell(), "覆盖", localFile + "已存在，是否覆盖？")) {
				return;
			}
			f.delete();
		}
		try {
			if (!f.createNewFile()) {
				throw new Exception();
			}
		} catch (Exception e) {
			MessageDialog.openError(getSite().getShell(), "错误", "无法创建目标文件。");
			return;
		}
		f.delete();
		
		// 启动下载任务
		String remoteFile = product.path + "/data/" + name;
		try {
	        DownloadJob job = new DownloadJob(localFile, remoteFile);
	        ProgressMonitorDialog progress = new ProgressMonitorDialog(getSite().getShell());
	        progress.setCancelable(true);
            progress.run(true, true, job);
            if (job.getException() != null) {
            	throw job.getException();
            }
        } catch (Exception e) {
            MessageDialog.openError(getSite().getShell(), "错误", e.toString());
        }
	}
	
	private void updateFile(String name) {
		if (fileDialog == null) {
			fileDialog = new FileDialog(getSite().getShell(), SWT.OPEN);
			fileDialog.setFilterExtensions(new String[] { "*.7z", "*.jar", "*.xml", "*.*" });
			fileDialog.setFilterNames(new String[] { "关卡数据文件(*.7z)", "JAR文件(*.jar)", "XML文件(*.xml)", "所有文件(*.*)" });
		}
		String localFile = fileDialog.open();
		if (localFile == null) {
			return;
		}
		if (!(new File(localFile).getName().equals(name))) {
			if (!MessageDialog.openConfirm(getSite().getShell(), "确认", "文件名不匹配，是否继续？")) {
				return;
			}
		}
		
		// 启动上传任务
		String remoteFile = product.path + "/data/" + name;
        try {
            String[][] s = new String[1][2];
            s[0][0] = localFile;
            s[0][1] = remoteFile;
	        UploadJob job = new UploadJob(s);
	        ProgressMonitorDialog progress = new ProgressMonitorDialog(getSite().getShell());
	        progress.setCancelable(true);
            progress.run(true, true, job);
            refreshFileList();
        } catch (Exception e) {
            MessageDialog.openError(getSite().getShell(), "错误", e.toString());
        }
	}

    private void uploadDir() {
        if (dirDialog == null) {
            dirDialog = new DirectoryDialog(getSite().getShell(), SWT.OPEN);
        }
        String path = dirDialog.open();
        if (path == null) {
            return;
        }
        
        // 读取文件列表（支持7z, jar, xml, txt文件），不支持子目录
        File[] files = new File(path).listFiles();
        List<File> flist = new ArrayList<File>();
        for (File f : files) {
            String n = f.getName();
            if (n.endsWith(".7z") || n.endsWith(".jar") || n.endsWith(".xml") || n.endsWith(".txt")) {
                flist.add(f);
            }
        }
        if (flist.size() == 0) {
            MessageDialog.openError(getSite().getShell(), "错误", "没有可以上传的文件。");
            return;
        }
        String[][] ffs = new String[flist.size()][2];
        for (int i = 0; i < flist.size(); i++) {
            File f = flist.get(i);
            ffs[i][0] = f.getAbsolutePath();
            ffs[i][1] = product.path + "/data/" + f.getName();
        }
        
        // 启动上传任务
        try {
            UploadJob job = new UploadJob(ffs);
            ProgressMonitorDialog progress = new ProgressMonitorDialog(getSite().getShell());
            progress.setCancelable(true);
            progress.run(true, true, job);
            refreshFileList();
        } catch (Exception e) {
            MessageDialog.openError(getSite().getShell(), "错误", e.toString());
        }
    }
	
	private void refreshFileList() {
		if (product == null) {
			fileList = new ArrayList<String[]>();
			fileViewer.setInput(fileList);
			return;
		}
		try {
			String text = HttpUtils.listFile(product.path + "/data");
			ArrayList<String[]> list = new ArrayList<String[]>();
			String[] lines = text.split("\n");
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i].trim();
				if (line.length() == 0) {
					continue;
				}
				String[] secs = line.split("\\s+");
				String size = secs[1];
				String name = secs[0];
				String time = "";
				for (int j = 2; j <= secs.length - 1; j++) {
					time += secs[j] + " ";
				}
				list.add(new String[] { name, size, time });
			}
			HttpUtils.sortFiles(list);
			fileList = list;
			fileViewer.setInput(fileList);
		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog.openError(getSite().getShell(), "错误", e.toString());
		}
	}

	/**
	 * Create the actions
	 */
	private void createActions() {
		refreshAction = new Action("刷新") {
			public void run() {
				refreshFileList();
			}
		};
		refreshAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(ClientPlugin.getDefault(), "icons/refresh.gif"));

		downloadAction = new Action("下载") {
			public void run() {
				StructuredSelection sel = (StructuredSelection)fileViewer.getSelection();
				if (sel.isEmpty()) {
					return;
				}
				String[] selObj = (String[])sel.getFirstElement();
				downloadFile(selObj[0]);
			}
		};
		downloadAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(ClientPlugin.getDefault(), "icons/download.gif"));

        uploadDirButton = new Action("上传目录") {
            public void run() {
                uploadDir();
            }
        };
        uploadDirButton.setImageDescriptor(ResourceManager.getPluginImageDescriptor(ClientPlugin.getDefault(), "icons/upload.gif"));
	}

	/**
	 * Initialize the toolbar
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();

		toolbarManager.add(refreshAction);

		toolbarManager.add(downloadAction);

		toolbarManager.add(uploadDirButton);
	}

	/**
	 * Initialize the menu
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

}
