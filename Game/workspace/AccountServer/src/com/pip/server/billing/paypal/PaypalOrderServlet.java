package com.pip.server.billing.paypal;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.account.bean.Account;
import com.pip.server.billing.Server;
/**
 * Paypal 支付基本流程:
 * step1: WEB充值网站 — 获取新订单 ->> 计费服
 * step2: WEB充值网站 — 充值请求 —>>www.paypal.com 
 * step3: www.paypal.com - 充值回调 —>>WEB充值网站
 * step4: www.paypal.com - 充值通知 —>>计费服
 * step5: WEB充值网站 - 订单查询 ->> 计费服
 *  
 * 相关接口:
 * WEB充值网站-> 计费服(paypal_order 获取新订单）
 * WEB充值网站-> 计费服(paypal_notify，充值结果通知)
 * WEB充值网站-> 计费服(paypal_query,订单查询)
 * WEB充值网站-> 计费服(paypal_charge_rate 获取充值兑换关系)
 * @author jyu
 *
 */
@SuppressWarnings("serial")
public class PaypalOrderServlet extends HttpServlet {

	private final Random rnd = new Random();
	private static Logger log = Logger.getLogger(PaypalOrderServlet.class);
	private Order_PaypalDAO dataDAO;
	private Server server;
	private final SimpleDateFormat payIDFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
	
	
	public PaypalOrderServlet(Server s,Order_PaypalDAO dao) {
		server = s;
		dataDAO = dao;
	}

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws  ServletException, IOException {
        
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
        //Integer amount = Integer.parseInt(request.getParameter("amount"));//货币金额，单位分
        Integer baseImoney = Integer.parseInt(request.getParameter("imoney"));//购买i币数量
        
        String currency_code = request.getParameter("currency_code");//货币代号
        
        int gameCode = Integer.parseInt(request.getParameter("gamecode"));
        String returnhttp = request.getParameter("returnhttp");
        if (name == null) {
            log.info("[paypal_order]accountid[" + id +"]baseImoney[" +baseImoney+ "]curencycode[" + currency_code + "]gamecode[" + gameCode + "]"+returnhttp);
        } else {
            log.info("[paypal_order]accountname[" + name +"]baseImoney[" +baseImoney+ "]curencycode[" + currency_code + "]gamecode[" + gameCode + "]"+returnhttp);
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
            log.info("[paypal_order] 帐号不存在");
            out.println("1");
            out.println("帐号不存在");
            return;
        }
        
        //确定充值额度
        int totaliMoney = baseImoney;
        int amount = 0; //实际充值币种金额
        int rmbamount = 0;//兑换人民币数量
        Integer[] values = ConstPaypal.IMONEY_MAP.get(baseImoney);
        if(values!=null){
        	totaliMoney += values[0];      
        	//VALUES[赠送额度（i币数),人民币,美元,加拿大元,欧元,港元]
        	if("USD".equals(currency_code)){
        		amount = values[2];
        	}else if("CAD".equals(currency_code)){
        		amount = values[3];
        	}else if("EUR".equals(currency_code)){
        		amount = values[4];
        	}else if("HKD".equals(currency_code)){
        		amount = values[5];
        	}
        	rmbamount = values[1];
        }
        
        // 验证金额
        if (amount==0) {
            log.info("[paypal_order] 金额错误");
            out.println("2");
            out.println("金额错误");
            return;
        }
        
        // 创建新的订单
        Order_Paypal order = new Order_Paypal();
        order.setAccountID(acc.getId());
        order.setUserName(acc.getName());
        order.setCreateTime(new java.util.Date());
        order.setMoney( amount);//保存充值额度, 单位元*100
        order.setCurrencyCode(currency_code);
        order.setRmbmoney(rmbamount);//保存人民币额度,单位元*100       
        order.setStatus(0);
        order.setGameCode(gameCode);
        
        String payseq = getNewOrderID();    	
        order.setPaySeq(payseq);
        
        order.setImoney(totaliMoney);
       
        dataDAO.create(order);
        if (order.getId() == 0) {
            log.info("[paypal_order] 创建订单失败");
            out.println("3");
            out.println("创建订单失败");
            return;
        }
        out.println("0");
        out.println(payseq);
    }
    private String getNewOrderID() {
	    Date now = new Date();
	    return "PAYPAL-PIP-" + payIDFormat.format(now) + "-" + (1000 + rnd.nextInt(9000));
	}
}
