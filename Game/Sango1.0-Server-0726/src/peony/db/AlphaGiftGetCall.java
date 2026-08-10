package peony.db;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.Gain;
import peony.game.GameObjectRef;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.gift.AlphaGiftHistory;
import peony.net.ClientSession;
import peony.net.Packet;

public class AlphaGiftGetCall extends ClientSessionAsyncCall {

	protected GameObjectRef ref;
	protected String name;
	protected String password;
	
	public AlphaGiftGetCall(ClientSession session,Packet pt,Player p){
		super(session);
		this.name = pt.getString();
		this.password = pt.getString();
		ref = p.ref();
	}
	
	public void callFinish() throws Exception {
		if(success){
			
		}else{
			ErrorHandler.sendErrorMessage(session, -1, OpCode.ALPHA_GIFT_CLIENT, errorMessage);
		}
	}

	public void run() {
		DBService dbService = Server.server.getServiceRegistry().getDbService();
		AlphaGiftHistory ah = dbService.alphaGiftDAO.getAlphaGiftHistory(name,
				password);
		synchronized (dbService.alphaGiftDAO) {
			if (ah == null) {
				error(null, "帐号或者密码错误");
				addToClientSession();
				return;
			}
			if (ah.exchanged) {
				error(null, "此帐号奖励已经被领取过");
				addToClientSession();
				return;
			}
			Player p = ObjectAccessor.getPlayer(ref.instanceId);
			if(p!=null){
				Gain g = new Gain(p);
				g.addGainItem(ObjectAccessor.createGameItem(1002314), 1);
				g.addGainItem(ObjectAccessor.createGameItem(1002315), 1);
				g.addGainItem(ObjectAccessor.createGameItem(1002316), 1);
				PlayerTransaction tx = p.newTransaction("GFT");
				try {
					p.addGainComplete(g, tx, true);
					ah.exchanged = true;
					dbService.alphaGiftDAO.updateEntity(ah);
					tx.commit();
				} catch (NoEnoughSpaceException e) {
					tx.rollback();
					error(null,"背包已满");
					addToClientSession();
					return;
				}
			}
		}
	}

}
