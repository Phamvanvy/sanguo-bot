package com.pip.server.billing.ruyifu;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

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
 * 如意付支付成功通知。
 * 请求参数：
 *     参见规范
 * 返回：
 *     0(成功)或1(失败)
 */
public class NotifyServlet_RuYiFu extends HttpServlet {
    private static Logger log = Logger.getLogger(NotifyServlet_RuYiFu.class);
    private Order_RuYiFuDAO dataDAO;
    private Server server;
    private AtomicInteger idGenerator = new AtomicInteger(10000);
    private EncDec cipher;
    
    public NotifyServlet_RuYiFu(Server s, Order_RuYiFuDAO dao) {
        server = s;
        dataDAO = dao;
        cipher = new EncDec();
        cipher.setKey(Const_RuYiFu.MERCHANT_KEY);
    }

    @Override
    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
        // 得到输入
        String szxVersion = request.getParameter("szxVersion");
        String szxOid = request.getParameter("szxOid");
        String szxSign = request.getParameter("szxSign");
        String szxUserId = request.getParameter("szxUserId");
        String szxDesc = request.getParameter("szxDesc");
        String szxGoods = request.getParameter("szxGoods");
        String szxGoodinfo = request.getParameter("szxGoodinfo");
        String szxPay = request.getParameter("szxPay");
        String szxRs = request.getParameter("szxRs");
        String szxCheckMd5 = request.getParameter("szxCheckMd5");

        log.info("[ruyifu_notify]orderid[" + szxOid + "]result[" + szxRs + "]md5[" + szxCheckMd5 + "]");
        
        // 设置返回格式
        response.setCharacterEncoding("GBK");
        PrintWriter out = response.getWriter();
        
        // 验证MD5
//        String verifySrc = szxVersion + szxOid + szxSign + szxUserId + szxDesc + szxGoods + szxGoodinfo +
//            szxPay + szxRs + Const_RuYiFu.MERCHANT_KEY;
//        if (!cipher.md5enc(verifySrc).equals(szxCheckMd5)) {
//            log.info("[ruyifi_notify] 验证码错误");
//            out.print("1");
//            return;
//        }
        
        String retInfo = szxUserId + szxOid;
        
        // 查找订单数据
        Order_RuYiFu order = dataDAO.getByOrderID(szxOid);
        if (order == null) {
            log.info("[ruyifu_notify] 订单不存在");
            out.print(retInfo);
            return;
        }
        
        // 修改订单状态并为用户添加i币
        if ("0".equals(szxRs)) {
            if (order.getStatus() != 1) {
                try {
                    order.setFeeID(addIMoney(order));
                } catch (Exception e) {
                    log.error(e, e);
                    log.info("[ruyifu_notify] 添加i币失败");
                    out.print(retInfo);
                    return;
                }
                log.info("[ruyifu_notify]orderid[" + szxOid + "]ChargeOK");
            } else {
                log.info("[ruyifu_notify]orderid[" + szxOid + "]Ignored");
            }
            order.setStatus(1);
        } else {
            order.setStatus(2);
        }
        order.setFinishTime(new java.util.Date());
        dataDAO.update(order);

        // 返回
        out.print(retInfo);

        // 回调
        String callback = GetOrderServlet_RuYiFu.callbacks.get(order.getOrderID());
        if (callback != null) {
            callback(callback, order.getOrderID(), order.getStatus() == 1, order.getAccountID(), order.getImoney());
        }
    }
    
    private int addIMoney(Order_RuYiFu order) throws Exception {
        // 在认证服务器创建订单
    	String channel;
    	if (order.getCardCorp() == Const_RuYiFu.OPERATOR_UNICOM) {
    		channel = "RUYIFU_UNICOM_";
    	} else if (order.getCardCorp() == Const_RuYiFu.OPERATOR_CTEL) {
    		channel = "RUYIFU_CTEL_";
    	} else {
    		channel = "RUYIFU_CMCC_";
    	}
    	
        Fee fee = server.newFee(order.getUserName(), order.getImoney() * 100, channel + (order.getMoney() / 100));
        
        // 完成订单，修改帐户余额
        if (!server.fulfillOrder(fee.getId())) {
            throw new Exception();
        }
        
        // 添加积分
        server.addCreditByMoney(order.getAccountID(), order.getMoney() / 100);
        
        return fee.getId();
    }
    
    public void callback(String callbackurl, String orderId, boolean success, int accountId, int amount) {
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
                log.info("[ruyifu_notify]orderid[" + orderId + "]CallbackOK");
            }
        } catch (Exception ex) {
            log.error(ex, ex);
        } finally {
            method.releaseConnection();
        }
    }
}
