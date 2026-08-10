package peony.service.account.cmcc;

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
import peony.service.account.ErrorMessages;

import com.pip.net.message.ErrorMessage;

public class CmccChargeCall extends AccountAsyncCall {
	
	private static final Logger log = Logger.getLogger(CmccChargeCall.class);

	protected int serial;
	protected int amount;
	protected String cmccUserId;
	protected String cmccUserKey;
	
	public CmccChargeCall(ClientSession session, Packet packet){
		super(session);
		this.serial = packet.getInt();
		this.cmccUserId = packet.getString();
		this.cmccUserKey = packet.getString();
		this.amount = packet.getInt();
	}
	
	public void callFinish() throws Exception {
		if(success){
			log.info("[CMCCCHARGEOK]CMCCUSER["+cmccUserId+"]CMCCKEY["+cmccUserKey+"]COUNT["+amount+"]");
			CmccChargeOkMessage msg = (CmccChargeOkMessage)message;
			Player p = (Player)session.getClient();
			if(p != null){
				Account a = (Account)session.getIdentity();
				a.setLongIMoney(msg.balance);
				p.addIntPropertyChangedItem(ChangedItem.IMONEY, (int)(a.getLongIMoney() / 100), true, true);
			}
			Packet pt = new Packet(OpCode.CMCC_CHARGE_SERVER);
			pt.putInt(serial);
			pt.putString(msg.msg);
			session.send(pt);
		}else{
			log.info("[CMCCCHARGEFAIL]CMCCUSER["+cmccUserId+"]CMCCKEY["+cmccUserKey+"]COUNT["+amount+"]");
			ErrorHandler.sendErrorMessage(session, serial, OpCode.CMCC_CHARGE_CLIENT, ErrorMessages.getErrorMesssage((ErrorMessage)message));
		}
	}

	public void run() {
		log.info("[CMCCCHARGE]CMCCUSER["+cmccUserId+"]CMCCKEY["+cmccUserKey+"]COUNT["+amount+"]");
		CmccChargeUpMessage message = new CmccChargeUpMessage(cmccUserId,
				cmccUserKey, amount);
		Server.server.getServiceRegistry().getAccountService().sendAndRegister(
				message, this);
	}

}
