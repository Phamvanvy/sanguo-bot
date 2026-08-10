package com.pip.gameaccount.qq;

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

public class ModifyAccountServlet extends HttpServlet {
	private final SessionFactory sf = HibernateUtil.getSessionFactory();
	private static Logger log = Logger.getLogger(ModifyAccountServlet.class);

	private final QQLoginService loginService;
	private final SuperQQService superQQService;
	private final FeeDAO feeDao;

	public ModifyAccountServlet(QQLoginService loginService, SuperQQService sqqService, FeeDAO feeDao) {
		this.loginService = loginService;
		this.superQQService = sqqService;
		this.feeDao = feeDao;
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		String cmd = req.getParameter("cmd");
		int id = Integer.parseInt(req.getParameter("id"));
		Transaction tx = sf.getCurrentSession().beginTransaction();
		try {
			if ("balance".equals(cmd)) {
				int value = Integer.parseInt(req.getParameter("value"));
				QQGameAccount account = loginService.getGameAccount(id);
				if(account!=null){
					loginService.addBalance(account.getName(), value);
					resp.getWriter().println("ID["+id+"]Name["+account.getName()+"]Balance["+account.getBalance()+"]");
				}
			}  else if ("status".equals(cmd)) {
				int value = Integer.parseInt(req.getParameter("value"));
				QQGameAccount account = loginService.changeStatus(id, value);
				if (account != null) {
					resp.getWriter().println("ID["+"]Name["+account.getName()+"]Status["+account.getStatus()+"]");
				}
			} else if ("show".equals(cmd)) {
				QQGameAccount account = loginService.getGameAccount(id);
				if (account != null) {
					StringBuilder sb = new StringBuilder(2000);
					sb.append("ID[");
					sb.append(account.getId());
					sb.append("]Name[");
					sb.append(account.getName());
					sb.append("]Balance[");
					sb.append(account.getBalance());
					sb.append("]Status[");
					sb.append(account.getStatus());
					sb.append("]");
					resp.getWriter().print(sb.toString());
				}
			} else if ("charge".equals(cmd)) {
				int amount = Integer.parseInt(req.getParameter("amount")) * 100;
				QQGameAccount account = loginService.getGameAccount(id);
				if (account!=null) {
					if (QQBuyOkHandler.chinarunAmountMap.containsKey(amount)) {
						amount = QQBuyOkHandler.chinarunAmountMap.get(amount); 
					}
					loginService.addBalance(account.getName(), amount * 100);
					log.info("ID[" + id + "]Name[" + account.getName() + "]AddBalance[" + (amount * 100) + "]");
					/*Fee fee = new Fee();
					fee.setAccountId(account.getId());
					fee.setCharged(true);
					Date now = new Date();
					fee.setCreateTime(now);
					fee.setFinishTime(now);
					fee.setAmount(amount);
					fee.setCharged(true);
					fee.setChannel(QQBuyOkHandler.CHANNEL_CHINARUN);
					feeDao.createFee(fee);*/
					resp.getWriter().println("ID["+id+"]Name["+account.getName()+"]Balance["+account.getBalance()+"]");
				}
			} else if ("superqqurl".equals(cmd)) {
				String newurl1 = req.getParameter("url1");
                String newurl2 = req.getParameter("url2");
				SuperQQService.URL1 = newurl1;
				SuperQQService.URL2 = newurl2;
				resp.getWriter().println(SuperQQService.URL1);
				resp.getWriter().println(SuperQQService.URL2);
			} else if ("clearcache".equals(cmd)) {
				superQQService.clearCache();
			}
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			log.error(e, e);
		}
	}
}
