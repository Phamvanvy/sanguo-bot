package peony.marriage;

import peony.game.GameObjectRef;

public class AskForGiftRequest {
	public int id;
	public int time;
	public GameObjectRef ref,targetRef;
	
	public AskForGiftRequest(int id,int time,GameObjectRef ref,GameObjectRef targetRef){
		this.id = id;
		this.time = time;
		this.ref = ref;
		this.targetRef = targetRef;
	}
}