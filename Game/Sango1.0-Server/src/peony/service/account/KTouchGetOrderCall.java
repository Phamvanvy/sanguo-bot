package peony.service.account;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * 天语SDK支付接入，请求创建订单。
 */
public class KTouchGetOrderCall extends ClientSessionAsyncCall {
	
	protected static final Logger log = LoggerFactory.getLogger(KTouchGetOrderCall.class);
	private int serial;
	private int amount;
	private String channel;
	private Player player;
	
	public KTouchGetOrderCall(ClientSession session, Packet pt) {
		super(session);
		this.serial = pt.getInt();
		this.amount = pt.getInt();
		player = (Player)session.getClient();
		this.channel = player.getAccount().getChannel();
	}
	
	public void callFinish() throws Exception {

	}

	public void run() {
		Account account = (Account) session.getIdentity();
		
		// 向billing服务器发起请求
		PostMethod method = new PostMethod(Server.server.billingURL + "ktouch_get_order");
		method.addRequestHeader("Connection", "close");
		method.setParameter("id", String.valueOf(account.getId()));
		method.setParameter("money", String.valueOf(amount));
		method.setParameter("gamecode", "6");
		method.setParameter("channel", channel);
		boolean partition = Server.server.getConfig().getBoolean("partition", false);
		method.setParameter("partition", partition ? Server.server.gameCode : "@" + Server.server.gameCode);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
		int code = 0;
		try {
			HttpClient httpclient = new HttpClient();
			httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
			httpclient.getParams().setSoTimeout(30000);
			code = httpclient.executeMethod(method);
			BufferedReader br = new BufferedReader(new InputStreamReader(method
				.getResponseBodyAsStream(), "UTF-8"));
			if (code == 200) {
				String line = br.readLine();
				int retCode = Integer.parseInt(line);
				if (retCode != 0) {
					// 提交出错
					ErrorHandler.sendErrorMessage(getSession(), serial, OpCode.KTOUCH_GET_ORDER_CLIENT, br.readLine());
				} else {
					// 提交成功
					String orderID = br.readLine();
					String notifyURL = br.readLine();
					Packet pt = new Packet(OpCode.KTOUCH_GET_ORDER_SERVER);
					pt.putInt(serial);
					pt.putString(orderID);
					pt.putString(notifyURL);
					getSession().send(pt);
				}
			} else {
				// 网络错误
				ErrorHandler.sendErrorMessage(getSession(), serial, OpCode.KTOUCH_GET_ORDER_CLIENT, "网络故障，请稍后重试");
			}
		} catch (Exception ex) {
			log.error(ex.toString(), ex);
			ErrorHandler.sendErrorMessage(getSession(), serial, OpCode.KTOUCH_GET_ORDER_CLIENT, "网络故障，请稍后重试");
		} finally {
			method.releaseConnection();
		}
	}
}
