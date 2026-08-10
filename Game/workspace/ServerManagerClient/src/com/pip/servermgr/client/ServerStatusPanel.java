package com.pip.servermgr.client;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;

import com.pip.servermgr.data.Configuration;
import com.pip.servermgr.data.HttpUtils;
import com.pip.servermgr.data.IServerStatusListener;
import com.pip.servermgr.data.Server;
import com.pip.servermgr.data.SynchronizeThread;
import com.pip.servermgr.report.DataFetchDialog;
import com.pip.servermgr.report.FetchPlayerDialog;
import com.swtdesigner.ResourceManager;

public class ServerStatusPanel extends Composite implements IServerStatusListener {
	private Server server;
	private Label typeLabel, nameLabel, shellLabel, pidLabel, processStatusLabel, portStatusLabel, refreshTimeLabel, accessResultLabel;
	private Button startButton, stopButton, refreshButton;
	private Button[] addButtons;
	private Button logButton;
	private Button errorLogButton;
	private Button memLogButton;
	private Button dbButton;
	private Button fetchButton;
	private Display display;
	private boolean disposed = false;
	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public ServerStatusPanel(Composite parent, Server server) {
		super(parent, SWT.BORDER);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(final DisposeEvent e) {
				disposed = true;
				SynchronizeThread.instance.removeListener(ServerStatusPanel.this);
			}
		});
		
		this.server = server;
		
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		Label label;
		label = new Label(this, SWT.NONE);
		label.setText("服务类型：");

		typeLabel = new Label(this, SWT.NONE);
		typeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label label_1 = new Label(this, SWT.NONE);
		label_1.setText("服务名称：");

		nameLabel = new Label(this, SWT.NONE);
		nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label label_2 = new Label(this, SWT.NONE);
		label_2.setText("控制脚本：");

		shellLabel = new Label(this, SWT.NONE);
		shellLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label label_3 = new Label(this, SWT.NONE);
		label_3.setText("进程ID：");

		pidLabel = new Label(this, SWT.NONE);
		pidLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label label_4 = new Label(this, SWT.NONE);
		label_4.setText("进程状态：");

		processStatusLabel = new Label(this, SWT.NONE);
		processStatusLabel.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/on.gif"));

		if (!"agent".equals(server.type)) {
			final Label label_5 = new Label(this, SWT.NONE);
			label_5.setText("端口状态：");
	
			portStatusLabel = new Label(this, SWT.NONE);
			portStatusLabel.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/on.gif"));
		}

		if ("dispatcher".equals(server.type) || "billing".equals(server.type)) {
			final Label label_5 = new Label(this, SWT.NONE);
			label_5.setText("访问测试：");
	
			accessResultLabel = new Label(this, SWT.NONE);
			accessResultLabel.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/on.gif"));
		}

		final Label label_6 = new Label(this, SWT.NONE);
		label_6.setText("探测时间：");

		refreshTimeLabel = new Label(this, SWT.NONE);
		refreshTimeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Composite buttonPanel = new Composite(this, SWT.NONE);
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.verticalSpacing = 2;
		gridLayout_1.marginWidth = 2;
		gridLayout_1.marginHeight = 2;
		gridLayout_1.horizontalSpacing = 2;
		gridLayout_1.numColumns = 3;
		buttonPanel.setLayout(gridLayout_1);
		buttonPanel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

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
		
		if (server.addOps.length > 0 || server.logInfo != null || server.dbInfo != null) {
            final Composite buttonPanel2 = new Composite(this, SWT.NONE);
            final GridLayout gridLayout_2 = new GridLayout();
            gridLayout_2.verticalSpacing = 2;
            gridLayout_2.marginWidth = 2;
            gridLayout_2.marginHeight = 2;
            gridLayout_2.horizontalSpacing = 2;
            gridLayout_2.numColumns = server.addOps.length + (server.logInfo == null ? 0 : 3) + (server.dbInfo == null ? 0 : 2);
            if (gridLayout_2.numColumns > 3) {
            	gridLayout_2.numColumns = 3;
            }
            buttonPanel2.setLayout(gridLayout_2);
            buttonPanel2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
    
            if (server.logInfo != null) {
            	logButton = new Button(buttonPanel2, SWT.NONE);
            	logButton.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(final SelectionEvent e) {
                    	try {
                    		queryLog();
                    	} catch (Exception e1) {
                    		MessageDialog.openError(getShell(), "错误", e1.toString());
                    	}
                    }
                });
            	logButton.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/log.gif"));
            	logButton.setText("查询日志");
            	
            	errorLogButton = new Button(buttonPanel2, SWT.NONE);
            	errorLogButton.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(final SelectionEvent e) {
                    	try {
                    		queryErrorLog();
                    	} catch (Exception e1) {
                    		MessageDialog.openError(getShell(), "错误", e1.toString());
                    	}
                    }
                });
            	errorLogButton.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/log.gif"));
            	errorLogButton.setText("错误报告");
            	
            	memLogButton = new Button(buttonPanel2, SWT.NONE);
            	memLogButton.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(final SelectionEvent e) {
                    	try {
                    		queryMemLog();
                    	} catch (Exception e1) {
                    		MessageDialog.openError(getShell(), "错误", e1.toString());
                    	}
                    }
                });
            	memLogButton.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/log.gif"));
            	memLogButton.setText("内存报告");
            }
            
            if (server.dbInfo != null) {
            	dbButton = new Button(buttonPanel2, SWT.NONE);
            	dbButton.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(final SelectionEvent e) {
                    	try {
                    		queryDB();
                    	} catch (Exception e1) {
                    		MessageDialog.openError(getShell(), "错误", e1.toString());
                    	}
                    }
                });
            	dbButton.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/log.gif"));
            	dbButton.setText("数据分析");
            	
            	fetchButton = new Button(buttonPanel2, SWT.NONE);
            	fetchButton.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(final SelectionEvent e) {
                    	try {
                    		fetchPlayer();
                    	} catch (Exception e1) {
                    		MessageDialog.openError(getShell(), "错误", e1.toString());
                    	}
                    }
                });
            	fetchButton.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/log.gif"));
            	fetchButton.setText("复制玩家");
            }

            if (server.addOps.length > 0) {
	            addButtons = new Button[server.addOps.length];
	    		for (int i = 0; i < server.addOps.length; i++) {
	    		    addButtons[i] = new Button(buttonPanel2, SWT.NONE);
	                addButtons[i].addSelectionListener(new SelectionAdapter() {
	                    public void widgetSelected(final SelectionEvent e) {
	                        for (int i = 0; i < addButtons.length; i++) {
	                            if (e.widget == addButtons[i]) {
	                                triggerAddOp(i);
	                                break;
	                            }
	                        }
	                    }
	                });
	                addButtons[i].setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/addop.gif"));
	                addButtons[i].setText(server.addOps[i][0]);
	    		}
            }
		}
		
		// 初始化数据
		display = this.getDisplay();
		SynchronizeThread.instance.addListener(this);
		updateFields();
	}
	
	static SearchLogDialog searchDlg;
	private void queryLog() throws Exception {
		if (searchDlg == null) {
			searchDlg = new SearchLogDialog(getShell());
		}
		if (searchDlg.open() == SearchLogDialog.OK) {
			String searchText = searchDlg.searchText;
			boolean isreg = searchDlg.isRegEx;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String startTimeStr = sdf.format(searchDlg.startTime);
			String endTimeStr = sdf.format(searchDlg.endTime);
			long maxSize = searchDlg.maxSize;
			String url = "http://" + server.logInfo.ip + ":" + server.logInfo.port + "/qlog?user=pip&pass=" +
				URLEncoder.encode("log&2009@kgi", "UTF-8") + "&path=" + URLEncoder.encode(server.logInfo.path, "UTF-8") +
				"&prefix=" + URLEncoder.encode(server.logInfo.prefix, "UTF-8") +
				"&filter=" + URLEncoder.encode(searchText, "UTF-8") + "&isregex=" + (isreg ? 1 : 0) +
				"&starttime=" + startTimeStr + "&endtime=" + endTimeStr + "&maxsize=" + maxSize;
			if (server.logInfo.proxy != null) {
				url = server.logInfo.proxy + "?url=" + URLEncoder.encode(url, "UTF-8");
			}
			
			// 下载日志文件到临时文件
			File localFile = File.createTempFile("_lqa_", ".log");
			try {
				DownloadLogJob job = new DownloadLogJob(localFile, new URL(url));
		        ProgressMonitorDialog progress = new ProgressMonitorDialog(getShell());
		        progress.setCancelable(true);
	            progress.run(true, true, job);
	            if (job.getException() != null) {
	            	throw job.getException();
	            }
	        } catch (Exception e) {
	            MessageDialog.openError(getShell(), "错误", e.toString());
	            localFile.delete();
	            return;
	        }
	        
	        // 对日志文件进行解压缩
	        try {
	        	decompress(localFile);
	        } catch (Exception e) {
	        }
	        
	        // 检查日志文件是否包含了正确的结果
	        FileInputStream fis = null;
	        try {
	        	fis = new FileInputStream(localFile);
	        	InputStreamReader isr = new InputStreamReader(fis, "GBK");
	        	BufferedReader br = new BufferedReader(isr);
	        	String line = br.readLine();
	        	fis.close();
	        	fis = null;
	        	
	        	if (line == null) {
	        		throw new Exception("查询结果为空。");
	        	} else if (line.startsWith("authfail")) {
	        		throw new Exception("授权错误。");
	        	} else if (line.startsWith("toolarge")) {
	        		throw new Exception("查询结果过大，请重新检查查询条件。");
	        	}
	        } catch (Exception e) {
	        	MessageDialog.openError(getShell(), "错误", e.toString());
	        	localFile.delete();
	        	return;
	        } finally {
	        	if (fis != null) {
	        		fis.close();
	        	}
	        }
	        
	        // todo
	        processLogFile(localFile);
		}
	}
	
	private void queryDB() throws Exception {
		new DataFetchDialog(this.getShell(), server).open();
	}
	
	private void fetchPlayer() throws Exception {
		new FetchPlayerDialog(this.getShell(), server).open();
	}
	
	private void decompress(File file) throws Exception {
		if (file.length() == 0) {
			return;
		}
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			GZIPInputStream zis = new GZIPInputStream(fis);
			byte[] buf = new byte[256];
			ByteArrayOutputStream tmpData = new ByteArrayOutputStream(10240);
			while (true) {
				int len = zis.read(buf);
				if (len > 0) {
					tmpData.write(buf, 0, len);
				} else if (len < 0) {
					break;
				}
			}
			zis.close();
			fis = null;
	        
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(tmpData.toByteArray());
			fos.close();
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}
	
	static ProcessLogDialog processDlg;
	private void processLogFile(File logFile) throws Exception {
		if (processDlg == null) {
			processDlg = new ProcessLogDialog(getShell());
		}
		processDlg.logPath = logFile.getAbsolutePath();
		if (processDlg.open() == ProcessLogDialog.OK) {
			if (processDlg.processType == 0) {
				Runtime.getRuntime().exec("\"" + processDlg.editorApp + "\" " + logFile.getName(), null, logFile.getParentFile());
			} else if (processDlg.processType == 1) {
				IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path((logFile.getAbsolutePath())));
				FileStoreEditorInput input = new FileStoreEditorInput(fileStore);
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, AdvancedAnalyseEditor.ID);
			}
		}
	}
	
	private void triggerAddOp(int index) {
	    if (!MessageDialog.openConfirm(getShell(), "执行", "你确认要执行此操作吗？")) {
	        return;
	    }
	    try {
            HttpUtils.executeShell(server.getShellScript(), server.addOps[index][1], false, true);
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.openError(getShell(), "错误", e.toString());
            return;
        }
        MessageDialog.openInformation(getShell(), "成功", "操作完成。");
	}

	private void updateFields() {
		typeLabel.setText(server.type);
		nameLabel.setText(server.toString());
		shellLabel.setText(server.getShellScript());
		if (server.statusTime == -1) {
			pidLabel.setText("未知");
			processStatusLabel.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/off.gif"));
			if (portStatusLabel != null) {
				portStatusLabel.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/off.gif"));
			}
			if (accessResultLabel != null) {
				accessResultLabel.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/off.gif"));
			}
			refreshTimeLabel.setText("未知");
			startButton.setEnabled(false);
			stopButton.setEnabled(false);
		} else {
			pidLabel.setText(String.valueOf(server.processID));
			if (server.processExist) {
				processStatusLabel.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/on.gif"));
			} else {
				processStatusLabel.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/off.gif"));
			}
			if (portStatusLabel != null) {
				if (server.portListen) {
					portStatusLabel.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/on.gif"));
				} else {
					portStatusLabel.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/off.gif"));
				}
			}
			if (accessResultLabel != null) {
				if (server.accessResult) {
					accessResultLabel.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/on.gif"));
				} else {
					accessResultLabel.setImage(ResourceManager.getPluginImage(ClientPlugin.getDefault(), "icons/off.gif"));
				}
			}
			refreshTimeLabel.setText(new SimpleDateFormat(" MM月dd日 HH:mm").format(new Date(server.statusTime)));
			startButton.setEnabled(server.canStart());
			stopButton.setEnabled(server.canStop());
		}
		if (!Configuration.allowModify) {
			startButton.setEnabled(false);
			stopButton.setEnabled(false);
			if (addButtons != null) {
				for (int i = 0; i < addButtons.length; i++) {
					addButtons[i].setEnabled(false);
				}
			}
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
	
	/*
	 * 查询一个服务器某一天的错误日志状况，形成报表。
	 * @throws Exception
	 */
	private void queryErrorLog() throws Exception {
		// 首先让用户选择查询日期
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		String initValue = df.format(new Date(System.currentTimeMillis() - 86400000L));
		InputDialog idlg = new InputDialog(getShell(), "输入日期", "请输入查询日期（格式YYYYMMDD）", initValue, null);
		if (idlg.open() != InputDialog.OK) {
			return;
		}
		String dateStr = idlg.getValue();
		Date queryDate = df.parse(idlg.getValue());
		
		List<ExceptionRecord> exceptionRecords = new ArrayList<ExceptionRecord>();
		
		// 三国、轩辕、西游、明珠侠可以查找stdout.log里的异常
		String pt = server.parent.parent.type;
		if ("sanguo".equals(pt) || "xiyou".equals(pt) || "xuanyuan".equals(pt) || "mzx".equals(pt)) {
			String url = "http://" + server.logInfo.ip + ":" + server.logInfo.port + "/qstdouterrorlog?user=pip&pass=" +
				URLEncoder.encode("log&2009@kgi", "UTF-8") + "&path=" + URLEncoder.encode(server.logInfo.path, "UTF-8") +
				"&starttime=" + dateStr + "000000&endtime=" + dateStr + "235959";
			if (server.logInfo.proxy != null) {
				url = server.logInfo.proxy + "?url=" + URLEncoder.encode(url, "UTF-8");
			}
			byte[] data = HttpUtils.httpGet(url);
			String errorLog = new String(data, "GBK");
			parseExceptionLog(errorLog, exceptionRecords, "stdout.log");
		}
		
		// 所有类型的服务器都可以从服务器日志中提取异常
		String url = "http://" + server.logInfo.ip + ":" + server.logInfo.port + "/qworlderrorlog?user=pip&pass=" +
			URLEncoder.encode("log&2009@kgi", "UTF-8") + "&path=" + URLEncoder.encode(server.logInfo.path, "UTF-8") +
			"&prefix=" + URLEncoder.encode(server.logInfo.prefix, "UTF-8") +
			"&starttime=" + dateStr + "000000";
		if (server.logInfo.proxy != null) {
			url = server.logInfo.proxy + "?url=" + URLEncoder.encode(url, "UTF-8");
		}
		byte[] data = HttpUtils.httpGet(url);
		String errorLog = new String(data, "GBK");
		parseExceptionLog(errorLog, exceptionRecords, "服务器日志");
		
		// 三国、轩辕、西游、明珠侠可以从服务器日志中提取TOOLONG信息进行统计（OPCODETOOLONG或CALLTOOLONG）
		LongLogReport longReport = new LongLogReport();
		if ("sanguo".equals(pt) || "xiyou".equals(pt) || "xuanyuan".equals(pt) || "mzx".equals(pt)) {
			url = "http://" + server.logInfo.ip + ":" + server.logInfo.port + "/qworldtoolonglog?user=pip&pass=" +
			URLEncoder.encode("log&2009@kgi", "UTF-8") + "&path=" + URLEncoder.encode(server.logInfo.path, "UTF-8") +
				"&prefix=" + URLEncoder.encode(server.logInfo.prefix, "UTF-8") +
				"&starttime=" + dateStr + "000000";
			if (server.logInfo.proxy != null) {
				url = server.logInfo.proxy + "?url=" + URLEncoder.encode(url, "UTF-8");
			}
			data = HttpUtils.httpGet(url);
			String longLog = new String(data, "GBK");
			longReport = parseLongLog(longLog);
		}
		
		ErrorLogReportInput einput = new ErrorLogReportInput(exceptionRecords, longReport);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(einput, ErrorLogReportEditor.ID);
	}
	
	/*
	 * 解析TOOLONG日志，提取其中的OPCODETOOLONG和CYCLETOOLONG记录。
	 */
	private LongLogReport parseLongLog(String log) throws IOException {
		LongLogReport report = new LongLogReport();
		BufferedReader br = new BufferedReader(new StringReader(log));
		String line;
		while ((line = br.readLine()) != null) {
			if (line.contains("[OPCODETOOLONG][")) {
				int start = line.indexOf("[OPCODETOOLONG][") + "[OPCODETOOLONG][".length();
				int stop = line.indexOf("]", start);
				String[] secs = line.substring(start, stop).split(",");
				report.addOpCodeTime(Integer.parseInt(secs[0]), Integer.parseInt(secs[1]));
			} else if (line.contains("[CALLTOOLONG]")) {
				String className = getSec(line, "CLASS[", "]");
				int time = Integer.parseInt(getSec(line, "TIME[", "]"));
				report.addCallTime(className, time);
			}
		}
		return report;
	}
	
	private static String getSec(String all, String start, String end) {
	    int pos = all.indexOf(start);
	    int pos2 = all.indexOf(end, pos + start.length());
	    return all.substring(pos + start.length(), pos2);
	}
	
	/*
	 * 从logqueryagent返回的错误日志文件中分析异常，并添加到异常记录列表中。
	 * 返回的日志文件格式为：
	 * Repeat:次数
	 * 异常栈（多行）
	 * ...重复
	 */
	private void parseExceptionLog(String errorLog, List<ExceptionRecord> exceptionRecords, String source) throws IOException {
		BufferedReader br = new BufferedReader(new StringReader(errorLog));
		String line;
		int thisCount = 0;
		StringBuilder thisStack = new StringBuilder();
		while ((line = br.readLine()) != null) {
			if (line.startsWith("Repeat:")) {
				if (thisStack.length() > 0) {
					ExceptionRecord er = new ExceptionRecord(thisCount, thisStack.toString(), source);
					exceptionRecords.add(er);
					thisStack.setLength(0);
				}
				thisCount = Integer.parseInt(line.substring("Repeat:".length()));
			} else {
				if (thisStack.length() > 0) {
					thisStack.append("\n");
				}
				thisStack.append(line);
			}
		}
		if (thisStack.length() > 0) {
			ExceptionRecord er = new ExceptionRecord(thisCount, thisStack.toString(), source);
			exceptionRecords.add(er);
			thisStack.setLength(0);
		}
	}
	
	/*
	 * 查询一个服务器的内存状况。
	 * @throws Exception
	 */
	private void queryMemLog() throws Exception {
		String url = "http://" + server.logInfo.ip + ":" + server.logInfo.port + "/qgclog?user=pip&pass=" +
			URLEncoder.encode("log&2009@kgi", "UTF-8") + "&path=" + URLEncoder.encode(server.logInfo.path, "UTF-8");
		if (server.logInfo.proxy != null) {
			url = server.logInfo.proxy + "?url=" + URLEncoder.encode(url, "UTF-8");
		}
		byte[] data = HttpUtils.httpGet(url);
		String result = new String(data, "GBK");
		BufferedReader br = new BufferedReader(new StringReader(result));
		int minMem = Integer.parseInt(br.readLine());
		int maxMem = Integer.parseInt(br.readLine());
		String msg = "当前内存：" + (minMem / 1024) + "M\n最大内存：" + (maxMem / 1024) + "M\n" + 
			"使用比例：" + (minMem * 100 / maxMem) + "%";
		MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "内存报告", msg);
	}
}
