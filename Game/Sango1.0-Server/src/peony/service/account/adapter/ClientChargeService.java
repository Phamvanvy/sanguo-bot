package peony.service.account.adapter;

import java.io.BufferedReader;
import java.io.StringReader;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.account.Account;
import sun.misc.BASE64Encoder;

/**
 * 支持客户端充值模式。这种模式下，客户端调用某种充值接口扣费，并由客户端通知服务器添加余额（高度危险！！！）
 * @author lighthu
 */
public class ClientChargeService implements Service {
	private static Logger log = Logger.getLogger(ClientChargeService.class);
	private static BASE64Encoder base64Encoder = new BASE64Encoder();
	
	public ClientChargeService() {
	}

	public void startup() {
	}
	
	public void shutdown() {
	}
	
	/**
	 * 客户端直接充值模式，请求生成一个新的订单。
	 * serial					int
	 * phone					String 手机号
	 * clientid					String 手机串号
	 */
	public void generateOrder(ClientSession session, Packet packet) {
		int serial = packet.getInt();
		String phone = packet.getString();
		String clientID = packet.getString();
		if (session.getIdentity() != null) {
			Account acc = (Account)session.getIdentity();
			Server.server.getServiceRegistry().getDbService().schedule(
					new GenerateOrderCall(session, acc, serial, phone, clientID));
		}
	}
	
	private static class GenerateOrderCall extends ClientSessionAsyncCall {
		protected int serial;
		protected Account acc;
		protected String phone;
		protected String clientID;
		protected String orderID;
		protected String encKey;

		public GenerateOrderCall(ClientSession session, Account acc, int serial, String phone, String clientID) {
			super(session);
			this.serial = serial;
			this.acc = acc;
			this.phone = phone;
			this.clientID = clientID;
			log.info("[CLIENT_CHARGE]ACC[" + acc.getId() + "]PHONE[" + phone + "]CLIENTID[" + clientID + "]TRY GETORDER");
		}

		public void callFinish() throws Exception {
			if (success) {
				/**
				 * 客户端直接充值模式，生成订单成功。
				 * serial					int
				 * orderid					String 订单ID
				 * enckey					String 加密密钥
				 */
				Packet packet = new Packet(OpCode.CLIENT_CHARGE_GET_ORDER_SERVER);
				packet.putInt(serial);
				packet.putString(orderID);
				packet.putString(encKey);
				session.send(packet);
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.CLIENT_CHARGE_GET_ORDER_CLIENT, errorMessage);
			}
		}

