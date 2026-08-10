package com.pip.server.billing.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.server.account.bean.Fee;
import com.pip.server.billing.Server;

public class BindCallbackServlet extends HttpServlet {
	
	protected AccountSecurityService service;
	protected Server server;
	
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	protected static Logger log = Logger.getLogger(BindCallbackServlet.class);
	
	private static final String BINDOK_HTML = 
	"<html>\n"+
	"  <head>\n"+
	"     <title>密码保护 - 邮箱</title>\n"+
	"     <meta http-equiv=\"Content-Type\" content=\"text/html;charset=gb2312\">\n"+
	"</head>\n"+
	"<body>\n"+
	"您的账户%s已成功绑定到邮箱%s。如遇密码丢失，可用手机登录明珠社区官网3g.pipgame.cn通过邮箱找回密码。<br>\n"+
	"<a href=\"http://pipgame.com\">访问明珠官网</a><br>\n"+
	"<a href=\"javascript:window.close()\">关闭窗口</a>\n"+
	"</body>\n"+
	"</html>";
	
	private static final String BINDFAIL_HTML = 
	
    "<html>\n"+
	"  <head>\n"+
	"    <title>密码保护 - 邮箱</title>\n"+
	" <meta http-equiv=\"Content-Type\" content=\"text/html;charset=gb2312\">\n"+
	"  </head>\n"+
	"<body>\n"+
	"%s<br>\n"+
	"<a href=\"http://pipgame.com\">访问明珠官网</a><br>\n"+
	"<a href=\"javascript:window.close()\">关闭窗口</a>\n"+
	"</body>\n"+
	"</html>\n";
	
	private static final String BINDOK_WML = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
		"<!DOCTYPE wml PUBLIC \"-//WAPFORUM//DTD WML 1.1//EN\" \"http://www.wapforum.org/DTD/wml_1.1.xml\">\n"+
		"<wml>\n"+
		"<head>\n"+
		"<meta forua=\"true\" content=\"max-age=0\" http-equiv=\"Cache-Control\"/>\n"+
		"</head>\n"+
		"<card title=\"密码保护 - 邮箱\" id=\"card1\">\n"+
		"<p>\n"+
		"您的账户%s已成功绑定到邮箱%s。如遇密码丢失，可登录明珠社区官网3g.pipgame.cn通过邮箱找回密码。<br/>\n"+
		"<anchor title=\"back\">返回上一级<prev/></anchor><br/>\n"+
		"<a href=\"http://pipgame.com\">访问官网</a>\n"+
		"</p>\n"+
		"</card>\n"+
		"</wml>";		
	
	private static final String BINDFAIL_WML =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
		"<!DOCTYPE wml PUBLIC \"-//WAPFORUM//DTD WML 1.1//EN\" \"http://www.wapforum.org/DTD/wml_1.1.xml\">\n"+
		"<wml>\n"+
		"<head>\n"+
		"<meta forua=\"true\" content=\"max-age=0\" http-equiv=\"Cache-Control\"/>\n"+
		"</head>\n"+
		"<card title=\"密码保护 - 邮箱\" id=\"card1\">\n"+
		"<p>\n"+
		"%s<br/>\n"+
		"<anchor title=\"back\">返回上一级<prev/></anchor><br/>\n"+
		"<a href=\"http://pipgame.com\">访问官网</a>\n"+
		"</p>\n"+
		"</card>\n"+
		"</wml>";		
	
	public BindCallbackServlet(AccountSecurityService service,Server server){
		this.service = service;
		this.server = server;
	}
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String uid = SecurityUtil.getUid(req);
		if(uid!=null){
			Transaction tx = sf.getCurrentSession().beginTransaction();
			try {
				AccountSecurity as = service.bindCallbackByMail(uid);
				printOkPage(req,resp,as.getName(),as.getMail());
				Fee fee = server.newFee(as.getName(), 0/*9900*/, "gm_bindmail");
				if(fee!=null){
					server.fulfillOrder(fee.getId());
				}
				tx.commit();
			} catch (IllegalCallbackStringException e) {
				printErrorPage(req,resp,SecurityUtil.ERROR_REQUEST);
				tx.rollback();
			} catch (AccountNotFoundException e) {
				printErrorPage(req,resp,SecurityUtil.ERROR_ILLEGALID);
				tx.rollback();
			} catch (BindStatusException e){
				printErrorPage(req,resp,SecurityUtil.ERROR_MAILBINDED);
				tx.rollback();				
			} catch (Exception e){
				log.error(e,e);
				tx.rollback();
			}
		}else{
			printErrorPage(req,resp,SecurityUtil.ERROR_REQUEST);
		}
	}
	
	protected void printErrorPage(HttpServletRequest req,HttpServletResponse resp,String error) throws IOException{
		String head = req.getHeader("Accept");
	    head = head.toLowerCase();
	    if(head.indexOf("vnd.wap.wml")==-1){
	    	resp.setCharacterEncoding("gbk");
	    	resp.setContentType("text/html;charset=gbk");
	    	resp.getWriter().print(String.format(BINDFAIL_HTML, error));
	    }else{
	    	resp.setCharacterEncoding("utf-8");
	    	resp.setContentType("text/vnd.wap.wml;charset=utf-8");
	    	resp.getWriter().print(String.format(BINDFAIL_WML, error));
	    }
	}
	
	protected void printOkPage(HttpServletRequest req,HttpServletResponse resp,String name,String mail) throws IOException{
		String head = req.getHeader("Accept");
	    head = head.toLowerCase();
	    if(head.indexOf("vnd.wap.wml")==-1){
	    	resp.setCharacterEncoding("gbk");
	    	resp.setContentType("text/html;charset=gbk");
	    	resp.getWriter().print(String.format(BINDOK_HTML, name,mail));
	    }else{
	    	resp.setCharacterEncoding("utf-8");
	    	resp.setContentType("text/vnd.wap.wml;charset=utf-8");
	    	resp.getWriter().print(String.format(BINDOK_WML, name,mail));
	    }		
	}
	
}
