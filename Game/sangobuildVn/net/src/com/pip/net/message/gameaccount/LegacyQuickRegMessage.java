package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class LegacyQuickRegMessage extends AbstractMessage{
	
	protected String phone;
	protected String version;
	protected String model;
	protected String serviceId;
	protected String realPhone = "";
	
	public LegacyQuickRegMessage(int serial,String phone,String version,String model,String serviceId){
		super(GameAccountMessageType.LEGACY_QUICKREG,serial);
		this.phone = phone;
		this.version = version;
		this.model = model;
		this.serviceId = serviceId;
	}
	
	public LegacyQuickRegMessage(String phone,String version,String model,String serviceId){
		super(GameAccountMessageType.LEGACY_QUICKREG);
		this.phone = phone;
		this.version = version;
		this.model = model;
		this.serviceId = serviceId;
	}	

    public LegacyQuickRegMessage(int serial,String phone,String version,String model,String serviceId,String realPhone){
        super(GameAccountMessageType.LEGACY_QUICKREG,serial);
        this.phone = phone;
        this.version = version;
        this.model = model;
        this.serviceId = serviceId;
        this.realPhone = realPhone;
    }
    
    public LegacyQuickRegMessage(String phone,String version,String model,String serviceId,String realPhone){
        super(GameAccountMessageType.LEGACY_QUICKREG);
        this.phone = phone;
        this.version = version;
        this.model = model;
        this.serviceId = serviceId;
        this.realPhone = realPhone;
    }   

    public String getPhone() {
		return phone;
	}

	public String getVersion() {
		return version;
	}

	public String getModel() {
		return model;
	}

	public String getServiceId() {
		return serviceId;
	}

	public String getRealPhone() {
	    return realPhone;
	}
}
