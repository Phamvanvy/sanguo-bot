package com.pip.server.billing.u19pay;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.billing.Server;

/**
 * 19Pay支付流程回调。
 * 请求参数：
 *     参见19pay规范。
 * 返回：
 *     重定向到结果页面。
 */
public class Callback19PayServlet extends HttpServlet {
	private static Logger log = Logger.getLogger(Callback19PayServlet.class);
	private Order_19PayDAO dataDAO;
	private Server server;
	
	public Callback19PayServlet(Server s, Order_19PayDAO dao) {
		server = s;
		dataDAO = dao;
	}

    @Override
    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
        // 得到输入
        String verifyStr = request.getParameter("verifystring");
        int orderID = Integer.parseInt(request.getParameter("order_id"));
        String orderDate = request.getParameter("order_date");
        String amount = request.getParameter("amount");
        String paySeq = request.getParameter("pay_sq");
        String payDate = request.getParameter("pay_date");
        String pmID = request.getParameter("pm_id");
        String pcID = request.getParameter("pc_id");
        String result = request.getParameter("result");
        String cardNo = request.getParameter("pay_cardno");
        if (cardNo == null) {
            cardNo = "";
        }
        String cardPwd = request.getParameter("pay_cardpwd");
        if (cardPwd == null) {
            cardPwd = "";
        } 
        log.info("[19pay_callback]orderid[" + orderID + "]payseq[" + paySeq + "]result[" + result + 
                "]cardno[" + cardNo + "]cardpwd[" + cardPwd + "]md5[" + verifyStr + "]");
        
        // 验证MD5
        String verifySrc = "version_id=" + Const19Pay.VERSION + "&merchant_id=" + Const19Pay.MERCHANT_ID + "&order_date=" +
            orderDate + "&order_id=" + orderID + "&amount=" + amount + "&currenty=" + Const19Pay.CURRENCY_RMB + "&pay_sq=" +
            paySeq  + "&pay_date=" + payDate + "&pc_id=" + pcID + "&result=" + result + "&merchant_key=" + Const19Pay.MERCHANT_KEY;
        if (Const19Pay.getMD5(verifySrc).equals(verifyStr)) {
            log.info("[19pay_callback] 验证码错误");
            response.sendRedirect(Const19Pay.CALLBACK_URL + "?gamecode=1&code=1");
            return;
        }
        
        // 查找订单数据
        Order_19Pay order = dataDAO.getByID(orderID);
        if (order == null) {
            log.info("[19pay_callback] 订单不存在");
            response.sendRedirect(Const19Pay.CALLBACK_URL + "?gamecode=1&code=2");
            return;
        }
        
        // 返回
        if ("Y".equals(result)) {
            response.sendRedirect(Const19Pay.CALLBACK_URL + "?gamecode=" + order.getGameCode() + "&code=0");
        } else {
            response.sendRedirect(Const19Pay.CALLBACK_URL + "?gamecode=" + order.getGameCode() + "&code=3");
        }
    }
}
