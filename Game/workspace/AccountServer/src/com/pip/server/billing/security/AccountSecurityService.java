package com.pip.server.billing.security;

import java.util.Date;

import com.pip.server.account.bean.Account;
import com.pip.server.billing.Server;

public class AccountSecurityService {
	
	protected AccountSecurityDAO accountDao;
	protected BindRequestDAO bindDao;
	protected Server server;
	
	protected Object bindLock = new Object();

	
	public AccountSecurityService(AccountSecurityDAO accountDao,BindRequestDAO bindDao,Server server){
		this.accountDao = accountDao;
		this.bindDao = bindDao;
		this.server = server;
	}
	
	protected BindRequest createPhoneBindRequest(int accountId,String phone){
		BindRequest result = new BindRequest();
		result.setAccountid(accountId);
		result.setContent("000000");
		result.setCreateTime(new Date());
		String randomString = null;
		BindRequest r = null;
		do {
			randomString = RandomStringUtil.randomNumeric(6);
			r = bindDao.findPhoneRequestByRandomString(randomString);
		} while (r != null);
		result.setRandomString(randomString);
		result.setType(BindRequest.TYPE_PHONE);
		result.setUsed(false);
		bindDao.create(result);
		return result;
	}
	
	
	protected BindRequest createMailBindRequest(int accountId,String mail){
		BindRequest result = new BindRequest();
		result.setAccountid(accountId);
		result.setContent(mail);
		result.setCreateTime(new Date());
		result.setRandomString(CommonUtil.randomString());
		result.setType(BindRequest.TYPE_MAIL);
		result.setUsed(false);
		bindDao.create(result);
		return result;
	}
	
	public AccountSecurity bindCallbackByMail(String randomString)
			throws IllegalCallbackStringException, AccountNotFoundException,
			BindStatusException {
		synchronized (bindLock) {
			BindRequest request = bindDao
					.findMailRequestByRandomString(randomString);
			if (request == null || !request.isMailRequest() || request.isUsed())
				throw new IllegalCallbackStringException();
			AccountSecurity as = findAndCreateAccountSecurity(request
					.getAccountid());
			if (as.getMail() != null)
				throw new BindStatusException();
			as.setMail(request.getContent());
			as.setBindMailTime(new Date());
			accountDao.update(as);
			request.setUsed(true);
			bindDao.update(request);
			return as;
		}
	}

	public AccountSecurity bindCallbackBySms(String phone, String randomString)
			throws IllegalCallbackStringException, AccountNotFoundException,
			BindStatusException {
		synchronized (bindLock) {
			BindRequest request = bindDao
					.findPhoneRequestByRandomString(randomString);
			if (request == null || !request.isPhoneRequest()
					|| request.isUsed())
				throw new IllegalCallbackStringException();
			AccountSecurity as = findAndCreateAccountSecurity(request
					.getAccountid());
			String oldBindPhone = as.getBindPhone();
			if (oldBindPhone != null)
				throw new BindStatusException();
			as.setBindPhone(phone);
			as.setBindPhoneTime(new Date());
			accountDao.update(as);
			request.setUsed(true);
			bindDao.update(request);
			return as;
		}

	}
	
	public String modifyPhone(int id,String phone) throws IllegalPhoneException,AccountNotFoundException,NoIdCardException{
//		if(!CommonUtil.validPhone(phone))
//			throw new IllegalPhoneException();
		AccountSecurity as = findAndCreateAccountSecurity(id);
		if (as.getIdcard() == null || as.getIdcard().length() == 0) {
		    throw new NoIdCardException();
		}
		BindRequest request = createPhoneBindRequest(id,phone);
		return request.getRandomString();
	}
	
	public void modifyIdcard(int id,String idcard) throws IllegalIdcardException,AccountNotFoundException{
		AccountSecurity as = findAndCreateAccountSecurity(id);
		if (!CommonUtil.validIdcard(idcard))
			throw new IllegalIdcardException();
		as.setIdcard(idcard);
		as.setBindIDCardTime(new Date());
		accountDao.update(as);
	}
	
	public void modifyQnA(int id, String question, String answer)
			throws IllegalQuestionException, IllegalAnswerException,
			AccountNotFoundException,NoIdCardException {
		AccountSecurity as = findAndCreateAccountSecurity(id);
		if (as.getIdcard() == null || as.getIdcard().length() == 0) {
            throw new NoIdCardException();
        }
		if(question==null||question.trim().length()==0)
			throw new IllegalQuestionException();
		if(answer==null||answer.trim().length()==0)
			throw new IllegalAnswerException();
		question = question.trim();
		answer = answer.trim();
		as.setQuestion(question);
		as.setAnswer(answer);
		as.setBindQuestionTime(new Date());
		accountDao.update(as);
	}
	
