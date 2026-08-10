package com.pip.server.billing.umpay;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.account.util.Const;
import com.pip.server.billing.Server;

/**
 * 商户向手机钱包平台下订单，WAP方式。此接口只验证帐户合法性，并重定向到UMPAY的付费页面。
 */
public class SendOrderWapServlet extends HttpServlet {
	private static Logger log = Logger.getLogger(SendOrderWapServlet.class);
	private Server server;
	
	public SendOrderWapServlet(Server s) {
		server = s;
	}

	public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
        // 得到用户账号
        String account = request.getParameter("account");
        // 得到重复输入的用户帐号
        String account2 = request.getParameter("account2");
        // 得到充值金额
        String amountStr = request.getParameter("amount");

        log.info("Unipay(SendOrderWap): account[" + account + "]");
        log.info("Unipay(SendOrderWap): account2[" + account2 + "]");
        log.info("Unipay(SendOrderWap): amount[" + amountStr + "]");

        // 验证输入的用户帐号
        // 若用户帐号或者重复输入的用户帐号的值为空，
        // 则显示账号输入不一致（code==2）的信息。
        if (account == null || account2 == null) {
            response.sendRedirect(Const.PORTAL_REDIRECT_UNIPAY +
                                  Const.INPUT_ERROR);
            return;
        }
        // 若用户帐号和重复输入的用户帐号不一致，
        // 则显示账号输入不一致（code=2）的信息。
        if (!account.equals(account2)) {
            response.sendRedirect(Const.PORTAL_REDIRECT_UNIPAY +
                                  Const.ACCOUNT_INPUT_ERROR);
            return;
        }
        // 验证充值金额
        String productID;
        if ("200".equals(amountStr)) {
        	productID = "020#" + account;
        } else if ("1500".equals(amountStr)) {
        	productID = "150#" + account;
        } else if ("210".equals(amountStr)) {
        	productID = "021#" + account;
        } else if ("555701".equals(amountStr)) {
        	productID = "555701#" + account;
        } else if ("800".equals(amountStr)) {
        	productID = "080#" + account;
        } else if ("1600".equals(amountStr)) {
        	productID = "160#" + account;
        } else if ("3000".equals(amountStr)) {
        	productID = "300#" + account;
        } else {
        	response.sendRedirect(Const.PORTAL_REDIRECT_UNIPAY + Const.INPUT_ERROR);
            return;
        }
        ProductHandler handler = ProductManager.instance.findHandlerByProduct(productID);
        if (handler == null) {
        	response.sendRedirect(Const.PORTAL_REDIRECT_UNIPAY + Const.INPUT_ERROR);
            return;
        }
        
        // 检查用户是否允许充值
        int accountId = handler.checkAvailability(account);
        if (accountId < 0) {
            if (accountId == -1) {
                // 显示用户不存在（code=1）的信息。
                response.sendRedirect(Const.PORTAL_REDIRECT_UNIPAY +
                                      Const.ACCOUNT_NOT_FOUND);
                return;
            } else if (accountId == -2) {
                // 显示暂时不允许充值（code=4）的信息。
                response.sendRedirect(Const.PORTAL_REDIRECT_UNIPAY +
                                      Const.NOT_ALLOWED);
                return;
            }
        }

        // 生成重定向地址
        String redirectURL;
        account = "@" + accountId;
        redirectURL = handler.getWAPAddress() + java.net.URLEncoder.encode(account, "UTF-8");
        log.info("Unipay(SendOrderWap): redirect to " + redirectURL);
        response.sendRedirect(redirectURL);
    }
}
