package peony.game;

import ch.javasoft.util.intcoll.IntHashMap;

public class QuestReward {

	protected int questId;
	protected IntHashMap<QuestRewardBranch> branchs = new IntHashMap<QuestRewardBranch>();
	
	public QuestReward(int questId){
		this.questId = questId;
	}
	
	public void addQuestRewardBranch(QuestRewardBranch branch){
		branchs.put(branch.id, branch);
	}
	
	public boolean containsBranch(int branchId){
		return branchs.containsKey(branchId);
	}
	
	public void gain(int branchId,Gain gain){
		QuestRewardBranch branch = branchs.get(branchId);
		if(branch!=null){
			branch.gain(gain);
		}else
			throw new IllegalArgumentException();
	}
}
