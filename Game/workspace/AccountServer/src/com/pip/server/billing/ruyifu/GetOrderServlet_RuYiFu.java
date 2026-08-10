package com.pip.server.billing.ruyifu;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import com.pip.server.account.bean.Account;
import com.pip.server.billing.Server;

/**
 * 如意付渠道下单。
 * 请求参数：
 *     id = 帐号ID
 *     name = 帐号名
 *     上面2个参数任意指定一个即可
 *     amount = 金额(分)
 *     gamecode = 游戏代码
 *     cardno = 卡号
 *     cardpass = 密码
 *     cardarea = 卡地区（0 - 全国(移动,电信,联通), 31 - 移动浙江卡，16 - 移动江苏卡
 *     cardcorp = 所属公司（1 - 移动，2 - 联通，3 - 电信）
 *     channel = 用户渠道号
 *     returnhttp = 结果通知回调地址
 * 返回(UTF-8编码)：
 *     第一行是返回代码，0表示成功，1表示失败
 *     如果失败，第二行是错误信息，如果成功，第二行是订单ID
 */
public class GetOrderServlet_RuYiFu extends HttpServlet {
    private static Logger log = Logger.getLogger(GetOrderServlet_RuYiFu.class);
    private Order_RuYiFuDAO dataDAO;
    private Server server;
    private AtomicInteger idGenerator = new AtomicInteger(10000);
    private EncDec cipher;
    static ConcurrentHashMap<String, String> callbacks = new ConcurrentHashMap<String, String>();
    
    public GetOrderServlet_RuYiFu(Server s, Order_RuYiFuDAO dao) {
        server = s;
        dataDAO = dao;
        cipher = new EncDec();
        cipher.setKey(Const_RuYiFu.MERCHANT_KEY);
    }

    @Override
    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
        // 验证请求IP
        String addr = request.getRemoteAddr();
        if (!server.trustip_directfee.contains(addr)) {
            log.warn("Possible attack from [" + addr + "] is rejected.");
            return;
        }
        
        // 取得参数
        String name = request.getParameter("name");
        int id = -1;
        if (name == null) {
            id = Integer.parseInt(request.getParameter("id"));
        }
        int amount = Integer.parseInt(request.getParameter("amount"));
        int gameCode = Integer.parseInt(request.getParameter("gamecode"));
        String cardNo = request.getParameter("cardno");
        String cardPass = request.getParameter("cardpass");
        int cardArea = Integer.parseInt(request.getParameter("cardarea"));
        int cardCorp = Const_RuYiFu.OPERATOR_CMCC;
        try {
            cardCorp = Integer.parseInt(request.getParameter("cardcorp"));
        } catch (Exception e) {
        }
        String channel = request.getParameter("channel");
        String callbackURL = request.getParameter("returnhttp");
        
        if (name == null) {
            log.info("[ruyifu_order]accountid[" + id + "]amount[" + amount + "]gamecode[" + gameCode + 
                    "]cardno[" + cardNo + "]cardpass[" + cardPass + "]cardarea[" + cardArea + "]returnurl[" + callbackURL + "]");
        } else {
            log.info("[ruyifu_order]accountname[" + name + "]amount[" + amount + "]gamecode[" + gameCode + 
                    "]cardno[" + cardNo + "]cardpass[" + cardPass + "]cardarea[" + cardArea + "]returnurl[" + callbackURL + "]");
        }

        // 设置返回格式
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        // 验证用户名和密码
        Account acc;
        if (name != null) {
            acc = server.findAccountByName(name);
        } else {
            acc = server.findAccount(id);
        }
        if (acc == null) {
            log.info("[ruyifu_order] 帐号不存在");
            out.println("1");
            out.println("帐号不存在");
            return;
        }
        
        // 验证金额
        if (!Const_RuYiFu.IMONEY_MAP.containsKey(amount)) {
            log.info("[ruyifu_order] 金额错误");
            out.println("2");
            out.println("金额错误");
            return;
        }
        
        // 创建新的订单
        Order_RuYiFu order = new Order_RuYiFu();
        order.setAccountID(acc.getId());
        order.setUserName(acc.getName());
        order.setCreateTime(new java.util.Date());
        order.setMoney(amount);
        order.setStatus(0);
        order.setGameCode(gameCode);
        order.setImoney(Const_RuYiFu.IMONEY_MAP.get(amount));
        String orderID = Const_RuYiFu.DATE_FORMAT.format(order.getCreateTime());
        String suffix = String.valueOf(idGenerator.getAndIncrement());
        orderID += suffix.substring(suffix.length() - 4);
        order.setOrderID(orderID);
        order.setChannel(channel);
        order.setCardNo(cardNo);
        order.setCardPass(cardPass);
        order.setCardCorp(cardCorp);
        dataDAO.create(order);
        if (order.getId() == 0) {
            log.info("[ruyifu_order] 创建订单失败");
            out.println("3");
            out.println("创建订单失败");
            return;
        }
        
