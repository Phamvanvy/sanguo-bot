package peony.service.accountbinding;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import peony.game.Server;
import peony.service.Service;

public class AccountBindingService implements Service {

	private static final Logger log = Logger
			.getLogger(AccountBindingService.class);

	public void shutdown() {

	}

	public void startup() throws Exception {

	}
	
	public String[] getAccountBindingBlurStatus(int accountId){
		int code = -1;
		BufferedReader br = null;
		String[] ret = null;
		PostMethod method = new PostMethod(Server.server.billingURL + "status");
		method.getParams().setContentCharset("utf-8");
		method.addRequestHeader("Connection", "close");
		method.setParameter("id", String.valueOf(accountId));
		method.setParameter("type", "blur");
		try {
			HttpClient httpclient = new HttpClient();
			httpclient.getHttpConnectionManager().getParams()
					.setConnectionTimeout(30000);
			httpclient.getParams().setSoTimeout(30000);
			code = httpclient.executeMethod(method);
			br = new BufferedReader(new InputStreamReader(method
					.getResponseBodyAsStream(), "UTF-8"));
			if (code == 200) {
				String line = br.readLine();
				int retCode = Integer.parseInt(line);
				if (retCode == 0) {// 取成功
					ret = new String[4];
					ret[0] = br.readLine();
					ret[1] = br.readLine();
					ret[2] = br.readLine();
					ret[3] = br.readLine();
				}
			}
		} catch (Exception ex) {
			log.error(ex, ex);
		} finally {
			method.releaseConnection();
		}
		return ret;
	}

	public byte getAccountBindingStatus(int accountId) {
		int code = -1;
		byte ret = -1;
		BufferedReader br = null;
		log.info("[ACCOUNTBINDINGSTATUS]ACCOUNT["+accountId+"]");
		PostMethod method = new PostMethod(Server.server.billingURL + "status");
		method.getParams().setContentCharset("utf-8");
		method.addRequestHeader("Connection", "close");
		method.setParameter("id", String.valueOf(accountId));
		method.setParameter("type", "simple");
		try {
			HttpClient httpclient = new HttpClient();
			httpclient.getHttpConnectionManager().getParams()
					.setConnectionTimeout(30000);
			httpclient.getParams().setSoTimeout(30000);
			code = httpclient.executeMethod(method);
			br = new BufferedReader(new InputStreamReader(method
					.getResponseBodyAsStream(), "UTF-8"));
			if (code == 200) {
				String line = br.readLine();
				log.info("[ACCOUNTBINDINGSTATUS]RESULT["+line+"]");
				int retCode = Integer.parseInt(line);
				if (retCode == 0) {// 取成功
					line = br.readLine();
					ret = 0;
					ret |= line.charAt(0) == '1' ? 1 : 0;
					ret |= (line.charAt(1) == '1' ? 1 : 0) << 1;
					ret |= (line.charAt(2) == '1' ? 1 : 0) << 2;
					ret |= (line.charAt(3) == '1' ? 1 : 0) << 3;
				} else if (retCode == 1) {
					ret = -1;
				}
			}
		} catch (Exception ex) {
			log.error(ex, ex);
		} finally {
			method.releaseConnection();
		}
		return ret;
	}

	public void bindIdCard(int accountId, String idCard) throws BindException {
		int code = 0;
		BufferedReader br = null;
		String line = null;
		PostMethod method = new PostMethod(Server.server.billingURL + "modify");
		method.getParams().setContentCharset("utf-8");
		method.addRequestHeader("Connection", "close");
		method.setParameter("id", String.valueOf(accountId));
		method.setParameter("idcard", idCard);
		try {
			HttpClient httpclient = new HttpClient();
			httpclient.getHttpConnectionManager().getParams()
					.setConnectionTimeout(30000);
			httpclient.getParams().setSoTimeout(30000);
			code = httpclient.executeMethod(method);
			br = new BufferedReader(new InputStreamReader(method
					.getResponseBodyAsStream(), "UTF-8"));
			if (code == 200) {
				line = br.readLine();
				int retCode = Integer.parseInt(line);
				if (retCode == 1) {// 取成功
					throw new BindException(br.readLine());
				}
			}
		} catch (Exception ex) {
			if(ex instanceof BindException){
				throw (BindException)ex;
			}else{
				log.error(ex, ex);
				throw new BindException("Khóa thất bại, xin đợi sau đó tiến hành khóa lại");
			}
		} finally {
			method.releaseConnection();
		}
	}

