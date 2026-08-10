package peony.service.stat;

import java.util.ArrayList;
import java.util.List;

public class Achievement {
	public int achieveId;
	public int type;
	public int achieveTypeId;
	public String achievementName;
	public String dec;
	public String param1;
	public String param2;
	public byte acomplish;
	public int point;
	public String finiTime = "";
	public List<Integer> rewardItems = new ArrayList<Integer>();

	public Achievement(int achieveId, int type, int achieveTypeId,String achievementName,
			String dec,String param1,String param2, int point) {
		this.achieveId = achieveId;
		this.type = type;
		this.achieveTypeId = achieveTypeId;
		this.achievementName = achievementName;
		this.dec = dec;
		this.param1 = param1;
		this.param2 = param2;
		this.point = point;
	}
	
	public void addRewardItem(int itemId){
		rewardItems.add(itemId);
	}
	
	public List<Integer> getRewardItem(){
		return rewardItems;
	}
}