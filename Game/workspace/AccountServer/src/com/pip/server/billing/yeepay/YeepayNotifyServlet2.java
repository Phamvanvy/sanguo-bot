package com.pip.server.billing.yeepay;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.server.account.bean.Fee;
import com.pip.server.billing.Server;
import com.pip.server.billing.chinarund.PayInfo;
import com.pip.server.billing.chinarund.PayInfoDAO;

/**
 * 易宝神州行支付结果通知(游戏内直冲)。
 * 请求参数：
 *     参见规范
 * 返回:
 *     成功返回一行success，否则返回fail
 * @author lighthu
 */
public class YeepayNotifyServlet2 extends HttpServlet implements Runnable {
	private static final Logger log = Logger.getLogger(YeepayNotifyServlet2.class);

	protected SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	protected Server server;
	protected PayInfoDAO dao;
	
	// 回调地址表
	private Map<String, String> callbacks;
	// 等待中的通知
	private ArrayList<PayInfo> pendingNotify = new ArrayList<PayInfo>();

	public YeepayNotifyServlet2(Server s, PayInfoDAO dao, Map<String, String> cbs) {
	    this.server = s;
		this.dao = dao;
		callbacks = cbs;
		new Thread(this).start();
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	    
	    // 读取参数
	    String r0_Cmd = request.getParameter("r0_Cmd");
        String r1_Code = request.getParameter("r1_Code");
        String p1_MerId = request.getParameter("p1_MerId");
        String rb_Order = request.getParameter("rb_Order");
        String r2_TrxId = request.getParameter("r2_TrxId");
        String pa_MP = request.getParameter("pa_MP");
        String rc_Amt = request.getParameter("rc_Amt");
        String rq_CardNo = request.getParameter("rq_CardNo");
        String rq_ReturnMsg = request.getParameter("rq_ReturnMsg");
        String hmac = request.getParameter("hmac");
        log.info("[yeepay_notify2]orderid[" + rb_Order + "]amount[" + rc_Amt + "]cardno[" + rq_CardNo + 
                "]result[" + r1_Code + "]msg[" + rq_ReturnMsg + "]");
        
        // 设置返回格式
        response.setCharacterEncoding("GBK");
        response.setContentType("text/html;charset=gbk");
        PrintWriter out = response.getWriter();
        
        // 验证签名
        String merchantKey = ConstYeepay.getMerchantKey(p1_MerId);
        String newHmac = DigestUtil.getHmac(new String[] { 
            r0_Cmd, r1_Code, p1_MerId, rb_Order, r2_TrxId, pa_MP, rc_Amt 
        }, merchantKey);
        if (!hmac.equals(newHmac)) {
            out.println("fail");
            return;
        }
        
        // 查找订单
        ConstYeepay.recordNotify();
        PayInfo pi = null;
		Transaction tx = sessionFactory.getCurrentSession().beginTransaction();
		try {
			pi = dao.getInfoByPayId(rb_Order);
			if (pi == null) {
			    // 订单不存在
			    tx.rollback();
			    log.info("[yeepay_notify2] order not found.");
			    out.println("fail");
			    return;
			} else if (!pi.isValid()) {
			    // 如果是报错通知，设置通知时间
                if (!"1".equals(r1_Code)) {
                	if (pi.getNotifyTime() == null) {
                		pi.setNotifyTime(new Date());
                		dao.update(pi);
                		tx.commit();
                	} else {
                		tx.rollback();
                	}
                    out.println("success");
                    scheduleNotify(pi);
                    
                    // 记一次支付失败，同一帐号一天只能支付失败20次
                    ConstYeepay.addFailRecord(pi.getAccountId());
                    return;
                } else {
    			    // 订单存在，且尚未成功，则标记为成功
    			    int realAmount = (int)Double.parseDouble(rc_Amt);
    			    pi.setMoney(String.valueOf(realAmount));

    			    // 修改订单状态保证不会重复交易
			        pi.setI_sum(ConstYeepay.calcIMoney(realAmount * 100));
			        pi.setValid(true);
			        pi.setNotifyTime(new Date());
			        dao.update(pi);
			        tx.commit();
			        out.println("success");
			        scheduleNotify(pi);
                }
			} else {
			    // 订单存在，且已经完成，说明这是一个重复请求，直接返回success
			    tx.rollback();
                out.println("success");
                return;
			}
		} catch (Exception ex) {
			tx.rollback();
			log.error(ex, ex);
			out.println("fail");
			return;
		}

        // 为用户添加i币并修改订单状态
		if (!pi.getUserName().equals("*qq")) {
	        try {
	            pi.setFeeid(addIMoney(pi));
	            log.info("[yeepay_notify2]orderid[" + rb_Order + "] add i money success.");
	            pi.setAddIFail(false);
	        } catch (Exception e) {
	            log.info("[yeepay_notify2]orderid[" + rb_Order + "] add i money fail.");
	        }
		}
        tx = sessionFactory.getCurrentSession().beginTransaction();
        try {
            dao.update(pi);
            tx.commit();
        } catch (Exception ex) {
            tx.rollback();
            log.error(ex, ex);
        }
	}
	
