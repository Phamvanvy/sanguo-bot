package com.pip.server.billing.u19pay;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import com.pip.server.account.bean.Fee;
import com.pip.server.billing.Server;

/**
 * 19Pay支付成功通知（直连方式）。
 * 请求参数：
 *     参见规范
 * 返回：
 *     Y或N
 */
public class PaymentNotifyServlet_19PayD extends HttpServlet {
	private static Logger log = Logger.getLogger(PaymentNotifyServlet_19PayD.class);
	private Order_19PayDAO dataDAO;
	private Server server;
	
	public PaymentNotifyServlet_19PayD(Server s, Order_19PayDAO dao) {
		server = s;
		dataDAO = dao;
	}

    @Override
    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
        // 得到输入
    	String version_id = request.getParameter("version_id");
    	String merchant_id = request.getParameter("merchant_id");
        String verifystring = request.getParameter("verifystring");
        String order_date = request.getParameter("order_date");
        if (order_date == null) {
            order_date = "";
        }
        int order_id = Integer.parseInt(request.getParameter("order_id"));
        String result = request.getParameter("result");
        String amount = request.getParameter("amount");
        String currency = request.getParameter("currency");
        String pay_sq = request.getParameter("pay_sq");
        String pay_date = request.getParameter("pay_date");
        String count = request.getParameter("count");
        if (count == null) {
        	count = "";
        }
        String card_num1 = request.getParameter("card_num1");
        String card_pwd1 = request.getParameter("card_pwd1");
        String pm_id1 = request.getParameter("pm_id1");
        String pc_id1 = request.getParameter("pc_id1");
        String card_status1 = request.getParameter("card_status1");
        String card_code1 = request.getParameter("card_code1");
        String card_date1 = request.getParameter("card_date1");
        String r1 = request.getParameter("r1");

        log.info("[19payd_notify]orderid[" + order_id + "]payseq[" + pay_sq + "]result[" + result + "]md5[" + verifystring + "]");
        
        // 设置返回格式
        response.setCharacterEncoding("GBK");
        PrintWriter out = response.getWriter();
        
        // 验证MD5
        String verifySrc = "version_id=" + version_id + "&merchant_id=" + merchant_id + "&order_id=" + 
        	order_id + "&result=" + result + "&order_date=" + order_date + "&amount=" + amount + "&currency=" + 
        	currency + "&pay_sq=" + pay_sq + "&pay_date=" + pay_date + "&count=" + count + "&card_num1=" +
        	card_num1 + "&card_pwd1=" + card_pwd1 + "&pc_id1=" + pc_id1 + "&card_status1=" + card_status1 +
        	"&card_code1=" + card_code1 + "&card_date1=" + card_date1 + "&r1=" + r1 + "&merchant_key=" +
        	Const19Pay.MERCHANT_KEY;
        String md52 = Const19Pay.getMD5(verifySrc);
        if (!md52.equals(verifystring)) {
            log.info("[19payd_notify] 验证码错误 verify[" + verifySrc + "]md5[" + md52 + "]");
            out.print("N");
            return;
        }
        
        // 查找订单数据
        Order_19Pay order = dataDAO.getByID(order_id);
        if (order == null) {
            log.info("[19payd_notify] 订单不存在");
            out.print("N");
            return;
        }
        
        // 修改订单状态并为用户添加i币
        if ("Y".equals(result)) {
            if (order.getStatus() != 1) {
                try {
                    order.setFeeID(addIMoney(order));
                } catch (Exception e) {
                    log.error(e, e);
                    log.info("[19payd_notify] 添加i币失败");
                    out.print("N");
                    return;
                }
                log.info("[19payd_notify] ChargeOK");
            } else {
                log.info("[19payd_notify] Ignored");
            }
            order.setStatus(1);
        } else {
            order.setStatus(2);
        }
        try {
            order.setFinishTime(Const19Pay.DATE_FORMAT2.parse(pay_date));
        } catch (Exception e) {
            order.setFinishTime(new java.util.Date());
        }
        dataDAO.update(order);
        
        // 返回
        out.print("Y");
        
        // 回调
        String callback = GetOrderServlet_19PayD.callbacks.get(String.valueOf(order.getId()));
        if (callback != null) {
            callback(callback, order.getId(), order.getStatus() == 1, order.getAccountID(), order.getImoney());
        }
    }
    
    private int addIMoney(Order_19Pay order) throws Exception {
        // 在认证服务器创建订单
    	String cid = "19PAYD_";
    	if (order.getCardType() == 0) {
    		cid = "UNICOM_";
    	}
        Fee fee = server.newFee(order.getUserName(), order.getImoney() * 100, cid + (order.getMoney() / 100));
        
        // 完成订单，修改帐户余额
        if (!server.fulfillOrder(fee.getId())) {
            throw new Exception();
        }
        
        // 添加积分
        server.addCreditByMoney(order.getAccountID(), order.getMoney() / 100);
        
        return fee.getId();
    }
    
    public void callback(String callbackurl, int orderId, boolean success, int accountId, int amount) {
    	String url = callbackurl;
		url += "?orderid=" + orderId + "&accountid=" + accountId + "&success=" + (success ? "true" : "false") + "&amount=" + amount + "&channel=RUYIFU";
		url = server.wrapCallbackURL(url);
		GetMethod method = new GetMethod(url);
		method.addRequestHeader( "Connection", "close");
        try {
            HttpClient httpclient = new HttpClient();
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(
                    10000);
            httpclient.getParams().setSoTimeout(30000);
            int code = httpclient.executeMethod(method);
            if (code == 200) {
                log.info("[19payd_notify]orderid[" + orderId + "]CallbackOK");
            }
        } catch (Exception ex) {
            log.error(ex, ex);
        } finally {
            method.releaseConnection();
        }
    }
}
