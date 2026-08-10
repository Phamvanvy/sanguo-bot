package peony.vtc.charge;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.log4j.Logger;
import org.mortbay.util.ajax.JSON;
import com.pip.net.message.gameaccount.AddBalanceMessage;
import com.pip.net.message.gameaccount.AddBalanceOkMessage;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.changed.ChangedItem;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.Account;
import peony.service.account.AccountAsyncCall;
import sun.misc.BASE64Encoder;

public class ViettelCardChargeCall extends AccountAsyncCall {
	private static final Logger log = Logger.getLogger(ViettelCardChargeCall.class);

	protected int serial;
	protected Player player;
	protected Account acc;
	protected String cardNo;
	protected String pin;
	protected int amount;
	
	public ViettelCardChargeCall(ClientSession session, Packet packet) {
		super(session);
		this.player = (Player)session.getClient();
		this.acc = (Account)session.getIdentity();
		this.serial = packet.getInt();
		this.cardNo = packet.getString();
		this.pin = packet.getString();
		log.info("[VIETTEL_CHARGE]" + LogUtil.getPlayerLogString(this.player)+ "CARDNO[" + this.cardNo + "]PIN[" + this.pin + "]TRY");
	}

	public void callFinish() throws Exception {
		if (success) {
			// 添加元宝成功
			AddBalanceOkMessage msg = (AddBalanceOkMessage)message;
			long oldIMoney = acc.getLongIMoney();
			acc.setLongIMoney(msg.getValue() + oldIMoney);
			//player.addIntPropertyChangedItem(ChangedItem.IMONEY, (int)(acc.getLongIMoney() / 100), true, true);
			String showPrice = player.ibToYuanbao(acc.getLongIMoney());
 			player.addStringPropertyChangedItem(ChangedItem.YUANBAO, showPrice, true);
			
			Packet pt = new Packet(OpCode.VIETNAM_VIETTEL_CHARGE_SERVER);
			pt.putInt(serial);
			session.send(pt);
			player.message(-1,peony.Messages.STRING_00869, -1, -1);
			log.info("[VIETTEL_CHARGE]" + LogUtil.getPlayerLogString(this.player) + "CARDNO[" + this.cardNo + "]PIN[" + this.pin + "]AMOUNT[" + amount + "]ADD_BALANCE_OK");
		} else {
			// 添加元宝失败（严重错误！）
			ErrorHandler.sendErrorMessage(session, serial, OpCode.VIETNAM_VIETTEL_CHARGE_CLIENT, errorMessage);
			log.info("[VIETTEL_CHARGE]" + LogUtil.getPlayerLogString(this.player) + "CARDNO[" + this.cardNo + "]PIN[" + this.pin + "]AMOUNT[" + amount + "]ADD_BALANCE_ERROR");
		}
	}

	public void run() {
		try {
			// 发起请求
			amount = viettelChargeRequest(acc.getName(), cardNo, pin);
		} catch (Exception e) {
			// 充值错误
			ErrorHandler.sendErrorMessage(session, serial, OpCode.VIETNAM_VIETTEL_CHARGE_CLIENT, e.getMessage());
			return;
		}
		
		log.info("[VIETTEL_CHARGE]" + LogUtil.getPlayerLogString(this.player) + "CARDNO[" + this.cardNo + "]PIN[" + this.pin + "]AMOUNT[" + amount + "]OK");
		
		// 成功，向认证服务器发请求加元宝，250越南盾兑换1元宝
		double imoney =  amount * 3600.0 / 250.0;
		AddBalanceMessage msg = new AddBalanceMessage(acc.getId(), (int)imoney, "viettel_" + cardNo, "", amount);
		Server.server.getServiceRegistry().getAccountService().sendAndRegister(msg, this);
	}

	// Viettel充值接口
	public static String DES_KEY = "7454739e907f5595ae61d84b";
	public static String GAME_ID = "mcgame";
	public static String CHARGE_URL = "http://210.211.99.18:8888/";

	public static String encrypt(String key, String data) throws Exception {
		Cipher cipher = Cipher.getInstance("TripleDES");
		SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "TripleDES");
		cipher.init(Cipher.ENCRYPT_MODE, keyspec);
		byte[] stringBytes = data.getBytes();
		byte[] raw = cipher.doFinal(stringBytes);
		BASE64Encoder encoder = new BASE64Encoder();
		String base64 = encoder.encode(raw);
		return base64;
	}
	
	// 访问服务器充值，并返回充值金额。如果出错，抛出带错误信息的异常。
	public static int viettelChargeRequest(String accountName, String cardno, String pin) throws Exception {
		String errorMsg = null;
		try {
			InetAddress thisIp = InetAddress.getLocalHost();
			pin = encrypt(DES_KEY, pin);
			String ip = thisIp.getHostAddress();
			String url = CHARGE_URL + "?serial=" + cardno + "&pin=" + URLEncoder.encode(pin, "ASCII") +
				"&ip=" + ip + "&username=" + URLEncoder.encode(accountName, "UTF-8") + 
				"&game=" + GAME_ID;
			HttpURLConnection conn = (HttpURLConnection)(new URL(url).openConnection());
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			String line;
			String response = "";
			while ((line = reader.readLine()) != null) {
				response += line;
			}
			reader.close();
			log.info("[VIETTEL_CHARGE]RESULT[" + response + "]");
			
			Map map = (Map)JSON.parse(response);
			if ("00".equals(String.valueOf(map.get("errorCode")))) {
				return Integer.parseInt((String)map.get("amount"));
			}
			errorMsg = (String)map.get("errorMessage");
		} catch (Exception e) {
			throw new Exception(peony.Messages.STRING_00961);
		}
		throw new Exception(errorMsg);
	}
}

