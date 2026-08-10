package peony.game.attendant;

import peony.common.AsyncCall;

public class AttendantFollowCall implements AsyncCall {

	protected Attendant attendant;
	
	public AttendantFollowCall(Attendant attendant){
		this.attendant = attendant;
	}
	
	public void run() {
		
	}

	public void callFinish() throws Exception {
		attendant.follow();
		attendant.addBuff(attendant.owner);
		if(attendant.owner!=null)
			attendant.owner.attendantWaitRelive = false;
	}

}
