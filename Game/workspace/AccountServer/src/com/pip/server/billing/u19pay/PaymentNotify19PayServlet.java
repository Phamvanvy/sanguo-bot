package com.pip.server.billing.u19pay;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.account.bean.Fee;
import com.pip.server.billing.Server;

/**
 * 19Pay支付成功通知。
 * 请求参数：
 *     参见规范
 * 返回：
 *     Y或N
 */
public class PaymentNotify19PayServlet extends HttpServlet {
	private static Logger log = Logger.getLogger(PaymentNotify19PayServlet.class);
	private Order_19PayDAO dataDAO;
	private Server server;
	
	public PaymentNotify19PayServlet(Server s, Order_19PayDAO dao) {
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
        if (orderDate == null) {
            orderDate = "";
        }
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
        log.info("[19pay_notify]orderid[" + orderID + "]payseq[" + paySeq + "]result[" + result + 
                "]cardno[" + cardNo + "]cardpwd[" + cardPwd + "]md5[" + verifyStr + "]");
        
        // 设置返回格式
        response.setCharacterEncoding("GBK");
        PrintWriter out = response.getWriter();
        
        // 验证MD5
        String verifySrc = "version_id=" + Const19Pay.VERSION + "&merchant_id=" + Const19Pay.MERCHANT_ID + "&order_id=" + 
            orderID + "&result=" + result + "&order_date=" + orderDate + "&amount=" + amount + "&currency=" + 
            Const19Pay.CURRENCY_RMB + "&pay_sq=" + paySeq  + "&pay_date=" + payDate + "&pc_id=" + pcID + 
            "&merchant_key=" + Const19Pay.MERCHANT_KEY;
        String md52 = Const19Pay.getMD5(verifySrc);
        if (!md52.equals(verifyStr)) {
            log.info("[19pay_notify] 验证码错误 verify[" + verifySrc + "]md5[" + md52 + "]");
            out.print("N");
            return;
        }
        
        // 查找订单数据
        Order_19Pay order = dataDAO.getByID(orderID);
        if (order == null) {
            log.info("[19pay_notify] 订单不存在");
            out.print("N");
            return;
        }
        
        // 修改订单状态并为用户添加i币
        if ("Y".equals(result)) {
            if (order.getStatus() != 1) {
                try {
                    order.setCardNo(cardNo);
                    order.setCardPass(cardPwd);
                    order.setFeeID(addIMoney(order));
                } catch (Exception e) {
                    log.error(e, e);
                    log.info("[19pay_notify] 添加i币失败");
                    out.print("N");
                    return;
                }
                log.info("[19pay_notify] ChargeOK");
            } else {
                log.info("[19pay_notify] Ignored");
            }
            order.setStatus(1);
        } else {
            order.setStatus(2);
        }
        try {
            order.setFinishTime(Const19Pay.DATE_FORMAT2.parse(payDate));
        } catch (Exception e) {
            order.setFinishTime(new java.util.Date());
        }
        order.setPaySeq(paySeq);
        dataDAO.update(order);
        
        // 返回
        out.print("Y");
    }
    
    private int addIMoney(Order_19Pay order) throws Exception {
        // 在认证服务器创建订单
        Fee fee = server.newFee(order.getUserName(), order.getImoney() * 100, "UNICOM_" + (order.getMoney() / 100));
        
        // 完成订单，修改帐户余额
        if (!server.fulfillOrder(fee.getId())) {
            throw new Exception();
        }
        
        // 添加积分
        server.addCreditByMoney(order.getAccountID(), order.getMoney() / 100);
        
        return fee.getId();
    }
}
