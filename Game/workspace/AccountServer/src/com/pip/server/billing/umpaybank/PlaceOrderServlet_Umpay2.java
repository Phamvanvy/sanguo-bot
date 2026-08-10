package com.pip.server.billing.umpaybank;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import com.pip.server.account.bean.Account;
import com.pip.server.account.bean.Fee;
import com.pip.server.billing.Server;
import com.umpay.SignEnc;
import com.umpay.SignEncException;

/**
 * 手机钱包银行卡支付：商户向手机钱包平台下订单。
 * 请求参数：
 *     id = 帐号ID
 *     name = 帐号名
 *     上面2个参数任意指定一个即可
 *     amount = 金额(分)
 *     phone = 手机号
 * 返回(UTF-8编码)：
 *     第一行是返回代码，0表示成功，1表示失败
 *     如果失败，第二行是错误信息
 * @author Light Hu
 */
public class PlaceOrderServlet_Umpay2 extends HttpServlet {
    private static Logger log = Logger.getLogger(PlaceOrderServlet_Umpay2.class);
    private Server server;
    
    public PlaceOrderServlet_Umpay2(Server s) {
        server = s;
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
        String phone = request.getParameter("phone");
        
        if (name == null) {
            log.info("[umpay2_order]accountid[" + id + "]amount[" + amount + "]phone[" + phone +"]");
        } else {
            log.info("[umpay2_order]accountname[" + name + "]amount[" + amount + "]phone[" + phone +"]");
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
            log.info("[umpay2_order] 帐号不存在");
            out.println("1");
            out.println("帐号不存在");
            return;
        }
        
        // 验证金额
        if (amount < 100 || amount > 10000000 || (amount % 100) != 0) {
            log.info("[umpay2_order] 金额错误");
            out.println("2");
            out.println("金额错误");
            return;
        }
        
        // 创建新的订单
        Fee fee = server.newFee(acc.getName(), amount * 360, "UMPAY2_" + amount);
        if (fee == null) {
            log.info("[umpay2_order] 创建订单失败");
            out.println("3");
            out.println("创建订单失败");
            return;
        }
        
        // 向手机钱包平台发起支付请求
        String orderID = String.valueOf(fee.getId());
        String amountStr = String.valueOf(amount);
        String dateTime = ConstUmpayBank.getDate();
        String randSign = ConstUmpayBank.getRandomSign();
        String params = "FUNCODE=" + ConstUmpayBank.FUNC_CODE + "&SPID=" + ConstUmpayBank.SPID +
            "&ORDERID=" + orderID + "&AMOUNT=" + amountStr + "&DATETIME=" + dateTime + "&MOBILEID=" +
            phone + "&RDPWD=" + randSign;
        String sign;
        try {
            // 生成签名
            sign = SignEnc.sign(params);
        } catch (SignEncException e) {
            log.error(e, e);
            log.info("[umpay2_order] 签名失败");
            out.println("4");
            out.println("访问手机钱包平台失败");
            return;
        }

        // 发起请求
        String url = ConstUmpayBank.ORDER_URL + "?" + params + "&SIGN=" + URLEncoder.encode(sign, "UTF-8") + 
            "&REMARK=" + URLEncoder.encode(ConstUmpayBank.getRemark(amount), "UTF-8");
        log.info("[umpay2_order] request " + url);
        GetMethod method = new GetMethod(url);
        method.addRequestHeader( "Connection", "close");
        try {
            HttpClient httpclient = new HttpClient();
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
            httpclient.getParams().setSoTimeout(30000);
            int code = httpclient.executeMethod(method);
            if (code == 200) {
                String result = method.getResponseBodyAsString();
                int pos = result.indexOf(" CONTENT=\"") + 10;
                int pos2 = result.indexOf('"', pos);
                log.info("[umpay2_order] " + result.substring(pos, pos2));
                String[] secs = result.substring(pos, pos2).split("\\|");
                if ("0000".equals(secs[0])) {
                    out.println("0");
                } else {
                    out.println("4");
                    out.println("订购失败：" + secs[secs.length - 1]);
                }
            } else {
                log.info("[umpay2_order] code=" + code);
                out.println("4");
                out.println("访问手机钱包平台失败");
            }
        } catch (Exception ex1) {
            log.error(ex1, ex1);
        } finally {
            method.releaseConnection();
        }
    }
}
