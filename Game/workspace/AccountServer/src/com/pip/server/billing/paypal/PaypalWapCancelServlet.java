package com.pip.server.billing.paypal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.billing.Server;

/**
 * paypal通知，用户取消交易
 * @author jyu
 *
 */
@SuppressWarnings("serial")
public class PaypalWapCancelServlet extends HttpServlet{
	
	private Server server;
	private Order_PaypalDAO dataDAO;
		
	private static Logger log = Logger.getLogger(PaypalWapCancelServlet.class);
	
	public PaypalWapCancelServlet(Server s,Order_PaypalDAO dao){
		server = s;
		dataDAO = dao;
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws  ServletException, IOException {
      
		String token = request.getParameter("token");
		log.info("[paypal_wap_cancel] 用户取消操作[token]"+token);
		try {
			Order_Paypal order = dataDAO.getByPaypalID(token);

			String order_no = null;
			if (order != null && order.getStatus() == 0) {
				order.setStatus(-1);// 用户取消操作
				order.setFinishTime(new java.util.Date());
				dataDAO.update(order);
				order_no = order.getPaySeq();
			}
			String returnhttp = PaypalWapCallbackServlet.returnURLMap.get(order_no);
			if (returnhttp == null || returnhttp.trim().equals("")) {
				returnhttp =ConstPaypal.callbackURL;
			}
			if(returnhttp.endsWith("?")){
			}else if(returnhttp.indexOf("&")>0){
				returnhttp = returnhttp + "&" ;
			}else{
				returnhttp = returnhttp + "?" ;
			}
			returnhttp = returnhttp + "retcode=-1&retmsg=cancel" ;
			response.sendRedirect(returnhttp);

			// 把结果保存在缓存中等待查询
			ConstPaypal.orderResults.put(order_no, new String[] { order_no,"结果参数" });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
