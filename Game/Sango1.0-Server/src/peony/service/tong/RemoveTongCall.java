package peony.service.tong;

import peony.common.ClientSessionAsyncCall;
import peony.db.DBService;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.tong.apply.TongBattleApplyService;

public class RemoveTongCall extends ClientSessionAsyncCall {

	protected Player player;
	protected int serial;
	protected int type;
	
	public RemoveTongCall(ClientSession session, Packet packet) {
		super(session);
		player = (Player)session.getClient();
		serial = packet.getInt();
		this.type = packet.getInt();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.TONG_REMOVE_SERVER);
			pt.putInt(serial);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_REMOVE_CLIENT, errorMessage);
		}
	}

	public void run() {
		if(player!=null){
			try {
				TongService service = Server.server.getServiceRegistry().getTongService();
				TongBattleApplyService applyService = Server.server.getServiceRegistry().getTongBattleApplyService();
				Tong tong = service.getPlayerTong(player.id,true);
				TongMember member = service.getPlayerInfo(player.id);
				if(tong!=null && member!=null){
					if(applyService.getApplyByTongId(tong.id)==null){
						if(tong.members!=null && tong.members.size()==1){
							synchronized (tong) {
								service.returnContribute(player,type);
								tong.members.remove(member);
								tong.modify = true;
								service.removeTongMember(player.id);
								DBService dbs = Server.server.getServiceRegistry().getDbService();
								dbs.tongMemberDAO.makeTransient(member);
								service.removeTong(tong.id);
								dbs.tongDAO.makeTransient(tong);
								player.setGuildName("");
								player.refreshProperties(false);
								if(service.autoAcceptTongs.contains(tong.id)){
									service.autoAcceptTongs.remove(new Integer(tong.id));
								}
							}
						}else{
							error(peony.Messages.STRING_00674);
						}
					}else{
						error(peony.Messages.STRING_00675);
					}
				}else{
					error(peony.Messages.STRING_00676);
				}
			} catch (TongException e) {
				error(e.getMessage());
			} catch (Exception e){
				error(peony.Messages.STRING_00677);
			}
		}
		addToClientSession();
	}

}
