package com.pip.server.billing.yeepay;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.server.account.bean.Account;
import com.pip.server.billing.Server;
import com.pip.server.billing.chinarund.PayInfo;
import com.pip.server.billing.chinarund.PayInfoDAO;

/**
 * 易宝神州行支付下单（游戏内直冲）。
 * 请求参数：
 *     id = 帐号ID
 *     money = 金额(元)
 *     game = 游戏代码
 *     cardtype = 卡类型，字符串，取值包括：1 - 移动神州行，2 - 联通卡，3 - 电信卡，如果没有指定此参数，默认为神州行
 *     cardsn = 卡号
 *     password = 密码
 *     returnhttp = 回调URL
 *     channel = 用户渠道号
 * 返回：
 *     HEADER[result] 下单结果，200表示成功，1001表示失败
 *     HEADER[orderId] 订单ID
 */
public class YeepayOrderServlet2 extends HttpServlet {
	private static final Random RND = new Random();
	private static Logger log = Logger.getLogger(YeepayOrderServlet2.class);
	
    private final SimpleDateFormat payIDFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
    private final SimpleDateFormat payTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    protected SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
    protected PayInfoDAO dao;
    private Server server;
    public static YeepayOrderServlet2 instance;
    
    // 回调地址表
    private Map<String, String> callbacks;
    
	public YeepayOrderServlet2(Server s, PayInfoDAO dao, Map<String, String> cbs) {
	    server = s;
		this.dao = dao;
		callbacks = cbs;
		instance = this;
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
        int id = Integer.parseInt(request.getParameter("id"));
        int amount = Integer.parseInt(request.getParameter("money")) * 100;
        int gameCode = Integer.parseInt(request.getParameter("game"));
        String cardType = ConstYeepay.CARD_TYPE_SZX;
        try {
        	int cardTypeID = Integer.parseInt(request.getParameter("cardtype"));
        	if (cardTypeID == 2) {
        		cardType = ConstYeepay.CARD_TYPE_UNICOM;
        	} else if (cardTypeID == 3) {
        		cardType = ConstYeepay.CARD_TYPE_TELECOM;
        	}
        } catch (Exception e) {
        }
        String cardNo = request.getParameter("cardsn");
        String cardPass = request.getParameter("password").trim();
        String callback = request.getParameter("returnhttp").trim();
        String channel = request.getParameter("channel");
        
        if (true) {
	        // 下单
	        String[] info = placeOrder(id, amount, gameCode, cardType, cardNo, cardPass, callback, channel);
	        response.setHeader("result", info[0]);
	        response.setHeader("orderId", info[1]);
        } else {
	        // 临时用19pay替代yeepay的代码
	        String[] info = server.servlet19pay.placeOrder(id, null, amount, gameCode, cardNo, cardPass, 0, channel, callback);
	        if ("0".equals(info[0])) {
	        	info[0] = "200";
	        } else {
	        	info[1] = "1001";
	        }
	        response.setHeader("result", info[0]);
	        response.setHeader("orderId", info[1]);
        }
    }
	
	/**
	 * 向易宝平台下订单。
	 * @param accId 帐号ID
	 * @param amount 金额（分）
	 * @param gameCode 游戏代码
	 * @param cardno 卡号
	 * @param cardpass 密码
	 * @param cburl 回调地址
	 * @return 返回下单结果代码和订单ID。
	 */
	public String[] placeOrder(int id, int amount, int gameCode, String cardType, String cardNo, String cardPass, String callback, String channel) {
		log.info("[yeepay_order2]accountid[" + id + "]amount[" + amount + "]gamecode[" + gameCode + 
                "]cardno[" + cardNo + "]cardpass[" + cardPass + "]cardtype[" + cardType + "]callback[" + callback + "]");

		// 验证是否重复提交
        if (!ConstYeepay.checkSubmitPermission(id, cardNo, cardPass)) {
        	log.info("[yeepay_order2] 重复提交");
        	return new String[] { "1001", "" };
        }
        
        // 验证用户名和密码
        Account acc;
        if (callback.startsWith("qq")) {
        	acc = new Account();
        	acc.setId(id);
        	acc.setName("*qq");
        } else {
	        acc = server.findAccount(id);
	        if (acc == null) {
	            log.info("[yeepay_order2] 帐号不存在");
	            return new String[] { "1001", "" };
	        }
        }
        
        // 验证金额
        if (!ConstYeepay.IMONEY_MAP.containsKey(amount)) {
            log.info("[yeepay_order2] 金额错误");
            return new String[] { "1001", "" };
        }

        // 验证卡号密码
        if (!ConstYeepay.checkInput(cardNo, cardPass)) {
        	log.info("[yeepay_order2] 卡号密码格式错误");
        	return new String[] { "1001", "" };
        }
        
        // 创建新的订单
        String orderId = getNewOrderID();
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
            pi.setCardno(cardNo);
            pi.setCardpass(cardPass);
            pi.setCardType(cardType);
            dao.create(pi);
            tx.commit();
            log.info("[yeepay_order2]payid[" + orderId + "] created ok");
            insertok = true;
        } catch(Exception ex) {
            log.error(ex,ex);
            tx.rollback();
        }
        if (!insertok) {
            log.info("[yeepay_order2] 创建订单失败");
            return new String[] { "1001", "" };
        }
        
        // 向易宝支付平台发起支付请求
        callbacks.put(orderId, callback);
        String notifyURL = server.getServerURL() + "/yeepay_notify2";
        int ret = YeepayOrderManager.tryPlaceOrder(notifyURL, gameCode, channel, acc, orderId, amount, cardNo, cardPass, cardType);
        if (ret == 0) {
        	return new String[] { "200", orderId };
        } else if (ret == 1) {
        	return new String[] { "200", orderId };
        } else {
        	callbacks.remove(orderId);
        	return new String[] { "1001", orderId };
        }
	}

	/**
	 * 生成新的不重复的订单ID。
	 * @return
	 */
	private String getNewOrderID() {
	    Date now = new Date();
	    return "YP-PIP-" + payIDFormat.format(now) + "-" + (1000 + RND.nextInt(9000));
	}
}
