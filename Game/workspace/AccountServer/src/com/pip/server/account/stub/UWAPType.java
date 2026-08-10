package com.pip.server.account.stub;

public interface UWAPType {
	
	
	public static final short ERROR = (byte)-1;
	
	public static final short SERVERLOGIN = (byte)180;
	public static final short SERVERLOGIN_OK = (byte)181;
	public static final short SERVERLOGIN_ERROR = (byte)182;
	
	
	public static final short LOGIN = (byte)101;
	public static final short LOGINOK = (byte)101;
	public static final short IMONEY = (byte)102;
	public static final short IMONEYOK = (byte)102;
	public static final short CHANGE_STATUS = (byte)103;
	public static final short CHANGE_STATUS_OK = (byte)103;
	public static final short IMONEY_NOSESSION = (byte)104;
	public static final short IMONEY_NOSESSION_OK = (byte)104;
	public static final short REG = (byte)105;
	public static final short REG_OK = (byte)105;
}
