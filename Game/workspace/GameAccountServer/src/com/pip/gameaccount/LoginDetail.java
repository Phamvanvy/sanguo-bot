package com.pip.gameaccount;

import java.util.Date;


public class LoginDetail {
	private String name;
	private String key;
	private String serverId;
	private Date loginTime;
	
	public LoginDetail(String name,String key,String serverId,Date loginTime){
		this.name = name;
		this.key = key;
		this.serverId = serverId;
		this.loginTime = loginTime;
	}
	
	public String getServerId(){
		return serverId;
	}
	
	public String getKey(){
		return key;
	}
	
	public String getName() {
		return name;
	}


	public Date getLoginTime() {
		return loginTime;
	}
	
}
