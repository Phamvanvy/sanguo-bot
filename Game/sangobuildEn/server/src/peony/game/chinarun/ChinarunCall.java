package peony.game.chinarun;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class ChinarunCall extends ClientSessionAsyncCall {

	private static final Logger log = Logger.getLogger(ChinarunCall.class);

	protected int serial;
	protected int money;
	protected String serialNum;
	protected String password;
	protected String playerName; // 为他人充值的角色名
	protected int playerId;
	protected int accountId;
	protected String channel;
	protected String message;
	protected int type;

	protected final static String CHINARUN_HTTP = "http://218.206.80.185:8102/yeepay_order2";
	protected static String PAY19_HTTP = "http://218.206.80.185:8102/19payd_order"; //19pay的地址，给联通使用的

	public ChinarunCall(ClientSession session, Packet pt, Player player,String channel) {
		super(session);
		this.serial = pt.getInt();
		this.money = pt.getInt();
		this.serialNum = pt.getString();
		this.password = pt.getString();
		this.playerName = pt.getString();
		this.type = pt.get();
		this.playerId = player.id;
		this.accountId = player.accountId;
		this.channel = Server.server.getServiceRegistry().getAccountService()
				.getAccount(this.accountId).getChannel();
		if(type!=0)
			log.info("[CHINARUN]" + LogUtil.getPlayerLogString(player) + "MONEY["
				+ money + "]SERIAL[" + serialNum + "]PASS[" + password
				+ "]PLAYERNAME[" + playerName + "]TRY");
		else{
			log.info("[UNICOM]" + LogUtil.getPlayerLogString(player) + "MONEY["
					+ money + "]SERIAL[" + serialNum + "]PASS[" + password
					+ "]PLAYERNAME[" + playerName + "]TRY");
		}
	}

	public void callFinish() throws Exception {
		if (message != null) {
			Packet pt = new Packet(OpCode.CHINARUN_SERVER);
			pt.putInt(serial);
			pt.putString(message);
			session.send(pt);
		}
	}

	public void run() {
		if (playerName.length() != 0) {
			Actor actor = Server.server.getServiceRegistry()
					.getActorCacheService().find(playerName);
			if (actor == null) {
				message = "沒有找到目標角色";
				addToClientSession();
				return;
			}
			accountId = actor.accountId;
		}
		if(type==0){ //如果是联通卡
			unicom();
		}else{
			chinarun();
		}


		addToClientSession();
	}
	
	/**
	 * 神州行充值
	 */
	protected void chinarun(){
		String callbackHttp = Server.server.getServiceRegistry().getJettyService().getUrl("chinarun");
		PostMethod method;
		method = new PostMethod(CHINARUN_HTTP);
		method.addRequestHeader("Connection", "close");
		method.setParameter("id", String.valueOf(accountId));
		method.setParameter("name", playerName);
		method.setParameter("cardsn", serialNum);
		method.setParameter("password", password);
		method.setParameter("money", String.valueOf(money));
		method.setParameter("game", "6");
		method.setParameter("returnhttp", callbackHttp);
		// method.setParameter("returnhttp","http://" +
		// configuration.getString("localip")
		// + ":" + configuration.getString("webport")
		// + "/chinarun");
		method.setParameter("channel", channel);
		int code = 0;
		Header header = null;
		try {
			HttpClient httpclient = new HttpClient();
			httpclient.getHttpConnectionManager().getParams()
					.setConnectionTimeout(30000);
			httpclient.getParams().setSoTimeout(30000);
			code = httpclient.executeMethod(method);
			header = method.getResponseHeader("result");
			log.info("[CHINARUNRESULT]SERIAL[" + serialNum + "]CODE[" + code
					+ "]HEAD[" + (header == null ? "EMPTY" : header.getValue())
					+ "]CALLBACK["+callbackHttp+"]");
		} catch (Exception ex) {
			log.error(ex, ex);
		} finally {
			method.releaseConnection();
		}
		if (code == 200) {
			// 发送成功，提示用户请求已经发出。
			if ("200".equals(header.getValue())) {
				message = "您的充值申請已提交,請稍等几分鐘.";
			} else {
				message = "您的充值申請提交失敗,請核對序列號和密碼重新輸入.";
			}
		} else {
			message = "您的充值申請提交失敗,請核對序列號和密碼重新輸入.";
		}
	}
	
	/**
	 * 联通充值
	 */
	protected void unicom(){
		String callbackHttp = Server.server.getServiceRegistry().getJettyService().getUrl("chinarun");
		PostMethod method;
		BufferedReader br = null;
		method = new PostMethod(PAY19_HTTP);
		method.addRequestHeader("Connection", "close");
		method.setParameter("id", String.valueOf(accountId));
//		method.setParameter("name", playerName);
		method.setParameter("cardno", serialNum);
		method.setParameter("cardpass", password);
		method.setParameter("amount", String.valueOf(money*100));
		method.setParameter("gamecode", "6");
		method.setParameter("returnhttp", callbackHttp);
		method.setParameter("cardtype", "0");
		method.setParameter("channel", channel);
		int code = 0;
		try {
			HttpClient httpclient = new HttpClient();
			httpclient.getHttpConnectionManager().getParams()
					.setConnectionTimeout(30000);
			httpclient.getParams().setSoTimeout(30000);
			code = httpclient.executeMethod(method);
			br = new BufferedReader(new InputStreamReader(method
					.getResponseBodyAsStream(), "UTF-8"));
			int retCode = 2;
			if (code == 200) {
				// 发送成功，提示用户请求已经发出。
				String line = br.readLine();
				retCode = Integer.parseInt(line);
				if(retCode==0){  //成功
					message = "您的充值申請已提交,請稍等几分鐘.";
				}
				else{
					message = br.readLine();
				}
			} else {
				message = "您的充值申請提交失敗,請核對序列號和密碼重新輸入.";
			}
			log.info("[UNICOM]SERIAL[" + serialNum + "]CODE[" + code
					+ "]CALLBACK["+callbackHttp+"]RETCODE[" + retCode+"]");
		} catch (Exception ex) {
			log.error(ex, ex);
		} finally {
			method.releaseConnection();
		}

	}
}
