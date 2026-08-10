package com.pip.server.billing.paypal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.account.bean.Account;
import com.pip.server.billing.Server;
/**
 * Paypal WAP支付基本流程:
 * step1: WAP充值网站 — 充值,新订单,（用户名，充值额度，充值方式，返回地址url） ->> 计费服
 * step2: 计费服 — 取tocken —>>www.paypal.com 
 * step3: 计费服 - 重定向 ->> www.parpal.com
 * step4a: www.parpal.com - 用户取消充值 ->> 计费服->>返回地址url?retcode=-1
 * step4b: www.paypal.com - 用户点击充值 —>>计费服. -取确认信息，确认支付->> 返回地址url?retcode=0&retmsg=充值成功|或其他
 *  
 * 相关接口:
 * WAP充值网站-> 计费服(paypalw_order 充值订单）
 * www.parpal.com-> 计费服(paypalw_cancel，用户取消支付)
 * www.parpal.com-> 计费服(paypalw_callback，用户确认支付)
 * WAP充值网站-> 计费服(paypal_charge_rate 获取充值兑换关系)
 * @author jyu
 */
@SuppressWarnings("serial")
public class PaypalWapOrderServlet extends HttpServlet {

	private final Random rnd = new Random();
	private static Logger log = Logger.getLogger(PaypalWapOrderServlet.class);
	private Order_PaypalDAO dataDAO;
	private Server server;
	private final SimpleDateFormat payIDFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
	
	
	public PaypalWapOrderServlet(Server s,Order_PaypalDAO dao) {
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
        
        Integer baseImoney = Integer.parseInt(request.getParameter("imoney"));//购买i币数量
        String currency_code = request.getParameter("currency_code");//货币代号
        int gameCode = Integer.parseInt(request.getParameter("gamecode"));
        
        String returnhttp = request.getParameter("returnhttp");
        
        if(returnhttp==null || returnhttp.trim().equals("")){
        	returnhttp = ConstPaypal.callbackURL;
        }        
        if (name == null) {
            log.info("[paypal_wap_order]accountid[" + id +"]baseImoney[" +baseImoney+ "]curencycode[" + currency_code + "]gamecode[" + gameCode + "]"+returnhttp);
        } else {
            log.info("[paypal_wap_order]accountname[" + name +"]baseImoney[" +baseImoney+ "]curencycode[" + currency_code + "]gamecode[" + gameCode + "]"+returnhttp);
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
            log.info("[paypal_wap_order] 帐号不存在");
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
            log.info("[paypal_wap_order] 金额错误");
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
            log.info("[paypal_wap_order] 创建订单失败");
            out.println("3");
            out.println("创建订单失败");
            return;
        }
        
        String callbackUrl =server.getServerURL() + "/paypalw_callback";
        String cancelUrl = server.getServerURL() + "/paypalw_cancel";
        
        String token = "";
        try {
            token = requestToken(ConstPaypal.fmtCValue(amount), currency_code,callbackUrl,cancelUrl,payseq);
        } catch (Exception ae) {
        	 out.println("4");
             out.println("paypal订单创建失败:"+ae.getMessage());
             return;
        }
        order.setPaypalID(token);
        dataDAO.update(order);
        
        String redirectURL = ConstPaypal.WAP_URL_REDIECT_REQUEST +"?cmd=_express-checkout-mobile&useraction=commit&token="+token;

        PaypalWapCallbackServlet.returnURLMap.put(payseq, returnhttp);
        
        out.println("0");
        out.println(payseq);
        out.println(redirectURL);
        log.info("[paypal_wap_order] 创建订单成功:["+payseq+"]["+token+"]");
    }
    private String getNewOrderID() {
	    Date now = new Date();
	    return "PAYPALWAP-PIP-" + payIDFormat.format(now) + "-" + (1000 + rnd.nextInt(9000));
	}
    
    
    private static String requestToken(String money, String currencycode,String callbackUrl,String cancelUrl,String orderId) throws Exception {
    	        
        String url = ConstPaypal.WAP_URL_TOCKEN_REQUEST ;
        StringBuffer query = new StringBuffer("METHOD=SetExpressCheckout");
        query.append("&USER=").append(ConstPaypal.WAP_USER);
        query.append("&PWD=").append(ConstPaypal.WAP_PWD);
        query.append("&SIGNATURE=").append(ConstPaypal.WAP_SIGN);
        query.append("&VERSION=").append(ConstPaypal.WAP_VERSION);

        query.append("&RETURNURL=").append(callbackUrl);
        query.append("&CANCELURL=").append(cancelUrl);
        query.append("&AMT=").append(money);
        query.append("&CURRENCYCODE=").append(currencycode);
        query.append("&INVNUM=").append(orderId);
        HttpURLConnection conn = null;
        InputStream is = null;
        OutputStream out = null;

        try {
            URL urlObj = new URL(url);
            conn = (HttpURLConnection)urlObj.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            out = conn.getOutputStream();
            out.write(query.toString().getBytes("utf-8"));
            out.flush();
            out.close();

            int retCode = conn.getResponseCode();
            if(retCode == 200){
                is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line = br.readLine();
                //解析名值对
                HashMap<String,String> params = ConstPaypal.parseParams(line);
                String act = params.get("ACK");
                if("success".equals(act.toLowerCase())|| "successwithwarning".equals(act.toLowerCase())){//执行成功
                    String tocken = params.get("TOKEN");
                    return tocken;
                }else{
                    String errcode = params.get("L_ERRORCODE0");
//                    String msg = PaypalConstant.ERR_MSG.get(errcode);
                    throw new Exception("交易请求失败:"+errcode);
                }
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
            }

            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
            }
        }
        return "";
    }
}
