package peony.service.account.adapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.mortbay.util.ajax.JSON;

import com.pip.net.message.gameaccount.AddBalanceMessage;
import com.pip.net.message.gameaccount.AddBalanceOkMessage;

import peony.common.AsyncCall;
import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.changed.ChangedItem;
import peony.game.chinarun.ChinarunCall;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.account.Account;
import peony.service.account.AccountAsyncCall;
import sun.misc.BASE64Encoder;

public class AppStoreService implements Service {
	private static Logger log = Logger.getLogger(AppStoreService.class);
	private static BASE64Encoder base64Encoder = new BASE64Encoder();
	
	public AppStoreService() {
	}

	public void startup() {
	}
	
	public void shutdown() {
	}

	/**
	 * 新版充值接口，把信息传递到billing服务器进行验证。
	 */
	public void checkReceipt2(ClientSession session, Account acc, Player p, String bid, byte[] receipt, String clientID) {
		Server.server.getServiceRegistry().getDbService().schedule(
				new AppStoreCharge2Call(session, acc, p, 0, bid, receipt, clientID));
	}
	
	public void checkReceipt3(ClientSession session, Account acc, Player p, int serial, String bid, byte[] receipt, String clientID) {
		Server.server.getServiceRegistry().getDbService().schedule(
				new AppStoreCharge2Call(session, acc, p, serial, bid, receipt, clientID));
	}
	
	private static class AppStoreCharge2Call extends ClientSessionAsyncCall {
		protected Account acc;
		protected Player p;
		protected int serial;
		protected String bid;
		protected String receipt;
		protected String clientID;

		public AppStoreCharge2Call(ClientSession session, Account acc, Player p, int serial, String bid, byte[] receipt, String clientID) {
			super(session);
			this.acc = acc;
			this.p = p;
			this.bid = bid;
			this.clientID = clientID;
			this.receipt = base64Encoder.encode(receipt);
			this.serial = serial;
			
			log.info("[APPSTORE_CHARGE]" + LogUtil.getPlayerLogString(p) + "BID[" + bid + "]RECEIPT[" + receipt + "]TRY");
		}

		public void callFinish() throws Exception {
			if (serial > 0) {
				Packet pt = new Packet(OpCode.APP_STORE_CHARGE3_SERVER);
				pt.putInt(serial);
				session.send(pt);
			}
		}

		public void run() {
			PostMethod method = new PostMethod(Server.server.billingURL + "appstore_order");
			method.addRequestHeader("Connection", "close");
			method.setParameter("id", String.valueOf(acc.getId()));
			method.setParameter("gamecode", "6");
			method.setParameter("bid", bid);
			method.setParameter("receipt", receipt);
			method.setParameter("channel", acc.getChannel());
			method.setParameter("clientid", clientID);
			try {
				HttpClient httpclient = new HttpClient();
				httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
				httpclient.getParams().setSoTimeout(30000);
				int code = httpclient.executeMethod(method);
				if (code != 200) {
					log.info("[APPSTORE_CHARGE]CODE[" + code + "]");
					report("系统错误，请联系客服。");
					return;
				}
				String result = method.getResponseBodyAsString();
				BufferedReader br = new BufferedReader(new StringReader(result));
				String codeStr = br.readLine();
				String info = br.readLine();
				if ("0".equals(codeStr)) {
					// 成功
					log.info("[APPSTORE_CHARGE]IMONEY[" + info + "]OK");
				} else {
					// 失败
					report(info);
					log.info("[APPSTORE_CHARGE]" + info);
				}
			} catch (Exception ex) {
				log.error(ex, ex);
				report("系统错误，请联系客服。");
			} finally {
				method.releaseConnection();
			}
			addToClientSession();
		}
		
		/*
		 * 向客户端报告错误信息。
		 */
		private void report(String msg) {
			p.message(-1, msg, -1, -1);
		}
	}
	
	public void listProduct2(ClientSession session, int serial, String bid, String clientID) {
		Server.server.getServiceRegistry().getDbService().schedule(
				new AppStoreListProduct2Call(session, serial, bid, clientID));
	}
	
	private static class AppStoreListProduct2Call extends ClientSessionAsyncCall {
		protected int serial;
		protected String bid;
		protected String clientID;
		protected List<String[]> products;
		protected int limit;

		public AppStoreListProduct2Call(ClientSession session, int serial, String bid, String clientID) {
			super(session);
			this.serial = serial;
			this.bid = bid;
			this.clientID = clientID;
			products = new ArrayList<String[]>();
		}

		public void callFinish() throws Exception {
			if (success) {
				Packet packet = new Packet(OpCode.APP_STORE_LIST_PRODUCT2_SERVER);
				packet.putInt(serial);
				packet.put(products.size());
				for (int i = 0; i < products.size(); i++) {
					String[] arr = products.get(i);
					packet.putString(arr[0]);
					packet.putString(arr[1]);
					packet.putInt(Integer.parseInt(arr[2]));
					packet.putInt(Integer.parseInt(arr[3]) * 100);
				}
				packet.putInt(limit);
				session.send(packet);
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.APP_STORE_LIST_PRODUCT2_CLIENT, errorMessage);
			}
		}

		public void run() {
			PostMethod method = new PostMethod(Server.server.billingURL + "appstore_list_product");
			method.addRequestHeader("Connection", "close");
			method.setParameter("bid", bid);
			Account acc = (Account)session.getIdentity();
			method.setParameter("accountid", String.valueOf(acc.getId()));
			method.setParameter("clientid", clientID);
			
			// 根据此账号在本服务器的最大角色等级，确定用户信任级别1-7
			Player player = (Player)session.getClient();
			int maxLevel = Server.server.getServiceRegistry().getDbService().playerDAO.getMaxLevelOfAccount(acc.getId());
			if (player.level > maxLevel) {
				maxLevel = player.level;
			}
			method.setParameter("level", String.valueOf(maxLevel));
			try {
				HttpClient httpclient = new HttpClient();
				httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
				httpclient.getParams().setSoTimeout(30000);
				int code = httpclient.executeMethod(method);
				if (code != 200) {
					error("系统错误，请联系客服。");
					addToClientSession();
					return;
				}
				String result = method.getResponseBodyAsString();
				BufferedReader br = new BufferedReader(new StringReader(result));
				String codeStr = br.readLine();
				if ("0".equals(codeStr)) {
					// 成功
					String line;
					while ((line = br.readLine()) != null) {
						String[] secs = line.trim().split("\t");
						if (secs.length == 4) {
							products.add(secs);
						}
					}
					try {
						limit = Integer.parseInt(method.getResponseHeader("maximum-amount").getValue());
					} catch (Exception e) {
						limit = 0;
					}
					addToClientSession();
				} else {
					// 失败
					error(br.readLine());
					addToClientSession();
				}
			} catch (Exception ex) {
				log.error(ex, ex);
				error("系统错误，请联系客服。");
				addToClientSession();
			} finally {
				method.releaseConnection();
			}
		}
	}
}
