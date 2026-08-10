package com.pip.server.account.http;

import java.io.IOException;

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
import com.pip.server.account.PropertyService;
import com.pip.server.account.bean.LoginInfo;
import com.pip.server.account.bean.Purchased;

public class ModifyAccountServlet extends HttpServlet {

	private final SessionFactory sf = HibernateUtil.getSessionFactory();
	private static Logger log = Logger.getLogger(ModifyAccountServlet.class);

	private final AccountService accountService;
	private final PropertyService propertyService;

	public ModifyAccountServlet(AccountService accountService,
			PropertyService propertyService) {
		this.accountService = accountService;
		this.propertyService = propertyService;
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		String cmd = req.getParameter("cmd");
		String admin = req.getParameter("admin");
		String password = req.getParameter("pass");
		String pp = propertyService.get(admin);
		if (pp == null || !pp.equals(password)) {
			resp.getWriter().println("security error!");
			return;
		}
		int id = Integer.parseInt(req.getParameter("id"));
		Transaction tx = sf.getCurrentSession().beginTransaction();
		try {
			if ("release".equals(cmd)) {
				accountService.release(id);
				resp.getWriter().println("release ok");
			} else if ("abalance".equals(cmd)) {
				int value = Integer.parseInt(req.getParameter("value"));
				AccountEntity ae = accountService.getAccountEntity(id);
				if (ae != null) {
					if (value > 0) {
						accountService.addABalance(ae.getName(), value);
						resp.getWriter().println("abalance ok");
					} else {
						accountService.decABalance(ae.getName(),-value);
						resp.getWriter().println("abalance ok");
					}
				}
			} else if ("bbalance".equals(cmd)) {
				int value = Integer.parseInt(req.getParameter("value"));
				AccountEntity ae = accountService.getAccountEntity(id);
				if (ae != null) {
					if (value > 0) {
						accountService.addBBalance(ae.getName(), value);
						resp.getWriter().println("bbalance ok");
					}else{
						accountService.decBBalance(ae.getName(), value);
						resp.getWriter().println("bbalance ok");						
					}
				}
			} else if ("status".equals(cmd)) {
				int value = Integer.parseInt(req.getParameter("value"));
				AccountEntity ae = accountService.getAccountEntity(id);
				if (ae != null) {
					accountService.changeStatus(ae.getName(), value, "");
					resp.getWriter().println("status ok");
				}
			} else if ("unsubscribe".equals(cmd)) {
				int code = Integer.parseInt(req.getParameter("code"));
				AccountEntity ae = accountService.getAccountEntity(id);
				if (ae != null) {
					accountService.unPurchase(ae.getName(), code);
					resp.getWriter().println("unsubscribe ok");
				}
			} else if ("subscribe".equals(cmd)) {
				int code = Integer.parseInt(req.getParameter("code"));
				AccountEntity ae = accountService.getAccountEntity(id);
				if (ae != null) {
					accountService.purchaseProduct(ae.getName(), code, -1, "");
					resp.getWriter().println("subscribe ok");
				}
			} else if ("password".equals(cmd)) {
				String p = req.getParameter("password");
				AccountEntity ae = accountService.getAccountEntity(id);
				if (ae != null) {
					accountService.modifyPassword(ae.getName(), ae
							.getPassWord(), p);
					resp.getWriter().println("password ok");
				}
			} else if ("show".equals(cmd)) {
				AccountEntity ae = accountService.getAccountEntity(id);
				if (ae != null) {
					StringBuilder sb = new StringBuilder(2000);
					sb.append("ID[");
					sb.append(ae.getId());
					sb.append("]Name[");
					sb.append(ae.getName());
					sb.append("]Balance[");
					sb.append(ae.getBalance());
					sb.append("]Status[");
					sb.append(ae.getStatus());
					sb.append("]Password[");
					sb.append(ae.getPassWord());
					sb.append("]\nLoginInfos{");
					LoginInfo[] lis = ae.getLoginInfos();
					for (int i = 0; i < lis.length; i++) {
						sb.append(lis[i]);
					}
					sb.append("}Purchaseds{");
					Purchased[] ps = ae.getPurchaseds();
					for (int i = 0; i < ps.length; i++) {
						sb.append(ps[i]);
					}
					sb.append("}");
					resp.getWriter().print(sb.toString());
				}
			}
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			log.error(e, e);
		}
	}
}
