package com.pip.servermgr.client;

import java.io.*;
import java.net.HttpURLConnection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.pip.servermgr.data.HttpUtils;

/**
 * 文件上传任务，这个任务会把文件分成50K一个的小段，分段上传。
 * @author lighthu
 */
public class DownloadJob implements IRunnableWithProgress {
	private String localFile;
	private String remoteFile;
	private Exception exception;
	
	public DownloadJob(String localFile, String remoteFile) throws IOException {
		this.localFile = localFile;
		this.remoteFile = remoteFile;
	}
	
	public Exception getException() {
		return exception;
	}

	public void run(IProgressMonitor monitor) {
		monitor.beginTask("开始下载...", 100);
		HttpURLConnection conn = null;
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			// 获取文件信息
			Object[] info = HttpUtils.downloadFile(remoteFile);
			conn = (HttpURLConnection)info[0];
			is = (InputStream)info[1];
			int totalLen = ((Integer)info[2]).intValue();
			monitor.setTaskName("正在下载(0/" + totalLen + ")...");
			
			// 开始下载，一边下载一边保存
			fos = new FileOutputStream(localFile);
			byte[] buf = new byte[50000];
			int finishLen = 0;
			while (finishLen < totalLen) {
				if (monitor.isCanceled()) {
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
				int percent = ((finishLen + thisLen) * 100 / totalLen) - (finishLen * 100 / totalLen); 
				if (percent > 0) {
					monitor.worked(percent);
				}
				finishLen += thisLen;
				monitor.setTaskName("正在下载(" + finishLen + "/" + totalLen + ")...");
				if (thisLen > 0) {
					fos.write(buf, 0, thisLen);
				}
			}
			fos.close();
		} catch (Exception e) {
			exception = e;
		} finally {
			monitor.done();
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
	}
}
