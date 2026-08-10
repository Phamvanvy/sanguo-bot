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

/**
 * 天语通充值接口。
 * @author lighthu
 */
public class KTouchChargeService implements Service {
	private static Logger log = Logger.getLogger(KTouchChargeService.class);
	
	public KTouchChargeService() {
	}

	public void startup() {
	}
	
	public void shutdown() {
	}

	/**
	 * 验证订单信息。
	 * serial					int
	 * orderid 			string		订单ID
	 * price			int			价格（分）
	 * resultcode		string		返回代码
	 * sign				string		订单签名
	 */
	public void checkReceipt(ClientSession session, Packet packet) {
		int serial = packet.getInt();
		String orderid = packet.getString();
		int price = packet.getInt();
		String resultcode = packet.getString();
		String sign = packet.getString();
		Server.server.getServiceRegistry().getDbService().schedule(
				new CheckReceiptCall(session, serial, orderid, price, resultcode, sign));
	}
	
	private static class CheckReceiptCall extends ClientSessionAsyncCall {
		protected int serial;
		protected String orderid;
		protected int price;
		protected String resultcode;
		protected String sign;
		protected int accountID;
		protected String channel;

		public CheckReceiptCall(ClientSession session, int serial, String orderid, int price, String resultcode, String sign) {
			super(session);
			this.serial = serial;
			this.orderid = orderid;
			this.price = price;
			this.resultcode = resultcode;
			this.sign = sign;
			accountID = ((Account)session.getIdentity()).getId();
			channel = ((Account)session.getIdentity()).getChannel();
			log.info("[KTOUCH_CHARGE]ACC[" + accountID + "]ORDERID[" + this.orderid + "]PRICE[" + price + "]CODE[" + resultcode + "]SIGN[" + sign + "]TRY");
		}

		public void callFinish() throws Exception {
			if (success) {
				Packet packet = new Packet(OpCode.KTOUCH_CHECK_RECEIPT_SERVER);
				packet.putInt(serial);
				session.send(packet);
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.KTOUCH_CHECK_RECEIPT_CLIENT, errorMessage);
			}
		}

		public void run() {
			/**
			 * KTouch客户端SDK充值完成后，获得回执，传送到此接口检查。如果回执合法，则添加余额。
			 * 请求参数：
			 *     id - 账号ID
			 *     gamecode - 游戏充值代码
			 *     orderid - 订单号
			 *     price - 价格，单位为分
			 *     resultcode - SDK返回结果代码
			 *     sign - 订单签名
			 *     channel - 用户渠道号
			 *     partition - 分区ID，@开头的表示充入主账户
			 * 返回(UTF-8编码)：
			 *     第一行是返回代码，0表示成功，1表示失败
			 *     如果失败，第二行是错误信息
			 */
			PostMethod method = new PostMethod(Server.server.billingURL + "ktouch_check_receipt");
			method.addRequestHeader("Connection", "close");
			method.setParameter("id", String.valueOf(accountID));
			method.setParameter("gamecode", "6");
			method.setParameter("orderid", orderid);
			method.setParameter("price", String.valueOf(price));
			method.setParameter("resultcode", resultcode);
			method.setParameter("sign", sign);
			method.setParameter("channel", channel);
			method.setParameter("partition", Server.server.usePartitionBalance ? Server.server.gameCode : "@"+Server.server.gameCode);
			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
			try {
				HttpClient httpclient = new HttpClient();
				httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
				httpclient.getParams().setSoTimeout(30000);
				int code = httpclient.executeMethod(method);
				if (code != 200) {
					error(peony.Messages.STRING_00981);
					addToClientSession();
					log.info("[KTOUCH_CHARGE]ACC[" + accountID + "]ORDERID[" + this.orderid + "]PRICE[" + price + "]CODE[" + resultcode + "]SIGN[" + sign + "]ERROR " + code);
					return;
				}
				String result = method.getResponseBodyAsString();
				BufferedReader br = new BufferedReader(new StringReader(result));
				String codeStr = br.readLine();
				if ("0".equals(codeStr)) {
					// 成功
					log.info("[KTOUCH_CHARGE]ACC[" + accountID + "]ORDERID[" + this.orderid + "]PRICE[" + price + "]CODE[" + resultcode + "]SIGN[" + sign + "]OK");
				} else {
					// 失败
					error(br.readLine());
					log.info("[KTOUCH_CHARGE]ACC[" + accountID + "]ORDERID[" + this.orderid + "]PRICE[" + price + "]CODE[" + resultcode + "]SIGN[" + sign + "]ERROR");
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