	public void modifyMail(int id,String mail) throws IllegalMailException,AccountNotFoundException,NoIdCardException{
		AccountSecurity as = findAndCreateAccountSecurity(id);
        if (as.getIdcard() == null || as.getIdcard().length() == 0) {
            throw new NoIdCardException();
        }
		if(!CommonUtil.validEmail(mail))
			throw new IllegalMailException();
		BindRequest request = createMailBindRequest(id,mail);
		Mail m = getBindMail(mail,as.getName(),mail,request.randomString);
		MailUtil.send(m);
	}
	
	
	protected static final String MAIL_BIND = "尊敬的用户，您好，\n\n您已申请把您的明珠通行证%s绑定到邮箱%s,请回复本邮件,或点击下面的链接完成绑定：\n\nhttp://%s/bindconfirm?uid=%s\n\n祝您游戏愉快。\n\n掌上明珠运营中心\n客服电话：010-59787888";
    protected static final String MAIL_BIND_HTML = 
        "<html><head><META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html;charset=gb2312\"></head>" +
        "<body>尊敬的用户，您好，<br><br>您已申请把您的明珠通行证%s绑定到邮箱%s,请回复本邮件,或点击下面的链接完成绑定：<br><br>" +
        "<a href=\"http://%s/bindconfirm?uid=%s\">http://%s/bindconfirm?uid=%s</a><br><br>" +
        "祝您游戏愉快。<br><br>掌上明珠运营中心<br>客服电话：010-59787888</body></html>";

	protected Mail getBindMail(String to, String name, String bindMail,
			String randomString) {
		Mail m = new Mail("bind_" + randomString + "@pipgame.mobi", to, "明珠通行证 - 邮箱绑定", 
		        String.format(MAIL_BIND, name, bindMail, server.getHost() + ":" + 
		                server.getPort(), randomString));
		String html = String.format(MAIL_BIND_HTML,
                name, bindMail, server.getHost() + ":" + server.getPort(),
                randomString, server.getHost() + ":" + server.getPort(), randomString);
		m.setHTML(html);
		return m;
	}
	
	
	public AccountSecurity findAndCreateAccountSecurity(int id) throws AccountNotFoundException{
		AccountSecurity as = accountDao.getAccountSecurity(id);
		if(as==null){
			Account account = server.findAccount(id);
			if(account==null)
				throw new AccountNotFoundException();
			as = createAccountSecurity(account);
		} else if (as.getName().startsWith("游客")) {
		    // 游客帐号可能会改名，这里到认证服务器去查询最新的名字
		    Account acc = server.findAccount(id);
		    if (!as.getName().equals(acc.getName())) {
		        as.setName(acc.getName());
		        accountDao.makePersistent(as);
		    }
		}
		return as;
	}
	
	/**
	 * 通过手机找回密码,将给手机发送重置以后的密码
	 * @param id
	 * @throws AccountNotFoundException
	 */
	
	public String getbackByPhone(int id,String phone) throws AccountNotFoundException,
			BindStatusException,NotEnoughPaymentException {
		AccountSecurity as = findAndCreateAccountSecurity(id);
		if(as.getBindPhone()==null)
			throw new BindStatusException();
		if(!as.getBindPhone().equals(phone))
			throw new BindStatusException();
		String password = server.resetPassword(as.getName(), 0);
		if(password.length()==0)
			throw new NotEnoughPaymentException();
		return password;
//		SmsUtil.send(as.getBindPhone(), "密码:"+password);
	}
	
	public String getbackByPhone(String name,String phone) throws AccountNotFoundException,
			BindStatusException, NotEnoughPaymentException {
		int id = getIdByName(name);
		return getbackByPhone(id,phone);
	}

	public int getIdByName(String name) throws AccountNotFoundException{
		Account a = server.findAccountByName(name);
		if(a==null)
			throw new AccountNotFoundException();
		return a.getId();
	}
	
	/**
	 * 通过邮件找回密码,将发送给用户邮箱重置以后的密码
	 * 
	 * @param id
	 * @throws AccountNotFoundException
	 */

	public void getbackByMail(int id) throws AccountNotFoundException,
			BindStatusException, NotEnoughPaymentException {
		AccountSecurity as = findAndCreateAccountSecurity(id);
		if (as.getMail() == null)
			throw new BindStatusException();
		Account a = server.findAccount(id);
		Mail m = buildGetbackMail(as.getMail(),as.getName(),a.getPasswordDec());
		MailUtil.send(m);
	}

	protected static final String MAIL_GETBACK = "尊敬的用户，您好，\n\n您的明珠通行证%s的密码是%s。温馨提示：请尽快修改并牢记密码，以防帐号丢失。\n\n祝您游戏愉快。\n\n掌上明珠运营中心\n客服电话：010-59787888";
	
	protected String SUPPORT_MAIL = "support@pearlinpalm.com";
	
	protected Mail buildGetbackMail(String to,String name,String password){
		Mail m = new Mail(SUPPORT_MAIL,to,"明珠通行证 - 找回密码",String.format(MAIL_GETBACK, name,password));
		return m;
	}

	/**
	 * 通过问题找回密码,当问题以及回答正确以后返回重置后的密码
	 * 
	 * @param id
	 * @param question
	 * @param answer
	 * @return 重置以后的密码
	 */
	public String getbackByQnA(int id, String question, String answer)
			throws AccountNotFoundException, BindStatusException,NotEnoughPaymentException,VerificationException {
		AccountSecurity as = findAndCreateAccountSecurity(id);
		if(as.getQuestion()==null||as.getAnswer()==null)
			throw new BindStatusException();
		if(as.getQuestion().equals(question)&&as.getAnswer().equals(answer)){
			String password = server.resetPassword(as.getName(), 0);
			if(password.length()==0)
				throw new NotEnoughPaymentException();
			return password;
		}else{
			throw new VerificationException();
		}
	}
	
	protected AccountSecurity createAccountSecurity(Account account){
		AccountSecurity as = new AccountSecurity();
		as.setId(account.getId());
		as.setName(account.getName());
		as.setCreateTime(new Date());
		accountDao.create(as);
		return as;
	}
}
