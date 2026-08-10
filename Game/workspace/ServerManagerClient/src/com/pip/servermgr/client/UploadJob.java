package com.pip.servermgr.client;

import java.io.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;

import com.pip.servermgr.data.HttpUtils;

/**
 * 文件上传任务，这个任务会把文件分成50K一个的小段，分段上传。
 * @author lighthu
 */
public class UploadJob implements IRunnableWithProgress {
    private String[][] uploadFiles;
    private int[] fileLengths;
    private int totalLen = 0;
	
	public UploadJob(String[][] files) throws IOException {
	    uploadFiles = files;
	    fileLengths = new int[files.length];
        for (int i = 0; i < files.length; i++) {
        	fileLengths[i] = (int)(new File(files[i][0]).length());
            totalLen += fileLengths[i];
        }
	}

	public void run(IProgressMonitor monitor) {
		int blockLen = 50000;
		monitor.beginTask("上传" + uploadFiles[0][1] + "(0/" + fileLengths[0] + ")...", totalLen);
		
		byte[] buf = new byte[blockLen];
		FileInputStream fis = null;
		try {
			for (int i = 0; i < uploadFiles.length; i++) {
				fis = new FileInputStream(uploadFiles[i][0]);
	            int startPos = 0;
				while (true) {
					// 检查是否用户取消了
	    			if (monitor.isCanceled()) {
	    				return;
	    			}
	    			
	    			monitor.setTaskName("上传" + uploadFiles[i][1] + "(" + startPos + "/" + fileLengths[i] + ")...");
	    			
	    			// 准备1段数据
	    			int thisLen = fis.read(buf);
	    			if (thisLen == -1) {
	    				break;
	    			} else if (thisLen == 0) {
	    				continue;
	    			}
	    			
	    			// 尝试上传这一段数据，如果失败，则反复尝试
	    			while (!monitor.isCanceled()) {
	    				try {
	    					if (thisLen < buf.length) {
	    						byte[] buf2 = new byte[thisLen];
	    						System.arraycopy(buf, 0, buf2, 0, thisLen);
	    						HttpUtils.uploadFile(uploadFiles[i][1], fileLengths[i], startPos, buf2);
	    					} else {
	    						HttpUtils.uploadFile(uploadFiles[i][1], fileLengths[i], startPos, buf);
	    					}
	    					break;
	    				} catch (Exception e) {
	    					e.printStackTrace();
	    					monitor.setTaskName("连接错误，正在重新尝试...");
	    					try {
	    						Thread.sleep(1000);
	    					} catch (Exception e1) {
	    					}
	    				}
	    			}
	    			
	    			// 更新进度
	    			startPos += thisLen;
	    			monitor.setTaskName("上传" + uploadFiles[i][1] + "(" + startPos + "/" + fileLengths[i] + ")...");
	    			monitor.worked(thisLen);
				}
				fis.close();
			}
		} catch (IOException e) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "错误", e.toString());
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (Exception e) {
			}
		}
        monitor.done();
	}
	
	private static byte[] loadFileData(String fname) throws IOException {
        File f = new File(fname);
        byte[] buf = new byte[(int)f.length()];
        FileInputStream fis = new FileInputStream(f);
        new DataInputStream(fis).readFully(buf);
        fis.close();
        return buf;
	}
}
