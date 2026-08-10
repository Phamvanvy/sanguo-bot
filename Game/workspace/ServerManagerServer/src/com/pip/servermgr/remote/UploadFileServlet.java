package com.pip.servermgr.remote;

import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 上传文件覆盖服务器上的文件。
 * 参数：path = 绝对路径，输入流是文件内容
 * 返回：无
 */
public class UploadFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    public UploadFileServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 读取上传数据
		DataInputStream dis = new DataInputStream(request.getInputStream());
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int readLen;
		while ((readLen = dis.read(buf)) >= 0) {
		    if (readLen > 0) {
		        bos.write(buf, 0, readLen);
		    }
		}
		byte[] data = bos.toByteArray();
		dis.close();
		
		// 保存文件
        String path = request.getParameter("path");
		FileOutputStream fos = new FileOutputStream(path);
		fos.write(data);
		fos.close();
	}
}