        // 向如意付平台发起支付请求
        String returnURL = server.getServerURL() + "/ruyifu_orderok";
        String notifyURL = server.getServerURL() + "/ruyifu_notify";
        GetMethod method = new GetMethod(Const_RuYiFu.ORDER_URL);
        method.addRequestHeader( "Connection", "close");
        try {
            HttpClient httpclient = new HttpClient();
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
            httpclient.getParams().setSoTimeout(30000);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new NameValuePair("szxVersion", Const_RuYiFu.VERSION));
//            log.info("szxVersion = " + Const_RuYiFu.VERSION);
            params.add(new NameValuePair("szxOid", order.getOrderID()));
//            log.info("szxOid = " + order.getOrderID());
            params.add(new NameValuePair("szxSign", String.valueOf(acc.getId())));
//            log.info("szxSign = " + String.valueOf(acc.getId()));
            params.add(new NameValuePair("szxUserId", Const_RuYiFu.MERCHANT_USERID));
//            log.info("szxUserId = " + Const_RuYiFu.MERCHANT_USERID);
            params.add(new NameValuePair("szxPass", cipher.encode(Const_RuYiFu.MERCHANT_PASS)));
//            log.info("szxPass = " + cipher.encode(Const_RuYiFu.MERCHANT_PASS));
            params.add(new NameValuePair("szxPay", String.valueOf(amount / 100)));
//            log.info("szxPay = " + String.valueOf(amount / 100));
            params.add(new NameValuePair("szxReturnUrl", returnURL));
//            log.info("szxReturnUrl = " + returnURL);
            params.add(new NameValuePair("szxNotifyUrl", notifyURL));
//            log.info("szxNotifyUrl = " + notifyURL);
            String md5src = Const_RuYiFu.VERSION + order.getOrderID() + String.valueOf(acc.getId()) +
                Const_RuYiFu.MERCHANT_USERID + cipher.encode(Const_RuYiFu.MERCHANT_PASS) + 
                String.valueOf(amount / 100) + returnURL + notifyURL + 
                Const_RuYiFu.PAYTYPE_NEWCARD + Const_RuYiFu.PAYTYPE_SINGLE +
                Const_RuYiFu.INTERFACE_PAY + cardNo + cardPass + String.valueOf(amount / 100) +
                cardCorp + cardArea + Const_RuYiFu.MERCHANT_KEY;
//            log.info(md5src);
            params.add(new NameValuePair("szxCheckMd5", cipher.md5enc(md5src)));
//            log.info("szxCheckMd5 = " + cipher.md5enc(md5src));
            params.add(new NameValuePair("payType", String.valueOf(Const_RuYiFu.PAYTYPE_NEWCARD)));
//            log.info("payType = " + String.valueOf(Const_RuYiFu.PAYTYPE_NEWCARD));
            params.add(new NameValuePair("payNums", String.valueOf(Const_RuYiFu.PAYTYPE_SINGLE)));
//            log.info("payNums = " + String.valueOf(Const_RuYiFu.PAYTYPE_SINGLE));
            params.add(new NameValuePair("Ncard", ""));
//            log.info("Ncard = " + "");
            params.add(new NameValuePair("Npass", ""));
//            log.info("Npass = " + "");
            params.add(new NameValuePair("szxPerm", Const_RuYiFu.INTERFACE_PAY));
//            log.info("szxPerm = " + Const_RuYiFu.INTERFACE_PAY);
            params.add(new NameValuePair("szxCard", cardNo));
//            log.info("szxCard = " + cardNo);
            params.add(new NameValuePair("szxCardPass", cardPass));
//            log.info("szxCardPass = " + cardPass);
            params.add(new NameValuePair("szxAmount", String.valueOf(amount / 100)));
//            log.info("szxAmount = " + String.valueOf(amount / 100));
            params.add(new NameValuePair("szxCorp", String.valueOf(cardCorp)));
//            log.info("szxCorp = " + String.valueOf(cardCorp));
            params.add(new NameValuePair("szxArea", String.valueOf(cardArea)));
//            log.info("szxArea = " + String.valueOf(cardArea));
            NameValuePair[] arr = new NameValuePair[params.size()];
            params.toArray(arr);
            method.setQueryString(arr);
            int code = httpclient.executeMethod(method);
            if (code == 200) {
                String result = method.getResponseBodyAsString().trim();
                log.info("[ruyifu_order] " + result);
                if ("ERROR0000".equals(result)) {
                    out.println("0");
                    out.println(order.getOrderID());
                    if (callbackURL != null) {
                        callbacks.put(order.getOrderID(), callbackURL);
                    }
                } else {
                    out.println("5");
                    out.println(Const_RuYiFu.getErrorMessage(result));
                }
            } else {
                log.info("[ruyifu_order] code=" + code);
                out.println("4");
                out.println("访问如意付平台失败");
            }
        } catch (Exception ex1) {
            log.error(ex1, ex1);
        } finally {
            method.releaseConnection();
        }
    }
}
