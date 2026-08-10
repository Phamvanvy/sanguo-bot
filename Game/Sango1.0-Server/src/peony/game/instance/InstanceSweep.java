package peony.game.instance;

import java.util.ArrayList;
import java.util.List;

public class InstanceSweep{
	
	public int id;
	public String instanceName;
	public int time;
	public int dayTimes;
	public String reward;
	public long endTime = 0l;
	public List<Integer> killBoss = new ArrayList<Integer>();
	
	public InstanceSweep(int id,String instanceName,int time,String reward,int dayTimes){
		this.id = id;
		this.instanceName = instanceName;
		this.time = time;
		this.reward = reward;
		this.dayTimes = dayTimes;
	}
	
	public InstanceSweep(int id,long endTime){
		this.id = id;
		this.endTime = endTime;
	}
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return instanceName;
	}
	
	public long getEndTime(){
		return endTime;
	}
	
}
