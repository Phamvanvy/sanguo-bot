package com.pip.server.billing.umpay;

import org.apache.log4j.Logger;

import com.pip.server.account.bean.Account;
import com.pip.server.account.bean.Fee;
import com.pip.server.account.util.Const;
import com.pip.server.billing.Server;

/**
 * 手机钱包普通8元续费产品。
 */
public class UMPay_Product_080 implements ProductHandler {
	private static Logger log = Logger.getLogger(UMPay_Product_080.class);
	private Server server;
	
	public UMPay_Product_080(Server s) {
		server = s;
	}

	/**
	 * 得到产品ID，例如020#，020#*等等。
	 */
	public String getProductID() {
		return "080#";
	}
	
	/**
	 * 得到产品对应的订单类型。
	 */
	public String getOrderType() {
		return "umpay_080";
	}

	/**
	 * 得到产品付费金额（分）。
	 */
	public int getPayAmount() {
		return 800;
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
		
//	    // 检查绑定手机号
//        UMPayData umpayData = server.umpayDataDAO.getOrCreate(a.getId(), a.getName());
//        if (!umpayData.isPhoneBounded(phone)) {
//            return -2;
//        }

		// 创建订单
		Fee fee = server.newFee(a.getName(), Const.UNIPAY_FEE_080, getOrderType());
        if (fee == null) {
            return -1;
        } else {
    		String retRemark = Const.UNIPAY_REMARK8.replace("%account%", a.getName());
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
			// 单次
			log.info("AccountID[" + a.getId() + "]FeeID[" + fee.getId() + "]Fee["
					+ fee.getAmount() + "]iMoney[" + a.getCbalance() + "]");
			if (!server.fulfillOrder2(fee.getId())) {
				return false;
			}
			
			String retRemark = Const.UNIPAY_REMARK8.replace("%account%", a.getName());
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
		return Const.UNIPAY_ORDER_URL_8;
	}
}
