package peony.service.expansionbattle;

/**
 * 战役时间信息
 * @author dchen
 */
public class ExpansionPeriod {

	public int startHour;
	public int startMin;
	public int endHour;
	public int endMin;
	public long duration;
	
	public ExpansionPeriod(int startHour,int startMin,int endHour,int endMin,long duration){
		this.startHour = startHour;
		this.startMin = startMin;
		this.endHour = endHour;
		this.endMin = endMin;
		this.duration = duration;
	}
	
}
