package peony.mobiphone;

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

public class TelcoChargeCall extends AccountAsyncCall {

	protected int serial;
	protected String cardCode;
	protected int type;
	protected Player player;
	private static final Logger log = Logger.getLogger(TelcoChargeCall.class);
	
	public TelcoChargeCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.cardCode = packet.getString();
		this.type = packet.getByte();
		this.player = (Player)session.getClient();
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
 			
			Packet pt = new Packet(OpCode.VIETNAM_TELCO_MOBIPHONE_CHARGE_SERVER);
			pt.putInt(serial);
			session.send(pt);
			player.message(-1,peony.Messages.STRING_00869, -1, -1);
		} else {
			ErrorHandler.sendErrorMessage(session, serial, OpCode.VIETNAM_TELCO_MOBIPHONE_CHARGE_CLIENT, errorMessage);
		}
	}

	public void run() {
		if(player!=null){
			TelcoChargeService service = Server.server.getServiceRegistry().getTelcoChargeService();
			try {
				service.charge(player,type,cardCode,this);
			} catch (MobilePhoneChargeException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.VIETNAM_TELCO_MOBIPHONE_CHARGE_CLIENT, e.getMessage());
			}
		}
	}

}
