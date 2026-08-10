package com.pip.server.billing.umpay;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.account.bean.Account;
import com.pip.server.billing.Server;

/**
 * 登录手机号绑定系统。
 * 请求参数：
 *     name = 帐号名
 *     password = 密码
 * 返回(UTF-8编码)：
 *     第一行是返回代码，0表示成功，1表示失败
 *     如果成功，第二到五行分别是：帐号ID、帐号名、绑定手机号、最后修改时间
 *     如果失败，第二行是错误信息
 */
public class UMPayLoginServlet extends HttpServlet {
	private static Logger log = Logger.getLogger(UMPayLoginServlet.class);
	private UMPayDataDAO umpayDataDAO;
	private Server server;
	
	public UMPayLoginServlet(Server s, UMPayDataDAO dao) {
		server = s;
		umpayDataDAO = dao;
	}

    @Override
    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
        // 验证请求IP
        String addr = request.getRemoteAddr();
        if (!server.trustip_directfee.contains(addr)) {
            log.warn("Possible attack from [" + addr + "] is rejected.");
            return;
        }
        
        // 取得参数
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        
        // 设置返回格式
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        // 验证用户名和密码
        Account acc = server.findAccountByName(name);
        if (acc == null || !acc.getPasswordDec().equals(password)) {
            out.println("1");
            out.println("帐号不存在或密码错误");
            return;
        }
        
        // 获取绑定手机号信息
        UMPayData umpayData = umpayDataDAO.getOrCreate(acc.getId(), acc.getName());
        
        // 返回信息
        out.println("0");
        out.println(acc.getId());
        out.println(acc.getName());
        out.println(acc.getBalance().getValue());
        out.println(umpayData.getPhones());
        out.println(umpayData.getLastModifyTime().getTime());
    }
}
