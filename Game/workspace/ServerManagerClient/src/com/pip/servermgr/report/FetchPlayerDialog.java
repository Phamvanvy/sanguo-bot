package com.pip.servermgr.report;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.servermgr.data.HttpUtils;
import com.pip.servermgr.data.Server;
import com.pip.servermgr.data.TextFile;
import com.pip.util.EFSUtil;
import com.pipimage.utils.GZIP;
import com.pipimage.utils.Utils;

/**
 * 这个对话框专门用于从服务器提取一个玩家的数据。
 * @author lighthu
 */
public class FetchPlayerDialog extends Dialog {

	private Combo comboEncoding;
	private Text textPlayerID;
	
	private String progressText = "";
	private Display display;
	
	public Server server;
	private Label progressLabel;
	private boolean inProgress;
	private FetchThread workThread;
	private BatchFetchThread batchWorkThread;
	private File resultFile;
	
	int playerID;
	int encoding;
	
	private ArrayList<Integer> batchPlayerIds = new ArrayList<Integer>();
	private Button batchButton; //批量导出
	private Text batchFileDirText;

	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public FetchPlayerDialog(Shell parentShell, Server server) {
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

		final Label label_2 = new Label(container, SWT.NONE);
		label_2.setText("玩家ID：");

		textPlayerID = new Text(container, SWT.BORDER);
		textPlayerID.setText("0");
		final GridData gd_textPlayerID = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textPlayerID.setLayoutData(gd_textPlayerID);

		final Label label = new Label(container, SWT.NONE);
		label.setText("编码：");

		comboEncoding = new Combo(container, SWT.READ_ONLY);
		comboEncoding.setItems(new String[] {"GBK", "UTF-8"});
		comboEncoding.select(0);
		final GridData gd_comboEncoding = new GridData(SWT.FILL, SWT.CENTER, true, false);
		comboEncoding.setLayoutData(gd_comboEncoding);

		batchButton = new Button(container, SWT.NONE);
		batchButton.setText("批量复制");
		batchButton.addSelectionListener(new SelectionAdapter(){
           public void widgetSelected(SelectionEvent e){
        	   onBatchFile();
           }
		}
		);
		batchFileDirText = new Text(container, SWT.BORDER);
		batchFileDirText.setText("                                                           ");
		batchFileDirText.setEditable(false);
		final GridData gd_batchFileDir = new GridData(SWT.FILL, SWT.FILL, true, true);
//		gd_batchFileDir.widthHint = 200;
//		gd_batchFileDir.minimumWidth = 200;
		comboEncoding.setLayoutData(gd_batchFileDir);
		
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
		return new Point(284, 178);
	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("复制玩家");
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			if (inProgress) {
				return;
			}
			encoding = comboEncoding.getSelectionIndex();
			inProgress = true;
			if(batchPlayerIds.size() > 0) {
				batchWorkThread = new BatchFetchThread();
				batchWorkThread.start();
				return;
			} else {
				playerID = Integer.parseInt(textPlayerID.getText());				
				workThread = new FetchThread();
				workThread.start();
				return;
			}
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
				AbstractReportEngine engine = AbstractReportEngine.create(server.parent.parent.name);
				params.put("playerTable", engine.getPlayerTable());
				params.put("playerID", String.valueOf(playerID));
				if (encoding == 0) {
					params.put("encoding", "gbk");
				} else {
					params.put("encoding", "utf8");
				}
				
				// 首先初始化数据提取线程
				setProgress("开始提取");
				int[] ids;
				try {
					ids = HttpUtils.startFetchData(server.getShellScript(), params, "com.pip.servermgr.report.GenericPlayerFetcher");
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
					Utils.saveFileData(tempFile, data);
					
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
			} finally {
				inProgress = false;
			}
		}
	}
	
	private class BatchFetchThread extends Thread {
		public void run() {
			try {
				setProgress("开始提取");
				ByteArrayOutputStream outstream = new ByteArrayOutputStream();
				File tempFile = null;
				
				for(int i=0; i<batchPlayerIds.size(); i++) {
					Integer pid = batchPlayerIds.get(i);
					Map<String, String> params = new HashMap<String, String>();
					AbstractReportEngine engine = AbstractReportEngine.create(server.parent.parent.name);
					params.put("playerTable", engine.getPlayerTable());
					params.put("playerID", String.valueOf(pid));
					if (encoding == 0) {
						params.put("encoding", "gbk");
					} else {
						params.put("encoding", "utf8");
					}
					
					// 首先初始化数据提取线程
					setProgress("开始提取" + (i + 1) + "/" + batchPlayerIds.size());
					int[] ids;
					try {
						ids = HttpUtils.startFetchData(server.getShellScript(), params, "com.pip.servermgr.report.GenericPlayerFetcher");
					} catch (Exception e) {
						if (batchWorkThread == this) {
							setProgress(e.toString());
						}
						return;
					}
					if (batchWorkThread != this) {
						HttpUtils.cancelFetchData(ids[0], ids[1]);
						return;
					}
					
					// 循环检查服务器提取进度
					while (true) {
						try {
							Thread.sleep(500);
						} catch (Exception e) {
						}
						if (batchWorkThread != this) {
							HttpUtils.cancelFetchData(ids[0], ids[1]);
							return;
						}
						try {
							Object[] info = HttpUtils.queryFetchData(ids[0], ids[1]);
							int status = ((Integer)info[0]).intValue();
							String msg = (String)info[1];
							if (status == 0) { // 正在执行
								msg += (i + 1) + "/" + batchPlayerIds.size();
								if (batchWorkThread == this) {
									setProgress(msg);
								}
							} else if (status == 1) { // 已经完成
								break;
							} else { // 发生错误
								if (batchWorkThread == this) {
									setProgress(msg);
								}
								HttpUtils.cancelFetchData(ids[0], ids[1]);
								return;
							}
						} catch (Exception e) {
							if (batchWorkThread == this) {
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
					
					try {
						tempFile = File.createTempFile("_report_data", ".dat");
						
						// 获取文件信息
						Object[] info = HttpUtils.downloadFetchData(ids[0], ids[1]);
						conn = (HttpURLConnection)info[0];
						is = (InputStream)info[1];
						int totalLen = ((Integer)info[2]).intValue();
						if (batchWorkThread != this) {
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
							if (batchWorkThread != this) {
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
						
						String delSql = "delete from player where id=" + pid + ";\r\n";
						outstream.write(delSql.getBytes(encoding == 0 ? "gbk" : "utf8"));
						outstream.write(data);						

						// 下载完成，执行清理
						HttpUtils.cleanFetchData(ids[0], ids[1]);
						
					} catch (Exception e) {
						if (batchWorkThread == this) {
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
					

					if(inProgress == false ) {
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
						return;
					}
					
				}
							
				try {
					
					Utils.saveFileData(tempFile, handleBatchFile(outstream).toByteArray());
				}catch(Exception e) {
					if (outstream != null) {
						try {
							outstream.close();
						} catch (Exception e1) {
						}
						outstream = null;
					}
					if (tempFile != null) {
						tempFile.delete();
					}
				} finally {
					if (outstream != null) {
						try {
							outstream.close();
						} catch (Exception e1) {
						}
						outstream = null;
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
			} finally {
				inProgress = false;
			}
		}
	}
	
	private ByteArrayOutputStream handleBatchFile(ByteArrayOutputStream bos) {
		//去掉无用的行
		ByteArrayOutputStream ret = new ByteArrayOutputStream();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bos.toByteArray())));
			String line;
			while((line=br.readLine()) != null) {
				if(line.startsWith("--") == false &&
						line.startsWith("/*") == false &&
						line.startsWith("LOCK TABLES") == false &&
						line.startsWith("UNLOCK TABLES") == false &&
						"".equals(line) == false) {	
					line += "\r\n";
					ret.write(line.getBytes(encoding == 0 ? "gbk" : "utf8"));
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	
	private void onFinished() {
		// 结果保存到文件
		FileDialog dlg = new FileDialog(getShell(), SWT.SAVE);
		dlg.setFilterExtensions(new String[] { ".sql" });
		dlg.setFilterNames(new String[] { "SQL文件" });
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
			return;
 		}
	}
	
	private void onBatchFile() {
		// 选择批量导出的文件，只有一列，都是playerid，一次最多只能导出10个player
		FileDialog dlg = new FileDialog(getShell(), SWT.SAVE);
		dlg.setFilterExtensions(new String[] { "*.*" });
		dlg.setFilterNames(new String[] { "文本文件" });
		dlg.setText("请选择批量复制的文件");
		String path = dlg.open();
		if (path != null) {
			File file = new File(path);
			if(file.exists()) {
				try {
					TextFile tf = new TextFile(file);
					String content = tf.getPage(1);
					String[] ids = content.split("\r\n");
					if(ids != null) {
						if(ids.length > 50) {
							MessageDialog.openError(getShell(), "错误", "打开文件失败：批量复制记录数不能超过50");
						} else {
							for(String id : ids) {
								if(ids != null && "".equals(ids) == false) {
									batchPlayerIds.add(Integer.parseInt(id));
								}
							}							
						}
					}
					
				}catch(Exception e) {
					MessageDialog.openError(getShell(), "错误", "打开文件失败：" + e.toString());
				}
				batchFileDirText.setText(path);
			} else {
				MessageDialog.openError(getShell(), "错误", "文件不存在");
				batchFileDirText.setText("");
			}
			
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
