package com.pip.servermgr.data;

import java.util.*;
import java.io.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.XMLOutputter;

import com.pip.servermgr.Utils;

public class Configuration {
	private static HashMap<String, Account> accounts;
	public static Product[] products;
	private static String basePath;
	private static long productsModifyTime;
	private static long accountsModifyTime;

	public static void init(String path) throws Exception {
		basePath = path;
		loadAccounts();
		loadProducts();
	}

	/**
	 * 登录用户，返回可用的产品配置XML文件。
	 * 
	 * @param name
	 * @param pass
	 * @return 如果登录失败，返回null。
	 */
	public static String checkLogin(String name, String pass, String ip) {
		try {
			checkConfig();
			Account acc = accounts.get(name);
			if (acc == null || !acc.checkPassword(pass) || !acc.checkIP(ip)) {
				return null;
			}
			List<Product> authProducts = new ArrayList<Product>();
			for (int i = 0; i < products.length; i++) {
				if (acc.hasRole(products[i].requiredRole)) {
					authProducts.add(products[i]);
				}
			}
			
			return saveToXML(acc.allowModify, authProducts);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 验证用户对某个路径是否有权限。
	 */
	public static boolean authenticate(String name, String pass, String path, boolean isModify, String ip) {
		try {
			Account acc = accounts.get(name);
			if (acc == null || !acc.checkPassword(pass) || !acc.checkIP(ip)) {
				return false;
			}
			if (isModify && !acc.allowModify) {
				return false;
			}
			int pos = path.indexOf('/');
			if (pos != -1) {
				path = path.substring(0, pos);
			}
			for (int i = 0; i < products.length; i++) {
				if (products[i].path.equals(path)) {
					return acc.hasRole(products[i].requiredRole);
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 修改用户密码。
	 * @param name 用户名
	 * @param oldpass 旧密码
	 * @param newpass 新密码
	 * @throws Exception 如果密码验证失败，或出现其他系统错误，抛出异常
	 */
	public static void changePassword(String name, String oldpass, String newpass) throws Exception {
		Account acc = accounts.get(name);
		if (acc == null || !acc.checkPassword(oldpass)) {
			throw new Exception();
		}
		acc.setPassword(newpass);
		saveAccounts();
	}
	
	/**
	 * 根据shell的路径查找对应的server。
	 */
	public static Server findServer(String path) {
		try {
			String[] secs = path.split("/");
			
			// 查找产品
			Product prod = null;
			for (int i = 0; i < products.length; i++) {
				if (products[i].path.equals(secs[0])) {
					prod = products[i];
					break;
				}
			}
			if (prod == null) {
				return null;
			}
			
			// 查找服务器组
			ServerGroup sg = null;
			for (int i = 0; i < prod.servers.length; i++) {
				if (prod.servers[i].path.equals(secs[1])) {
					sg = prod.servers[i];
					break;
				}
			}
			if (sg == null) {
				return null;
			}
			
			// 查找服务器
			for (int i = 0; i < sg.servers.length; i++) {
				if (sg.servers[i].shell.equals(secs[2])) {
					return sg.servers[i];
				}
			}
			
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static String saveToXML(boolean allowModify, List<Product> ps) {
		Element root = new Element("servers");
		root.setAttribute("allowmodify", allowModify ? "true" : "false");
		Document doc = new Document(root);

		for (Product product : ps) {
			Element elem = new Element("product");
			elem.setAttribute("type", product.type);
			elem.setAttribute("name", product.name);
			elem.setAttribute("path", product.path);
			for (ServerGroup group : product.servers) {
				Element elem2 = new Element("servergroup");
				elem2.setAttribute("type", group.type);
				elem2.setAttribute("name", group.name);
				elem2.setAttribute("path", group.path);
				for (Server server : group.servers) {
					Element elem3 = new Element("server");
					elem3.setAttribute("type", server.type);
					elem3.setAttribute("name", server.name);
					elem3.setAttribute("shell", server.shell);
					for (String[] ops : server.addOps) {
					    Element elem4 = new Element("op");
					    elem4.setAttribute("name", ops[0]);
					    elem4.setAttribute("param", ops[1]);
					    elem3.addContent(elem4);
					}
					if (server.logInfo != null) {
						Element elem4 = new Element("log");
						elem4.setAttribute("ip", server.logInfo.ip);
						elem4.setAttribute("port", server.logInfo.port);
						elem4.setAttribute("path", server.logInfo.path);
						elem4.setAttribute("prefix", server.logInfo.prefix);
						if (server.logInfo.proxy != null) {
							elem4.setAttribute("proxy", server.logInfo.proxy);
						}
						elem3.addContent(elem4);
					}
					if (server.dbInfo != null) {
						Element elem4 = new Element("db");
						Element elem5 = new Element("master");
						elem5.setText(server.dbInfo.masterURL);
						elem4.addContent(elem5);
						elem5 = new Element("slave");
						elem5.setText(server.dbInfo.slaveURL);
						elem4.addContent(elem5);
						elem5 = new Element("user");
						elem5.setText(server.dbInfo.user);
						elem4.addContent(elem5);
						elem5 = new Element("password");
						elem5.setText(server.dbInfo.password);
						elem4.addContent(elem5);
						elem3.addContent(elem4);
					}
					
					elem2.addContent(elem3);
				}
				elem.addContent(elem2);
			}
			root.addContent(elem);
		}

		StringWriter sw = new StringWriter();
		try {
			XMLOutputter out = new XMLOutputter("    ", true, "UTF-8");
			out.output(doc, sw);
			sw.flush();
		} catch (Exception e) {
		}
		return sw.toString();
	}

	private static void checkConfig() throws Exception {
		File file = new File(basePath, "account.xml");
		if (file.lastModified() != accountsModifyTime) {
			loadAccounts();
		}
		file = new File(basePath, "servers.xml");
		if (file.lastModified() != productsModifyTime) {
			loadProducts();
		}
	}

	private static void loadAccounts() throws Exception {
		File file = new File(basePath, "account.xml");
		InputStream is = new FileInputStream(file);
		Document doc = new SAXBuilder(false).build(is);
		List list = doc.getRootElement().getChildren("account");
		HashMap<String, Account> ret = new HashMap<String, Account>();
		for (int i = 0; i < list.size(); i++) {
			Element elem = (Element) list.get(i);
			Account acc = new Account();
			acc.name = elem.getAttributeValue("name");
			acc.password = elem.getAttributeValue("password");
			String[] roles = elem.getAttributeValue("role").split(",");
			for (String r : roles) {
				acc.roles.add(r);
			}
			acc.allowModify = "true".equals(elem.getAttributeValue("allowmodify"));
			String ips = elem.getAttributeValue("ip");
			String[] secs = ips.split(",");
			acc.allowIPs = new int[secs.length][2];
			for (int j = 0; j < secs.length; j++) {
				String[] secs2 = secs[j].split("/");
				acc.allowIPs[j][0] = Utils.str2ip(secs2[0]);
				acc.allowIPs[j][1] = Utils.str2ip(secs2[1]);
			}
			ret.put(acc.name, acc);
		}
		accounts = ret;
		is.close();
		accountsModifyTime = file.lastModified();
	}
	
	private static void saveAccounts() throws Exception {
		File file = new File(basePath, "account.xml");
		Element root = new Element("accounts");
		Document doc = new Document(root);
		for (Account acc : accounts.values()) {
			Element elem = new Element("account");
			elem.setAttribute("name", acc.name);
			elem.setAttribute("password", acc.password);
			elem.setAttribute("allowmodify", acc.allowModify ? "true" : "false");
			String stemp = "";
			for (String r : acc.roles) {
				if (stemp.length() > 0) {
					stemp += ",";
				}
				stemp += r;
			}
			elem.setAttribute("role", stemp);
			
			stemp = "";
			for (int[] arr : acc.allowIPs) {
				if (stemp.length() > 0) {
					stemp += ",";
				}
				stemp += Utils.ip2str(arr[0]) + "/" + Utils.ip2str(arr[1]);
			}
			elem.setAttribute("ip", stemp);
			root.addContent(elem);
		}
		
		try {
			XMLOutputter out = new XMLOutputter("    ", true, "UTF-8");
			FileOutputStream fos = new FileOutputStream(file);
			out.output(doc, fos);
			fos.close();
		} catch (Exception e) {
		}
		accountsModifyTime = file.lastModified();
	}

	private static void loadProducts() throws Exception {
		File file = new File(basePath, "servers.xml");
		InputStream is = new FileInputStream(file);
		Document doc = new SAXBuilder(false).build(is);
		List list = doc.getRootElement().getChildren("product");
		Product[] ret = new Product[list.size()];
		for (int i = 0; i < list.size(); i++) {
			ret[i] = loadProduct((Element) list.get(i));
		}
		products = ret;
		is.close();
		productsModifyTime = file.lastModified();
	}

	private static Product loadProduct(Element elem) {
		String type = elem.getAttributeValue("type");
		String name = elem.getAttributeValue("name");
		String path = elem.getAttributeValue("path");
		String owner = elem.getAttributeValue("owner");
		List list = elem.getChildren("servergroup");
		Product ret = new Product(type, name, path, list.size());
		ret.owners = owner.split(",");
		ret.requiredRole = elem.getAttributeValue("requiredrole");
		for (int i = 0; i < list.size(); i++) {
			ret.servers[i] = loadServerGroup(ret, (Element) list.get(i));
		}
		return ret;
	}

	private static ServerGroup loadServerGroup(Product parent, Element elem) {
		String type = elem.getAttributeValue("type");
		String name = elem.getAttributeValue("name");
		String path = elem.getAttributeValue("path");
		List list = elem.getChildren("server");
		ServerGroup ret = new ServerGroup(parent, type, name, path, list.size());
		ret.isTest = "true".equals(elem.getAttributeValue("test"));
		for (int i = 0; i < list.size(); i++) {
			ret.servers[i] = loadServer(ret, (Element) list.get(i));
		}
		return ret;
	}

	private static Server loadServer(ServerGroup parent, Element elem) {
		String type = elem.getAttributeValue("type");
		String name = elem.getAttributeValue("name");
		String shell = elem.getAttributeValue("shell");
		List list = elem.getChildren("op");
		String[][] ops = new String[list.size()][2];
		for (int i = 0; i < list.size(); i++) {
		    Element elem2 = (Element)list.get(i);
		    ops[i][0] = elem2.getAttributeValue("name");
		    ops[i][1] = elem2.getAttributeValue("param");
		}
		Server ret = new Server(parent, type, name, shell, ops);
		ret.isTest = "true".equals(elem.getAttributeValue("test"));
		
		Element elem2 = (Element)elem.getChild("log");
		if (elem2 != null) {
			String ip = elem2.getAttributeValue("ip");
			String port = elem2.getAttributeValue("port");
			String path = elem2.getAttributeValue("path");
			String prefix = elem2.getAttributeValue("prefix");
			String proxy = elem2.getAttributeValue("proxy");
			ret.logInfo = new ServerLogInfo(ip, port, path, prefix, proxy);
		}
		
		elem2 = (Element)elem.getChild("db");
		if (elem2 != null) {
			String master = elem2.getChildText("master");
			String slave = elem2.getChildText("slave");
			String user = elem2.getChildText("user");
			String password = elem2.getChildText("password");
			ret.dbInfo = new ServerDBInfo(master, slave, user, password);
		}
		
		return ret;
	}
}
