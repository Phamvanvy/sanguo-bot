package com.pip.server.billing.tiantian;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.server.account.bean.Fee;
import com.pip.server.billing.Server;
import com.pip.server.billing.chinarund.PayInfo;
import com.pip.server.billing.chinarund.PayInfoDAO;
import com.pip.server.billing.yeepay.ConstYeepay;



/**
 * 天天支付支付通知接口
 * @author jyu
 *
 */
@SuppressWarnings("serial")
public class TTFNotifyServlet extends HttpServlet {
	
	private static Logger log = Logger.getLogger(TTFNotifyServlet.class);
	
	protected SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	
	protected Server server;
	protected PayInfoDAO dao;
	
	public TTFNotifyServlet(Server s, PayInfoDAO dao){
		server = s;
		this.dao = dao;
	}
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws  ServletException, IOException {
		
		String merId = request.getParameter("merId");
		if(!TTFConstant.merId.equals(merId)){
			return ;
		}
		String orderId = request.getParameter("orderId");
		String status = request.getParameter("status");
		
		String payMoney = request.getParameter("payMoney");
		String operDate = request.getParameter("operDate");
		String userName = request.getParameter("userName");	
		String sign = request.getParameter("sign");
		//merId|orderId|status|payMoney|operDate|userName|key
		String verisign = MD5Tools.getMD5Str(TTFConstant.merId+"|"+orderId+"|"+status+"|"+payMoney+"|"+operDate+"|"+userName+"|"+TTFConstant.key);
		
		log.info("[ttf_notify]"+merId+"|"+orderId+"|"+status+"|"+payMoney+"|"+operDate+"|"+userName+"|"+TTFConstant.key+"|sign:"+sign);
		
		boolean  validate = false;
		if(verisign.toUpperCase().equals(sign)){
			log.info("[ttf_notify]"+ "verified ok");
			validate = true;
		}else{
			log.info("[ttf_notify]"+ "verified err");
		}
		response.getOutputStream().print("OK");
		
		if(!validate){
			// 记录订单通知时间
            Transaction tx = sessionFactory.getCurrentSession().beginTransaction();
            try {
            	PayInfo pi = dao.getInfoByPayId(orderId);
            	if (pi != null && pi.getNotifyTime() == null) {
	            	pi.setNotifyTime(new Date());
	            	dao.update(pi);
	            	tx.commit();
            	} else {
            		tx.rollback();
            	}
            } catch (Exception ex) {
            	tx.rollback();
            }
            // 记一次支付失败，同一帐号一天只能支付失败20次
            addFailRecord(orderId);
            return;
		}
		// 查找订单
		PayInfo order = null;
				
		// 查找订单
        PayInfo pi = null;
		Transaction tx = sessionFactory.getCurrentSession().beginTransaction();
		try {
			pi = dao.getInfoByPayId(orderId);
			if (pi == null) {
			    // 订单不存在
			    tx.rollback();
			    log.info("[ttf_notify] order not found.");
			    return;
			} else if (!pi.isValid()) {
			    // 订单存在，且尚未成功，则标记为成功
			    int realAmount = (int)Double.parseDouble(payMoney);
			    pi.setMoney(String.valueOf(realAmount));

			    // 修改订单状态保证不会重复交易
		        pi.setI_sum(ConstYeepay.calcIMoney(realAmount * 100));
		        pi.setValid(true);
		        pi.setNotifyTime(new Date());
		        dao.update(pi);
		        tx.commit();
			} else {
			    // 订单存在，且已经完成，说明这是一个重复请求，直接返回success
			    tx.rollback();
                return;
			}
		} catch (Exception ex) {
			tx.rollback();
			log.error(ex, ex);
			return;
		}

        // 为用户添加i币并修改订单状态
        try {
            pi.setFeeid(addIMoney(pi));
            log.info("[ttf_notify]orderid[" + orderId + "] add i money success.");
            pi.setAddIFail(false);
        } catch (Exception e) {
            log.info("[ttf_notify]orderid[" + orderId + "] add i money fail.");
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
	
	/**
	 * 完成一个订单，为用户增加i币。
	 * 
	 * @param order
	 * @throws Exception
	 */
    private int addIMoney(PayInfo order) throws Exception {
        // 在认证服务器创建订单
        Fee fee = server.newFee(order.getUserName(), (order.getI_sum() * 100 ), "TTF_" + order.getMoney());
        
        // 完成订单，修改帐户余额
        if (!server.fulfillOrder(fee.getId())) {
            throw new Exception();
        }
        // 添加积分
        server.addCreditByMoney(order.getAccountId(),Integer.parseInt(order.getMoney()));
        
        return fee.getId();
    }
    
    /*
     * 记录一次计费错误
     */
    private void addFailRecord(String orderId) {
        PayInfo pi = null;
        Transaction tx = sessionFactory.getCurrentSession().beginTransaction();
        try {
            pi = dao.getInfoByPayId(orderId);
            tx.rollback();
            if (pi != null) {
                ConstYeepay.addFailRecord(pi.getAccountId());
            }
        } catch (Exception ex) {
            tx.rollback();
        }
    }
}
