package peony.game.nation;

import peony.game.GameObject;

public class NationBattleFieldDef {
	public int sourceFaction,destFaction;  //防守方 阵营,进攻方阵营
	public int mapId; //地图Id
	public int kingId; //国王npc的Id
	public int[] guards; //防守的4个大将的Id,需要全部杀死才能击杀国王
	public int[][] doors; //东南西北4个门的传送点，用来在国王使用技能是传送用
	public int[] out1; //防守方移出的坐标
	public int[] out2; //进攻方移出的坐标
	public int[][] npcs;
	
	
	public NationBattleFieldDef(int sourceFaction, int destFaction, int mapId,
			int kingId, int[] guards, int[][] doors,int[] out1,int[] out2,int[][] npcs) {
		this.sourceFaction = sourceFaction;
		this.destFaction = destFaction;
		this.mapId = mapId;
		this.kingId = kingId;
		this.guards = guards;
		this.doors = doors;
		this.out1 = out1;
		this.out2 = out2;
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
		if(faction==sourceFaction){
			return out1;
		}
		else if(faction==destFaction){
			return out2;
		}
		else 
			throw new IllegalArgumentException();
	}
	
}
