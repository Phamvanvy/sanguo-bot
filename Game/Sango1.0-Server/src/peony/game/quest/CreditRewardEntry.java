package peony.game.quest;

import peony.game.Gain;

public class CreditRewardEntry extends AbstractQuestRewardEntity {

	protected int credit;
	
	public CreditRewardEntry(int credit){
		this.credit = credit;
	}
	
	public void gain(Gain gain) {
		super.gain(gain);
		gain.addCredit((int) (credit * (1 + extraRewardRatio)));
	}

}
