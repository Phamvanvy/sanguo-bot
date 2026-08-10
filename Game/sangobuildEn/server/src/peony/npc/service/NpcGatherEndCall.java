package peony.npc.service;

import java.text.MessageFormat;

import peony.game.GatherUnit;
import peony.game.NormalGatherEndCall;
import peony.game.Player;
import peony.game.Server;

public class NpcGatherEndCall extends NormalGatherEndCall {

	public void gatherEnd(GatherUnit gu, Player p) {
		super.gatherEnd(gu, p);
		if(gu!=null && p!=null){
			if(gu.id==3473559){
				Server.server.getServiceRegistry().getChatService().sendAreaSystemMessage(MessageFormat.format("恭喜{0}獲得大寶箱,請大家下次赶早.", p.name), 848);
			}else if(gu.id==3473558 || gu.id==3473555 || gu.id==3473557 || 
					gu.id==3473556 || gu.id==3473560){
				Server.server.getServiceRegistry().getChatService().sendAreaSystemMessage(MessageFormat.format("恭喜{0}獲得小寶箱,請大家下次赶早.", p.name), 848);
			}
		}
	}
	
}
