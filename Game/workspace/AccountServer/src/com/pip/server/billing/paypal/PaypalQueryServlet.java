package com.pip.server.billing.paypal;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.billing.Server;
/**
 * Paypal订单查询通知
 * 
 */
@SuppressWarnings("serial")
public class PaypalQueryServlet  extends HttpServlet{
	private static Logger log = Logger.getLogger(PaypalQueryServlet.class);
	private Server server;
	private Order_PaypalDAO dataDAO;
	
	public PaypalQueryServlet(Server server,Order_PaypalDAO dao){
		this.server=server;
		this.dataDAO = dao;
	}
	
	@Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// 验证请求IP
        String addr = request.getRemoteAddr();
        if (!server.trustip_directfee.contains(addr)) {
            log.warn("Possible attack from [" + addr + "] is rejected.");
            return;
        }
		try {
			// 设置返回格式
	        response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			String orderid = request.getParameter("order_no");
			
			// 查找订单数据
			String[] orderResult =ConstPaypal.orderResults.get(orderid);
			if(orderResult==null){
				out.println("3");
	            out.println("订单不存在");
	            return;
			}
			Order_Paypal order = dataDAO.getBySeqID(orderid);
	        if (order == null ) {
	            log.info("[paypal_query] 订单不存在");
	            out.println("3");
	            out.println("订单不存在");
	            return;
	        }
	        
	         if (order.getStatus() == 1) {
				// 成功
				out.println("0");
				out.println("充值成功");
			} else if (order.getStatus() == 0) {
				// 尚未有结果
	            out.println("2");
	            out.println("尚未有结果");
			} else {
				// 失败
				out.println("1");
				out.println("充值失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);
    }
}
