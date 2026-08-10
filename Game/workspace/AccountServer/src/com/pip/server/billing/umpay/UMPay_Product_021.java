package com.pip.server.billing.umpay;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.pip.server.account.bean.Account;
import com.pip.server.account.bean.Fee;
import com.pip.server.account.util.Const;
import com.pip.server.billing.Server;

/**
 * 手机钱包2元找回密码服务。
 */
public class UMPay_Product_021 implements ProductHandler {
	private static Logger log = Logger.getLogger(UMPay_Product_021.class);
	private Server server;
	
	public static String URL_CFGSERVER = "http://211.100.18.94:8383/MainServer/SMSReceive"; //财富港用户注册成功后的通知地址
	private static Hashtable HT_SEQ_INFO = new Hashtable();//存放上行短信中的SEQID,付费成功后通知同步财富港服务器
	private static int CUR_SEQ_PG = 0; //单机游戏计费账单序列号游标
	private static Hashtable SEQ_RECID_PG = new Hashtable();; //临时存放单机游戏计费账单序列号和手机号的对应关系
	
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
		
		//增加单机版计费处理
		if(productID.startsWith("HZS")){
	        log.info("GamePay: " + phone + " : " + productID);
	        // 指令代码“HZS”+游戏代码3位+收费类型2位+渠道代码3位  发送到106691608781200
	        String gameCode = null;
	        String payType = null;
	        String spNumber = null;
	        try
	        {
	           gameCode = productID.substring(3, 6);
	           payType = productID.substring(6, 8);
	           spNumber = productID.substring(8, 11);
	        }catch (Exception e)
	        {
	        	gameCode = "";
	            payType = "";
	            spNumber = "";
	        }
	        GamePayDAO gpDAO = new GamePayDAO();
	        GamePay gp = gpDAO.create(phone, productID, gameCode, payType, spNumber);
	        if(gp==null){	        	
	        	return -1;
	        }
	        CUR_SEQ_PG++;
	        SEQ_RECID_PG.put(CUR_SEQ_PG,phone);
	        String retRemark = "您已经购买成功，可以继续游戏，祝您游戏愉快!";
	    	remarkBuf.setLength(0);
	    	remarkBuf.append(retRemark);
	    	return CUR_SEQ_PG;
		} 
		
		Account a = null;	
		/**手机帐号注册或密码重置功能**/
		/**@todo 修改注册逻辑，截取SEQID字段，密码由系统生成6位随机数，成功后通知财富港服务**/
		try {
			int ret = server.requestPhoneReg(new String[]{"name","password","type","cbalance"},
					new String[]{phone,phone.substring(phone.length()-4),"2",""+Const.UNIPAY_FEE_021_4REG});
			if(ret!=1 && ret!=2){
				return -1;
			}
			//查找帐号	
			a = server.findAccountByName(phone);
			if (a == null) {
				return -1;
			}			
	        //设置用户可用状态为未激活Errors.ACCOUNT_FREEZE
//			取消用户状态冻结及激活流程
//			server.requestAccountService("status",new String[]{"admin","pass","id","value"},new String[]{"cfg","cfg",""+a.getId(),""+Errors.ACCOUNT_FREEZE});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return -1;
		}
		
		// 检查手机号
		if (phone.length() > 11) {
			phone = phone.substring(phone.length() - 11);
		}
		
		// 创建订单
		Fee fee = server.newFee(a.getName(), 0, getOrderType());
        if (fee == null) {
            return -1;
        } else {
        	
        	if(productID!=null && productID.length() > 15 ){//取SEQID
        		HT_SEQ_INFO.put(a.getName(),productID);
        	}
        	
        	String retRemark = Const.UNIPAY_REMARK2_1.replace("%account%", a.getName());
    		retRemark = retRemark.replace("%password%", a.getPasswordDec());
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
		//增加单机版计费处理
        String mobile = null;
        if((mobile=(String)SEQ_RECID_PG.get(orderID))!=null && mobile.equals(phone)){
        	String retRemark = "您已经购买成功，可以继续游戏，祝您游戏愉快!";
    		remarkBuf.setLength(0);
    		remarkBuf.append(retRemark);
    		SEQ_RECID_PG.remove(orderID);
			return true;
        }
        
		Fee fee = server.findFee(orderID);

        // 检查账单是否存在或有效
        if (fee == null || fee.isCharged()) {
        	return false;
        }
        
        // 完成订单，修改帐户余额
        Account a = server.findAccount(fee.getAccountId());
        if (a != null) {
        	
        	//取消用户状态冻结及激活流程
//        	boolean active = false;
//        	try {
//        		//用户状态激活
//        		active = server.requestAccountService("status",new String[]{"admin","pass","id","value"},new String[]{"cfg","cfg",""+a.getId(),"1"});
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				log.info("ActiveAcountError:AccountID[" + a.getId() + "]FeeID[" + fee.getId());
//			}
//        	if (!active) {
//        		return false;
//        	}
        	
        	//不影响关键流程，因此不处理错误情况
        	server.fulfillOrder2(fee.getId());
        	
			String newPass = server.resetPassword(a.getName(),0);
			if (newPass == null) {
				return false;
			}
			String tempSEQID =(String)HT_SEQ_INFO.get(a.getName());
			if(tempSEQID!=null){
				
				//通知财富港服务器
				StringBuffer notifyURL = new StringBuffer(URL_CFGSERVER);
				notifyURL.append("?action=reg&mobile=").append(a.getName()).append("&sid=").append(tempSEQID).append("&pwd=").append(newPass);
				
				HT_SEQ_INFO.remove(a.getName());
				try {
					String ret = requestURL(notifyURL.toString());
					log.info("ToCFGOk["+ret+"]:"+notifyURL);
				} catch (Exception e) {
					log.info("ToCFGErr:"+notifyURL);
					e.printStackTrace();
				}
			}
			log.info("AccountID[" + a.getId() + "]FeeID[" + fee.getId() + "]Password[" + newPass + "]");
			
    		String retRemark = Const.UNIPAY_REMARK2_1.replace("%account%", a.getName());
    		retRemark = retRemark.replace("%password%", newPass);
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

	
	private static String requestURL(String reqUrl)
			throws Exception {
		HttpURLConnection connection = null;
		InputStream is = null;
		try {
			// 建立连接
			URL url = new URL(reqUrl);
			connection = (HttpURLConnection) url.openConnection();
			int code = connection.getResponseCode();
			if (code != 200) {
				throw new Exception("Wrong response code!");
			}
			// 读取结果
			is = connection.getInputStream();
			return new BufferedReader(new InputStreamReader(is, "UTF-8"))
					.readLine();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
			}
			try {
				if (connection != null) {
					connection.disconnect();
				}
			} catch (Exception e) {
			}
		}
	}
}
