package com.pip.server.billing.yeepay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import com.pip.server.account.bean.Account;

/**
 * 易宝下单工具。这个工具增加了由于网络情况造成下单失败的处理。如果由于网络故障造成下单失败，那么这个订单会被保存在内存里，
 * 每过5分钟重下单一次，最多重试12次。
 * @author lighthu
 */
public class YeepayOrderManager extends Thread {
	private static Logger log = Logger.getLogger(YeepayOrderManager.class);
	
	public static class ChargeRequest {
		public String notifyURL;
		public int gameCode;
		public String channel;
		public Account acc;
		public String orderId;
		public int amount;
		public String cardNo;
		public String cardPass;
		public String cardType;
		public int retryTimes;
		public long lastTryTime;
	}
	
	public static List<ChargeRequest> queue = new ArrayList<ChargeRequest>();
	
	@Override
	public void run() {
		List<ChargeRequest> retryReqs = new ArrayList<ChargeRequest>();
		while (true) {
			try {
				Thread.sleep(10000L);
			} catch (Exception e) {
			}
			
			// 检查是否需要重载配置
			try {
				ConstYeepay.checkLoadConfig();
			} catch (Exception e) {
				log.error(e, e);
			}
			
			// 取得所有需要重试的请求
			synchronized (queue) {
				if (queue.size() == 0) {
					continue;
				}
				for (int i = 0; i < queue.size(); i++) {
					ChargeRequest req = queue.get(i);
					if (req.lastTryTime < System.currentTimeMillis() - 300000L) {
						// 5分钟重试一次
						retryReqs.add(req);
						queue.remove(i);
						i--;
					}
				}
			}
			
			// 一个个请求重试，如果请求成功或者重试次数过多，就不再加回等待队列了，否则加回等待队列。
			for (int i = 0; i < retryReqs.size(); i++) {
				ChargeRequest req = retryReqs.get(i);
				req.lastTryTime = System.currentTimeMillis();
				int ret = placeOrder(req.notifyURL, req.gameCode, req.channel, req.acc, req.orderId, req.amount, req.cardNo, req.cardPass, req.cardType);
				if (ret == 1 && req.retryTimes < 12) {
					req.retryTimes++;
					synchronized (queue) {
						queue.add(req);
					}
				}
			}
			
			retryReqs.clear();
		}
	}
	
	/**
	 * 尝试下一个易宝支付订单，如果易宝平台暂时不可能，这个订单会被保存在缓存里，并在接下来的一段时间内反复重试。
	 * @param notifyURL 通知地址
	 * @param gameCode 游戏代码 
	 * @param channel 用户渠道
	 * @param acc 账号
	 * @param orderId 订单ID
 	 * @param amount 订单金额（分）
	 * @param cardNo 卡号 
	 * @param cardPass 密码 
	 * @param cardType 卡类型
	 * @return 0 - 下单成功，1 - 平台错误，下单暂时不成功，押后重试，-1 - 订单信息验证失败，下单失败
	 */
	public static int tryPlaceOrder(String notifyURL, int gameCode, String channel, Account acc, String orderId, 
			int amount, String cardNo, String cardPass, String cardType) {
		
		// 如果有同卡号同密码的订单已经在等待发送队列中，取消上一个请求
		synchronized (queue) {
			for (int i = 0; i < queue.size(); i++) {
				if (queue.get(i).cardNo.equals(cardNo) && queue.get(i).cardPass.equals(cardPass)) {
					queue.remove(i);
					break;
				}
			}
		}
		
		// 尝试下单，如果下单返回平台错误，则把订单放到等待队列中
		int ret = placeOrder(notifyURL, gameCode, channel, acc, orderId, amount, cardNo, cardPass, cardType);
		if (ret == 1) {
			ChargeRequest request = new ChargeRequest();
			request.notifyURL = notifyURL;
			request.gameCode = gameCode;
			request.channel = channel;
			request.acc = acc;
			request.orderId = orderId;
			request.amount = amount;
			request.cardNo = cardNo;
			request.cardPass = cardPass;
			request.cardType = cardType;
			request.retryTimes = 0;
			request.lastTryTime = System.currentTimeMillis();
			queue.add(request);
		}
		return ret;
	}
	
	/**
	 * 尝试下一个易宝支付订单。
	 * @param notifyURL 通知地址
	 * @param gameCode 游戏代码 
	 * @param channel 用户渠道
	 * @param acc 账号
	 * @param orderId 订单ID
 	 * @param amount 订单金额（分）
	 * @param cardNo 卡号 
	 * @param cardPass 密码 
	 * @param cardType 卡类型
	 * @return 0 - 下单成功，1 - 平台错误，-1 - 订单信息验证失败，下单失败
	 */
	private static int placeOrder(String notifyURL, int gameCode, String channel, Account acc, String orderId, 
			int amount, String cardNo, String cardPass, String cardType) {
        PostMethod method = null;
        try {
        	method = new PostMethod(ConstYeepay.ORDER_URL);
            method.addRequestHeader( "Connection", "close");
            method.getParams().setContentCharset("GBK");
            String merchantID = ConstYeepay.getMerchantID(gameCode, channel, acc);
            String merchantKey = ConstYeepay.getMerchantKey(merchantID);
            HttpClient httpclient = new HttpClient();
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
            httpclient.getParams().setSoTimeout(30000);
            method.addParameter("p0_Cmd", ConstYeepay.CMD);
            method.addParameter("p1_MerId", merchantID);
            method.addParameter("p2_Order", orderId);
            method.addParameter("p3_Amt", (amount / 100) + ".00");
            method.addParameter("p8_Url", notifyURL);
            method.addParameter("pa_MP", "accountid:" + acc.getId());
            method.addParameter("pa7_cardNo", cardNo);
            method.addParameter("pa8_cardPwd", cardPass);
            method.addParameter("pd_FrpId", cardType);
            method.addParameter("pa0_Mode", ConstYeepay.PAY_MODE);
            method.addParameter("pr_NeedResponse", "1");
            method.addParameter("hmac", DigestUtil.getHmac(new String[] {
                ConstYeepay.CMD, merchantID, orderId, (amount / 100) + ".00",
                notifyURL, "accountid:" + acc.getId(), cardNo, cardPass,
                cardType, ConstYeepay.PAY_MODE, "1"
            }, merchantKey));
            int code = httpclient.executeMethod(method);
            if (code == 200) {
                // 记录提交成功
                ConstYeepay.addSubmitRecord(cardNo, cardPass);
                
                // 返回格式类似于Properties文件
                String result = method.getResponseBodyAsString();
                result = result.replace('\r', ' ');
                String[] lines = result.split("\n");
                result = result.replace('\n', ' ');
                log.info("[yeepay_result] " + result);
                HashMap<String, String> resultProps = new HashMap<String, String>();
                for (int i = 0; i < lines.length; i++) {
                    int pos = lines[i].indexOf('=');
                    if (pos == -1) {
                        continue;
                    }
                    resultProps.put(lines[i].substring(0, pos).trim(), lines[i].substring(pos + 1).trim());
                }
                
                // 处理返回结果
                if ("1".equals(resultProps.get("r1_Code"))) {
                	return 0;
                } else {
                	return -1;
                }
            } else {
                log.info("[yeepay_result] code=" + code);
                ConstYeepay.recordSubmitFail();
                return 1;
            }
        } catch (Exception ex1) {
        	ConstYeepay.recordSubmitFail();
            log.error(ex1, ex1);
            return 1;
        } finally {
        	if (method != null) {
        		try {
        			method.releaseConnection();
        		} catch (Exception ex) {
        		}
        	}
        }
	}
}
