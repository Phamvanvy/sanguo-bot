package com.pip.server.billing.appstore;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.mortbay.util.ajax.JSON;

import com.pip.server.account.bean.Account;
import com.pip.server.account.bean.Fee;
import com.pip.server.billing.Server;

/**
 * 苹果appstore下单工具。这个工具增加了由于网络情况造成下单失败的处理。如果由于网络故障造成下单失败，那么这个订单会被保存在内存里，
 * 每过5分钟重下单一次，最多重试12次。
 * @author lighthu
 */
public class AppStoreOrderManager extends Thread {
	private static Logger log = Logger.getLogger(AppStoreOrderManager.class);
	
	public static class ChargeRequest {
		public String bid;
		public int gameCode;
		public String channel;
		public Account acc;
		public String receipt;
		public int retryTimes;
		public long lastTryTime;
	}
	
	public static List<ChargeRequest> queue = new ArrayList<ChargeRequest>();
	private static Order_AppStoreDAO dataDAO = new Order_AppStoreDAO(); 
	public static Server server;
	
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
				ConstAppStore.checkLoadConfig();
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
				int ret = placeOrder(req.bid, req.gameCode, req.channel, req.acc, req.receipt);
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
	 * 尝试下一个appstore支付订单，如果appstore平台暂时不可能，这个订单会被保存在缓存里，并在接下来的一段时间内反复重试。
	 * @param bid app bundle id
	 * @param gameCode 游戏代码 
	 * @param channel 用户渠道
	 * @param acc 账号
	 * @param receipt 订单收据
	 * @return >0 - 下单成功，返回金额(1/100i)，-2 - 平台错误，下单暂时不成功，押后重试，-1 - 订单信息验证失败，下单失败
	 */
	public static int tryPlaceOrder(String bid, int gameCode, String channel, Account acc, String receipt) {
		
		// 如果有同receipt订单已经在等待发送队列中，取消上一个请求
		synchronized (queue) {
			for (int i = 0; i < queue.size(); i++) {
				if (queue.get(i).receipt.equals(receipt)) {
					queue.remove(i);
					break;
				}
			}
		}
		
		// 尝试下单，如果下单返回平台错误，则把订单放到等待队列中
		int ret = placeOrder(bid, gameCode, channel, acc, receipt);
		if (ret == 1) {
			ChargeRequest request = new ChargeRequest();
			request.bid = bid;
			request.gameCode = gameCode;
			request.channel = channel;
			request.acc = acc;
			request.receipt = receipt;
			request.retryTimes = 0;
			request.lastTryTime = System.currentTimeMillis();
			queue.add(request);
		}
		return ret;
	}
	
	/**
	 * 尝试下一个appstore支付订单。
	 * @param bid app bundle id
	 * @param gameCode 游戏代码 
	 * @param channel 用户渠道
	 * @param acc 账号
	 * @param receipt 订单收据
	 * @return >0 - 下单成功，返回金额(1/100i)，-2 - 平台错误，下单暂时不成功，押后重试，-1 - 订单信息验证失败，下单失败
	 */
	private static int placeOrder(String bid, int gameCode, String channel, Account acc, String receipt) {
		// 到appstore验证
        boolean isTest = ConstAppStore.isAppTest(bid);
        log.info("[appstore_order]BID[" + bid + "]RECEIPT[" + receipt + "]");
        String appStoreURL = isTest ? ConstAppStore.VERIFY_URL_TEST : ConstAppStore.VERIFY_URL;
        PostMethod method;
        if (ConstAppStore.proxyURL == null) {
        	method = new PostMethod(appStoreURL);
        } else {
        	String encodeURL;
        	try {
        		encodeURL = URLEncoder.encode(appStoreURL, "UTF-8");
        	} catch (Exception e) {
        		encodeURL = appStoreURL;
        	}
        	method = new PostMethod(ConstAppStore.proxyURL + "?url=" + encodeURL);
        }
		try {
			// 向AppStore发起验证请求
			Map map = new HashMap();
			map.put("receipt-data", receipt);
			StringRequestEntity entity = new StringRequestEntity(JSON.toString(map), "application/json", "utf-8");
            method.setRequestEntity(entity);
            
            HttpClient httpclient = new HttpClient();
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
            httpclient.getParams().setSoTimeout(30000);
            int code = httpclient.executeMethod(method);
            
            // 检查结果
            if (code != 200) {
            	log.info("[appstore_order] code=" + code);
            	return -1;
            }
            String str = method.getResponseBodyAsString();

            // 记录日志
            log.info("[appstore_order] result=" + str);
			
            map = (Map)JSON.parse(str);
            long result = ((Long)map.get("status")).longValue();
            if (result == 0) {
            	// 成功，为用户添加元宝
            	map = (Map)map.get("receipt");
            	String pid = (String)map.get("product_id");
            	int quantity = Integer.parseInt((String)map.get("quantity"));
            	bid = (String)map.get("bid");
            	String tid = (String)map.get("transaction_id");
            	if (dataDAO.getByOrderID(tid) != null) {
            		log.info("[appstore_order] 订单已存在");
            		return -1;
            	}
            	try {
            		return charge(acc, gameCode, channel, bid, pid, quantity, tid);
            	} catch (Exception e) {
            		log.info("[appstore_order] " + e.getMessage());
                    return -1;
            	}
            } else {
            	return -1;
            }
		} catch (IOException ex) {
			// 超时错误，重试
			log.error(ex, ex);
            return -2;
        } catch (Throwable ex) {
        	// 其他系统错误，放弃
        	log.error(ex, ex);
        	return -2;
        } finally {
            method.releaseConnection();
        }
	}
	
	/*
	 * 订单验证成功，尝试为这个订单在数据库中生成订单记录，并完成充值。如果订单已经存在并成功，抛出异常。
	 * @return 返回充值金额（单位1/100i）
	 */
	private static int charge(Account acc, int gameCode, String channel, String bid, String productID, int quantity, String tid) throws Exception {
		ConstAppStore.AppStoreProduct product = ConstAppStore.findProduct(bid, productID);
		if (product == null) {
			throw new Exception("商品无效");
		}

		// 数据库中创建订单
		Order_AppStore order = new Order_AppStore();
		order.setAccountID(acc.getId());
		order.setUserName(acc.getName());
		order.setCreateTime(new Date());
		order.setFinishTime(new Date());
		order.setMoney(product.price * quantity);
		order.setStatus(1);
		order.setGameCode(gameCode);
		order.setImoney(product.imoney * quantity);
		order.setOrderID(tid);
		order.setChannel(channel);
		order.setProductID(productID);
		order.setAppID(bid);
		dataDAO.create(order);
		
		order.setFeeID(addIMoney(order));
		dataDAO.update(order);
		
		log.info("[appstore_order]ACC[" + acc.getId() + "]BID[" + bid + "]TID[" + tid + "]PID[" + 
				productID + "] add i money success.");
	    
	    return order.getImoney() * 100;
	}
	
	/**
	 * 完成一个订单，为用户增加i币。
	 * @param order
	 * @throws Exception
	 */
    private static int addIMoney(Order_AppStore order) throws Exception {
        // 在认证服务器创建订单
    	String channel = "ITUNES_" + order.getMoney();
        Fee fee = server.newFee(order.getUserName(), order.getImoney() * 100, channel);
        
        // 完成订单，修改帐户余额
        if (!server.fulfillOrder(fee.getId())) {
            throw new Exception();
        }
        
        // 添加积分
        server.addCreditByMoney(order.getAccountID(), order.getMoney());
        
        return fee.getId();
    }
}
