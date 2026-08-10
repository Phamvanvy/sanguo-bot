package com.pip.server.billing.yeepaybank;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.server.account.bean.Account;
import com.pip.server.billing.Server;
import com.pip.server.billing.chinarund.PayInfo;
import com.pip.server.billing.chinarund.PayInfoDAO;
import com.pip.server.billing.yeepay.ConstYeepay;
import com.pip.server.billing.yeepay.DigestUtil;

public class YeepaybankOrder4WebServlet  extends HttpServlet {
	private static final Random RND = new Random();
	private static Logger log = Logger.getLogger(YeepaybankOrder4WebServlet.class);
	
	public static String ORDER_URL = "https://fortune.yeepay.com/app-merchant-proxy/node"; //wap支付通道的接入无测试环境
	
//	测试用参数
//	public static String ORDER_URL = "http://tech.yeepay.com:8080/robot/debug.action";
//	public static String TEST_MerchantID = "10000432521";
//	public static String TEST_MerchantKEY = "8UPp0KE8sq73zVP370vko7C39403rtK1YwX40Td6irH216036H27Eb12792t";
	
    private final SimpleDateFormat payIDFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
    private final SimpleDateFormat payTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    protected SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
    protected PayInfoDAO dao;
    private Server server;
    
    static HashMap<String,String> bankTypeTagMap = new HashMap<String,String>();//银行类型代码映射
    
	public YeepaybankOrder4WebServlet(Server s, PayInfoDAO dao) throws Exception{
	    server = s;
		this.dao = dao;
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
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
        String banktype = request.getParameter("bank"); //icbc:(工行 - ICBC-WAP）cmb:(招商银行 CMBCHINA-WAP)
        
        String channel = request.getParameter("channel");
         if (name == null) {
            log.info("[yeepaybank4web_order]accountid[" + id + "]amount[" + amount + "]gamecode[" + gameCode + 
                    "]banktype[" + banktype + "]");
        } else {
            log.info("[yeepaybank4web_order]accountname[" + name + "]amount[" + amount + "]gamecode[" + gameCode + 
            		"]banktype[" + banktype + "]");
        }
        String returnhttp = request.getParameter("returnhttp");

        // 设置返回格式
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
               
        if(banktype ==null ){
        	log.info("[yeepaybank4web_order] 缺少交易银行参数");
            out.println("6");
            out.println("缺少交易银行参数");
            return;	
        }
        
        //web充值支持卡的类型较多
        banktype = GetBankTypeTag(banktype);
        
        if(banktype==null || banktype.equals("")){
        	log.info("[yeepaybank4web_order] 不支持的银行类型");
            out.println("7");
            out.println("不支持的银行类型");
            return;	
        }	
       
        // 验证用户名和密码
        Account acc;
        if (name != null) {
            acc = server.findAccountByName(name);
        } else {
            acc = server.findAccount(id);
        }
        if (acc == null) {
            log.info("[yeepaybank4web_order] 帐号不存在");
            out.println("1");
            out.println("帐号不存在");
            return;
        }
               
        // 创建新的订单
        String orderId = getNewOrderID(banktype);
        boolean insertok = false;
        Transaction tx = sessionFactory.getCurrentSession().beginTransaction();
        try{
            PayInfo pi = new PayInfo();
            pi.setAccountId(acc.getId());
            pi.setAddIFail(true);
            pi.setGame(gameCode); //1 幻想 2武林
            pi.setI_sum(0);
            pi.setMoney(String.valueOf(amount / 100));
            pi.setPayId(orderId);
            pi.setPayTime(payTimeFormat.format(new Date()));
            pi.setUserName(acc.getName());
            pi.setValid(false);
            pi.setChannel(channel);
            pi.setCardno("");
            pi.setCardpass("");
            dao.create(pi);
            tx.commit();
            log.info("[yeepaybank4web_order]payid[" + orderId + "] created ok");
            insertok = true;
        } catch(Exception ex) {
            log.error(ex,ex);
            tx.rollback();
        }
        if (!insertok) {
            log.info("[yeepaybank4web_order] 创建订单失败");
            out.println("3");
            out.println("创建订单失败");
            return;
        }
        
        // 向易宝支付平台发起支付请求
        String notifyURL = server.getServerURL() + "/yeepaybank4web_notify";
        PostMethod method = new PostMethod(ORDER_URL);
        method.addRequestHeader( "Connection", "close");
        method.getParams().setContentCharset("GBK");
        try {
            String merchantID = ConstYeepay.getMerchantID(gameCode, channel, acc);//TEST_MerchantID
            String merchantKey = ConstYeepay.getMerchantKey(merchantID);//TEST_MerchantKEY
            HttpClient httpclient = new HttpClient();
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
            httpclient.getParams().setSoTimeout(30000);
            method.addParameter("p0_Cmd", "Buy");
            method.addParameter("p1_MerId", merchantID);
            method.addParameter("p2_Order", orderId);
            method.addParameter("p3_Amt", (amount / 100) + ".00");
            method.addParameter("p4_Cur", "CNY");
            method.addParameter("p5_Pid", "i");
            method.addParameter("p6_Pcat", "test");
            method.addParameter("p7_Pdesc", "test");
            method.addParameter("p8_Url", notifyURL);
            method.addParameter("p9_SAF", "0");
            method.addParameter("pa_MP", "accountid:" + acc.getId());
            
            method.addParameter("pd_FrpId", banktype);
            
            method.addParameter("pr_NeedResponse", "0");
            
            method.addParameter("noLoadingPage", "1");
            
            String hmac=DigestUtil.getHmac(new String[] {
                    "Buy", 
                    merchantID, 
                    orderId, 
                    (amount / 100) + ".00",
                    "CNY",
                    "i",
                    "test",
                    "test",
                    notifyURL,
                    "0",
                    "accountid:" + acc.getId(),
                    banktype,
                    "0"
                }, merchantKey);
            method.addParameter("hmac",hmac);
            int code = httpclient.executeMethod(method);
            if (code == 200) {
            	//返回格式是页面
                String result = method.getResponseBodyAsString();
                out.println(0);
                out.println(result);
                log.info("[yeepaybank4web_order] " + "0");
                YeepaybankNotify4WebServlet.orderMap.put(orderId, returnhttp==null?"":returnhttp);
            } else {
                log.info("[yeepaybank4web_order] code=" + code);
                out.println("4");
                out.println("访问支付平台失败");
            }
        } catch (Exception ex1) {
            log.error(ex1, ex1);
        } finally {
            method.releaseConnection();
        }
    }

