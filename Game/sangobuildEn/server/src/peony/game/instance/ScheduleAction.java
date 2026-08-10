package peony.game.instance;

import peony.game.Time;

public abstract class ScheduleAction {
	
	public int[] actionTimes;
	public boolean[] fireds;
	
	
	public ScheduleAction(int[] actionTimes){
		this.actionTimes = actionTimes;
		this.fireds = new boolean[this.actionTimes.length];
	}
	
	public abstract boolean action(int index);
	
	public boolean update(){
		for(int i=0;i<fireds.length;i++){
			boolean fired = fireds[i];
			if(!fired){
				if(actionTimes[i]<=Time.currTime){
					fireds[i] = true;
					return action(i);
				}
				break;
			}
		}
		return false;
	}
}
