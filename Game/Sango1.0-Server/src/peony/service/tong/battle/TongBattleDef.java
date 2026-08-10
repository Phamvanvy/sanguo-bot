package peony.service.tong.battle;

public class TongBattleDef {
	public String name;
	public int mapId;
	public int signMapId;
	public TongBattleSideDef defend,attack1,attack2;
	public int[] outPoints;
	public int duration;
	
	public int[] getOutPoint(){
		return outPoints;
	}
}
