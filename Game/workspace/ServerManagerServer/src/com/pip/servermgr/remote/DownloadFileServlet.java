package com.pip.servermgr.remote;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 下载一个文件。
 * 参数：
 * 		path - 绝对路径
 * 返回：
 * 		文件内容
 */
public class DownloadFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public DownloadFileServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getParameter("path");

		File file = new File(path);
		byte[] data = new byte[(int)file.length()];
		FileInputStream fis = new FileInputStream(file);
		new DataInputStream(fis).readFully(data);
		fis.close();

		response.setContentType("application/octet-stream");
		response.setContentLength(data.length);
		ServletOutputStream os = response.getOutputStream();
		os.write(data);
		os.close();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
