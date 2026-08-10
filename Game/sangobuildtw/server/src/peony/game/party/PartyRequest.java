package peony.game.party;

import peony.game.GameObjectRef;

public class PartyRequest {
	
	public int id;
	public int time;
	public GameObjectRef ref,targetRef;
	
	public PartyRequest(int id,int time,GameObjectRef ref,GameObjectRef targetRef){
		this.id = id;
		this.time = time;
		this.ref = ref;
		this.targetRef = targetRef;
	}
}
