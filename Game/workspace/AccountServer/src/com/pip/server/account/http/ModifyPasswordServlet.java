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
import com.pip.server.account.AccountService;
import com.pip.server.account.Errors;
import com.pip.server.account.ModifyException;

public class ModifyPasswordServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(ModifyPasswordServlet.class);

    private AccountService accountService;
    
//    private String trustIp = "192.168.0.30";
    private String trustIp = "218.206.80.186";

    protected SessionFactory sf = HibernateUtil.getSessionFactory();
    
    public ModifyPasswordServlet(AccountService service) {
        this.accountService = service;
    }


    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
        String address = request.getRemoteAddr();
        if (address.equals(trustIp)) {
            response.setCharacterEncoding("GBK");
            String accountName = request.getParameter("name");
            String oldPassword = request.getParameter("oldpassword");
            String password = request.getParameter("password");
            log.info("Modify Name["+accountName+"] OldPassword["+oldPassword+"] Password["+password+"]");
            if(accountName==null||oldPassword==null||password==null){
				sendError(response, "参数错误");
				return;           	
            }
            Transaction tx = sf.getCurrentSession().beginTransaction();
            try {
				accountService.modifyPassword(accountName, oldPassword, password);
				tx.commit();
				sendOk(response, password);
			} catch (ModifyException e) {
				tx.rollback();
				int code = e.getCode();
				if(code==Errors.UNKNOW_ACCOUNT){
					sendError(response, "用户名或者密码错误");
					return;
				}
				else if(code==Errors.ERROR_OLD_PASSWORD){
					sendError(response, "原始密码错误");
					return;					
				}
				else if(code==Errors.ILLEGAL_PASSWORD_LENGTH){
					sendError(response, "密码长度不能超过16个字符");
					return;						
				}
				else if(code==Errors.ILLEGAL_PASSWORD_CHAR){
					sendError(response, "新密码存在非法字符");
					return;						
				}
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
