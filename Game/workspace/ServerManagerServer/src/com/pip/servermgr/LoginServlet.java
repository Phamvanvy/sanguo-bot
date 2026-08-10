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
 * 用户登录。
 * 参数：
 * 		name - 用户名
 * 		password - 密码
 * 返回（UTF-8）编码：
 * 		用户可管理的服务器XML配置文件
 */
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public LoginServlet() {
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
		String xml = Configuration.checkLogin(userName, password, request.getRemoteAddr());
		if (xml == null) {
			response.sendError(500);
		} else {
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/xml;charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print(xml);
			out.close();
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
