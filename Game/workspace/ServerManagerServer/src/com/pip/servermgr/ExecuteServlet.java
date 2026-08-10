package com.pip.servermgr;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pip.servermgr.data.Configuration;
import com.pip.servermgr.data.SecurityUtils;

/**
 * 执行服务器上的一个命令或脚本。
 * 参数：
 * 		username 用户名
 * 		password 密码
 * 		cmd 脚本路径（相对于项目根目录）
 * 		param 脚本参数
 * 		hasret 1表示有返回信息
 * 		forceupdate 1表示强制刷新
 * 返回（UTF-8格式）：
 * 		如果有返回信息，返回返回信息文件的内容(temp/status.temp)
 */
public class ExecuteServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public ExecuteServlet() {
        super();
    }

	public void destroy() {
		super.destroy();
		ProbeUtils.stopProbe();
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			Configuration.init(getServletContext().getRealPath("/WEB-INF"));
		} catch (Exception e) {
			throw new ServletException(e);
		}
		ProbeUtils.startProbe();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userName = request.getParameter("username");
		String password = request.getParameter("password");
		try {
			password = SecurityUtils.decryptPassword(password);
		} catch (Exception e) {
			throw new ServletException();
		}
		String cmd = request.getParameter("cmd");
		String param = request.getParameter("param");
		
		// 检查权限
		if (!Configuration.authenticate(userName, password, cmd, !"status".equals(param), request.getRemoteAddr())) {
			throw new ServletException("权限错误");
		}

		boolean hasRet = "1".equals(request.getParameter("hasret"));
		boolean forceUpdate = "1".equals(request.getParameter("forceupdate"));
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/plain;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.print(ProbeUtils.probeByShell(cmd, param, hasRet, forceUpdate));
		out.close();
	}
}
