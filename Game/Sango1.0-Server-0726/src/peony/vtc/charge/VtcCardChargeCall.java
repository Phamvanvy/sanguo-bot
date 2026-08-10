package peony.vtc.charge;

import org.apache.log4j.Logger;
import com.pip.net.message.gameaccount.AddBalanceOkMessage;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.changed.ChangedItem;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.Account;
import peony.service.account.AccountAsyncCall;

public class VtcCardChargeCall extends AccountAsyncCall {
	protected int serial;
	protected Player player;
	protected String cardId;
	protected String cardCode;
	private static final Logger log = Logger.getLogger(VtcCardChargeCall.class);
	
	public VtcCardChargeCall(ClientSession session, Packet packet) {
		super(session);
		this.player = (Player)session.getClient();
		this.serial = packet.getInt();
		this.cardId = packet.getString();
		this.cardCode = packet.getString();
	}

	public void callFinish() throws Exception {
		if (success) {
			AddBalanceOkMessage msg = (AddBalanceOkMessage)message;
			Account a = (Account)player.session.getIdentity();
			long oldIMoney = a.getLongIMoney();
			a.setLongIMoney(msg.getValue() + oldIMoney);
			player.addIntPropertyChangedItem(ChangedItem.IMONEY, (int)(a.getLongIMoney() / 100), true, true);
			Packet pt = new Packet(OpCode.VIETNAM_VTC_CHARGE_SERVER);
			pt.putInt(serial);
			session.send(pt);
			player.message(-1,"≥‰÷µ≥…π¶", -1, -1);
		} else {
			ErrorHandler.sendErrorMessage(session, serial, OpCode.VIETNAM_VTC_CHARGE_CLIENT, errorMessage);
		}
	}

	public void run() {
		if(player!=null){
			VtcCardChargeService service = Server.server.getServiceRegistry().getVtcCardChargeService();
			try {
				service.vtccard(cardId, cardCode, player, this);
			} catch (VtcCardChargeException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.VIETNAM_VTC_CHARGE_CLIENT, e.getMessage());
			}
		}
	}

}
