package com.pip.server.account.http;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.server.account.AccountEntity;
import com.pip.server.account.AccountService;
import com.pip.server.account.BalanceException;
import com.pip.server.account.Errors;
import com.pip.server.account.FeeService;
import com.pip.server.account.bean.Fee;

public class ChargeServlet extends HttpServlet {

	protected AccountService accountService;
	protected FeeService feeService;
	protected Set<String> trusted;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
//	protected String trustIp = "192.168.0.30";

	private static final Logger log = Logger.getLogger(ChargeServlet.class);

	public ChargeServlet(AccountService accountService, FeeService feeService,
			Set<String> trusted) {
		this.accountService = accountService;
		this.feeService = feeService;
		this.trusted = trusted;
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("text/plain;charset=UTF-8");
		resp.setCharacterEncoding("UTF-8");
		log.info("Charge Request.");
		if (trusted.contains(req.getRemoteAddr())) {
			String name = req.getParameter("AccountId");
			int charge = Integer.parseInt(req.getParameter("Charge"));
			int money = Integer.parseInt(req.getParameter("Money"));
			String channel = req.getParameter("Channel");
			if(name==null||channel==null){
				resp.getWriter().println(1);
				resp.getWriter().println("错误参数");
				return;
			}
			Transaction tx = sf.getCurrentSession().beginTransaction();
			try {
				AccountEntity ae = accountService.charge(name, charge*100, true);
				Fee fee = feeService.addCompletedFee(ae.getId(), charge*100,
						channel + "_" + money);
				tx.commit();
				log.info("ID[" + ae.getId() + "]Charge[" + charge + "]Money["
						+ money + "]OK");
				resp.getWriter().println(0);
				if (fee != null) {
					resp.getWriter().print(fee.getId());
				}
			} catch (BalanceException e) {
				tx.rollback();
				if (e.getCode() == Errors.UNKNOW_ACCOUNT) {
					resp.getWriter().println(1);
					resp.getWriter().println("没找到对应帐号");
				}
				log.error(e, e);
			} 

		} else {
			resp.getWriter().println(1);
			resp.getWriter().println("地址非信任");
		}

	}

}
