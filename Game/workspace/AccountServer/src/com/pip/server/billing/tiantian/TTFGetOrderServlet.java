package com.pip.server.billing.tiantian;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
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



/**
 * 天天支付接口
 * @author jyu
 *
 */
@SuppressWarnings("serial")
public class TTFGetOrderServlet extends HttpServlet {
	private static final Random RND = new Random();
	private final SimpleDateFormat payIDFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
	private static Logger log = Logger.getLogger(TTFGetOrderServlet.class);
	private final SimpleDateFormat payTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	protected SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	protected PayInfoDAO dao;
    private Server server;
    
	public TTFGetOrderServlet(Server s, PayInfoDAO dao){
		server = s;
		this.dao = dao;
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
        int amount = Integer.parseInt(request.getParameter("amount"));
        int gameCode = Integer.parseInt(request.getParameter("gamecode"));
        
        String cardNo = request.getParameter("cardno").trim();
        String cardPass = request.getParameter("cardpass").trim();
        String channel = request.getParameter("channel");
        
        if (name == null) {
            log.info("[ttf_order]accountid[" + id + "]amount[" + amount + "]gamecode[" + gameCode + 
                    "]cardno[" + cardNo + "]cardpass[" + cardPass + "]");
        } else {
            log.info("[ttf_order]accountname[" + name + "]amount[" + amount + "]gamecode[" + gameCode + 
                    "]cardno[" + cardNo + "]cardpass[" + cardPass + "]");
        }

        // 设置返回格式
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        // 验证是否重复提交 /**@ todo 这里可借用易宝重复提交检查？**/
        if (!ConstYeepay.checkSubmitPermission(id, cardNo, cardPass)) {
        	log.info("[ttf_order] 重复提交");
            out.println("3");
            out.println("重复提交次数过多");
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
            log.info("[ttf_order] 帐号不存在");
            out.println("1");
            out.println("帐号不存在");
            return;
        }
        
        // 验证金额
        if (!ConstYeepay.IMONEY_MAP.containsKey(amount)) {
            log.info("[ttf_order] 金额错误");
            out.println("2");
            out.println("金额错误");
            return;
        }
        
        // 验证卡号密码
        if (!ConstYeepay.checkInput(cardNo, cardPass)) {
        	log.info("[ttf_order] 卡号密码格式错误");
        	out.println("2");
        	out.println("卡号密码格式错误");
            return;
        }
        
        // 创建新的订单
        String orderId = getNewOrderID();
        boolean insertok = false;
        Transaction tx = sessionFactory.getCurrentSession().beginTransaction();
        try{
        	// 创建新的订单
            PayInfo pi = new PayInfo();
            pi.setAccountId(acc.getId());
            pi.setAddIFail(true);
            pi.setGame(gameCode); 
            pi.setI_sum(0);
            pi.setMoney(String.valueOf(amount / 100));
            pi.setPayId(orderId);
            pi.setPayTime(payTimeFormat.format(new Date()));
            pi.setUserName(acc.getName());
            pi.setValid(false);
            pi.setChannel(channel);
            pi.setCardno(cardNo);
            pi.setCardpass(cardPass);
            pi.setCardType(ConstYeepay.CARD_TYPE_SZX);
            dao.create(pi);
            tx.commit();
            log.info("[ttf_order]payid[" + orderId + "] created ok");
            insertok = true;
        } catch(Exception ex) {
            log.error(ex,ex);
            tx.rollback();
        }
        if (!insertok) {
            log.info("[ttf_order] 创建订单失败");
            out.println("3");
            out.println("创建订单失败");
            return;
        }
        //MD5（merId|orderId|payMoney|sendUrl|userName|key）
		String sign = MD5Tools.getMD5Str(TTFConstant.merId+"|"+orderId+"|"+amount+"|"+""+"|"+acc.getName()+"|"+TTFConstant.key);
		
		log.info("[ttf_order]"+TTFConstant.merId+"|"+orderId+"|"+amount+"|"+""+"|"+acc.getName()+"|"+TTFConstant.key+"|sign:"+sign);
		
		PostMethod method = new PostMethod(TTFConstant.URL);
        method.addRequestHeader( "Connection", "close");
        method.getParams().setContentCharset("UTF-8");
        try {
            
            HttpClient httpclient = new HttpClient();
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
            httpclient.getParams().setSoTimeout(30000);
            method.addParameter("merId", TTFConstant.merId);
            method.addParameter("orderId", orderId );
            method.addParameter("payMoney", String.valueOf(amount));//单位分
            method.addParameter("parValue", String.valueOf(amount));//单位分
            method.addParameter("sn", cardNo);
            method.addParameter("password", cardPass);
            method.addParameter("sendUrl", "");
            method.addParameter("userName",acc.getName());
            method.addParameter("sign", sign.toUpperCase());
            method.addParameter("type", "net");
           
            int code = httpclient.executeMethod(method);
            if (code == 200) {
            	//返回格式是页面
                String result = method.getResponseBodyAsString();
                if("001".equals(result)){//数据发送成功
                	out.println(0);
                	out.println(orderId);
                }else{
                	out.println(2);
                	String msg = TTFConstant.CODE_MSG.get(result);
                    out.println(msg);
                }
            } else {
                log.info("[ttf_order] code=" + code);
                out.println("4");
                out.println("访问支付平台失败");
            }
        } catch (Exception ex1) {
            log.error(ex1, ex1);
        } finally {
            method.releaseConnection();
        }
	}
	
	private String getNewOrderID() {
	    Date now = new Date();
	    return "TTF-PIP-" + payIDFormat.format(now) + "-" + (1000 + RND.nextInt(9000));
	}
}
