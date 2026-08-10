package com.pip.server.billing.yeepay;

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
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.server.account.bean.Account;
import com.pip.server.billing.Server;
import com.pip.server.billing.chinarund.PayInfo;
import com.pip.server.billing.chinarund.PayInfoDAO;

/**
 * 易宝神州行支付下单。
 * 请求参数：
 *     id = 帐号ID
 *     name = 帐号名
 *     上面2个参数任意指定一个即可
 *     amount = 金额(分)
 *     gamecode = 游戏代码
 *     cardtype = 卡类型，字符串，取值包括：1 - 移动神州行，2 - 联通卡，3 - 电信卡，如果没有指定此参数，默认为神州行
 *     cardno = 卡号
 *     cardpass = 密码
 *     channel = 用户渠道号
 * 返回(UTF-8编码)：
 *     第一行是返回代码，0表示成功，非0表示失败
 *     如果失败，第二行是错误信息；如果成功，第二行是订单ID。
 */
public class YeepayOrderServlet extends HttpServlet {
	private static final Random RND = new Random();
	private static Logger log = Logger.getLogger(YeepayOrderServlet.class);
	
    private final SimpleDateFormat payIDFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
    private final SimpleDateFormat payTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    protected SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
    protected PayInfoDAO dao;
    private Server server;
    
	public YeepayOrderServlet(Server s, PayInfoDAO dao) throws Exception{
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
        String cardNo = request.getParameter("cardno").trim();
        String cardPass = request.getParameter("cardpass").trim();
        String channel = request.getParameter("channel");
        
        if (name == null) {
            log.info("[yeepay_order]accountid[" + id + "]amount[" + amount + "]gamecode[" + gameCode + 
                    "]cardno[" + cardNo + "]cardpass[" + cardPass + "]cardtype[" + cardType + "]");
        } else {
            log.info("[yeepay_order]accountname[" + name + "]amount[" + amount + "]gamecode[" + gameCode + 
                    "]cardno[" + cardNo + "]cardpass[" + cardPass + "]cardtype[" + cardType + "]");
        }

        // 设置返回格式
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        // 验证是否重复提交
        if (!ConstYeepay.checkSubmitPermission(id, cardNo, cardPass)) {
        	log.info("[yeepay_order] 重复提交");
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
            log.info("[yeepay_order] 帐号不存在");
            out.println("1");
            out.println("帐号不存在");
            return;
        }
        
        // 验证金额
        if (!ConstYeepay.IMONEY_MAP.containsKey(amount)) {
            log.info("[yeepay_order] 金额错误");
            out.println("2");
            out.println("金额错误");
            return;
        }
        
        // 验证卡号密码
        if (!ConstYeepay.checkInput(cardNo, cardPass)) {
        	log.info("[yeepay_order] 卡号密码格式错误");
        	out.println("2");
        	out.println("卡号密码格式错误");
            return;
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
            log.info("[yeepay_order]payid[" + orderId + "] created ok");
            insertok = true;
        } catch(Exception ex) {
            log.error(ex,ex);
            tx.rollback();
        }
        if (!insertok) {
            log.info("[yeepay_order] 创建订单失败");
            out.println("3");
            out.println("创建订单失败");
            return;
        }
        
        // 向易宝支付平台发起支付请求
        String notifyURL = server.getServerURL() + "/yeepay_notify";
        int ret = YeepayOrderManager.tryPlaceOrder(notifyURL, gameCode, channel, acc, orderId, amount, cardNo, cardPass, cardType);
        if (ret == 0) {
        	out.println("0");
            out.println(orderId);
        } else if (ret == 1) {
        	out.println("4");
            out.println("支付平台暂时故障，我们正在和支付平台提供商协商解决。您的订单已经被记录，待支付平台恢复服务后我们将自动替您重新提交。");
        } else {
        	out.println("5");
            out.println("支付错误，请检查卡号、密码和金额是否正确");
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
