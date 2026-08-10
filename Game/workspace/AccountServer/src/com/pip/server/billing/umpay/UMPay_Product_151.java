package com.pip.server.billing.umpay;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.pip.server.account.bean.Account;
import com.pip.server.account.bean.Fee;
import com.pip.server.account.util.Const;
import com.pip.server.billing.Server;


/**
 * 手机钱包包月产品。
 */
public class UMPay_Product_151 implements ProductHandler {
	private static Logger log = Logger.getLogger(UMPay_Product_151.class);
	private Server server;
	
	public UMPay_Product_151(Server s) {
		server = s;
	}

	/**
	 * 得到产品ID，例如020#，020#*等等。
	 */
	public String getProductID() {
		return "555701#";
	}
	
	/**
	 * 得到产品对应的订单类型。
	 */
	public String getOrderType() {
		return "sub_";
	}
	
	/**
	 * 得到产品付费金额（分）。
	 */
	public int getPayAmount() {
		return 1500;
	}

	/**
	 * 根据用户请求生成订单。
	 * @param productID 用户短信内容，已经去掉了前面的产品ID
	 * @param phone 手机号
	 * @param remarkBuf 返回商品描述字符串
	 * @return 如果下单成功，返回订单号；帐号不存在返回-1，余额过多返回-2.
	 */
	public int placeOrder(String productID, String phone, StringBuffer remarkBuf) {
		// 查找帐号
		Account a;
		if (productID.startsWith("@")) {
			a = server.findAccount(Integer.parseInt(productID.substring(1)));
		} else {
			a = server.findAccountByName(productID);
		}
		if (a == null) {
			return -1;
		}
		
		// 如果已经包月了，则不允许订购
		if (server.hasPurchased(a.getName(), 1)) {
			return -2;
		}
		
		// 检查：一个手机号只能为1个帐号订购包月
		if (server.findAccountBySubscribePhone(phone, 1) != null) {
			return -2;
		}

		// 创建订单
		Fee fee = server.newFee(a.getName(), 0, getOrderType() + phone);
        if (fee == null) {
            return -1;
        } else {
        	String retRemark = Const.UNIPAY_REMARK151.replace("%account%", a.getName());
    		remarkBuf.setLength(0);
    		remarkBuf.append(retRemark);
    		return fee.getId();
        }
	}
	
	/**
	 * 检查用户是否允许冲值。
	 * @param userName 用户帐号
	 * @return 如果可以冲值，返回帐号ID；帐号不存在返回-1，余额过多返回-2.
	 */
	public int checkAvailability(String userName) {
		Account acc = server.findAccountByName(userName);
		if (acc == null) {
			return -1;
		}
		
		// 如果已经包月了，则不允许订购
		if (server.hasPurchased(acc.getName(), 1)) {
			return -2;
		}
		return acc.getId();
	}
	
	/**
	 * 完成订单
	 * @param orderID 订单ID
	 * @param phone 手机号
	 * @param remarkBuf 返回商品描述字符串
	 * @return 如果订单完成成功，返回true，否则返回false。
	 */
	public boolean fulfilOrder(int orderID, String phone, StringBuffer remarkBuf) {
		// 检查手机号是否已经绑定了帐号了
		Account a = server.findAccountBySubscribePhone(phone, 1);
		if (a != null) {
			return false;
		}
        
        // 检查账单是否存在或有效
		Fee fee = server.findLatestFee(getOrderType() + phone);
        if (fee == null || fee.isCharged()) {
        	return false;
        }

        // 账单有效，完成订单，修改包月状态
        a = server.findAccount(fee.getAccountId());
        if (a == null) {
        	return false;
        }
		log.info("AccountID[" + a.getId() + "]Subscribe[" + phone + "]");
		if (!server.purchaseProduct(fee.getId(), 1, phone)) {
			return false;
		}

		String retRemark = Const.UNIPAY_REMARK151.replace("%account%", a.getName());
		remarkBuf.setLength(0);
		remarkBuf.append(retRemark);
		return true;
	}
	
	/**
	 * 取得这个渠道WAP下单的重定向地址。
	 */
	public String getWAPAddress() {
		return Const.UNIPAY_ORDER_URL_151;
	}
}
