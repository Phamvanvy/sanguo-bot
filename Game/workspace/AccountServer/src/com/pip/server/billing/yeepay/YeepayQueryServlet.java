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
 * 易宝神州行支付查询订单状态。
 * 请求参数：
 *     orderid = 订单ID
 * 返回(UTF-8编码)：
 *     第一行是返回代码，0表示成功，1表示失败，2表示尚未有结果
 *     如果失败，第二行是错误信息。
 */
public class YeepayQueryServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(YeepayQueryServlet.class);
    private Server server;
    
	public YeepayQueryServlet(Server s) throws Exception{
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
        
        // 取得参数
        String orderID = request.getParameter("orderid");

        // 设置返回格式
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        // 查询订单状态
        String[] result = YeepayNotifyServlet.orderResults.get(orderID);
        if (result == null) {
            // 尚未有结果
            out.println("2");
        } else if ("1".equals(result[0])) {
            // 成功
            out.println("0");
        } else {
            // 失败
            out.println("1");
            out.println(ConstYeepay.getErrorMessage(result[1]));
        }
	}
}
