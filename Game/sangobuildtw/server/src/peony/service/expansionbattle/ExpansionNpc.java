package peony.service.expansionbattle;

import peony.game.GameObject;

/**
 * 战役NPC
 * @author dchen
 */
public class ExpansionNpc {

	public int type;
	public GameObject npc;
	
	public ExpansionNpc(int type, GameObject npc){
		this.type = type;
		this.npc = npc;
	}
	
}
