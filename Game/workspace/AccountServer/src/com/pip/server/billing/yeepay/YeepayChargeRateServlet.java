package com.pip.server.billing.yeepay;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.billing.Server;

/**
 * 查询易宝充值获取元宝金额。
 * 返回(UTF-8编码)：
 *     多行，每行表示一个充值金额。每行的格式为：金额（分）+空格+获得i币金额（单位为i，36i=1元宝）
 */
public class YeepayChargeRateServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(YeepayChargeRateServlet.class);
    private Server server;
    
	public YeepayChargeRateServlet(Server s) throws Exception{
	    server = s;
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
        // 设置返回格式
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        Integer[] money = new Integer[ConstYeepay.IMONEY_MAP.size()];
        ConstYeepay.IMONEY_MAP.keySet().toArray(money);
        Arrays.sort(money);
        for (int i = 0; i < money.length; i++) {
        	out.println(money[i] + " " + ConstYeepay.IMONEY_MAP.get(money[i]));
        }
	}
}
