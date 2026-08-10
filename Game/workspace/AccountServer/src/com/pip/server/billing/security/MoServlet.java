package com.pip.server.billing.security;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

public class MoServlet extends HttpServlet{
	
	protected AccountSecurityService accountSecurityService;
	protected Server server;
	
    protected static Logger log = Logger.getLogger(MoServlet.class);
    
    protected SessionFactory sf = HibernateUtil.getSessionFactory();

    protected static final Map<String,Date> map = new HashMap<String,Date>(); 
    protected static final long ONEDAY = 24*3600*1000L;
    
    public MoServlet(AccountSecurityService accountSecurityService,Server server) {
    	this.accountSecurityService = accountSecurityService;
    	this.server = server;
    }

    @Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws
            ServletException, IOException {
//        req.setCharacterEncoding("gb2312");
        String msg = req.getParameter("msg");
//        byte[] data = req.getParameter("msg").getBytes("gb2312");
//        log.info("data:"+CommonUtil.getHexString(data));
//        String msg = new String(req.getParameter("msg").getBytes("ISO-8859-1"), "gb2312");
        String mobile = req.getParameter("mobile");
        log.info("[mo]mobile="+mobile+" msg:"+msg );
        String oldmobile = "";
        if(msg==null || mobile==null) return ;
        if(msg.toUpperCase().startsWith("BDP")){//BDHM 随机串码
        	String randomString = getSmsContent(msg);
        	log.info("randomString:"+randomString);
        	if(randomString.length()!=6)
        		return;
            Transaction tx = sf.getCurrentSession().beginTransaction();
            String sms = null;
            String error = null;
            String name = "";
            try {
				AccountSecurity as = accountSecurityService.bindCallbackBySms(mobile, randomString);
				Fee fee = server.newFee(as.getName(), 0/*9900*/, "gm_bindphone");
				if(fee!=null){
					server.fulfillOrder(fee.getId());
				}
				name = as.getName();
				sms = "您已经成功绑定手机号码!";
				tx.commit();
			} catch (IllegalCallbackStringException e) {
				log.info(e,e);
				error = "错误的代码";
				tx.rollback();
			} catch (AccountNotFoundException e) {
				log.info(e,e);
				error = "没找到指定用户";
				tx.rollback();
			} catch (BindStatusException e) {
				log.info(e,e);
				error = "已经存在绑定手机";
				tx.rollback();
			} catch (Exception e){
				log.info(e,e);
				error = "未知错误";
				tx.rollback();
			}
			if(error!=null)
				sms = error;
			returnMsg(resp,name,mobile,sms);
//            AuthProxy proxy = AuthProxy.getInstance();
//            String result = proxy.ChangeMobile(fields[1],oldmobile,mobile);
//            if(result.startsWith("OK")){
//                result = "您已经成功绑定手机号码！";
//                /**@todo 更新用户信息表 **/
//                UserInfoDAO udao = new UserInfoDAO();
//                UserInfo userinfo = udao.getUserInfoByUserName(fields[1]);
//                if (userinfo == null) {//没有用户信息
//                   result+="您需要重新登陆财富港，才能使用短信相关服务。";
//                } else {
//                    userinfo.setMobile(mobile);
//                    udao.updateUserInfo(userinfo);
//                }
//                log.info(result);
//            }else{
//                String[] temp = result.split(",");
//                result = "绑定手机号码失败:" + (temp[1] == null ? "" : temp[1]);
//                log.info(result);
//            }
//            returnMsg(resp,fields[1],mobile,result);
        }else if(msg.toUpperCase().startsWith("ZHP")){//MMZH 帐号
        	String name = getSmsContent(msg);
        	if(!canGetback(name))
        		return;
            Transaction tx = sf.getCurrentSession().beginTransaction();
            String sms = null;
            String error = null;
            try {
				String s = accountSecurityService.getbackByPhone(name,mobile);
				putGetback(name);
				sms = String.format("尊敬的%s,您的密码已重置为%s", name,s);
				tx.commit();
			} catch (AccountNotFoundException e) {
				tx.rollback();
				error = "没找到指定用户";
			} catch (BindStatusException e) {
				tx.rollback();
				error = "没有绑定手机号";
			} catch (NotEnoughPaymentException e) {
				tx.rollback();
				error = "没有足够的i币";
			} catch (Exception e){
				log.error(e,e);
				tx.rollback();
				error = "未知错误";
			}
			if(error!=null){
				sms = String.format("重置密码操作失败:%s", error);
			}
			returnMsg(resp,name,mobile,sms);
//            AuthProxy proxy = AuthProxy.getInstance();
//            String result = proxy.GetbackPassword(fields[1],mobile);//需替换方法
//            if(result.startsWith("OK")){//OK，用户ID,用户名,密码
//                String[] temp = result.split(",");
//                result ="尊敬的"+fields[1]+",您的密码已重置为"+temp[3]+"。";
//                log.info(result);
//            }else{
//                String[] temp = result.split(",");
//                result = "重置密码操作失败："+(temp[1] == null ? "" : temp[1]);
//                log.info(result);
//            }
//            returnMsg(resp,fields[1],mobile,result);
        }
    }
    
    protected String getSmsContent(String s){
    	String result = s.substring(3);
    	if(Character.isSpaceChar(result.charAt(0))||result.charAt(0)=='?'){
    		return result.substring(1);
    	}
    	return result;
    }
    
    protected boolean canGetback(String name){
    	Date time = map.get(name);
    	if(time==null)
    		return true;
    	return (System.currentTimeMillis()-time.getTime())>ONEDAY;
    }
    
    protected void putGetback(String name){
    	map.put(name, new Date());
    }

    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws
            ServletException, IOException {
    }
    public void returnMsg(HttpServletResponse response, String uname,String mobilephone,String result){

        log.info("[mt]:uname="+uname+" mobile="+mobilephone+" "+"msg:"+result);
        OutputStream os = null;
        try {
            os = response.getOutputStream();
            os.write(("9160878"+result).getBytes("UTF-8"));
            os.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                    os = null;
                }
            } catch (IOException ex1) {
            }
        }
    }
}

