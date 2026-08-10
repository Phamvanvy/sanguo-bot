package peony.game.actlead;

public class ActLeaderMap {
	public int mapId;
	public String mapName;
	public int x;
	public int y;
	public int minLevel;
	public int maxLevel;
	public String faction;
	private int[] factionArr;
	
	public ActLeaderMap(int mapId, String mapName, int x, int y, int minLevel, int maxLevel, String faction) throws Exception{
		this.mapId = mapId;
		this.mapName = mapName;
		this.x = x;
		this.y = y;
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
		this.faction = faction;
		parseConfig();
	}
	
	protected void parseConfig() throws Exception {
		try {
			String[] factionStr = faction.split(",");
			factionArr = new int[factionStr.length];
			for(int i=0;i<factionStr.length;i++){
				int ti = Integer.parseInt(factionStr[i]);
				factionArr[i] = ti;
			}
		} catch (Exception e) {
			throw new ActLeaderException("Cách biểu đạt không hợp lệ");
		}
	}
	
	public boolean inLevel(int playerLevel){
		return playerLevel>=minLevel && playerLevel<maxLevel;
	}
	
	public boolean isInFaction(int faction){
		if(faction<=0)
			return true;
		for(int t : factionArr){
			if(faction==t)
				return true;
		}
		return false;
	}
	
}
