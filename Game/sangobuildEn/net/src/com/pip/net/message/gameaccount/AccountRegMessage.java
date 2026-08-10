package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class AccountRegMessage extends AbstractMessage {
	
	protected String name;
	protected String phone;
	protected String recommend;
	protected int recommendId;
	protected String model;
	protected String service;
	protected String version;
	protected String realPhone = "";
	protected String initPassword = "";
	
	public AccountRegMessage(int serial,String name,String phone,String recommend,int recommendId,String model,String service,String version){
		super(GameAccountMessageType.ACCOUNT_REG,serial);
		this.name = name;
		this.phone = phone;
		this.recommend = recommend;
		this.recommendId = recommendId;
		this.model = model;
		this.service = service;
		this.version = version;
	}
	
	public AccountRegMessage(String name, String phone, String recommend,
			int recommendId, String model, String service, String version) {
		super(GameAccountMessageType.ACCOUNT_REG);
		this.name = name;
		this.phone = phone;
		this.recommend = recommend;
		this.recommendId = recommendId;
		this.model = model;
		this.service = service;
		this.version = version;
	}

    public AccountRegMessage(int serial,String name,String phone,String recommend,int recommendId,String model,String service,String version,String realPhone){
        super(GameAccountMessageType.ACCOUNT_REG,serial);
        this.name = name;
        this.phone = phone;
        this.recommend = recommend;
        this.recommendId = recommendId;
        this.model = model;
        this.service = service;
        this.version = version;
        this.realPhone = realPhone;
    }
    
    public AccountRegMessage(String name, String phone, String recommend,
            int recommendId, String model, String service, String version,String realPhone) {
        super(GameAccountMessageType.ACCOUNT_REG);
        this.name = name;
        this.phone = phone;
        this.recommend = recommend;
        this.recommendId = recommendId;
        this.model = model;
        this.service = service;
        this.version = version;
        this.realPhone = realPhone;
    }
    
    public AccountRegMessage(int serial,String name,String phone,String recommend,
            int recommendId,String model,String service,String version,
            String realPhone, String initpass){
        super(GameAccountMessageType.ACCOUNT_REG,serial);
        this.name = name;
        this.phone = phone;
        this.recommend = recommend;
        this.recommendId = recommendId;
        this.model = model;
        this.service = service;
        this.version = version;
        this.realPhone = realPhone;
        this.initPassword = initpass;
    }
    
    public AccountRegMessage(String name, String phone, String recommend,
            int recommendId, String model, String service, String version,
            String realPhone, String initpass) {
        super(GameAccountMessageType.ACCOUNT_REG);
        this.name = name;
        this.phone = phone;
        this.recommend = recommend;
        this.recommendId = recommendId;
        this.model = model;
        this.service = service;
        this.version = version;
        this.realPhone = realPhone;
        this.initPassword = initpass;
    }

    public String getName() {
		return name;
	}

	public String getPhone() {
		return phone;
	}

	public String getRecommend() {
		return recommend;
	}

	public int getRecommendId() {
		return recommendId;
	}

	public String getModel() {
		return model;
	}

	public String getService() {
		return service;
	}

	public String getVersion() {
		return version;
	}
	
	public String getRealPhone() {
	    return realPhone;
	}
	
	public String getInitPassword() {
	    return initPassword;
	}
}
