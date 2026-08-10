package com.pip.server.billing.yeepay;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;

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

/**
 * 易宝神州行支付结果通知。
 * 请求参数：
 *     参见规范
 * 返回:
 *     成功返回一行success，否则返回fail
 * @author lighthu
 */
public class YeepayNotifyServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(YeepayNotifyServlet.class);

	protected SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	protected Server server;
	protected PayInfoDAO dao;
	
	static HashMap<String, String[]> orderResults = new HashMap<String, String[]>();

	public YeepayNotifyServlet(Server s, PayInfoDAO dao) {
	    this.server = s;
		this.dao = dao;
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
        log.info("[yeepay_notify]orderid[" + rb_Order + "]amount[" + rc_Amt + "]cardno[" + rq_CardNo + 
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
        
        // 把结果保存在缓存中等待查询
        orderResults.put(rb_Order, new String[] { r1_Code, rq_ReturnMsg });
        
        // 如果是报错通知，直接返回，不修改数据库
        ConstYeepay.recordNotify();
        if (!"1".equals(r1_Code)) {
            out.println("success");
            
            // 记录订单通知时间
            Transaction tx = sessionFactory.getCurrentSession().beginTransaction();
            try {
            	PayInfo pi = dao.getInfoByPayId(rb_Order);
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
            addFailRecord(rb_Order);
            return;
        }
        
        // 查找订单
        PayInfo pi = null;
		Transaction tx = sessionFactory.getCurrentSession().beginTransaction();
		try {
			pi = dao.getInfoByPayId(rb_Order);
			if (pi == null) {
			    // 订单不存在
			    tx.rollback();
			    log.info("[yeepay_notify] order not found.");
			    out.println("fail");
			    return;
			} else if (!pi.isValid()) {
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
        try {
            pi.setFeeid(addIMoney(pi));
            log.info("[yeepay_notify]orderid[" + rb_Order + "] add i money success.");
            pi.setAddIFail(false);
        } catch (Exception e) {
            log.info("[yeepay_notify]orderid[" + rb_Order + "] add i money fail.");
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
	 * @param order
	 * @throws Exception
	 */
    private int addIMoney(PayInfo order) throws Exception {
        // 在认证服务器创建订单
    	String channelPrefix = "YEEPAY_";
    	if (ConstYeepay.CARD_TYPE_TELECOM.equals(order.getCardType())) {
    		channelPrefix = "YEEPAY_TEL_";
    	} else if (ConstYeepay.CARD_TYPE_UNICOM.equals(order.getCardType())) {
    		channelPrefix = "YEEPAY_UNI_";
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
