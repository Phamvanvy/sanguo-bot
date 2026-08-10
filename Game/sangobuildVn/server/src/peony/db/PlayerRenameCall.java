package peony.db;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.ItemUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.ServiceEvent;

public class PlayerRenameCall extends ClientSessionAsyncCall {

	protected int playerId;
	protected int serial;
	protected String name;
	protected boolean useItem; //是否使用物品
	
	public PlayerRenameCall(ClientSession session,int playerId,String name,int serial,boolean useItem){
		super(session);
		this.playerId = playerId;
		this.name = name;
		this.serial = serial;
		this.useItem = useItem;
	}
	
	public void callFinish() throws Exception {
		if(!success){
			if(useItem)
				ErrorHandler.sendErrorMessage(session, serial, OpCode.CHANGE_NAME_CLIENT, errorMessage);
		}
	}

	public void run() {
		Player p = ObjectAccessor.getPlayer(playerId);
		if (p != null) {
			Actor actor = Server.server.getServiceRegistry()
					.getActorCacheService().find(name);
			if (actor == null || actor.exist == 0) {
				if (useItem) {
					PlayerTransaction tx = p.newTransaction("RNM");
					if (p.bag.removeGameItem(ItemUtil.ITEM_CHANGE_NAME, -1, 1,
							tx, true) != null) {
						p.setName(name);
						tx.commit();
						Packet pt = new Packet(OpCode.CHANGE_NAME_SERVER);
						pt.putInt(serial);
						pt.putString(name);
						session.send(pt);
						Server.server.getEventManager().fireEvent(
								new ServiceEvent(ServiceEvent.EVENT_PLAYER_CHANGENAME,
										p));
						Server.server.getServiceRegistry().getPlayerService()
								.savePlayer(p);
					}else{
						tx.rollback();
						error(null, "Cần có phù đổi danh mới có thể đổi được ");
						addToClientSession();
					}
				} else {
					p.setName(name);
					Server.server.getEventManager().fireEvent(
							new ServiceEvent(ServiceEvent.EVENT_PLAYER_CHANGENAME,
									p));
					Server.server.getServiceRegistry().getPlayerService()
							.savePlayer(p);
				}
			} else {
				error(null, "Đã tồn tại nhân vật cùng tên");
				if (useItem)
					addToClientSession();
			}
		}
	}
}
