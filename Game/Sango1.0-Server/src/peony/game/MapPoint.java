package peony.game;

/**
 * 路点信息
 * @author Jeffrey
 *
 */
public class MapPoint {
	public int mapId;
	public int x,y;

	public MapPoint(int mapId,int x,int y){
		this.mapId = mapId;
		this.x = x;
		this.y = y;
	}
	
	public boolean equals(int mapId,int x,int y){
		return this.mapId==mapId&&this.x==x&&this.y==y;
	}
}
