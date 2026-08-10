package com.pip.servermgr.data;

/**
 * 服务器组。
 * @author lighthu
 */
public class ServerGroup {
	/**
	 * 所属产品。
	 */
	public Product parent;
	/**
	 * 类型：目前支持game，account，proxy，ipd。
	 */
	public String type;
	/**
	 * 服务器名称。
	 */
	public String name;
	/**
	 * 对应服务器路径（相对路径）。
	 */
	public String path;
	/**
	 * 是否测试服务器。
	 */
	public boolean isTest;
	/**
	 * 子服务器。
	 */
	public Server[] servers;
	
	public ServerGroup(Product pa, String t, String n, String p, int sc) {
		parent = pa;
		type = t;
		name = n;
		path = p;
		servers = new Server[sc];
	}
	
	public String getPath() {
		return parent.path + "/" + path;
	}
	
	public String toString() {
		return name;
	}
}
