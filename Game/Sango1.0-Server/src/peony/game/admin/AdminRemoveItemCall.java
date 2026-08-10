package peony.game.admin;


import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.Horse;
import peony.game.ItemTemplate;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class AdminRemoveItemCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int playerId;
	protected int itemID;
	protected int instanceIDorCount;
	protected Player p;
	
	public AdminRemoveItemCall(ClientSession session,Packet pt){
		super(session);
		this.serial = pt.getInt();
		this.playerId = pt.getInt();
		this.itemID = pt.getInt();
		this.instanceIDorCount = pt.getInt();
	}
	
	private void sendOK() {
		Packet pt = new Packet(OpCode.ADMIN_REMOVE_ITEM_SERVER);
		pt.putInt(serial);
		session.send(pt);
		
		Server.server.getServiceRegistry().getPlayerService().savePlayer(p);
	}
	
	public void callFinish() throws Exception {
		if (p != null) {
			ItemTemplate template = ObjectAccessor.getItemTemplate(itemID);
			int instanceID;
			int count;
			if (template.newInstance) {
				instanceID = this.instanceIDorCount;
				count = 1;
			} else {
				instanceID = -1;
				count = this.instanceIDorCount;
			}
			
			// 背包中查找
			PlayerTransaction tx = p.newTransaction("GM");
			GameItem gi = p.bag.removeGameItem(itemID, instanceID, count, tx, false);
			if (gi != null) {
				tx.commit();
				sendOK();
				return;
			} else {
				tx.rollback();
			}
			
			// 在身上装备查找
			if (template.equipment != null) {
				gi = p.equipments.unequip(itemID, instanceID);
				if (gi != null) {
					sendOK();
					return;
				}
			}
			
			// 在坐骑身上装备查找
			if (template.equipment != null) {
				for (Horse h : p.horseBag.horses) {
					gi = h.equs.unequip(itemID, instanceID, p);
					if (gi != null) {
						sendOK();
						return;
					}
				}
			}
			
			// 在仓库中查找
			if (p.depot != null) {
				tx = p.newTransaction("GM");
				p.depot.removeGameItem(itemID, instanceID, count, tx, false);
				if (gi != null) {
					tx.commit();
					sendOK();
					return;
				} else {
					tx.rollback();
				}
			}
			
			ErrorHandler.sendAdminErrorMessage(session, serial,
					OpCode.ADMIN_REMOVE_ITEM_CLIENT, peony.Messages.STRING_01685);
		} else {
			ErrorHandler.sendAdminErrorMessage(session, serial,
					OpCode.ADMIN_REMOVE_ITEM_CLIENT, peony.Messages.STRING_00529);
		}
	}

	public void run() {
		Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(playerId);
		if(actor==null){
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_REMOVE_ITEM_CLIENT, peony.Messages.STRING_00529);
			return;
		}
		p = Server.server.getServiceRegistry().getPlayerService().loadPlayerSilent(playerId);
		addToClientSession();
	}

}
