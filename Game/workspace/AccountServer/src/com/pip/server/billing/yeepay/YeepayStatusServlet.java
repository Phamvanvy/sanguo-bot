package com.pip.server.billing.yeepay;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.billing.Server;

/**
 * 查询易宝充值接口运行状态。
 * 返回(UTF-8编码)：
 *     0或者1。0表示异常，1表示正常。
 */
public class YeepayStatusServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(YeepayStatusServlet.class);
    private Server server;
    
	public YeepayStatusServlet(Server s) throws Exception{
	    server = s;
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
        // 验证请求IP
        String addr = request.getRemoteAddr();
        if (!server.trustip_directfee.contains(addr)) {
            log.warn("Possible attack from [" + addr + "] is rejected.");
            return;
        }
        
        // 设置返回格式
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        if (ConstYeepay.isHealthy()) {
        	out.print("1");
        } else {
        	out.print("0");
        }
	}
}
