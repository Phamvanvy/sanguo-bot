package com.pip.servermgr.data;

import java.io.BufferedReader;
import java.io.StringReader;

/**
 * 一个服务器的日志信息。
 * @author lighthu
 */
public class ServerLogInfo {
	/**
	 * 服务器IP地址。
	 */
	public String ip;
	/**
	 * 服务器日志查询代理端口。
	 */
	public String port;
	/**
	 * 服务器日志路径。
	 */
	public String path;
	/**
	 * 服务器日志前缀。
	 */
	public String prefix;
	/**
	 * 代理访问URL。
	 */
	public String proxy;
	
	public ServerLogInfo(String ip, String port, String path, String prefix, String proxy) {
		this.ip = ip;
		this.port = port;
		this.path = path;
		this.prefix = prefix;
		this.proxy = proxy;
	}
}
