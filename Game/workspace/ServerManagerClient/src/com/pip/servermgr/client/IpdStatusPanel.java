package com.pip.servermgr.client;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import com.pip.servermgr.client.ClientPlugin;
import com.pip.servermgr.data.Configuration;
import com.pip.servermgr.data.HttpUtils;
import com.pip.servermgr.data.IServerStatusListener;
import com.pip.servermgr.data.Server;
import com.pip.servermgr.data.SynchronizeThread;
import com.swtdesigner.ResourceManager;

public class IpdStatusPanel extends Composite implements IServerStatusListener {
	private Server server;
	private Label typeLabel, nameLabel, shellLabel, processStatusLabel, maintainStatusLabel, maintainMsgLabel, refreshTimeLabel;
	private Button startButton, stopButton, refreshButton, maintainButton, updateButton;
	private Display display;
	private boolean disposed = false;
	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public IpdStatusPanel(Composite parent, Server server) {
		super(parent, SWT.BORDER);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(final DisposeEvent e) {
				disposed = true;
				SynchronizeThread.instance.removeListener(IpdStatusPanel.this);
			}
		});
		
		this.server = server;
		
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		setLayout(gridLayout);

		final Label label_3 = new Label(this, SWT.NONE);
		label_3.setText("服务类型：");

		typeLabel = new Label(this, SWT.NONE);
		final GridData gd_typeLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		typeLabel.setLayoutData(gd_typeLabel);

		final Label label_1 = new Label(this, SWT.NONE);
		label_1.setText("服务名称：");

		nameLabel = new Label(this, SWT.NONE);
		nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		final Label label_2 = new Label(this, SWT.NONE);
		label_2.setText("控制脚本：");

		shellLabel = new Label(this, SWT.NONE);
		shellLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		final Label label_4 = new Label(this, SWT.NONE);
		label_4.setText("服务状态：");

		processStatusLabel = new Label(this, SWT.NONE);
		processStatusLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		processStatusLabel.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/on.gif"));

		final Label label_5 = new Label(this, SWT.NONE);
		label_5.setText("访问控制：");

		maintainStatusLabel = new Label(this, SWT.NONE);
		maintainStatusLabel.setLayoutData(new GridData());
		maintainStatusLabel.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/on.gif"));

		maintainButton = new Button(this, SWT.NONE);
		maintainButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				maintain();
			}
		});
		final GridData gd_maintainBtn = new GridData(SWT.LEFT, SWT.FILL, false, false);
		maintainButton.setLayoutData(gd_maintainBtn);
		maintainButton.setText("设置维护状态");

		final Label label = new Label(this, SWT.NONE);
		label.setText("维护消息：");

		maintainMsgLabel = new Label(this, SWT.NONE);
		maintainMsgLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		maintainMsgLabel.setText("无");

		final Label label_6 = new Label(this, SWT.NONE);
		label_6.setText("探测时间：");

		refreshTimeLabel = new Label(this, SWT.NONE);
		refreshTimeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		final Composite buttonPanel = new Composite(this, SWT.NONE);
		buttonPanel.setLayout(new RowLayout());
		buttonPanel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));

		startButton = new Button(buttonPanel, SWT.NONE);
		startButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				start();
			}
		});
		startButton.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/start.gif"));
		startButton.setText("启动");

		stopButton = new Button(buttonPanel, SWT.NONE);
		stopButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				stop();
			}
		});
		stopButton.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/stop.gif"));
		stopButton.setText("停止");

		refreshButton = new Button(buttonPanel, SWT.NONE);
		refreshButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				refresh();
			}
		});
		refreshButton.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/refresh.gif"));
		refreshButton.setText("刷新");

		updateButton = new Button(buttonPanel, SWT.NONE);
		updateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				updateApp();
			}
		});
		updateButton.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/sync.gif"));
		updateButton.setText("更新");
		
		// 初始化数据
		display = this.getDisplay();
		SynchronizeThread.instance.addListener(this);
		updateFields();
	}

	private void updateFields() {
		typeLabel.setText(server.type);
		nameLabel.setText(server.toString());
		shellLabel.setText(server.getShellScript());
		if (server.statusTime == -1) {
			processStatusLabel.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/off.gif"));
			maintainStatusLabel.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/off.gif"));
			maintainMsgLabel.setText("未知");
			refreshTimeLabel.setText("未知");
			startButton.setEnabled(false);
			stopButton.setEnabled(false);
			maintainButton.setText("设置维护状态");
			maintainButton.setEnabled(false);
			updateButton.setEnabled(false);
		} else {
			if (server.processExist) {
				processStatusLabel.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/on.gif"));
				if (!server.maintaining) {
					maintainStatusLabel.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/on.gif"));
					maintainMsgLabel.setText("无");
				} else {
					maintainStatusLabel.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/off.gif"));
					maintainMsgLabel.setText(server.maintainMsg);
				}
			} else {
				processStatusLabel.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/off.gif"));
				maintainStatusLabel.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/off.gif"));
				maintainMsgLabel.setText("无");
			}
			refreshTimeLabel.setText(new SimpleDateFormat(" MM月dd日 HH:mm").format(new Date(server.statusTime)));
			if (server.processExist) {
				startButton.setEnabled(false);
				stopButton.setEnabled(true);
				if (server.maintaining) {
					maintainButton.setText("清除维护状态");
				} else {
					maintainButton.setText("设置维护状态");
				}
				maintainButton.setEnabled(true);
			} else {
				startButton.setEnabled(true);
				stopButton.setEnabled(false);
				maintainButton.setText("设置维护状态");
				maintainButton.setEnabled(false);
			}
			updateButton.setEnabled(true);
		}
		if (!Configuration.allowModify) {
			startButton.setEnabled(false);
			stopButton.setEnabled(false);
			maintainButton.setEnabled(false);
			updateButton.setEnabled(false);
		}
	}
	
	private void maintain() {
		if (server.maintaining) {
			// 清除维护状态
			String url = "http://" + server.serverIP + ":" + server.serverPort + "/" + server.appName + 
				"/manager.jsp?action=clearmaintain";
			try {
				int code = HttpUtils.httpGet(url, server.adminName, server.adminPassword);
				if (code != 200) {
					throw new Exception("错误码：" + code);
				}
			} catch (Throwable e) {
				MessageDialog.openError(getShell(), "错误 ", e.toString());
			}
		} else {
			// 设置维护状态
			InputDialog dlg = new InputDialog(getShell(), "维护消息", "请输入维护消息：", "服务器正在维护中。", null);
			if (dlg.open() != InputDialog.OK) {
				return;
			}
			String msg = dlg.getValue().trim();
			if (msg.length() == 0) {
				MessageDialog.openError(getShell(), "错误", "维护消息不能为空。");
			}
			try {
				String url = "http://" + server.serverIP + ":" + server.serverPort + "/" + server.appName + 
					"/manager.jsp?action=setmaintain&msg=" + URLEncoder.encode(msg, "GBK");
				int code = HttpUtils.httpGet(url, server.adminName, server.adminPassword);
					if (code != 200) {
						throw new Exception("错误码：" + code);
					}
			} catch (Exception e) {
				MessageDialog.openError(getShell(), "错误 ", e.toString());
			}
		}
		refresh();
	}
	
	private void updateApp() {
		FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
		fileDialog.setFilterExtensions(new String[] { "*.war", "*.*" });
		fileDialog.setFilterNames(new String[] { "WEB应用打包文件(*.war)", "所有文件(*.*)" });
		String localFile = fileDialog.open();
		if (localFile == null) {
			return;
		}
		
		// 启动上传任务
		String remoteFile = server.parent.getPath() + "/ipd.war";
        try {
            String[][] s = new String[1][2];
            s[0][0] = localFile;
            s[0][1] = remoteFile;
	        UploadJob job = new UploadJob(s);
	        ProgressMonitorDialog progress = new ProgressMonitorDialog(getShell());
	        progress.setCancelable(true);
            progress.run(true, true, job);
            if (progress.getProgressMonitor().isCanceled()) {
            	return;
            }
        } catch (Exception e) {
            MessageDialog.openError(getShell(), "错误", e.toString());
            return;
        }
        
        // 更新应用
        try {
			HttpUtils.executeShell(server.getShellScript(), "upload", false, true);
			MessageDialog.openInformation(getShell(), "成功", "更新完成。");
		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog.openError(getShell(), "错误", e.toString());
		}
	}
	
	private void start() {
		try {
			HttpUtils.executeShell(server.getShellScript(), "start", false, true);
			Thread.sleep(5);
		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog.openError(getShell(), "错误", e.toString());
		}
		refresh();
	}
	
	private void stop() {
		String msg = "你确认要停止服务" + server.toString() + "吗？";
		if (!MessageDialog.openConfirm(getShell(), "确认", msg)) {
			return;
		}
		try {
			HttpUtils.executeShell(server.getShellScript(), "stop", false, true);
		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog.openError(getShell(), "错误", e.toString());
		}
		refresh();
	}
	
	private void refresh() {
		SynchronizeThread.instance.sync(server, true);
	}

	public void onError(Server server, Exception ex) {}

	public void statusChanged(Server server) {
		if (this.server == server && !disposed) {
			display.asyncExec(new Runnable() {
				public void run() {
					updateFields();
				}
			});
		}
	}
}
