package com.pip.itimes.server.billing;

import java.util.Date;
import java.util.Random;

import org.apache.log4j.Logger;

import com.pip.itimes.server.auth.Const;
import com.pip.itimes.server.bean.Account;
import com.pip.itimes.server.bean.Fee;

/**
 * 手机钱包2元找回密码服务。
 */
public class UMPay_Product_021 implements ProductHandler {
	private static Logger log = Logger.getLogger(UMPay_Product_021.class);
	private Server server;
	
	public UMPay_Product_021(Server s) {
		server = s;
	}

	/**
	 * 得到产品ID，例如020#，020#*等等。
	 */
	public String getProductID() {
		return "021#";
	}
	
	/**
	 * 得到产品对应的订单类型。
	 */
	public String getOrderType() {
		return "umpay_021";
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
		
		// 创建订单
		Fee fee = server.newFee(a.getId(), 0, getOrderType());
        if (fee == null) {
            return -1;
        } else {
    		String retRemark = Const.UNIPAY_REMARK2_1.replace("%account%", a.getUserName());
    		retRemark = retRemark.replace("%password%", a.getPassword());
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

        // 完成订单，修改帐户余额
        Account a = server.findAccount(fee.getAccountId());
        if (a != null) {
        	// 随机生成一个新密码
        	Random rand = new Random();
        	String newPass = "";
        	for (int i = 0; i < 6; i++) {
        		newPass += rand.nextInt(10);
        	}
        	a.setiMoney(0);
        	a.setPassword(newPass);
        	
        	// 如果玩家是3G注册用户，且未激活，则激活之
        	if (!a.getValid() && "注册".equals(a.getCause())) {
        		a.setValid(true);
        		a.setCause("注册成功");
        	}
        	
        	fee.setCharged(true);
			fee.setFinishTime(new Date());
			if (!server.updateAccount(a, fee)) {
				return false;
			}
			log.info("AccountID[" + a.getId() + "]FeeID[" + fee.getId() + "]Password[" + newPass + "]");
			
    		String retRemark = Const.UNIPAY_REMARK2_1.replace("%account%", a.getUserName());
    		retRemark = retRemark.replace("%password%", a.getPassword());
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
