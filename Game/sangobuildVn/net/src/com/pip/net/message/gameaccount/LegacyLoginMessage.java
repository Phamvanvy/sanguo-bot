package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class LegacyLoginMessage extends AbstractMessage {
	
	protected String name;
	protected String password;
	protected String phone = "";
	
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
	
	public String getName(){
		return name;
	}
	
	public String getPassword(){
		return password;
	}
	
	public String getPhone() {
	    return phone;
	}
}
