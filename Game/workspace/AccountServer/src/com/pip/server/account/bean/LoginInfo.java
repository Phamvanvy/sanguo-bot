package com.pip.server.account.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_logininfo")
public class LoginInfo implements Serializable{
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	private int id;
	
	@Column(name="accountid",nullable = false)
	private int accountId;
	
	@Column(name="logintime",nullable = false)
	private Date loginTime;
	
	@Column(name="serviceid",nullable = false)
	private String serviceId;
	
	@Column(name="sessionid",nullable = false)
	private String sessionId;
	
	@Column(name="valid",nullable=false)
	private boolean valid;
	
	public LoginInfo(){
		
	}
	
	public LoginInfo(int accountId,Date loginTime,String serviceId,String sessionId,boolean valid){
		this.accountId = accountId;
		this.loginTime = loginTime;
		this.serviceId = serviceId;
		this.sessionId = sessionId;
		this.valid = valid;
	}

	public int getAccountId() {
		return accountId;
	}



	public Date getLoginTime() {
		return loginTime;
	}

	public String getServiceId() {
		return serviceId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}


	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
	
	public String toString(){
		return "ID["+id+"]AcountId["+accountId+"]serviceId["+serviceId+"]SessionId["+sessionId+"]valid["+valid+"]";
	}
}
