package peony.game;

public class NPCUtil {
	public static final int[] NPCKING_ID = {
		0,
		1114115,
		983041,
		1441812,
	};
	
	public static int getKingFaction(Unit u){
		for(int i=1;i<NPCKING_ID.length;i++){
			if(u.id==NPCKING_ID[i])
				return i;
		}
		return 0;
	}
}
