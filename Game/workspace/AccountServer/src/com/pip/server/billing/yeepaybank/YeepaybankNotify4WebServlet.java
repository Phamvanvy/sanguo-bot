package com.pip.server.billing.yeepaybank;

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
import com.pip.server.billing.yeepay.ConstYeepay;
import com.pip.server.billing.yeepay.DigestUtil;

public class YeepaybankNotify4WebServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(YeepaybankNotify4WebServlet.class);

	protected SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	protected Server server;
	protected PayInfoDAO dao;
	
	static HashMap<String, String> orderMap = new HashMap<String, String>();
	
	/*需要为正式结果通知地址*/
	static String callbackURL = "http://news.pipgame.cn/webpg/pay_callback.do";
	
    /**
     * 根据充值金额计算对应i币。
     * @param amount 金额（分）
     * @return i币数量
     */
    public static int calcIMoney(int amount) {
    	/*当前优惠3%*/
    	double ybmoney = (amount / 100.0) * 10.0 * 1.03;
    	return (int)(Math.round(ybmoney) * 36);
    }
    
	public YeepaybankNotify4WebServlet(Server s, PayInfoDAO dao) {
	    this.server = s;
		this.dao = dao;
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	    
	    // 读取参数
		String p1_MerId = request.getParameter("p1_MerId");
        String r0_Cmd = request.getParameter("r0_Cmd");
	    String r1_Code = request.getParameter("r1_Code");
        String r2_TrxId = request.getParameter("r2_TrxId");
        String r3_Amt = request.getParameter("r3_Amt");
        String r4_Cur = request.getParameter("r4_Cur");
        String r5_Pid = request.getParameter("r5_Pid");        
        String r6_Order = request.getParameter("r6_Order");
        String r7_Uid = request.getParameter("r7_Uid");
        String r8_MP = request.getParameter("r8_MP");
        String r9_BType = request.getParameter("r9_BType");
        
        
        String rb_BankId = request.getParameter("rb_BankId");
//        String ro_BankOrderId = request.getParameter("ro_BankOrderId");
//        String rp_PayDate = request.getParameter("rp_PayDate");
//        String rq_CardNo = request.getParameter("rq_CardNo");
        String ru_Trxtime = request.getParameter("ru_Trxtime");
        
        String hmac = request.getParameter("hmac");
        log.info("[yeepaybank4web_notify]orderid[" + r6_Order + "]amount[" + r3_Amt + 
                "]result[" + r1_Code + "]bankId[" + rb_BankId + "]ru_Trxtime["+ru_Trxtime+"]");
        
        
        // 设置返回格式
        response.setCharacterEncoding("GBK");
        response.setContentType("text/html;charset=gbk");
        PrintWriter out = response.getWriter();
        
        // 验证签名
        String merchantKey = ConstYeepay.getMerchantKey(p1_MerId);
        String newHmac = DigestUtil.getHmac(new String[] { 
        		p1_MerId, r0_Cmd, r1_Code, r2_TrxId, r3_Amt,r4_Cur,r5_Pid,r6_Order,r7_Uid, r8_MP, r9_BType 
        }, merchantKey);
        
        if (!hmac.equals(newHmac)) {
        	if("1".equals(r9_BType)){
        		response.sendRedirect(callbackURL+"?code=1&gamecode=1"); //校验出错
        	}else if("2".equals(r9_BType)){
        		out.println("fail");
        	}
            return;
        }
       
        // 如果是报错通知，直接返回，不修改数据库
        if (!"1".equals(r1_Code)) {
        	if("1".equals(r9_BType)){
        		response.sendRedirect(callbackURL+"?code=3&gamecode=1");
        	}else if("2".equals(r9_BType)){
        		out.println("success");
        	}
            // 记一次支付失败，同一帐号一天只能支付失败20次
            addFailRecord(r6_Order);
            return;
        }
        
        // 查找订单
        PayInfo pi = null;
		Transaction tx = sessionFactory.getCurrentSession().beginTransaction();
		int gamecode = 1;
		int errorcode = 9;
		int realAmount =0;
		try {
			pi = dao.getInfoByPayId(r6_Order);
			if (pi == null) {
			    // 订单不存在
			    tx.rollback();
			    log.info("[yeepaybank4web_notify] order not found.");
			    errorcode = 2;
			} else if (!pi.isValid()) {
			    // 订单存在，且尚未成功，则标记为成功
			    realAmount = (int)Double.parseDouble(r3_Amt);
			    pi.setMoney(String.valueOf(realAmount));

			    // 修改订单状态保证不会重复交易
//		        pi.setI_sum(realAmount * 360 );
			    pi.setI_sum(calcIMoney(realAmount * 100));
		        pi.setValid(true);
		        pi.setNotifyTime(new Date());
		        dao.update(pi);
		        tx.commit();
		        gamecode = pi.getGame();
		        errorcode = 0;
		        
		        // 为用户添加i币并修改订单状态
		        try {
		            pi.setFeeid(addIMoney(pi));
		            log.info("[yeepaybank4web_notify]orderid[" + r6_Order + "] add i money success.");
		            pi.setAddIFail(false);
		        } catch (Exception e) {
		            log.info("[yeepaybank4web_notify]orderid[" + r6_Order + "] add i money fail.");
		        }
		        tx = sessionFactory.getCurrentSession().beginTransaction();
		        try {
		            dao.update(pi);
		            tx.commit();
		        } catch (Exception ex) {
		            tx.rollback();
		            log.error(ex, ex);
		        }
			} else {
			    // 订单存在，且已经完成，说明这是一个重复请求，直接返回success
			    tx.rollback();
			    gamecode = pi.getGame();
			    errorcode = 0;
			}
		} catch (Exception ex) {
			tx.rollback();
			log.error(ex, ex);
			errorcode = 2;
		}
           
        if("1".equals(r9_BType)){//重定向方式
         	String returnHttp = orderMap.get(r6_Order);
         	if(returnHttp==null){
         		returnHttp = callbackURL + "?code=0&name=&amount="+ (realAmount*100);
         	}else{
         		orderMap.remove(r6_Order);
         	}
         	returnHttp=returnHttp+"?code="+errorcode+"&gamecode="+gamecode;
         	response.sendRedirect(returnHttp);
         }else if("2".equals(r9_BType)){
         	out.println("SUCCESS");
         }
	}

	/**
	 * 完成一个订单，为用户增加i币。
	 * @param order
	 * @throws Exception
	 */
    private int addIMoney(PayInfo order) throws Exception {
        // 在认证服务器创建订单
        Fee fee = server.newFee(order.getUserName(), (int)(order.getI_sum() * 100 *
        		ConstYeepay.randomGiftRatio(server, order)), "YEEPAYBKW_" + order.getMoney());
        
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
