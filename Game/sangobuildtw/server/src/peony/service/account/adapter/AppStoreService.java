package peony.service.account.adapter;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.mortbay.util.ajax.JSON;

import com.pip.net.message.gameaccount.AddBalanceMessage;
import com.pip.net.message.gameaccount.AddBalanceOkMessage;

import peony.common.AsyncCall;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.changed.ChangedItem;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.account.Account;
import peony.service.account.AccountAsyncCall;
import sun.misc.BASE64Encoder;

public class AppStoreService implements Service {
	public static class AppStoreProduct {
		public String productID;		// 产品ID
		public String productName;		// 产品标题
		public int price;				// 价格（美分）
		public int imoney;				// 对应i币（1/100i）
		
		public AppStoreProduct(String id, String name, int price, int imoney) {
			this.productID = id;
			this.productName = name;
			this.price = price;
			this.imoney = imoney;
			
			chargeProducts.put(this.productID, this);
			chargeProductList.add(this);
		}
	}
	
	private static Logger log = Logger.getLogger(AppStoreService.class);
	private static Map<String, AppStoreProduct> chargeProducts = new HashMap<String, AppStoreProduct>();
	private static List<AppStoreProduct> chargeProductList = new ArrayList<AppStoreProduct>();
	static {
		new AppStoreProduct("yuanbao_30", "300元寶", 599, 300 * 36 * 100);
		new AppStoreProduct("yuanbao_50", "500元寶", 899, 500 * 36 * 100);
		new AppStoreProduct("yuanbao_100", "1000元寶", 1599, 1000 * 36 * 100);
	}
	private ExecutorService executor = new ThreadPoolExecutor(1, 10, 60L,
			TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	private static BASE64Encoder base64Encoder = new BASE64Encoder();
	
	public AppStoreService() {
	}

	public void startup() {
	}
	
	public void shutdown() {
	}
	
	public void checkReceipt(ClientSession session, int playerID, int accountID, byte[] receipt) {
		executor.execute(new AppStoreChargeCall(session, playerID, accountID, receipt));
	}
	
	public void listProduct(ClientSession session) {
		Packet packet = new Packet(OpCode.APP_STORE_LIST_PRODUCT_SERVER);
		packet.put(chargeProductList.size());
		for (int i = 0; i < chargeProductList.size(); i++) {
			AppStoreProduct product = chargeProductList.get(i);
			packet.putString(product.productID);
			packet.putString(product.productName);
			packet.putInt(product.price);
			packet.putInt(product.imoney);
		}
		session.send(packet);
	}
	
	private static class AppStoreChargeCall extends AccountAsyncCall {
		private int playerID;
		private int accountID;
		private byte[] receipt;
		private int amount;
		
		public AppStoreChargeCall(ClientSession session, int pid, int aid, byte[] rcpt) {
			super(session);
			this.playerID = pid;
			this.accountID = aid;
			this.receipt = rcpt;
		}
		
		public void callFinish() throws Exception {
			if (success) {
				// 记录日志
				StringBuilder sb = new StringBuilder(100);
				sb.append("[APP_STORE_CHARGE]ID[").append(playerID).append("]ACC[").append(accountID).append("]RECEIPT[");
				LogUtil.getBinaryString(sb, receipt);
				sb.append("]AMOUNT[").append(amount).append("]");
				log.info(sb.toString());
				
				// 同步余额
				AddBalanceOkMessage msg = (AddBalanceOkMessage)message;
				Account a = (Account)session.getIdentity();
				if (a != null) {
					int oldIMoney = a.getIMoney();
					a.setIMoney(msg.getValue() + oldIMoney);
					Player p = (Player)session.getClient();
					if (p != null) {
						p.addIntPropertyChangedItem(ChangedItem.IMONEY, a.getIMoney() / 100, true, true);
						p.message(-1, MessageFormat.format("您通過AppStore購買的{0}元寶已到帳,請核對您的帳戶余額.", (amount / 3600)), -1, -1);
					}
				}
			} else {
				reportError("購買時發生系統錯誤,請聯系GM處理.");
			}
		}
		
		public void run() {
			int retry = 1;
			while (true) {
				// 记录日志
				StringBuilder sb = new StringBuilder(100);
				sb.append("[APP_STORE_CHARGETRY]ID[").append(playerID).append("]ACC[").append(accountID).append("]RECEIPT[");
				String base64Receipt = base64Encoder.encode(receipt);
				sb.append(base64Receipt);
				sb.append("]RETRY[").append(retry).append("]");
				log.info(sb.toString());

				// PostMethod method = new PostMethod("https://buy.itunes.apple.com/verifyReceipt");
				PostMethod method = new PostMethod("https://sandbox.itunes.apple.com/verifyReceipt");
				try {
					// 向AppStore发起验证请求
					Map map = new HashMap();
					map.put("receipt-data", base64Receipt);
					StringRequestEntity entity = new StringRequestEntity(JSON.toString(map), "application/json", "utf-8");
		            method.setRequestEntity(entity);
		            
		            HttpClient httpclient = new HttpClient();
	                httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
	                httpclient.getParams().setSoTimeout(30000);
	                int code = httpclient.executeMethod(method);
	                
	                // 检查结果
	                if (code != 200) {
	                	reportError("AppStore訂單信息錯誤,充值失敗");
	                	return;
	                }
	                String str = method.getResponseBodyAsString();

	                // 记录日志
	                sb.setLength(0);
	                sb.append("[APP_STORE_RESPONSE]ID[").append(playerID).append("]ACC[").append(accountID).append("]RESULT[");
					sb.append(LogUtil.filter(str));
					sb.append("]");
					log.info(sb.toString());
					
	                map = (Map)JSON.parse(str);
	                long result = ((Long)map.get("status")).longValue();
	                if (result == 0) {
	                	// 成功，为用户添加元宝
	                	map = (Map)map.get("receipt");
	                	String pid = (String)map.get("product_id");
	                	if (chargeProducts.containsKey(pid)) {
	                		amount = chargeProducts.get(pid).imoney;
	                		AddBalanceMessage msg = new AddBalanceMessage(accountID, amount, "APP_STORE_CHARGE");
		                	Server.server.getServiceRegistry().getAccountService().sendAndRegister(msg, this);
	                	} else {
	    					log.info("[APP_STORE_ERROR]PRODUCT[" + pid + "]");
	                		reportError("AppStore訂單信息錯誤,充值失敗");
	                	}
	                } else {
	                	reportError("AppStore訂單信息錯誤,充值失敗");
	                }
	                return;
				} catch (IOException ex) {
					// 超时错误，重试
					retry++;
	            } catch (Throwable ex) {
	            	// 其他系统错误，放弃
	            	log.error(ex, ex);
	            	reportError("AppStore接口不可用,請聯系客服解決");
	            	return;
	            } finally {
	                method.releaseConnection();
	            }
			}
		}
		
		/*
		 * 向客户端报告错误信息。
		 */
		private void reportError(String msg) {
			Player p = (Player)session.getClient();
			if (p != null) {
				p.message(-1, msg, -1, -1);
			}
		}
	}
}