		public void run() {
			/**
			 * 客户端充值，生成订单。
			 * 请求参数：
			 *     accountid = 申请充值的账号ID
			 *     phone = 客户端手机号
			 *     clientid = 客户端串号
			 *     gamecode = 游戏代码
			 *     channel = 用户渠道号
			 * 返回(UTF-8编码)：
			 *     第一行是返回代码，0表示成功，1表示失败
			 *     如果失败，第二行是错误信息，如果成功，第二行是订单ID，第三行是加密字符串
			 */
			PostMethod method = new PostMethod(Server.server.billingURL + "clientcharge_order");
			method.addRequestHeader("Connection", "close");
			method.setParameter("accountid", String.valueOf(acc.getId()));
			method.setParameter("phone", clientID);
			method.setParameter("clientid", clientID);
			method.setParameter("gamecode", "6");
			method.setParameter("channel", acc.getChannel());
			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
			try {
				HttpClient httpclient = new HttpClient();
				httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
				httpclient.getParams().setSoTimeout(30000);
				int code = httpclient.executeMethod(method);
				if (code != 200) {
					error(peony.Messages.STRING_00981);
					addToClientSession();
					return;
				}
				String result = method.getResponseBodyAsString();
				BufferedReader br = new BufferedReader(new StringReader(result));
				String codeStr = br.readLine();
				if ("0".equals(codeStr)) {
					// 成功
					orderID = br.readLine();
					encKey = br.readLine();
					addToClientSession();
					log.info("[CLIENT_CHARGE]ACC[" + acc.getId() + "]PHONE[" + phone + "]CLIENTID[" + clientID + "]ORDERID[" + orderID + "]KEY[" + encKey + "]GETORDER OK");
				} else {
					// 失败
					error(br.readLine());
					addToClientSession();
				}
			} catch (Exception ex) {
				log.error(ex, ex);
				error(peony.Messages.STRING_00981);
				addToClientSession();
			} finally {
				method.releaseConnection();
			}
		}
	}

	/**
	 * 客户端直接充值模式，验证订单信息。
	 * serial					int
	 * receipt					byte[] 加密订单信息
	 */
	public void checkReceipt(ClientSession session, Packet packet) {
		int serial = packet.getInt();
		byte[] receipt = packet.getBytes();
		Server.server.getServiceRegistry().getDbService().schedule(
				new CheckReceiptCall(session, serial, receipt));
	}
	
	private static class CheckReceiptCall extends ClientSessionAsyncCall {
		protected int serial;
		protected String receipt;
		protected int accountID;

		public CheckReceiptCall(ClientSession session, int serial, byte[] receipt) {
			super(session);
			this.receipt = base64Encoder.encode(receipt);
			this.serial = serial;
			if (session.getIdentity() != null) {
				accountID = ((Account)session.getIdentity()).getId();
			}
			log.info("[CLIENT_CHARGE]ACC[" + accountID + "]RECEIPT[" + this.receipt + "]TRY CHECK");
		}

		/**
		 * 客户端直接充值模式，验证订单信息成功。
		 * serial					int
		 */
		public void callFinish() throws Exception {
			if (success) {
				/**
				 * 客户端直接充值模式，验证订单信息成功。
				 * serial					int
				 */
				Packet packet = new Packet(OpCode.CLIENT_CHARGE_CHECK_RECEIPT_SERVER);
				packet.putInt(serial);
				session.send(packet);
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.CLIENT_CHARGE_CHECK_RECEIPT_CLIENT, errorMessage);
			}
		}

		public void run() {
			/**
			 * 客户端充值完成后，检查客户端的订单回执。
			 * 请求参数：
			 *     receipt = 订单回执(base64编码)
			 *     optype = 0 - 只检查，1 - 检查成功后修改账户余额
			 * 返回(UTF-8编码)：
			 *     第一行是返回代码，0表示成功，1表示失败
			 *     如果失败，第二行是错误信息，如果成功，第二行是商品ID，第三行是商品价格
			 */
			PostMethod method = new PostMethod(Server.server.billingURL + "clientcharge_check");
			method.addRequestHeader("Connection", "close");
			method.setParameter("receipt", receipt);
			method.setParameter("optype", "1");
			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
			try {
				HttpClient httpclient = new HttpClient();
				httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
				httpclient.getParams().setSoTimeout(30000);
				int code = httpclient.executeMethod(method);
				if (code != 200) {
					error(peony.Messages.STRING_00981);
					addToClientSession();
					log.info("[CLIENT_CHARGE]ACC[" + accountID + "]RECEIPT[" + this.receipt + "]ERROR " + code);
					return;
				}
				String result = method.getResponseBodyAsString();
				BufferedReader br = new BufferedReader(new StringReader(result));
				String codeStr = br.readLine();
				if ("0".equals(codeStr)) {
					// 成功
					String itemID = br.readLine();
					String price = br.readLine();
					log.info("[CLIENT_CHARGE]ACC[" + accountID + "]RECEIPT[" + this.receipt + "]ITEMID[" + itemID + "]PRICE[" + price + "]OK");
				} else {
					// 失败
					error(br.readLine());
					log.info("[CLIENT_CHARGE]ACC[" + accountID + "]RECEIPT[" + this.receipt + "]ERROR");
				}
			} catch (Exception ex) {
				log.error(ex, ex);
				error(peony.Messages.STRING_00981);
			} finally {
				method.releaseConnection();
			}
			addToClientSession();
		}
	}
}
