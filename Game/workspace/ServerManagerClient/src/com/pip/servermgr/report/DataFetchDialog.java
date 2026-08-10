package com.pip.servermgr.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.pip.servermgr.data.HttpUtils;
import com.pip.servermgr.data.Server;
import com.pip.util.DBConfig;
import com.pip.util.DataFetcher;
import com.pip.util.EFSUtil;
import com.pipimage.utils.GZIP;
import com.pipimage.utils.Utils;

public class DataFetchDialog extends Dialog {

	private Combo comboDateType;
	private Combo comboMethod;
	private Text textBatchCount;
	private Text textMaxCount;
	private Text textLevel;
	private DateTime dateTimeEnd;
	private DateTime dateTimeStart;
	
	private String progressText = "";
	private Display display;
	
	public Server server;
	private Label progressLabel;
	private boolean inProgress;
	private FetchThread workThread;
	private File resultFile;
	
	int dateType;
	Date startDate;
	Date endDate;
	int minLevel;
	int maxCount;
	int batchCount;
	int fetchMode; 

	static {
		try {
			DBConfig.init("");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public DataFetchDialog(Shell parentShell, Server server) {
		super(parentShell);
		display = parentShell.getDisplay();
		this.server = server;
	}

	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);

		final Label label_6 = new Label(container, SWT.NONE);
		label_6.setText("日期类型：");

		comboDateType = new Combo(container, SWT.READ_ONLY);
		comboDateType.setItems(new String[] {"角色创建日期", "最后登录日期"});
		comboDateType.select(0);
		final GridData gd_comboDateType = new GridData(SWT.FILL, SWT.CENTER, true, false);
		comboDateType.setLayoutData(gd_comboDateType);

		final Label label = new Label(container, SWT.NONE);
		label.setText("起始日期：");

		dateTimeStart = new DateTime(container, SWT.NONE);
		final GridData gd_dateTimeStart = new GridData(SWT.FILL, SWT.CENTER, false, false);
		dateTimeStart.setLayoutData(gd_dateTimeStart);

		final Label label_1 = new Label(container, SWT.NONE);
		label_1.setText("结束日期：");

		dateTimeEnd = new DateTime(container, SWT.NONE);
		final GridData gd_dateTimeEnd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		dateTimeEnd.setLayoutData(gd_dateTimeEnd);

		final Label label_2 = new Label(container, SWT.NONE);
		label_2.setText("最小级别：");

		textLevel = new Text(container, SWT.BORDER);
		textLevel.setText("1");
		final GridData gd_textLevel = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textLevel.setLayoutData(gd_textLevel);

		final Label label_3 = new Label(container, SWT.NONE);
		label_3.setText("最大数量：");

		textMaxCount = new Text(container, SWT.BORDER);
		textMaxCount.setText("10000");
		final GridData gd_textMaxCount = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textMaxCount.setLayoutData(gd_textMaxCount);

		final Label label_4 = new Label(container, SWT.NONE);
		label_4.setText("每批数量：");

		textBatchCount = new Text(container, SWT.BORDER);
		textBatchCount.setText("1000");
		final GridData gd_textBatchCount = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textBatchCount.setLayoutData(gd_textBatchCount);

		final Label label_5 = new Label(container, SWT.NONE);
		label_5.setText("提取方式：");

		comboMethod = new Combo(container, SWT.READ_ONLY);
		comboMethod.setItems(new String[] {"在服务器提取", "在客户端提取（调试用）"});
		comboMethod.select(0);
		final GridData gd_comboMethod = new GridData(SWT.FILL, SWT.CENTER, true, false);
		comboMethod.setLayoutData(gd_comboMethod);

		progressLabel = new Label(container, SWT.CENTER);
		final GridData gd_progressLabel = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		progressLabel.setLayoutData(gd_progressLabel);
		
		return container;
	}

	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(284, 323);
	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("提取数据");
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			if (inProgress) {
				return;
			}
			dateType = comboDateType.getSelectionIndex();
			Calendar cal = Calendar.getInstance();
			cal.set(dateTimeStart.getYear(), dateTimeStart.getMonth(), dateTimeStart.getDay(), 0, 0, 0);
			startDate = cal.getTime();
			cal.set(dateTimeEnd.getYear(), dateTimeEnd.getMonth(), dateTimeEnd.getDay() + 1, 0, 0, 0);
			endDate = cal.getTime();
			minLevel = Integer.parseInt(textLevel.getText());
			maxCount = Integer.parseInt(textMaxCount.getText());
			batchCount = Integer.parseInt(textBatchCount.getText());
			fetchMode = comboMethod.getSelectionIndex();
			inProgress = true;
			workThread = new FetchThread();
			workThread.start();
			return;
		} else {
			if (inProgress) {
				inProgress = false;
				workThread = null;
				setProgress("操作已取消");
				return;
			}
		}
		super.buttonPressed(buttonId);
	}
	
	private class FetchThread extends Thread {
		public void run() {
			try {
				setProgress("开始提取");
				Map<String, String> params = new HashMap<String, String>();
				params.put("productName", server.parent.parent.name);
				params.put("serverName", server.parent.name);
				params.put("dateType", String.valueOf(dateType));
				params.put("startDate", String.valueOf(startDate.getTime()));
				params.put("endDate", String.valueOf(endDate.getTime()));
				params.put("minLevel", String.valueOf(minLevel));
				params.put("maxCount", String.valueOf(maxCount));
				params.put("batchCount", String.valueOf(batchCount));
				AbstractReportEngine engine = AbstractReportEngine.create(server.parent.parent.name);
				if (fetchMode == 0) {
					// 首先初始化数据提取线程
					setProgress("开始提取");
					int[] ids;
					try {
						ids = HttpUtils.startFetchData(server.getShellScript(), params, engine.getDataFetcherClass());
					} catch (Exception e) {
						if (workThread == this) {
							setProgress(e.toString());
						}
						return;
					}
					if (workThread != this) {
						HttpUtils.cancelFetchData(ids[0], ids[1]);
						return;
					}
					
					// 循环检查服务器提取进度
					while (true) {
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
						}
						if (workThread != this) {
							HttpUtils.cancelFetchData(ids[0], ids[1]);
							return;
						}
						try {
							Object[] info = HttpUtils.queryFetchData(ids[0], ids[1]);
							int status = ((Integer)info[0]).intValue();
							String msg = (String)info[1];
							if (status == 0) { // 正在执行
								if (workThread == this) {
									setProgress(msg);
								}
							} else if (status == 1) { // 已经完成
								break;
							} else { // 发生错误
								if (workThread == this) {
									setProgress(msg);
								}
								HttpUtils.cancelFetchData(ids[0], ids[1]);
								return;
							}
						} catch (Exception e) {
							if (workThread == this) {
								setProgress(e.toString());
							}
							HttpUtils.cancelFetchData(ids[0], ids[1]);
							return;
						}
					}
					
					// 只有提取正确完成才会执行到这里，开始下载文件
					HttpURLConnection conn = null;
					InputStream is = null;
					FileOutputStream fos = null;
					File tempFile = null;
					try {
						tempFile = File.createTempFile("_report_data", ".dat");
						
						// 获取文件信息
						Object[] info = HttpUtils.downloadFetchData(ids[0], ids[1]);
						conn = (HttpURLConnection)info[0];
						is = (InputStream)info[1];
						int totalLen = ((Integer)info[2]).intValue();
						if (workThread != this) {
							HttpUtils.cancelFetchData(ids[0], ids[1]);
							tempFile.delete();
							return;
						}
						setProgress("正在下载(0/" + totalLen + ")...");
						
						// 开始下载，一边下载一边保存
						fos = new FileOutputStream(tempFile);
						byte[] buf = new byte[50000];
						int finishLen = 0;
						while (finishLen < totalLen) {
							if (workThread != this) {
								HttpUtils.cancelFetchData(ids[0], ids[1]);
								fos.close();
								fos = null;
								tempFile.delete();
								return;
							}
							int max = totalLen - finishLen;
							if (max > 50000) {
								max = 50000;
							}
							int thisLen = is.read(buf, 0, max);
							if (thisLen == -1) {
								throw new IOException();
							}
							finishLen += thisLen;
							setProgress("正在下载(" + finishLen + "/" + totalLen + ")...");
							if (thisLen > 0) {
								fos.write(buf, 0, thisLen);
							}
						}
						fos.close();
						
						// 解压缩
						byte[] data = Utils.loadFileData(tempFile);
						data = GZIP.inflate(data);
						saveFileData(tempFile, data);
						
						// 下载完成，执行清理
						HttpUtils.cleanFetchData(ids[0], ids[1]);
					} catch (Exception e) {
						if (workThread == this) {
							setProgress(e.toString());
						}
						HttpUtils.cancelFetchData(ids[0], ids[1]);
						if (fos != null) {
							try {
								fos.close();
							} catch (Exception e1) {
							}
							fos = null;
						}
						if (tempFile != null) {
							tempFile.delete();
						}
						return;
					} finally {
						try {
			                if (is != null) {
			                    is.close();
			                }
			            } catch (Exception e) {
			            }
			            try {
			                if (conn != null) {
			                    conn.disconnect();
			                }
			            } catch (Exception e) {
			            }
			            try {
			            	if (fos != null) {
			            		fos.close();
			            	}
			            } catch (Exception e) {
			            }
					}
					
					// 提取完成
					setProgress("操作完成");
					resultFile = tempFile;
					
					display.asyncExec(new Runnable() {
						public void run() {
							onFinished();
						}
					});
				} else {
					// 首先初始化数据提取线程
					DataFetcher fetcher = null;
					try {
						fetcher = (DataFetcher)Class.forName(engine.getDataFetcherClass()).newInstance();
					} catch (Exception e) {
						if (workThread == this) {
							setProgress(e.toString());
						}
						return;
					}
					fetcher.setDBInfo(server.dbInfo.slaveURL, server.dbInfo.user, server.dbInfo.password);
					fetcher.setParams(params);
					new Thread(fetcher).start();
					
					// 循环检查服务器提取进度
					while (true) {
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
						}
						if (workThread != this) {
							fetcher.cancel();
							return;
						}
						try {
							if (fetcher.isOver()) {
								String error = fetcher.getError();
								if (error == null) {
									break;
								} else {
									setProgress(error);
									fetcher.cancel();
									return;
								}
							} else { // 正在执行
								setProgress(fetcher.getProgress());
							}
						} catch (Exception e) {
							if (workThread == this) {
								setProgress(e.toString());
							}
							fetcher.cancel();
							return;
						}
					}
					
					// 提取完成
					setProgress("操作完成");
					resultFile = fetcher.getFile();
					
					display.asyncExec(new Runnable() {
						public void run() {
							onFinished();
						}
					});
				}
			} finally {
				inProgress = false;
			}
		}
	}
	
	//分段存储，减少内存
    public static void saveFileData(File dest, byte[] data) throws IOException{
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(dest);
//            fos.write(data); //如果不分段，这里内存会爆掉
            int clip = 10;
            int clipSize = data.length / clip;
            
            for(int i=0; i<clip; i++) {
            	fos.write(data, i * clipSize, clipSize);            	
            }
            fos.write(data, clipSize * clip, data.length % clipSize);
            
        }catch(IOException e){
            throw e;
        }finally{
            if(fos != null){
                try{
                    fos.close();
                }catch(IOException e){
                }
            }
        }
    }
	
	private void onFinished() {
		// 结果保存到文件
		FileDialog dlg = new FileDialog(getShell(), SWT.SAVE);
		dlg.setText("请选择保存查询结果的文件");
		while (true) {
			String path = dlg.open();
			if (path == null) {
				boolean result = MessageDialog.openQuestion(getShell(), "消息", "确定要放弃保存查询结果吗？");
				if (result) {
					resultFile.delete();
					close();
					return;
				}
				continue;
			}
			try {
				EFSUtil.copyFile(resultFile, new File(path));
				resultFile.delete();
			} catch (Exception e) {
				MessageDialog.openError(getShell(), "错误", "保存文件失败：" + e.toString());
				continue;
			}
			
			close();
			
			UserReportInput input = new UserReportInput(new File(path));
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, UserReportEditor.ID);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
 		}
	}
	
	private void setProgress(String text) {
		progressText = text;
		display.asyncExec(new UpdateProgress());
	}
	
	private class UpdateProgress implements Runnable {
		public void run() {
			progressLabel.setText(progressText);
		}
	}
}
