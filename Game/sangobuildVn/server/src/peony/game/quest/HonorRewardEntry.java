package peony.game.quest;

import peony.game.Gain;

public class HonorRewardEntry implements QuestRewardEntry {

	protected int honor;
	
	public HonorRewardEntry(int honor){
		this.honor = honor;
	}
	
	public void gain(Gain gain) {
		gain.addHonor(honor);
	}

}