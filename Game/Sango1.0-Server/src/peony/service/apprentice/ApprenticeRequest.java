package peony.service.apprentice;

import peony.game.GameObjectRef;

public class ApprenticeRequest {
	public int id;
	public int time;
	public GameObjectRef ref,targetRef;
	
	public ApprenticeRequest(int id,int time,GameObjectRef ref,GameObjectRef targetRef){
		this.id = id;
		this.time = time;
		this.ref = ref;
		this.targetRef = targetRef;
	}
}