	/**
	 * 生成新的不重复的订单ID。
	 * @return
	 */
	private String getNewOrderID(String banktype) {
	    Date now = new Date();
	    return "YP-PIP-" + payIDFormat.format(now) + "-" + (1000 + RND.nextInt(9000));
	}
	
	static String GetBankTypeTag(String banktype){
		if(bankTypeTagMap.size()<=0){
			bankTypeTagMap.put("icbc", "ICBC-NET");
			bankTypeTagMap.put("cmbchina", "CMBCHINA-NET");
			bankTypeTagMap.put("abc", "ABC-NET");
			bankTypeTagMap.put("ccb", "CCB-NET");
			bankTypeTagMap.put("bccb", "BCCB-NET");
			bankTypeTagMap.put("boco", "BOCO-NET");
			bankTypeTagMap.put("cib", "CIB-NET");
			bankTypeTagMap.put("njcb", "NJCB-NET");
			bankTypeTagMap.put("cmbc", "CMBC-NET");
			bankTypeTagMap.put("ceb", "CEB-NET");
			bankTypeTagMap.put("boc", "BOC-NET");
			bankTypeTagMap.put("pinganbank", "PINGANBANK-NET");
			bankTypeTagMap.put("cbhb", "CBHB-NET");
			bankTypeTagMap.put("hkbea", "HKBEA-NET");
			bankTypeTagMap.put("ecitic", "ECITIC-NET");
			bankTypeTagMap.put("sdb", "SDB-NET");
			bankTypeTagMap.put("spdb", "SPDB-NET");
			bankTypeTagMap.put("post", "POST-NET");
		}
		return bankTypeTagMap.get(banktype);
	}
}
