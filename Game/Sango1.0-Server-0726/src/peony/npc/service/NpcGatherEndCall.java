package peony.npc.service;

import java.text.MessageFormat;
import java.util.List;

import peony.game.GatherUnit;
import peony.game.NormalGatherEndCall;
import peony.game.Player;
import peony.game.Server;
import peony.service.stat.Achievement;
import peony.service.stat.PvpInfo;
import peony.service.stat.StatService;

public class NpcGatherEndCall extends NormalGatherEndCall {

	public void gatherEnd(GatherUnit gu, Player p) {
		super.gatherEnd(gu, p);
		if(gu!=null && p!=null){
			StatService statService = Server.server.getServiceRegistry().getStatService();
			PvpInfo pvpInfo = statService.getPvpInfo(p.id, p.faction);
			if(gu.id==3473559){
				Server.server.getServiceRegistry().getChatService().sendAreaSystemMessage(MessageFormat.format("恭喜{0}获得大宝箱，请大家下次赶早。", p.name), 848);
				
				//统计获得大宝箱成就
				List<Achievement> achs = statService.getAchievementList(StatService.OTHERTYPE_ONCE_ACHIEVETYPE);
				if(achs!=null){
					for(Achievement a:achs){
						int type = Integer.parseInt(a.param1);
                        if(type == 4){
							if(pvpInfo.pool.getString(StatService.PROPERTY_FINISHTIME_BIGBOX) == ""){
								pvpInfo.pool.setString(StatService.PROPERTY_FINISHTIME_BIGBOX, statService.getFinishTime(System.currentTimeMillis()));
									statService.setMessage(p, a, false);
							}
                        }
					}
				}
				
			}else if(gu.id==3473558 || gu.id==3473555 || gu.id==3473557 || 
					gu.id==3473556 || gu.id==3473560){
				Server.server.getServiceRegistry().getChatService().sendAreaSystemMessage(MessageFormat.format("恭喜{0}获得小宝箱，请大家下次赶早。", p.name), 848);
			
				//统计获得小宝箱成就
				List<Achievement> achs = statService.getAchievementList(StatService.OTHERTYPE_ONCE_ACHIEVETYPE);
				if(achs!=null){
					for(Achievement a:achs){
						int type = Integer.parseInt(a.param1);
                        if(type == 5){
							if(pvpInfo.pool.getString(StatService.PROPERTY_FINISHTIME_SMALLBOX) == ""){
								pvpInfo.pool.setString(StatService.PROPERTY_FINISHTIME_SMALLBOX, statService.getFinishTime(System.currentTimeMillis()));
								statService.setMessage(p, a, false);
							}
                        }
					}
				}
			}
		}
	}
	
	
}
