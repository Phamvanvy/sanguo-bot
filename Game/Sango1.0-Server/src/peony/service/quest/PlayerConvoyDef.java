package peony.service.quest;

import java.util.ArrayList;
import java.util.List;

import peony.game.MapPoint;
import peony.game.Player;

/**
 * 押镖定义，阵营, 镖车id, 品质, 类型, 所属人, 以及经过的路点信息
 * @author bqzhang
 *
 */
public class PlayerConvoyDef {
	/**
	 * 国家阵营--魏蜀吴123
	 */
	public int faction;
	
	/**
	 * 路线类型
	 */
	public int pointType;
	
	/**
	 * 镖车ID
	 */
	public int npcId;

	public List<MapPoint> points = new ArrayList<MapPoint>();
	
	public PlayerConvoyDef(int faction, int type, int npcId){
		this.faction = faction;
		this.pointType = type;
		this.npcId = npcId;
	}
	
	public void addMapPoint(int mapId,int x,int y){
		points.add(new MapPoint(mapId,x,y));
	}
	
	public MapPoint getFirstPoint(){
		if(points.size()>0)
			return points.get(0);
		return null;
	}
	
	public int size(){
		return points.size();
	}
	
	public MapPoint getMapPoint(int index){
		return points.get(index);
	}
	
	public MapPoint getEndPoint(){
		if(points.size()>0)
			return points.get(points.size()-1);
		return null;
	}
	
}
