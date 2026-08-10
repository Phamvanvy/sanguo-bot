package com.pip.servermgr;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.omg.CORBA_2_3.portable.OutputStream;

import com.pip.servermgr.data.Configuration;
import com.pip.servermgr.data.SecurityUtils;

/**
 * 用于列出某个目录下的所有文件。
 * 参数：
 * 		username - 用户名
 * 		password - 密码
 * 		path - 相对于应用根目录的路径
 * 返回：
 * 		文件内容
 */
public class DownloadFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public DownloadFileServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userName = request.getParameter("username");
		String password = request.getParameter("password");
		try {
			password = SecurityUtils.decryptPassword(password);
		} catch (Exception e) {
			throw new ServletException();
		}
		String path = request.getParameter("path");

		// 检查权限
		if (!Configuration.authenticate(userName, password, path, false, request.getRemoteAddr())) {
			throw new ServletException("权限错误");
		}
		
		File file = new File(Utils.basePath, path);
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
