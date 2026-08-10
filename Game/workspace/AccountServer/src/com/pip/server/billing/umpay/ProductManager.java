package com.pip.server.billing.umpay;

import com.pip.server.billing.Server;

/**
 * 手机钱包支付渠道的产品管理类。
 */
public class ProductManager {
	public static ProductManager instance = null;
	private ProductHandler[] handlers;
	
	public ProductManager(Server server) {
		handlers = new ProductHandler[] {
			new UMPay_Product_020(server),
			new UMPay_Product_021_2(server),
			new UMPay_Product_021(server),
			new UMPay_Product_151(server),
			new UMPay_Product_080(server),
			new UMPay_Product_160(server),
			new UMPay_Product_300(server)
		};
	}
	
	/**
	 * 根据产品ID（短信内容）查找产品处理程序。
	 */
	public ProductHandler findHandlerByProduct(String productID) {
		for (int i = 0; i < handlers.length; i++) {
			if (productID.startsWith(handlers[i].getProductID())) {
				return handlers[i];
			}
		}
		return null;
	}
	
	/**
	 * 根据订单类型查找产品处理程序。
	 */
	public ProductHandler findHandlerByOrderType(String orderType) {
		for (int i = 0; i < handlers.length; i++) {
			if (orderType.startsWith(handlers[i].getOrderType())) {
				return handlers[i];
			}
		}
		return null;
	}
}
