package peony.service.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

import com.pip.partner.BillingAPI;
import com.pip.partner.PartnerAPI;
import com.pip.partner.PartnerException;

/**
 * 新版通用联运平台支付接入，请求生成订单。
 * @author light.hu
 */
public class PartnerGetOrderCall extends ClientSessionAsyncCall {
	
	protected static final Logger log = LoggerFactory.getLogger(PartnerGetOrderCall.class);
	private int serial;
	private String platform;
	private int amount;
	private String channel;
	private Player player;
	
	public PartnerGetOrderCall(ClientSession session, Packet pt) {
		super(session);
		this.serial = pt.getInt();
		this.platform = pt.getString();
		this.amount = pt.getInt();
		player = (Player)session.getClient();
		this.channel = player.getAccount().getChannel();
	}
	
	public void callFinish() throws Exception {

	}

	public void run() {
		Account account = (Account) session.getIdentity();
		
		// 访问计费服务器生成订单
		String orderInfo;
		try {
			log.info("[GETORDER]PLATFORM["+platform+"]ACCOUNTID["+account.getId()+"AMMOUNT["+amount+"]CHANNEL["+channel+"]");
			orderInfo = PartnerAPI.getOrder(platform, account.getId(), amount, channel);
		} catch (PartnerException pe) {
			ErrorHandler.sendErrorMessage(getSession(), serial, OpCode.PARTNER_GETORDER_CLIENT, 
					pe.getMessage());
			return;
		}
		
		// 向客户端发送订单信息
		Packet pt = new Packet(OpCode.PARTNER_GETORDER_SERVER);
		pt.putInt(serial);
		pt.putString(orderInfo);
		getSession().send(pt);
	}
}
