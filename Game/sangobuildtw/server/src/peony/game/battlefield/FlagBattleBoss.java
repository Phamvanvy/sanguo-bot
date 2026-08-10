package peony.game.battlefield;

public class FlagBattleBoss {
	
	public int mapId;
	public int minLevel;
	public int maxLevel;
	public int bossId;
	public int x;
	public int y;
	
	public FlagBattleBoss(int bossId, int mapId, int maxLevel, int minLevel,
			int x, int y) {
		super();
		this.bossId = bossId;
		this.mapId = mapId;
		this.maxLevel = maxLevel;
		this.minLevel = minLevel;
		this.x = x;
		this.y = y;
	}
	
}
