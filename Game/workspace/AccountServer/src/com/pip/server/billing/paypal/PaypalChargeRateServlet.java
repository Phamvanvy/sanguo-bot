package com.pip.server.billing.paypal;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.billing.Server;
/**
 * Paypal充值兑换比例
 * 
 */
@SuppressWarnings("serial")
public class PaypalChargeRateServlet  extends HttpServlet{
	private static Logger log = Logger.getLogger(PaypalChargeRateServlet.class);
	private Server server;
	public PaypalChargeRateServlet(Server server){
		this.server = server;
	}
	
	@Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// 验证请求IP
        String addr = request.getRemoteAddr();
        if (!server.trustip_directfee.contains(addr)) {
            log.warn("Possible attack from [" + addr + "] is rejected.");
            return;
        }
		 // 设置返回格式
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        Integer[] money = new Integer[ConstPaypal.IMONEY_MAP.size()];
        ConstPaypal.IMONEY_MAP.keySet().toArray(money);
        Arrays.sort(money);
        for (int i = 0; i < money.length; i++) {
        	Integer[] values = ConstPaypal.IMONEY_MAP.get(money[i]);
        	StringBuffer sbuf = new StringBuffer(String.valueOf(money[i]));
        	for(int j = 0;j<values.length;j++){
        		sbuf.append(",").append(values[j]);
        	}
        	out.println(sbuf.toString());
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);
    }
    
    
}
