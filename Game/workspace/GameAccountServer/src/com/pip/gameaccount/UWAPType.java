package com.pip.gameaccount;

public interface UWAPType {
	public static final byte ERROR = (byte)-1;
	
	public static final byte SERVERLOGIN = (byte)180;
	public static final byte SERVERLOGIN_OK = (byte)181;
	public static final byte SERVERLOGIN_ERROR = (byte)182;
	
	
	public static final byte LOGIN = (byte)101;
	public static final byte LOGINOK = (byte)101;
	public static final byte IMONEY = (byte)102;
	public static final byte IMONEYOK = (byte)102;
	public static final byte CHANGE_STATUS = (byte)103;
	public static final byte CHANGE_STATUS_OK = (byte)103;
	public static final byte IMONEY_NOSESSION = (byte)104;
	public static final byte IMONEY_NOSESSION_OK = (byte)104;
	public static final byte REG = (byte)105;
	public static final byte REG_OK = (byte)105;
	
	
	public static final byte CLIENT_ACCOUNTREG = (byte)1;
	public static final byte CLIENT_ACCOUNTREGOK = (byte)2;
	public static final byte CLIENT_LOGIN = (byte)77;
	public static final byte CLIENT_LOGINOK = (byte)78;
	
}
