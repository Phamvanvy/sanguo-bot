package peony.game.convoy;

import java.util.ArrayList;
import java.util.List;

import peony.game.MapPoint;

/**
 * 押镖定义，每个国家有一个，记录了阵营，镖车id以及经过的路点信息
 * @author Jeffrey
 *
 */
public class NationConvoyDef {
	//阵营
	public int faction;
	//镖车Id
	public int npcId;

	public List<MapPoint> points = new ArrayList<MapPoint>();
	
	public NationConvoyDef(int faction,int npcId){
		this.faction = faction;
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
