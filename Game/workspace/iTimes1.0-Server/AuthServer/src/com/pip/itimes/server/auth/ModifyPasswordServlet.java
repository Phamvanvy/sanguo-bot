package com.pip.itimes.server.auth;

import javax.servlet.http.*;
import java.io.IOException;
import javax.servlet.ServletException;
import org.apache.log4j.Logger;
import com.pip.itimes.server.bean.Account;
import com.pip.itimes.server.util.Utils;
import com.pip.security.SecurityUtils;

public class ModifyPasswordServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ModifyPasswordServlet.class);

    private AccountService accountService;

    public ModifyPasswordServlet(AccountService service) {
        this.accountService = service;
    }


    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
        String address = request.getRemoteAddr();
        if (address.equals("218.206.80.186")) {
            response.setCharacterEncoding("GBK");
            String accountName = request.getParameter("name");
            String oldPassword = request.getParameter("oldpassword");
            String password = request.getParameter("password");
            log.info("Modify Name["+accountName+"] OldPassword["+oldPassword+"] Password["+password+"]");
            int accountId = accountService.getAccountId(accountName);
            AccountState account = accountService.getAccount(accountId);
            Account a = null;
            if (account == null) {
                a = accountService.loadAccountById(accountId);
            } else {
                a = account.getAccount();
            }
            if (a != null) {
                if (!a.getPassword().equals(oldPassword)) {
                    if (!SecurityUtils.verifyMD5(oldPassword,
                                                 a.getPassword().substring(1))) {
                        sendError(response, "原始密码错误");
                        return;
                    }
                }
                if (password.getBytes("GBK").length > 16) {
                    sendError(response, "密码长度不能超过16个字符");
                    return;
                }
                if (!Utils.checkString(password, false)) {
                    sendError(response, "新密码存在非法字符");
                    return;
                }
                a.setPassword(password);
//                if (a.getModifyPasswordTimes() > 0) {
//                    a.setModifyPasswordTimes(a.getModifyPasswordTimes() - 1);
//                }
                accountService.saveAccount(a);
                sendOk(response, password);
            } else {
                sendError(response, "用户名或者密码错误");
            }
        }
    }

    private void sendError(HttpServletResponse response, String error) throws
            IOException {
        response.getWriter().println("2");
        response.getWriter().print(error);
    }


    private void sendOk(HttpServletResponse response, String password) throws
            IOException {
        response.getWriter().println("1");
        response.getWriter().print(password);
    }
}
