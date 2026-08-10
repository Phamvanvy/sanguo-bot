package peony.gatecard;

import org.apache.log4j.Logger;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.changed.ChangedItem;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.Account;
import peony.service.account.AccountAsyncCall;
import com.pip.net.message.gameaccount.AddBalanceOkMessage;

public class GateCardChargeCall extends AccountAsyncCall {

	protected int serial;
	protected Player player;
	protected String cardNum;
	protected String pin;
	private static final Logger log = Logger.getLogger(GateCardChargeCall.class);
	
	public GateCardChargeCall(ClientSession session, Packet packet) {
		super(session);
		this.player = (Player)session.getClient();
		this.serial = packet.getInt();
		this.cardNum = packet.getString();
		this.pin = packet.getString();
	}

	public void callFinish() throws Exception {
		if (success) {
			AddBalanceOkMessage msg = (AddBalanceOkMessage)message;
			Account a = (Account)player.session.getIdentity();
			long oldIMoney = a.getLongIMoney();
			a.setLongIMoney(msg.getValue() + oldIMoney);
			//player.addIntPropertyChangedItem(ChangedItem.IMONEY, (int)(a.getLongIMoney() / 100), true, true);
 			String showPrice = player.ibToYuanbao(a.getLongIMoney());
 			player.addStringPropertyChangedItem(ChangedItem.YUANBAO, showPrice, true);
			
			Packet pt = new Packet(OpCode.VIETNAM_CHARGE_SERVER);
			pt.putInt(serial);
			session.send(pt);
			player.message(-1,peony.Messages.STRING_00869, -1, -1);
		} else {
			ErrorHandler.sendErrorMessage(session, serial, OpCode.VIRTNAM_CHARGE_CLIENT, errorMessage);
		}
	}

	public void run() {
		if(player!=null){
			GateCardService service = Server.server.getServiceRegistry().getGateCardService();
			int accountId = player.accountId;
			try {
				service.charge(player, accountId, cardNum, pin, this);
			} catch (GateCardChargeException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.VIRTNAM_CHARGE_CLIENT, e.getMessage());
			}
		}
	}

}
