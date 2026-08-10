package com.pip.server.billing.appstore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * AppStore充值相关常量。
 * @author lighthu
 */
public class ConstAppStore {
	public static String VERIFY_URL = "https://buy.itunes.apple.com/verifyReceipt";
	public static String VERIFY_URL_TEST = "https://sandbox.itunes.apple.com/verifyReceipt";
	
	public static class AppStoreProduct {
		public String productID;		// 产品ID
		public String productName;		// 产品标题
		public int price;				// 价格（美分）
		public int imoney;				// 对应i币（单位是i）
		
		public AppStoreProduct() {}
		
		public AppStoreProduct(String id, String name, int price, int imoney) {
			this.productID = id;
			this.productName = name;
			this.price = price;
			this.imoney = imoney;
		}
	}
	
	/*
	 * 支持多个iphone应用。key是应用的bundle id，value是这个应用支持的商品列表。
	 */
	private static Map<String, AppStoreProduct[]> appProducts = new HashMap<String, AppStoreProduct[]>();
	/*
	 * key是应用的bundle id，value是应用的状态，true表示测试状态。
	 */
	private static Map<String, Boolean> appTestStatus = new HashMap<String, Boolean>();
	/*
	 * 访问apple平台的代理地址。null表示不使用代理。
	 */
	public static String proxyURL;
	
	private static long configFileModifyTime;
	
	/**
     * 立刻载入配置。
     * @throws Exception
     */
    public static void loadConfig() throws Exception {
    	SAXReader reader = new SAXReader();
    	File f = new File("appstore_config.xml");
    	configFileModifyTime = f.lastModified();
		Document doc = reader.read(f);
		Element root = doc.getRootElement();
		
		// 读取产品列表
		Map<String, AppStoreProduct[]> newProductMap = new HashMap<String, AppStoreProduct[]>();
		Map<String, Boolean> newProductStatus = new HashMap<String, Boolean>();
		Iterator itor1 = root.elementIterator("product");
		while (itor1.hasNext()) {
			Element element = (Element)itor1.next();
			String bundleID = element.attributeValue("bid");
			boolean isTest = "true".equals(element.attributeValue("test"));
			Iterator itor2 = element.elementIterator("purchase");
			List<AppStoreProduct> plist = new ArrayList<AppStoreProduct>();
			while (itor2.hasNext()) {
				Element elem2 = (Element)itor2.next();
				AppStoreProduct newp = new AppStoreProduct();
				newp.productID = elem2.attributeValue("id");
				newp.productName = elem2.attributeValue("title");
				newp.price = Integer.parseInt(elem2.attributeValue("price"));
				newp.imoney = Integer.parseInt(elem2.attributeValue("imoney"));
				plist.add(newp);
			}
			AppStoreProduct[] arr = new AppStoreProduct[plist.size()];
			plist.toArray(arr);
			newProductMap.put(bundleID, arr);
			newProductStatus.put(bundleID, isTest);
		}
		appProducts = newProductMap;
		appTestStatus = newProductStatus;
		proxyURL = null;
		if (root.element("proxy") != null) {
			proxyURL = root.elementText("proxy");
		}
    }
    
    /**
     * 检查配置是否更新，如有更新立刻重载。
     * @throws Exception
     */
    public static void checkLoadConfig() throws Exception {
    	File f = new File("appstore_config.xml");
    	if (configFileModifyTime != f.lastModified()) {
    		loadConfig();
    	}
    }
	
	/**
	 * 判断是否支持某个应用的支付。
	 * @param bid
	 * @return
	 */
	public static boolean isAppValid(String bid) {
		return appProducts.containsKey(bid);
	}
	
	/**
	 * 判断某个应用是否在测试状态。
	 * @param bid
	 * @return
	 */
	public static boolean isAppTest(String bid) {
		return appTestStatus.get(bid);
	}
	
	/**
	 * 查找某个应用的某个商品。
	 * @param bid
	 * @param productName
	 * @return
	 */
	public static AppStoreProduct findProduct(String bid, String productID) {
		AppStoreProduct[] arr = appProducts.get(bid);
		for (int i = 0; arr != null && i < arr.length; i++) {
			if (arr[i].productID.equals(productID)) {
				return arr[i];
			}
		}
		return null;
	}
	
	/**
	 * 列出某个应用的全部商品。
	 */
	public static AppStoreProduct[] listProduct(String bid) {
		return appProducts.get(bid);
	}
}
