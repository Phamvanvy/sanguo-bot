package com.pip.server.billing.umpay;

import java.util.Date;
import java.util.Random;

import org.apache.log4j.Logger;

import com.pip.server.account.bean.Account;
import com.pip.server.account.bean.Fee;
import com.pip.server.account.util.Const;
import com.pip.server.billing.Server;

/**
 * 手机钱包2元修改绑定手机号服务。
 */
public class UMPay_Product_021_2 implements ProductHandler {
	private static Logger log = Logger.getLogger(UMPay_Product_021_2.class);
	private Server server;
	
	public UMPay_Product_021_2(Server s) {
		server = s;
	}

	/**
	 * 得到产品ID，例如020#，020#*等等。
	 */
	public String getProductID() {
		return "021#*";
	}
	
	/**
	 * 得到产品对应的订单类型。
	 */
	public String getOrderType() {
		return "pn_";
	}
	
	/**
	 * 得到产品付费金额（分）。
	 */
	public int getPayAmount() {
		return 200;
	}

	/**
	 * 根据用户请求生成订单。
	 * @param productID 用户短信内容，已经去掉了前面的产品ID
	 * @param phone 手机号
	 * @param remarkBuf 返回商品描述字符串
	 * @return 如果下单成功，返回订单号；帐号不存在返回-1，余额过多返回-2.
	 */
	public int placeOrder(String productID, String phone, StringBuffer remarkBuf) {
		// 短信格式为：021#*帐号名,新手机号
		String[] secs = productID.split(",");
		if (secs.length != 2) {
			return -1;
		}
		// 查找帐号
		Account a = server.findAccountByName(secs[0]);
		if (a == null) {
			return -1;
		}
		
		// 检查手机号
		if (phone.length() > 11) {
			phone = phone.substring(phone.length() - 11);
		}
		String accPhone = a.getPhone();
		if (accPhone != null && accPhone.length() > 11) {
			accPhone = accPhone.substring(accPhone.length() - 11);
		}
		if (!phone.equals(accPhone)) {
			return -1;
		}
		
		// 检查新手机号
		if (secs[1].length() != 11) {
			return -1;
		}
		
		// 创建订单
		Fee fee = server.newFee(a.getName(), 0, getOrderType() + secs[1]);
        if (fee == null) {
            return -1;
        } else {
    		String retRemark = Const.UNIPAY_REMARK2_3.replace("%account%", a.getName());
    		retRemark = retRemark.replace("%phone%", secs[1]);
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
        Fee fee = server.findFee(orderID);

        // 检查账单是否存在或有效
        if (fee == null || fee.isCharged()) {
        	return false;
        }

        // 完成订单，修改绑定手机号
        Account a = server.findAccount(fee.getAccountId());
        if (a != null) {
        	String newPhone = fee.getChannel().substring(getOrderType().length());
			if (!server.changePhone(fee.getId(), newPhone)) {
				return false;
			}
			log.info("AccountID[" + a.getId() + "]FeeID[" + fee.getId() + "]Phone[" + newPhone + "]");
			
    		String retRemark = Const.UNIPAY_REMARK2_3.replace("%account%", a.getName());
    		retRemark = retRemark.replace("%phone%", a.getPhone());
    		remarkBuf.setLength(0);
    		remarkBuf.append(retRemark);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 取得这个渠道WAP下单的重定向地址。
	 */
	public String getWAPAddress() {
		return Const.UNIPAY_ORDER_URL_21;
	}
}
