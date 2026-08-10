package com.pip.itimes.server.auth;

import javax.servlet.http.*;
import java.io.IOException;
import javax.servlet.ServletException;
import org.apache.log4j.Logger;
import com.pip.itimes.server.bean.Account;
import com.pip.security.SecurityUtils;

public class VerifyAccountServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(VerifyAccountServlet.class);

    private AccountService accountService;

    public VerifyAccountServlet(AccountService accountService) {
        this.accountService = accountService;
    }

    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
        String address = request.getRemoteAddr();
//        if(address.equals("218.206.80.185")){
        response.setCharacterEncoding("GBK");
            String name = request.getParameter("name");
            String password = request.getParameter("password");
            log.info("Verify Name["+name+"] Password["+password+"]");
            Account account = accountService.loadAccountByName(name);
            if (account == null) {
                sendError(response, "用户名或者密码错误");
            } else {
                if(!account.getPassword().equals(password)){
                    if(!SecurityUtils.verifyMD5(password,account.getPassword().substring(1))){
                        sendError(response, "原始密码错误");
                        return;
                    }else{
                        sendOk(response, "");
                    }
                }else{
                    sendOk(response, "");
                }
            }
//        }
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
