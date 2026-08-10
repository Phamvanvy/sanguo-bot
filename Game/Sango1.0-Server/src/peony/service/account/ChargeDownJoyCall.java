package peony.service.account;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * 当乐充值(189hi平台充值)
 * @author dchen
 */
public class ChargeDownJoyCall extends ClientSessionAsyncCall {

	protected static final Logger log = Logger.getLogger(ChargeDownJoyCall.class);
	
	public static String chargeBackUrl = "";
	
	protected int serial;
	protected int money;
	protected String serialNum;
	protected String password;
	protected String playerName; // 为他人充值的角色名
	protected int playerId;
	protected int accountId;
	protected String channel;
	protected String model;
	protected String message;
	protected int type;      // 0 - 移动卡， 1 - 联通卡， 2 - 电信卡
	
	protected static String gameCode = "6"; //gameCode
	
	public ChargeDownJoyCall(ClientSession session, Packet pt){
		super(session);
		this.serial = pt.getInt();
		this.money = pt.getInt();
		this.serialNum = pt.getString();
		this.password = pt.getString();
		this.playerName = pt.getString();
		this.type = pt.get();
		Player player = (Player)session.getClient();
		this.playerId = player.getInstanceId();
		this.accountId = player.accountId;
		this.channel = player.getAccount().getChannel();
		this.model = player.getAccount().getModel();
		log.info("[CHARGE_DOWNJOY]ID[" + player.getInstanceId()+ "]ACC[" +player.accountId + "]MONEY["
			+ money + "]SERIAL[" + serialNum + "]PASS[" + password
			+ "]PLAYERNAME[" + playerName + "]TYPE[" + type + "]TRY");
	}
	
	public void callFinish() {
		
	}

	public void run() {
		if(chargeBackUrl==null || chargeBackUrl.equals(""))
			chargeBackUrl = Server.server.getServiceRegistry().getJettyService().getUrl("chinarun");
		if (playerName.length() > 0) {
			// 如果是给他人充值，查找目标角色的账号
			Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(playerName);
			if (actor == null) {
				message = "没有找到目标角色";
				Packet pkt = new Packet(OpCode.CHARGE_DOWNJOY_SERVER);
				pkt.putInt(serial);
				pkt.putString(message);
				pkt.put(0);
				session.send(pkt);
				addToClientSession();
				return;
			}
			accountId = actor.accountId;
		}
		String msg = "";
		int state = 1;
		
		// 向billing服务器发起请求
		PostMethod method = new PostMethod(Server.server.billingURL + "downjoy_charge");
		method.addRequestHeader("Connection", "close");
		method.setParameter("accountid", String.valueOf(accountId));
		method.setParameter("ip", session.getClientIP());
		method.setParameter("cardtype", String.valueOf(type));
		method.setParameter("cardno", serialNum);
		method.setParameter("cardpass", password);
		method.setParameter("money", String.valueOf(money));
		method.setParameter("gamecode", getGameCode(model));
		method.setParameter("channel", channel);
		method.setParameter("model", model);
		boolean partition = Server.server.getConfig().getBoolean("partition", false);
		String gameCode = Server.server.gameCode;
		method.setParameter("partition", partition ? gameCode : "@" + gameCode);
		method.setParameter("returnhttp", chargeBackUrl);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
		int code = 0;
		try {
			HttpClient httpclient = new HttpClient();
			httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
			httpclient.getParams().setSoTimeout(30000);
			code = httpclient.executeMethod(method);
			BufferedReader br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), "UTF-8"));
			if (code == 200) {
				String line = br.readLine();
				int retCode = Integer.parseInt(line);
				if (retCode != 0) {
					// 提交出错
					msg = br.readLine();
					state = 0;
					log.info("[CHARGE_DOWNJOY]ID[" + playerId + "]ACC[" + accountId + "]MONEY["
						+ money + "]SERIAL[" + serialNum + "]PASS[" + password
						+ "]PLAYERNAME[" + playerName + "]TYPE[" + type + "]FAIL");
				} else {
					// 提交成功
					msg = "您的充值申请已提交，请稍等几分钟。";
					log.info("[CHARGE_DOWNJOY]ID[" + playerId + "]ACC[" + accountId + "]MONEY["
						+ money + "]SERIAL[" + serialNum + "]PASS[" + password
						+ "]PLAYERNAME[" + playerName + "]TYPE[" + type + "]OK");
				}
			} else {
				// 网络错误
				msg = "您的充值申请提交失败，请核对序列号和密码重新输入。";
				state = 0;
				log.info("[CHARGE_DOWNJOY]ID[" + playerId + "]ACC[" + accountId + "]MONEY["
					+ money + "]SERIAL[" + serialNum + "]PASS[" + password
					+ "]PLAYERNAME[" + playerName + "]TYPE[" + type + "]FAIL");
			}
		} catch (Exception ex) {
			log.error(ex.toString(), ex);
			msg = "您的充值申请提交失败，请核对序列号和密码重新输入。";
			state = 0;
			log.info("[CHARGE_DOWNJOY]ID[" + playerId + "]ACC[" + accountId + "]MONEY["
				+ money + "]SERIAL[" + serialNum + "]PASS[" + password
				+ "]PLAYERNAME[" + playerName + "]TYPE[" + type + "]FAIL");
		} finally {
			method.releaseConnection();
		}
		
		// 通知客户端
		Packet pt = new Packet(OpCode.CHARGE_DOWNJOY_SERVER);
		pt.putInt(serial);
		pt.putString(msg);
		pt.put(state);
		session.send(pt);
	}
	
	public String getGameCode(String model){
		return gameCode;
	}
	
}
