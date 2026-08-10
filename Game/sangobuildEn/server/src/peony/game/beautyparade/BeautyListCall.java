package peony.game.beautyparade;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class BeautyListCall extends ClientSessionAsyncCall {

	private int serial;
	private Player p;
	private Object[] arr;
	private int maxNum = 30;
	
	public BeautyListCall(Packet packet, ClientSession session) {
		super(session);
		this.serial = packet.getInt();
		this.p = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if(success){
			BeautyParadeService service = Server.server.getServiceRegistry().getBeautyParadeService();
			Packet pt = new Packet(OpCode.BEAUTYPARADE_LIST_SERVER);
			pt.putInt(serial);
			int num = maxNum;
			num = Math.min(num, (arr.length-1));
			if(service.isBeauty(p.id) && service.getPositionInBeautys(p.id)>=num){
				pt.putInt(num+1);
				for(int i=0;i<num;i++){
					Beauty b = (Beauty)arr[i];
					pt.putInt(i+1);
					pt.putInt(b.playerId);
					pt.putString(b.name);
					pt.put(b.sex);
					pt.putString(b.slogan);
					pt.putInt(b.votes);
					pt.put(b.faction);
				}
				Beauty owner = service.getBeauty(p.id);
				pt.putInt(service.getPositionInBeautys(p.id)+1);
				pt.putInt(owner.playerId);
				pt.putString(owner.name);
				pt.put(owner.sex);
				pt.putString(owner.slogan);
				pt.putInt(owner.votes);
				pt.put(owner.faction);
			}else{
				pt.putInt(num);
				for(int i=0;i<num;i++){
					Beauty b = (Beauty)arr[i];
					pt.putInt(i+1);
					pt.putInt(b.playerId);
					pt.putString(b.name);
					pt.put(b.sex);
					pt.putString(b.slogan);
					pt.putInt(b.votes);
					pt.put(b.faction);
				}
			}
			if(arr[arr.length-1]!=null){
				VotePlayer v = (VotePlayer)arr[arr.length-1];
				pt.putInt(v.playerId);
				pt.putString(v.name);
				pt.putInt(v.votes);
				pt.put(v.faction);
			}else{
				pt.putInt(0);
				pt.putString("");
				pt.putInt(0);
				pt.put(0);
			}
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.BEAUTYPARADE_LIST_CLINET, errorMessage);
		}
	}

	public void run() {
		if(p!=null){
			BeautyParadeService service = Server.server.getServiceRegistry().getBeautyParadeService();
			try {
				arr = service.beautyList();
			} catch (BeautyParadeException e) {
				error(e.getMessage());
			}
		}
		addToClientSession();
	}

}
