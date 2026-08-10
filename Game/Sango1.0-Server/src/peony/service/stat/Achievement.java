package peony.service.stat;

import java.util.ArrayList;
import java.util.List;

public class Achievement {
	public int achieveId;
	public int type;
	public String achievementName;
	public String dec;
	public String param1;
	public String param2;
	public String property;
	public byte acomplish;
	public int point;
	public String finiTime = "";
	public List<Integer> rewardItems = new ArrayList<Integer>();

	public Achievement(int achieveId, int type,String achievementName,
			String dec,String param1,String param2, int point,String property) {
		this.achieveId = achieveId;
		this.type = type;
		this.achievementName = achievementName;
		this.dec = dec;
		this.param1 = param1;
		this.param2 = param2;
		this.point = point;
		this.property = property;
	}
	
	public void addRewardItem(int itemId){
		rewardItems.add(itemId);
	}
	public String getacomplist(){
		return acomplish+"";
	}
	
	public List<Integer> getRewardItem(){
		return rewardItems;
	}
}