package peony.game;

import org.apache.log4j.Logger;

import peony.service.ServiceEvent;

public class OutPrisonUtil {
	
	private static final Logger log = Logger.getLogger(OutPrisonUtil.class);
	
	public static void outPrison(Player p){
		if (p.map.map != null) {
		    int[] pos = p.map.map.mapDef.mapInfo.getPathFinder().tryOutPrison(p.x, p.y);
		    if (pos == null) {
		        // 已彻底卡死，回复活点
				int[] relivePoint = p.map.map.getRelivePoint(p.faction);
				try{
				int oldMapId = p.map.map.getId();
				int oldX = p.x;
				int oldY = p.y;
				p.goMap(relivePoint[0], relivePoint[1], relivePoint[2]);
					Server.server
							.getEventManager()
							.fireEvent(
									new ServiceEvent(
											ServiceEvent.EVENT_PLAYER_OUTPRISON_RELIVEPOINT,
											p,oldMapId,oldX,oldY));
				} catch (VMapException e) {
					log.error(e,e);
				}
			} else {
			    try {
					p.goMap(p.map.map.getId(), pos[0], pos[1]);
				} catch (VMapException e) {
					//不应该被执行到
					log.error(e,e);
				}
			}
		}
	}
}
