package peony.game.battlefield;

public class FlagBattleFieldDefinition {
	
	public int maxplayer;
	public int flagId;
	public int x,y;
	public int mapId;
	public int[][] in;
	public int[][] out;
	public int faction1,faction2;
	public int[][] aim;
	
	public FlagBattleFieldDefinition(int maxplayer, int flagId,int x,int y, int mapId,
			int[][] in, int[][] out,int faction1,int faction2,int[][] aim) {
		this.maxplayer = maxplayer;
		this.flagId = flagId;
		this.x = x;
		this.y = y;
		this.mapId = mapId;
		this.in = in;
		this.out = out;
		this.faction1 = faction1;
		this.faction2 = faction2;
		this.aim = aim;
	}
	
	public int[] getIn(int faction){
		if(faction1==faction){
			return in[0];
		}
		return in[1];
	}
	
	public int[] getOut(int faction){
		if(faction1==faction){
			return out[0];
		}
		return out[1];
	}
	
	public int[] getAim(int faction){
		if(faction1==faction)
			return aim[0];
		return aim[1];
	}
}
