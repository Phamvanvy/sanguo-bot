package peony.game.quest;

import peony.game.Gain;

public class HonorRewardEntry extends AbstractQuestRewardEntity {

	protected int honor;
	
	public HonorRewardEntry(int honor){
		this.honor = honor;
	}
	
	public void gain(Gain gain) {
		super.gain(gain);
		gain.addHonor((int) (honor * (1 + extraRewardRatio)));
	}

}