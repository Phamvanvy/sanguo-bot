package com.pip.servermgr;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pip.servermgr.data.Configuration;
import com.pip.servermgr.data.SecurityUtils;

/**
 * 用于列出某个目录下的所有文件。
 * 参数：
 * 		username - 用户名
 * 		password - 密码
 * 		path - 相对于应用根目录的路径
 * 返回（UTF-8）编码：
 * 		每个文件一行，各字段用空格分隔，格式为：文件名 文件大小 最后修改时间
 */
public class ListFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日 HH:mm");
       
    public ListFileServlet() {
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
		
		File dir = new File(Utils.basePath, path);
		if (dir.equals(new File(Utils.basePath))) {
			return;
		}
		File[] files = dir.listFiles();
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/plain;charset=utf-8");
		PrintWriter out = response.getWriter();
		for (File f : files) {
			if (!f.isFile()) {
				continue;
			}
			out.print(f.getName());
			out.print(' ');
			out.print(f.length());
			out.print(' ');
			out.println(dateFormat.format(new Date(f.lastModified())));
		}
		out.close();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
