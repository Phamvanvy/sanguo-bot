package com.pip.servermgr.data;

import java.util.*;
import java.io.*;
import org.jdom.*;
import org.jdom.input.*;

public class Configuration extends Thread {
	public static Product[] products;
	public static boolean allowModify;
	public static String userName;
	public static String password;
	
	public static String getEncryptPassword() throws Exception {
		return SecurityUtils.encryptPassword(password);
	}
	
	public static void load() throws Exception {
		HttpUtils.syncTime();
		String xml = HttpUtils.login();
		Document doc = new SAXBuilder(false).build(new StringReader(xml));
		allowModify = "true".equals(doc.getRootElement().getAttributeValue("allowmodify"));
		List list = doc.getRootElement().getChildren("product");
		products = new Product[list.size()];
		for (int i = 0; i < list.size(); i++) {
			products[i] = loadProduct((Element)list.get(i));
		}
		new Configuration().start();
	}
	
	private static Product loadProduct(Element elem) {
		String type = elem.getAttributeValue("type");
		String name = elem.getAttributeValue("name");
		String path = elem.getAttributeValue("path");
		List list = elem.getChildren("servergroup");
		Product ret = new Product(type, name, path, list.size());
		for (int i = 0; i < list.size(); i++) {
			ret.servers[i] = loadServerGroup(ret, (Element)list.get(i));
		}
		return ret;
	}
	
	private static ServerGroup loadServerGroup(Product parent, Element elem) {
		String type = elem.getAttributeValue("type");
		String name = elem.getAttributeValue("name");
		String path = elem.getAttributeValue("path");
		List list = elem.getChildren("server");
		ServerGroup ret = new ServerGroup(parent, type, name, path, list.size());
		for (int i = 0; i < list.size(); i++) {
			ret.servers[i] = loadServer(ret, (Element)list.get(i));
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
	
	public void run() {
		while (true) {
			for (int i = 0; i < products.length; i++) {
				for (int j = 0; j < products[i].servers.length; j++) {
					for (int k = 0; k < products[i].servers[j].servers.length; k++) {
						SynchronizeThread.instance.sync(products[i].servers[j].servers[k], false);
					}
				}
			}
			try {
				Thread.sleep(3 * 60 * 1000);
			} catch (Exception e) {
			}
		}
	}
}
