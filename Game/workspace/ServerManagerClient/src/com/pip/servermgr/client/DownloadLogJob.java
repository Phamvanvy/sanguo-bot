package com.pip.servermgr.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.pip.servermgr.data.HttpUtils;

/**
 * 日志查询下载任务。
 * @author lighthu
 */
public class DownloadLogJob implements IRunnableWithProgress {
	private File localFile;
	private URL remoteFile;
	private Exception exception;
	
	public DownloadLogJob(File localFile, URL remoteFile) throws IOException {
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
		try {
			// 获取文件信息
			Object[] info = HttpUtils.downloadFile(remoteFile);
			conn = (HttpURLConnection)info[0];
			is = (InputStream)info[1];
			int totalLen = ((Integer)info[2]).intValue();
			monitor.setTaskName("正在下载(0/" + totalLen + ")...");
			
			// 开始下载并同期保存
			FileOutputStream fos = null;
			try {
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
					if (thisLen > 0) {
						fos.write(buf, 0, thisLen);
					}
					int percent = ((finishLen + thisLen) * 100 / totalLen) - (finishLen * 100 / totalLen); 
					if (percent > 0) {
						monitor.worked(percent);
					}
					finishLen += thisLen;
					monitor.setTaskName("正在下载(" + finishLen + "/" + totalLen + ")...");
				}
			} finally {
				if (fos != null) {
					fos.close();
				}
			}
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
		}
	}
}
