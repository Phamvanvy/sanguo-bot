package peony.game.touchaction;

import peony.game.Creature;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.Packet;
import peony.service.tong.Tong;
import peony.service.tong.apply.TongBattleApplyService;

public class TongWinTouchAction implements TouchAction {

	private String arg1;
	private String arg2;
	
	public TongWinTouchAction(String[] arg){
		this.arg1 = arg[1];
		this.arg2 = arg[2];
	}
	
	public void touch(Player player, Creature npc) {
		if(player!=null){
			Tong tong = Server.server.getServiceRegistry().getTongService().getPlayerTong(player.id,false);
			TongBattleApplyService service = Server.server.getServiceRegistry().getTongBattleApplyService();
			if(tong!=null && service.getWinnerTong(player.map.id)!=null && service.getWinnerTong(player.map.id).id==tong.id){
				Packet pt = new Packet(OpCode.OPENUI_SERVER);
				pt.putString(arg1);
				pt.putString(arg2);
				player.send(pt);
			}else{
				player.message(-1, peony.Messages.STRING_01014, -1, -1);
//				ErrorHandler.sendErrorMessage(player.session, -1, -1, "非占领军团成员，无法进入商店。");
			}
		}
	}

}
