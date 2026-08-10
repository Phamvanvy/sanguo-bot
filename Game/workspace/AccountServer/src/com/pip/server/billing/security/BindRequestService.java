package com.pip.server.billing.security;

import java.util.Date;

import com.pip.server.billing.Server;

public class BindRequestService {
	
	protected Server server;
	protected BindRequestDAO dao;

	public BindRequestService(Server server,BindRequestDAO dao){
		this.server = server;
		this.dao = dao;
	}
	

	
	public BindRequest createPhoneBindRequest(int accountId,String phone){
		BindRequest result = new BindRequest();
		result.setAccountid(accountId);
		result.setContent(phone);
		result.setCreateTime(new Date());
		result.setRandomString(CommonUtil.randomString());
		result.setType(BindRequest.TYPE_PHONE);
		result.setUsed(false);
		dao.create(result);
		return result;
	}
	
	public BindRequest createMailBindRequest(int accountId,String mail){
		BindRequest result = new BindRequest();
		result.setAccountid(accountId);
		result.setContent(mail);
		result.setCreateTime(new Date());
		result.setRandomString(CommonUtil.randomString());
		result.setType(BindRequest.TYPE_MAIL);
		result.setUsed(false);
		dao.create(result);
		return result;
	}
}