	// 把一个订单加入通知世界服务器的队列
	private void scheduleNotify(PayInfo pi) {
	    String callback = callbacks.get(pi.getPayId());
	    if (callback != null) {
	        synchronized (pendingNotify) {
	            pendingNotify.add(pi);
	            pendingNotify.notify();
	        }
	    }
	}

    /**
	 * 完成一个订单，为用户增加i币。
	 * @param order
	 * @throws Exception
	 */
    private int addIMoney(PayInfo order) throws Exception {
        // 在认证服务器创建订单
    	String channelPrefix = "YEEPAYD_";
    	if (ConstYeepay.CARD_TYPE_TELECOM.equals(order.getCardType())) {
    		channelPrefix = "YEEPAYD_TEL_";
    	} else if (ConstYeepay.CARD_TYPE_UNICOM.equals(order.getCardType())) {
    		channelPrefix = "YEEPAYD_UNI_";
    	}
        Fee fee = server.newFee(order.getUserName(), (int)(order.getI_sum() * 100 * 
        		ConstYeepay.randomGiftRatio(server, order)), channelPrefix + order.getMoney());
        
        // 完成订单，修改帐户余额
        if (!server.fulfillOrder(fee.getId())) {
            throw new Exception();
        }
        
        // 添加积分
        server.addCreditByMoney(order.getAccountId(), Integer.parseInt(order.getMoney()));
        
        return fee.getId();
    }

    /**
     * 回调游戏世界服务器，通知支付结果。
     * @param callbackurl 世界服务器URL
     * @param orderId 订单ID
     * @param success 是否成功
     * @param accountId 帐号ID
     * @param amount 元宝数
     */
    public void callback(String callbackurl, String orderId, boolean success, String accountId, int amount) {
    	String url = callbackurl;
		url += "?orderid=" + orderId + "&accountid=" + accountId + "&success=" + (success ? "true" : "false") + "&amount=" + amount + "&channel=YEEPAY";
		url = server.wrapCallbackURL(url);
		GetMethod method = new GetMethod(url);
		method.addRequestHeader( "Connection", "close");
        try {
            HttpClient httpclient = new HttpClient();
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
            httpclient.getParams().setSoTimeout(30000);
            int code = httpclient.executeMethod(method);
            if (code == 200) {
                log.info("[yeepay_notify2]orderid[" + orderId + "] callback ok");
            }
        } catch (Exception ex) {
            log.error(ex, ex);
        } finally {
            method.releaseConnection();
        }
    }
    
    /**
     * 回调游戏世界服务器，通知支付结果(简单参数模式)。
     * @param callbackurl 世界服务器URL
     * @param orderId 订单ID
     * @param success 是否成功
     * @param accountId 帐号ID
     */
    public void callback(String callbackurl, String orderId, boolean success) {
        GetMethod method = new GetMethod(callbackurl + (success ? "1" : "0"));
        method.addRequestHeader( "Connection", "close");
        try {
            HttpClient httpclient = new HttpClient();
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
            httpclient.getParams().setSoTimeout(30000);
            int code = httpclient.executeMethod(method);
            if (code == 200) {
                log.info("[yeepay_notify2]orderid[" + orderId + "] callback ok");
            }
        } catch (Exception ex) {
            log.error(ex, ex);
        } finally {
            method.releaseConnection();
        }
    }
    
    /**
     * 启动一个线程来处理向世界服务器通知支付结果的任务。
     */
    public void run() {
       while (true) {
           try {
               PayInfo pi = null;
               synchronized (pendingNotify) {
                   if (pendingNotify.size() == 0) {
                       pendingNotify.wait();
                   }
                   if (pendingNotify.size() > 0) {
                       pi = pendingNotify.remove(0);
                   }
               }
               if (pi != null) {
            	   String url = callbacks.get(pi.getPayId());
            	   if (url.startsWith("qq")) {
            		   callback(url.substring(2), pi.getPayId(), pi.isValid());
            	   } else {
            		   callback(callbacks.get(pi.getPayId()), pi.getPayId(), pi.isValid(), String.valueOf(pi.getAccountId()), pi.getI_sum());
            	   }
               }
           } catch (Throwable e) {
           }
       }
    }
}
