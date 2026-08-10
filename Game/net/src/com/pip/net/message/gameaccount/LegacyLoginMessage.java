package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class LegacyLoginMessage extends AbstractMessage {
	
	protected String name;
	protected String password;
	protected String phone = "";
	protected String partition="";
	protected String version = "";
	protected String model = "";
	
	public LegacyLoginMessage(int serial,String name,String password){
		super(GameAccountMessageType.LEGACY_LOGIN,serial);
		this.name = name;
		this.password = password;
	}
	
	public LegacyLoginMessage(String name,String password){
		super(GameAccountMessageType.LEGACY_LOGIN);
		this.name = name;
		this.password = password;
	}
	
	public LegacyLoginMessage(int serial,String name,String password,String phone){
        super(GameAccountMessageType.LEGACY_LOGIN,serial);
        this.name = name;
        this.password = password;
        this.phone = phone;
    }
    
    public LegacyLoginMessage(String name,String password,String phone){
        super(GameAccountMessageType.LEGACY_LOGIN);
        this.name = name;
        this.password = password;
        this.phone = phone;
    }

    public LegacyLoginMessage(int serial,String name,String password,String phone,String partition,String version, String model){
        super(GameAccountMessageType.LEGACY_LOGIN,serial);
        this.name = name;
        this.password = password;
        this.phone = phone;
        this.partition = partition;
        this.version = version;
        this.model = model;
    }
    
    public LegacyLoginMessage(String name,String password,String phone,String partition,String version,String model){
        super(GameAccountMessageType.LEGACY_LOGIN);
        this.name = name;
        this.password = password;
        this.phone = phone;
        this.partition = partition;
        this.version = version;
        this.model = model;
    }
	
	public String getName(){
		return name;
	}
	
	public String getPassword(){
		return password;
	}
	
	public String getPhone() {
	    return phone;
	}
	
	public String getPartition() {
		return partition;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}
}
