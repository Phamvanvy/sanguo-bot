package com.pip.server.billing.chinarund;

import java.io.IOException;
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
import com.pip.server.account.bean.Account;
import com.pip.server.account.bean.Fee;
import com.pip.server.billing.Server;
import com.pip.server.billing.yeepay.ConstYeepay;

public class ChianRunCallbackServlet extends HttpServlet {

	private static final Logger log = Logger
			.getLogger(ChianRunCallbackServlet.class);

	protected SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	protected PayInfoDAO dao;

	private Map<String, String> callbacks;
	private Server server;
	
	
	public ChianRunCallbackServlet(Server s, PayInfoDAO dao,
			Map<String, String> callbacks, String chargeurl) {
	    this.server = s;
		this.dao = dao;
		this.callbacks = callbacks;
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String version = request.getParameter("version");
		String merId = request.getParameter("merId");
		int money = Integer.parseInt(request.getParameter("payMoney"))/100;
		String orderId = request.getParameter("orderId");
		boolean success = request.getParameter("payResult").equals("0") ? false
				: true;
		String privateField = request.getParameter("privateField");
		String payDetail = request.getParameter("payDetails");
		String md5String = request.getParameter("md5String");
		log.info("version[" + version + "]merId[" + merId + "]orderId["
				+ orderId + "]money[" + money + "]success[" + success
				+ "]privateField[" + privateField + "]payDetail[" + payDetail
				+ "]");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().write(orderId);
		int amount = 0;
		if (success) {
			boolean valid = false;
			PayInfo pi = null;
			Transaction tx = sessionFactory.getCurrentSession()
					.beginTransaction();
			try{
				pi = dao.getInfoByPayId(orderId);
				if(pi != null&&!pi.isValid()){
					pi.setValid(true);
					pi.setI_sum(getCharge(money));
					amount = pi.getI_sum();
					pi.setMoney(String.valueOf(money));
					pi.setNotifyTime(new Date());
					valid = true;
					log.info("orderId["+orderId+"]valid");
				}
				tx.commit();
			} catch(Exception ex){
				tx.rollback();
				log.error(ex,ex);
			}
			if (valid) {
				tx = sessionFactory.getCurrentSession().beginTransaction();
				try {
				    pi.setFeeid(addIMoney(pi));
					log.info("orderId[" + orderId + "]ChargeOk");
					pi.setAddIFail(false);
					dao.update(pi);
					tx.commit();
				} catch (Exception ex) {
					tx.rollback();
					log.error(ex, ex);
				}
			}
		} else {
			// 充值错误，记录通知时间
			PayInfo pi = null;
			Transaction tx = sessionFactory.getCurrentSession().beginTransaction();
			try{
				pi = dao.getInfoByPayId(orderId);
				if (pi != null && !pi.isValid() && pi.getNotifyTime() == null) {
					pi.setNotifyTime(new Date());
					tx.commit();
				} else {
					tx.rollback();
				}
			} catch(Exception ex){
				tx.rollback();
			}
		}
		String callback = callbacks.get(orderId);
		if (callback != null) {
			callback(callback, orderId, success, privateField, amount);
		}
	}

	public void callback(String callbackurl, String orderId, boolean success,
			String accountId, int amount) {
		String url = callbackurl;
		url += "?orderid=" + orderId + "&accountid=" + accountId + "&success=" + (success ? "true" : "false") + "&amount=" + amount + "&channel=CHINARUN";
		url = server.wrapCallbackURL(url);
		GetMethod method = new GetMethod(url);
		method.addRequestHeader( "Connection", "close");
		try {
			HttpClient httpclient = new HttpClient();
			httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(
					10000);
			httpclient.getParams().setSoTimeout(30000);
			int code = httpclient.executeMethod(method);
			if (code == 200) {
				log.info("OrderId[" + orderId + "]CallbackOk");
			}
		} catch (Exception ex) {
			log.error(ex, ex);
		} finally {
			method.releaseConnection();
		}
	}

    /**
     * 完成一个订单，为用户增加i币。
     * @param order
     * @throws Exception
     */
    private int addIMoney(PayInfo order) throws Exception {
        // 在认证服务器创建订单
        Account acc = server.findAccount(order.getAccountId());
        Fee fee = server.newFee(acc.getName(), (int)(order.getI_sum() * 100 * 
                ConstYeepay.randomGiftRatio(server, order)), "CHINARUND_" + order.getMoney());
        
        // 完成订单，修改帐户余额
        if (!server.fulfillOrder(fee.getId())) {
            throw new Exception();
        }
        
        // 添加积分
        server.addCreditByMoney(order.getAccountId(), Integer.parseInt(order.getMoney()));
        
        return fee.getId();
    }

	public int getCharge(int money) {
	    try {
	        return ConstYeepay.calcIMoney(money * 100);
	    } catch (Exception e) {
	        return 0;
	    }
	}

}
