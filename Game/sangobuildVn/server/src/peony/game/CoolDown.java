package peony.game;

public class CoolDown implements Comparable<CoolDown>,Cloneable{
	
	public int id; //cd组ID
	public int endTime; //cd结束时间，从服务器开机开始计算
	public int startTime; //cd开始时间，从服务器开机开始计算
	
	public CoolDown(int id,int startTime,int time){
		this.id = id;
		this.startTime = startTime;
		this.endTime = time;
	}
	
	public int compareTo(CoolDown o) {
		return endTime - o.endTime;
	}
	
	@Override
	public CoolDown clone(){
		return new CoolDown(id,startTime,endTime);
	}
	
	public int getLeaveTime(){
		return Math.max(0, endTime - Time.currTime);
	}
	
}
