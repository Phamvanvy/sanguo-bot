package com.pip.gameaccount.qq;

import java.util.Date;

import com.pip.net.ISession;

public class QQLoginDetail {
	
	private QQLoginKey key;
	private QQGameAccount account;
	private Date loginTime;  //用户从游戏服务器进行登陆时的时间，跟QQLoginKey的时间不同。QQLoginKey的时间是用户在QQ服务器登陆的时间
	private ISession session;  //保存的是登陆是世界服务器的session
	
	public QQLoginDetail(QQLoginKey key,QQGameAccount account,Date loginTime){
		this.key = key;
		this.account = account;
		this.loginTime = loginTime;
	}
	
	public void setSession(ISession session){
		this.session = session;
	}

	public ISession getSession() {
		return session;
	}


	public QQLoginKey getKey() {
		return key;
	}

	public QQGameAccount getAccount() {
		return account;
	}

	public Date getLoginTime() {
		return loginTime;
	}
	
	
	
}
