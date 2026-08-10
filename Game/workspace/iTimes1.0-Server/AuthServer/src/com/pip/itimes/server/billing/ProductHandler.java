package com.pip.itimes.server.billing;

/**
 * 手机钱包支付渠道的一个商品的处理类。每个商品都需要实现3个接口：检查续费可用性、下单和完成订单。
 */
public interface ProductHandler {
	/**
	 * 得到产品ID，例如020#，020#*等等。
	 */
	public String getProductID();
	
	/**
	 * 得到产品对应的订单类型。
	 */
	public String getOrderType();
	
	/**
	 * 得到产品付费金额（分）。
	 */
	public int getPayAmount();
	
	/**
	 * 根据用户请求生成订单。
	 * @param productID 用户短信内容，已经去掉了前面的产品ID
	 * @param phone 手机号
	 * @param remarkBuf 返回商品描述字符串
	 * @return 如果下单成功，返回订单号；帐号不存在返回-1，余额过多返回-2.
	 */
	public int placeOrder(String productID, String phone, StringBuffer remarkBuf);
	
	/**
	 * 检查用户是否允许冲值。
	 * @param userName 用户帐号
	 * @return 如果可以冲值，返回帐号ID；帐号不存在返回-1，余额过多返回-2.
	 */
	public int checkAvailability(String userName);
	
	/**
	 * 完成订单
	 * @param orderID 订单ID
	 * @param phone 手机号
	 * @param remarkBuf 返回商品描述字符串
	 * @return 如果订单完成成功，返回true，否则返回false。
	 */
	public boolean fulfilOrder(int orderID, String phone, StringBuffer remarkBuf);
	
	/**
	 * 取得这个渠道WAP下单的重定向地址。
	 */
	public String getWAPAddress();
}