	public void bindMail(int accountId, String mail) throws BindException {
		int code = 0;
		BufferedReader br = null;
		String line = null;
		PostMethod method = new PostMethod(Server.server.billingURL + "modify");
		method.getParams().setContentCharset("utf-8");
		method.addRequestHeader("Connection", "close");
		method.setParameter("id", String.valueOf(accountId));
		method.setParameter("mail", mail);
		try {
			HttpClient httpclient = new HttpClient();
			httpclient.getHttpConnectionManager().getParams()
					.setConnectionTimeout(30000);
			httpclient.getParams().setSoTimeout(30000);
			code = httpclient.executeMethod(method);
			br = new BufferedReader(new InputStreamReader(method
					.getResponseBodyAsStream(), "UTF-8"));
			if (code == 200) {
				line = br.readLine();
				log.info("[BINDMAIL]"+line);
				int retCode = Integer.parseInt(line);
				if (retCode == 1) {// 取成功
					throw new BindException(br.readLine());
				}
			}else{
				log.info("[BINDMAIL]CODE["+code+"]");
			}
		} catch (Exception ex) {
			if(ex instanceof BindException){
				throw (BindException)ex;
			}else{
				log.error(ex, ex);
				throw new BindException("Khóa thất bại, xin đợi sau đó tiến hành khóa lại");
			}
		} finally {
			method.releaseConnection();
		}
	}

	public void bindQna(int accountId, String question, String answer)
			throws BindException {
		int code = 0;
		BufferedReader br = null;
		String line = null;
		PostMethod method = new PostMethod(Server.server.billingURL + "modify");
		method.getParams().setContentCharset("utf-8");
		method.addRequestHeader("Connection", "close");
		method.setParameter("id", String.valueOf(accountId));
		method.setParameter("question", question);
		method.setParameter("answer", answer);
		try {
			HttpClient httpclient = new HttpClient();
			httpclient.getHttpConnectionManager().getParams()
					.setConnectionTimeout(30000);
			httpclient.getParams().setSoTimeout(30000);
			code = httpclient.executeMethod(method);
			br = new BufferedReader(new InputStreamReader(method
					.getResponseBodyAsStream(), "UTF-8"));
			if (code == 200) {
				line = br.readLine();
				int retCode = Integer.parseInt(line);
				if (retCode == 1) {// 取成功
					throw new BindException(br.readLine());
				}
			}
		} catch (Exception ex) {
			if(ex instanceof BindException){
				throw (BindException)ex;
			}else{
				log.error(ex, ex);
				throw new BindException("Khóa thất bại, xin đợi sau đó tiến hành khóa lại");
			}
		} finally {
			method.releaseConnection();
		}
	}

	public String bindPhone(int accountId,String phone) throws BindException{
		int code = 0;
		String sms = null;
		BufferedReader br = null;
		String line = null;
		PostMethod method = new PostMethod(Server.server.billingURL + "modify");
    	method.getParams().setContentCharset("utf-8");      
    	method.addRequestHeader( "Connection", "close");
		method.setParameter("id", String.valueOf(accountId));
		method.setParameter("phone", phone);
		try{
			HttpClient httpclient = new HttpClient();
			httpclient.getHttpConnectionManager().getParams()
					.setConnectionTimeout(30000);
			httpclient.getParams().setSoTimeout(30000);
			code = httpclient.executeMethod(method);
			br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), "UTF-8"));
			if(code==200){
	    		line = br.readLine();
	            int retCode = Integer.parseInt(line);
	            if (retCode == 1) {//取成功
	            	throw new BindException(br.readLine());
	            }
	            if(retCode == 0){
	            	sms =  br.readLine();
	            }
			}
		} catch (Exception ex) {
			if(ex instanceof BindException){
				throw (BindException)ex;
			}else{
				log.error(ex, ex);
				throw new BindException("Khóa thất bại, xin đợi sau đó tiến hành khóa lại");
			}
		} finally {
			method.releaseConnection();
		}
		return sms;
	}
	
	public boolean validIdcard(int accountId,String idcard){
		int code = 0;
		BufferedReader br = null;
		String line = null;
		PostMethod method = new PostMethod(Server.server.billingURL + "valid");
		method.getParams().setContentCharset("utf-8");
		method.addRequestHeader("Connection", "close");
		method.setParameter("id", String.valueOf(accountId));
		method.setParameter("idcard", idcard);
		try {
			HttpClient httpclient = new HttpClient();
			httpclient.getHttpConnectionManager().getParams()
					.setConnectionTimeout(30000);
			httpclient.getParams().setSoTimeout(30000);
			code = httpclient.executeMethod(method);
			br = new BufferedReader(new InputStreamReader(method
					.getResponseBodyAsStream(), "UTF-8"));
			if (code == 200) {
				line = br.readLine();
				int retCode = Integer.parseInt(line);
				if(retCode==0){
					return true;
				}
				return false;
			}else{
				return false;
			}
		} catch (Exception ex) {
			return false;
		} finally {
			method.releaseConnection();
		}
	}
}
