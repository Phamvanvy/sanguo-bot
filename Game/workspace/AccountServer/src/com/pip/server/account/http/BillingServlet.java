package com.pip.server.account.http;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.server.account.AccountCreditService;
import com.pip.server.account.AccountEntity;
import com.pip.server.account.AccountService;
import com.pip.server.account.BalanceException;
import com.pip.server.account.FeeService;
import com.pip.server.account.ModifyException;
import com.pip.server.account.bean.Fee;
import com.pip.server.account.util.Const;

public class BillingServlet extends HttpServlet {
	
	protected static final Logger log = Logger.getLogger(BillingServlet.class);
	
	protected Set<String> trustips = new HashSet<String>();
	protected AccountService accountService;
	protected AccountCreditService creditService;
	protected FeeService feeService;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	
	public BillingServlet(AccountService accountService,AccountCreditService cs,
	        FeeService feeService,Set<String> trustips){
		this.accountService = accountService;
		this.creditService = cs;
		this.feeService = feeService;
		this.trustips = trustips;
	}
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Transaction tx = sf.getCurrentSession().beginTransaction();
		try {
			resp.setContentType("text/plain;charset=UTF-8");
			resp.setCharacterEncoding("UTF-8");
			String addr = req.getRemoteAddr();
			if (!trustips.contains(addr)) {
			    log.info("BillingServlet: AddressRejected[" + addr + "]");
			    return;
			}
			req.setCharacterEncoding("UTF-8");
			String cmd = req.getParameter("cmd"); 
			if ("findAccount".equals(cmd)) {
			    // Account findAccount(int id)
				int id = Integer.parseInt(req.getParameter("id"));
			    AccountEntity acc = accountService.getAccountEntity(id);
			    if (acc != null) {
			    	resp.getWriter().println(Const.objectToString(acc.getAccount()));
			    } else {
			    	resp.getWriter().println(Const.objectToString(null));
			    }
			} else if ("findAccountByName".equals(cmd)) {
			    // Account findAccountByName(String name)
			    String name = req.getParameter("name");
			    AccountEntity acc = accountService.getAccountEntity(name);
			    if (acc != null) {
			    	resp.getWriter().println(Const.objectToString(acc.getAccount()));
			    } else {
			    	resp.getWriter().println(Const.objectToString(null));
			    }
			} else if ("findAccountBySubscribePhone".equals(cmd)) {
			    // Account findAccountBySubscribePhone(String phone, int productCode)
			    String phone = req.getParameter("phone");
			    int productCode = Integer.parseInt(req.getParameter("productCode"));
			    AccountEntity acc = accountService.getAccountEntityBySubscribePhone(productCode, phone);
			    if (acc != null) {
			    	resp.getWriter().println(Const.objectToString(acc.getAccount()));
			    } else {
			    	resp.getWriter().println(Const.objectToString(null));
			    }
			} else if ("findFee".equals(cmd)) {
			    // Fee findFee(int id)
			    int id = Integer.parseInt(req.getParameter("id"));
			    Fee fee = feeService.findFee(id);
			    resp.getWriter().println(Const.objectToString(fee));
			} else if ("findLatestFee".equals(cmd)) {
			    // Fee findLatestFee(String channel)
			    String channel = req.getParameter("channel");
			    Fee fee = feeService.findLatestFee(channel);
			    resp.getWriter().println(Const.objectToString(fee));
			} else if ("newFee".equals(cmd)) {
			    // Fee newFee(String accountName, int amount, String channel) {
				String accountName = req.getParameter("accountName");
			    int amount = Integer.parseInt(req.getParameter("amount"));
			    String channel = req.getParameter("channel");
			    Fee fee = feeService.newFee(accountName, amount, channel);
			    resp.getWriter().println(Const.objectToString(fee));
			} else if ("getMonthPayment".equals(cmd)) {
			    // int getMonthPayment(String channel)
			    String channel = req.getParameter("channel");
			    int amount = feeService.getMonthPayment(channel);
			    resp.getWriter().println(amount);
			} else if ("getChinarunCharge".equals(cmd)) {
			    // int getChinarunCharge(int accountID)
			    int accountID = Integer.parseInt(req.getParameter("id"));
			    int amount = feeService.getChinarunCharge(accountID);
			    resp.getWriter().println(amount);
			} else if ("unPurchase".equals(cmd)) {
				// boolean unPurchase(String accountName, int productCode)
				String accountName = req.getParameter("accountName");
			    int productCode = Integer.parseInt(req.getParameter("productCode"));
			    boolean result = accountService.unPurchase(accountName, productCode);
			    resp.getWriter().println(result ? "1" : "0");
			} else if ("fulfillOrder".equals(cmd)) {
				// boolean fulfillOrder(int feeID)
				int feeID = Integer.parseInt(req.getParameter("feeID"));
			    boolean result = feeService.fulfillOrder(feeID);
			    resp.getWriter().println(result ? "1" : "0");
			} else if ("fulfillOrder2".equals(cmd)) {
				// boolean fulfillOrder(int feeID)
				int feeID = Integer.parseInt(req.getParameter("feeID"));
			    boolean result = feeService.fulfillOrder2(feeID);
			    resp.getWriter().println(result ? "1" : "0");
			}else if ("purchaseProduct".equals(cmd)) {
				// boolean purchaseProduct(int feeID, int productCode, String phone)
				int feeID = Integer.parseInt(req.getParameter("feeID"));
				int productCode = Integer.parseInt(req.getParameter("productCode"));
				String phone = req.getParameter("phone");
			    boolean result = feeService.purchaseProduct(feeID, productCode, phone);
			    resp.getWriter().println(result ? "1" : "0");
			} else if ("resetPassword".equals(cmd)) {
				// String resetPassword(int feeID)
				int feeID = Integer.parseInt(req.getParameter("feeID"));
				String ret = feeService.resetPassword(feeID);
				resp.getWriter().println(ret == null ? "" : ret);
			} else if ("changePhone".equals(cmd)) {
				// boolean changePhone(int feeID, String newPhone)
				int feeID = Integer.parseInt(req.getParameter("feeID"));
				String newPhone = req.getParameter("newPhone");
				boolean result = feeService.changePhone(feeID, newPhone);
			    resp.getWriter().println(result ? "1" : "0");
			} else if ("hasPurchased".equals(cmd)) {
				// boolean hasPurchased(String accountName, int productCode)
				String accountName = req.getParameter("accountName");
				int productCode = Integer.parseInt(req.getParameter("productCode"));
				boolean result = accountService.hasPurchased(accountName, productCode);
			    resp.getWriter().println(result ? "1" : "0");
			} else if("resetPassword2".equals(cmd)){
				String accountName = req.getParameter("accountName");
				int pay = Integer.parseInt(req.getParameter("pay"));
				try {
					AccountEntity ae = accountService.resetPassword(accountName, pay);
					resp.getWriter().println(ae.getPassWord());
				} catch (ModifyException e) {
					resp.getWriter().println("");
				} catch (BalanceException e){
					resp.getWriter().println("");
				}
			} else if ("addCredit".equals(cmd)) {
			    int accountID = Integer.parseInt(req.getParameter("id"));
			    int amount = Integer.parseInt(req.getParameter("amount"));
			    creditService.changeAccountCredit(accountID, amount);
			    int credit = creditService.getAccountCredit(accountID);
			    resp.getWriter().println(credit);
			} else if ("queryCredit".equals(cmd)) {
			    int accountID = Integer.parseInt(req.getParameter("id"));
			    int credit = creditService.getAccountCredit(accountID);
			    resp.getWriter().println(credit);
			} else if ("addTimeCredit".equals(cmd)) {
			    int accountID = Integer.parseInt(req.getParameter("id"));
                int duration = Integer.parseInt(req.getParameter("duration"));
                creditService.addTimeCredit(accountID, duration);
                int credit = creditService.getAccountCredit(accountID);
                resp.getWriter().println(credit);
			} else if ("testCharge".equals(cmd)) {
				String accountName = req.getParameter("accountName");
			    int amount = Integer.parseInt(req.getParameter("amount"));
			    Fee fee = feeService.newFee(accountName, amount, "gm");
			    boolean result = feeService.fulfillOrder(fee.getId());
			    resp.getWriter().println(result ? "1" : "0");
			}else {
			    throw new ServletException("Invaid request.");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			tx.commit();
		}

	}
}
