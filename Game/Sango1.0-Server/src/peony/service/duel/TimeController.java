package peony.service.duel;

/**
 * 比武招亲时间信息
 * @author dchen
 */
public class TimeController {

	public int startHour;
	public int startMin;
	public int endHour;
	public int endMin;
	public long duration;
	public int npcId; // 公主ID
	public int mapId;
	public int x;
	public int y;
	
	public TimeController(int startHour,int startMin,int endHour,int endMin,long duration,
			int npcId,int mapId,int x,int y){
		this.startHour = startHour;
		this.startMin = startMin;
		this.endHour = endHour;
		this.endMin = endMin;
		this.duration = duration;
		this.npcId = npcId;
		this.mapId = mapId;
		this.x = x;
		this.y = y;
	}
	
}
