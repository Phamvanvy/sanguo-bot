package com.pip.itimes.server.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.pip.itimes.server.bean.Account;
import com.pip.itimes.server.util.KeywordsUtil;
import com.pip.itimes.server.util.Utils;
import org.apache.log4j.Logger;

public class AccountServlet extends HttpServlet {

    private Logger log = Logger.getLogger(AccountServlet.class);

    private AccountService accountService;

    public AccountServlet(AccountService accountService) {
        this.accountService = accountService;
    }

    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String phone = request.getParameter("phone");
        String gameCode = request.getParameter("gamecode");
        name = name.trim();
        response.setCharacterEncoding("GBK");
        if (name.length() == 0) {
            sendError(response, "帐号名不能为空");
            return;
        }
        if (name.getBytes("GBK").length > 16) {
            sendError(response, "帐号名太长");
            return;
        }
        if (KeywordsUtil.isInvalidName(name.toLowerCase())) {
            sendError(response, "帐号名出现非法字符");
            return;
        }
        if (!Utils.checkString(name, false)) {
            sendError(response, "帐号名出现非法字符");
            return;
        }
        String newName = KeywordsUtil.filterKeywords(name);
        if (!newName.equals(name)) {
            sendError(response, "帐号名出现非法字符");
            return;
        }
        if (!Utils.isValidMobilePhone(phone)) {
            sendError(response, "手机号有误");
            return;
        }
        int count = accountService.getAccountCountByPhone(phone);
        if (count == -1) {
            sendError(response, "注册错误");
            return;
        }

        if (count >= 3) {
            sendError(response, "同一手机号只能注册3个帐号");
            return;
        }

        Account account = accountService.loadAccountByName(name);
//            if(password.length()==0)
//                throw new ITimesException("密码不能为空",data.getSerial(),data.getSessionId(),data.getAppType());
//            if(password.getBytes("GBK").length>16)
//                throw new ITimesException("密码超过最大长度",data.getSerial(),data.getSessionId(),data.getAppType());
//            if(!Utils.checkString(password,false))
//                throw new ITimesException("密码出现非法字符",data.getSerial(),data.getSessionId(),data.getAppType());
        if (account != null) {
            if (account.getPhone().equals(phone)) {
                sendError(response, "该帐号已经存在，如有问题请打客服电话：010-64465123。");
                return;
            } else {
                sendError(response, "已经存在同名帐号");
                return;
            }
        }

        account = accountService.createNewAccount(name, password,
                                                  "", phone, "", 10000, "注册", true,
                                                  "CCCCCWEB", 0, 0,"opera",gameCode);
        if (account != null) {
            log.info(account.getUserName() + "Registered  model web");
            sendOk(response,password);
        } else {
            sendError(response,"创建帐号错误");
        }

    }

    private void sendError(HttpServletResponse response, String error) throws IOException{
        response.getWriter().println("2");
        response.getWriter().print(error);
    }

    private void sendOk(HttpServletResponse response, String password) throws IOException{
        response.getWriter().println("1");
        response.getWriter().print(password);
    }

}
