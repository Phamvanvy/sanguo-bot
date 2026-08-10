package com.pip.servermgr;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pip.servermgr.data.Configuration;
import com.pip.servermgr.data.SecurityUtils;

/**
 * 上传文件覆盖服务器上的文件。
 * 参数：通过post二进制流传递，格式为：用户名，密码，文件路径、文件大小、本段开始位置、本段长度、文件数据
 * 返回：无
 */
public class UploadFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static ConcurrentHashMap<String, byte[]> fileBuffer = new ConcurrentHashMap<String, byte[]>();
       
    public UploadFileServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 读取上传数据
		DataInputStream dis = new DataInputStream(request.getInputStream());
		String userName = dis.readUTF();
		String password = dis.readUTF();
		try {
			password = SecurityUtils.decryptPassword(password);
		} catch (Exception e) {
			throw new ServletException();
		}
		String path = dis.readUTF();
		int totalLen = dis.readInt();
		int start = dis.readInt();
		int thisLen = dis.readInt();
		byte[] data = new byte[thisLen];
		dis.readFully(data);
		dis.close();
		
		// 检查权限
		if (!Configuration.authenticate(userName, password, path, true, request.getRemoteAddr())) {
			throw new ServletException("权限错误");
		}
		
		// 在缓存中查找已有数据
		byte[] existBuf = fileBuffer.get(path);
		if (existBuf == null || existBuf.length != totalLen) {
			existBuf = new byte[totalLen];
			fileBuffer.put(path, existBuf);
		}
		System.arraycopy(data, 0, existBuf, start, thisLen);
		
		// 如果已经上传了最后一段，则存盘并清除缓存
		if (start + thisLen >= totalLen) {
			fileBuffer.remove(path);
			File f = new File(Utils.basePath, path);
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(existBuf);
			fos.close();

			// 检查目标目录中是否有sync.sh脚本，如果有，还需要执行此脚本同步到额外的服务器（QQ区需要这个功能）
			File shell = new File(new File(Utils.basePath, path).getParentFile(), "sync.sh");
			if (shell.exists()) {
				String cmd = shell.getAbsolutePath() + " " + new File(Utils.basePath, path).getName();
				ProbeUtils.executeShell(cmd);
			}
		}
	}
}
