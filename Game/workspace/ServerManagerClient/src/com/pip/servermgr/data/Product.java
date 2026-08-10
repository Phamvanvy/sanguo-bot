package com.pip.servermgr.data;

/**
 * 游戏产品。同一个产品共享一份程序和数据。
 * @author lighthu
 */
public class Product {
	/**
	 * 类型：目前支持itimes，wulin2，account，proxy，sanguo，xiyou，xuanyuan，mzx。
	 */
	public String type;
	/**
	 * 产品名称。
	 */
	public String name;
	/**
	 * 对应服务器路径（相对路径）。
	 */
	public String path;
	/**
	 * 需要的用户角色。
	 */
	public String requiredRole;
	/**
	 * 负责人电话号码。
	 */
	public String[] owners;
	/**
	 * 服务器组。
	 */
	public ServerGroup[] servers;
	
	public Product(String t, String n, String p, int sc) {
		type = t;
		name = n;
		path = p;
		servers = new ServerGroup[sc];
	}
	
	public String toString() {
		return name;
	}
}
