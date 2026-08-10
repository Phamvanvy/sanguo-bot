package peony.game.nation;

import peony.game.GameObject;

public class NationSneakBattleFieldDef {
	public int sourceFaction,destFaction; //防守方阵营，进攻方阵营
	public int mapId;
	public int[][] outs;
	public int[] backPoint; //交东西的地点
	public int[][] npcs;
	public int[] in1;
	public int[] in2;
	
	public NationSneakBattleFieldDef(int sourceFaction,int destFaction,int mapId,int[][] outs,int[] in1,int[] in2,int[] backPoint,int[][] npcs){
		this.sourceFaction = sourceFaction;
		this.destFaction = destFaction;
		this.mapId = mapId;
		this.outs = outs;
		this.in1 = in1;
		this.in2 = in2;
		this.backPoint = backPoint;
		this.npcs = npcs;
	}
	
	public int[] getNpc(int faction){
		if(faction==GameObject.FACTION_WEI)
			return npcs[0];
		if(faction==GameObject.FACTION_SHU)
			return npcs[1];
		if(faction==GameObject.FACTION_WU)
			return npcs[2];
		throw new IllegalArgumentException();
	}
	
	public int[] getOutPoint(int faction){
		return outs[faction-1];
	}
	
	public int[] getInPoint(int faction){
		if(faction==sourceFaction)
			return in1;
		if(faction==destFaction)
			return in2;
		throw new IllegalArgumentException();
	}
}
