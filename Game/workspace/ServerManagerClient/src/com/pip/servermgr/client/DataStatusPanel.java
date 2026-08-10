package com.pip.servermgr.client;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.pip.servermgr.data.Configuration;
import com.pip.servermgr.data.HttpUtils;
import com.pip.servermgr.data.ServerGroup;
import com.swtdesigner.ResourceManager;

public class DataStatusPanel extends Composite implements Runnable, DisposeListener {
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
	
	private TableViewer fileViewer;
	private Table fileTable;
	private Label dataTimeLabel;
	private ServerGroup group;
	private List<String[]> fileList;
	private Button syncButton;
	private Display display;
	private boolean closed = false;
	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public DataStatusPanel(Composite parent, ServerGroup group) {
		super(parent, SWT.BORDER);
		this.group = group;
		display = getDisplay();
		
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		setLayout(gridLayout);

		fileViewer = new TableViewer(this, SWT.FULL_SELECTION | SWT.BORDER);
		fileViewer.setLabelProvider(new FileListLabelProvider());
		fileViewer.setContentProvider(new FileListContentProvider());
		fileTable = fileViewer.getTable();
		fileTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 3, 1));
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

		final Button button = new Button(fileTable, SWT.NONE);
		button.setText("button");

		dataTimeLabel = new Label(this, SWT.NONE);
		dataTimeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		syncButton = new Button(this, SWT.NONE);
		syncButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				sync();
			}
		});
		syncButton.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/sync.gif"));
		final GridData gd_syncButton = new GridData(SWT.FILL, SWT.CENTER, false, false);
		syncButton.setLayoutData(gd_syncButton);
		syncButton.setText("同步数据");
		if (AsyncExecuteThread.instance.isRequestExists(getSyncToken())) {
			syncButton.setEnabled(false);
			new Thread(this).start();
		}

		final Button refreshButton = new Button(this, SWT.NONE);
		refreshButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				refreshFileList(true);
			}
		});
		refreshButton.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/refresh.gif"));
		final GridData gd_refreshButton = new GridData(SWT.FILL, SWT.CENTER, false, false);
		refreshButton.setLayoutData(gd_refreshButton);
		refreshButton.setText("刷新状态");
		
		// 初始化数据
		refreshFileList(false);
		if (!Configuration.allowModify) {
			syncButton.setEnabled(false);
		}
		this.addDisposeListener(this);
	}
	
	public void widgetDisposed(DisposeEvent evt) {
		closed = true;
	}
	
	private String[] getSyncToken() {
		return new String[] { group.getPath() + "/data.sh", "upload" };
	}
	
	public void run() {
		while (!closed) {
			if (!AsyncExecuteThread.instance.isRequestExists(getSyncToken())) {
				display.asyncExec(new Runnable() {
					public void run() {
						refreshFileList(true);
						MessageDialog.openInformation(getShell(), "成功", "同步完成。");
						syncButton.setEnabled(true);
					}
				});
				return;
			}
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}
	}
	
	private void sync() {
		String msg = "请确认你已经在文件管理器中上传了正确的文件，并且确实要更新到" + group.toString() + "？";
		if (!MessageDialog.openConfirm(getShell(), "确认", msg)) {
			return;
		}
		syncButton.setEnabled(false);
		AsyncExecuteThread.instance.addRequest(group, getSyncToken());
		new Thread(this).start();
	}
	
	private void refreshFileList(boolean forceUpdate) {
		try {
			String text = HttpUtils.executeShell(group.getPath() + "/data.sh", "status", true, forceUpdate);
			ArrayList<String[]> list = new ArrayList<String[]>();
			String[] lines = text.split("\n");
			long dataTime = Long.parseLong(lines[0].trim());
			dataTimeLabel.setText(new SimpleDateFormat("探测时间： MM月dd日 HH:mm").format(new Date(dataTime)));
			for (int i = 1; i < lines.length; i++) {
				String line = lines[i].trim();
				if (line.length() == 0) {
					continue;
				}
				String[] secs = line.split("\\s+");
				String size = secs[4];
				String name = secs[secs.length - 1];
				String time = "";
				for (int j = 5; j <= secs.length - 2; j++) {
					time += secs[j] + " ";
				}
				list.add(new String[] { name, size, time });
			}
			HttpUtils.sortFiles(list);
			fileList = list;
			fileViewer.setInput(fileList);
		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog.openError(getShell(), "错误", e.toString());
		}
	}
}
