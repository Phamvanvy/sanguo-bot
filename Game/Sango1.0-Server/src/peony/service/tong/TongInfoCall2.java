package peony.service.tong;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.net.ClientSession;
import peony.net.Packet;

public class TongInfoCall2  extends ClientSessionAsyncCall {

		private int serial;
		private int playerId;
		private int contribute;   		//¸öÈË¹±Ï×¶È
		private int lastLogIn;
		
		
		
		public  TongInfoCall2(ClientSession session, Packet packet) {
			super(session);
			this.serial = packet.getInt();
			this.playerId = packet.getInt();
		}

		public void callFinish() throws Exception {
			if(success){
				Packet pt = new Packet(OpCode.TONG_PLAYERINFO_SERVER);
				pt.putInt(serial);
				pt.putInt(contribute);
				pt.putInt(lastLogIn);
				session.send(pt);
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_INFO_CLIENT, errorMessage);
			}
		}

		public void run() {
			Player p = ObjectAccessor.getPlayer(playerId);
			if(p == null){
				p = Server.server.getServiceRegistry().getDbService().playerDAO.getPlayerById(playerId);
			}
			if(p!=null){
				TongService tongService = Server.server.getServiceRegistry().getTongService();
				Tong tong = tongService.getPlayerTong(p.id,true);
				TongMember tm = tongService.getPlayerInfo(p.id);
				if(tm==null || tong==null){
					error(peony.Messages.STRING_00748);
					addToClientSession();
					return;
				}
				this.contribute = p.contribute;
				long lastLogInTime = p.lastLoginTime.getTime();
				this.lastLogIn = (int)((Time.currDate.getTime() - lastLogInTime)/(60*60*1000));
			}
			addToClientSession();
		}
	}
