package com.pip.server.account.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.server.account.AccountEntity;
import com.pip.server.account.AccountService;
import com.pip.server.account.util.Util;

public class BalanceServlet extends HttpServlet {
	
	protected AccountService accountService;
	
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	
	public BalanceServlet(AccountService accountService){
		this.accountService = accountService;
	}
	
	protected void service(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
        String name = req.getParameter("account");
        String password = req.getParameter("password");
        resp.setCharacterEncoding("GBK");
        if (name == null || name.length() == 0 || password == null
				|| password.length() == 0) {
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println("1");
			resp.getWriter().println("²ÎÊý´íÎó");
			return;
		}
        Transaction tx = sf.getCurrentSession().beginTransaction();
        AccountEntity ae = accountService.getAccountEntity(name);
        tx.commit();
        if(ae==null){
        	resp.setStatus(HttpServletResponse.SC_OK);
        	resp.getWriter().println("1");
        	resp.getWriter().println("ÕÊºÅ²»´æÔÚ");        	
        }else{
        	if(!ae.getPassWord().equals(password)){
        		if(ae.getPassWord().startsWith("#")){
        			if(!Util.verifyMD5(password, ae.getPassWord().substring(1))){
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().println("1");
                        resp.getWriter().println("ÕÊºÅ»òÕßÃÜÂë´íÎó");
                        return;       				
        			}
        		}else{
        			resp.setStatus(HttpServletResponse.SC_OK);
        			resp.getWriter().println("1");
        			resp.getWriter().println("ÕÊºÅ»òÕßÃÜÂë´íÎó");
                    return;       			
        		}
        	}
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().println("0");
            resp.getWriter().println(ae.getBalance().getValue() / 100);
//            resp.getWriter().println(ae.getConfirmedBalance() / 100);
//            Date lastBillingTime = ae.getLastBillingTime();
//            java.util.Date currentTime = new java.util.Date();
//            if (a.getSubscribeStatus() == Account.SUBSCRIBED) {
//                response.getWriter().println(Const.MONTH_MAX / 100);
//            } else if (Const.inLaterMonth(lastBillingTime, currentTime)) {
//                if (a.getMonthFee() > Const.MONTH_MAX) {
//                    response.getWriter().println(Const.MONTH_MAX / 100);
//                } else {
//                    response.getWriter().println(a.getMonthFee() / 100);
//                }
//            } else {
//                response.getWriter().println(0);
//            }        	
        }
     
	}
}
