package com.pip.server.billing.alipay;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.account.bean.Account;
import com.pip.server.billing.Server;

public class GetOrderAlipayServlet extends HttpServlet {
	private final Random rnd = new Random();
	private static Logger log = Logger.getLogger(GetOrderAlipayServlet.class);
	private Order_AlipayDAO dataDAO;
	private Server server;
	
	public GetOrderAlipayServlet(Server s,Order_AlipayDAO dao) {
		server = s;
		dataDAO = dao;
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
//        String callbackurl = request.getParameter("url");
        if (name == null) {
            log.info("[alipay_order]accountid[" + id + "]amount[" + amount + "]gamecode[" + gameCode + "]");
        } else {
            log.info("[alipay_order]accountname[" + name + "]amount[" + amount + "]gamecode[" + gameCode + "]");
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
            log.info("[alipay_order] 帐号不存在");
            out.println("1");
            out.println("帐号不存在");
            return;
        }
        
        // 验证金额
        if (amount<1&&amount>2000) {
            log.info("[alipay_order] 金额错误");
            out.println("2");
            out.println("金额错误");
            return;
        }
        
        // 创建新的订单
        Order_Alipay order = new Order_Alipay();
        order.setAccountID(acc.getId());
        order.setUserName(acc.getName());
        order.setCreateTime(new java.util.Date());
        order.setMoney(amount);
        order.setStatus(0);
        order.setGameCode(gameCode);
        
        String payseq = System.currentTimeMillis()+ Tools.getRandom(rnd);    	
        order.setPaySeq(payseq);
        
        order.setImoney(Tools.calcIMoney(amount*100));
       
        dataDAO.create(order);
        if (order.getId() == 0) {
            log.info("[alipay_order] 创建订单失败");
            out.println("3");
            out.println("创建订单失败");
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
		params.put("subject", order.getImoney()+"i币");//商品名称
		params.put("totalFee", String.valueOf( amount));//商品总价
		params.put("buyerAccountName", "");//买家帐号
		
		//构造完整的URL
		String notifyurl =  server.getServerURL() + "/alipay_notify?oid="+order.getId()+"&amp;sid="+payseq;
		String callbackurl =  server.getServerURL() + "/alipay_callback?oid="+order.getId()+"&amp;sid="+payseq;
		params.put("notifyUrl",notifyurl);//接收支付宝发送的通知的url
		params.put("call_back_url",callbackurl);//回调URL
       
        params.put("outTradeNo", payseq);//外部交易号
		params.putAll(Tools.params);
		params.put("AccountID",new String().valueOf(acc.getId()));
		params.put("zero_pay","false");
		
		Map<String, String> reqParams = Tools.prepareTradeRequestParamsMap(params);
		
		String businessResult = DoAlipay.TradeCreate(reqParams);
		
		String redirectURL = DoAlipay.AuthAndExecute(businessResult, params); 
        if (Tools.isNotBlank(redirectURL)) {
        	out.println("0");
            out.println(redirectURL);
            log.info("[alipay_order]accountid[" + acc.getId() + "]amount[" + amount + "]gamecode[" + gameCode + "]result[ok]");
        }else{
        	log.info("[alipay_order] 交易暂时无法完成");
        	out.println("3");
            out.println("交易暂时无法完成，请稍候再试");
        }        
    }

}
