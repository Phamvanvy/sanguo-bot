package com.pip.servermgr.data;

import java.io.BufferedReader;
import java.io.StringReader;

/**
 * 一个服务器的数据库信息。
 * @author lighthu
 */
public class ServerDBInfo {
	/**
	 * 主库地址。
	 */
	public String masterURL;
	/**
	 * 从库地址。
	 */
	public String slaveURL;
	/**
	 * 数据库用户名。
	 */
	public String user;
	/**
	 * 数据库密码。
	 */
	public String password;
	
	public ServerDBInfo(String url1, String url2, String u, String p) {
		this.masterURL = url1;
		this.slaveURL = url2;
		this.user = u;
		this.password = p;
	}
}
