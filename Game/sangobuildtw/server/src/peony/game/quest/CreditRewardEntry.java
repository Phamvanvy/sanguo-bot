package peony.game.quest;

import peony.game.Gain;

public class CreditRewardEntry implements QuestRewardEntry {

	protected int credit;
	
	public CreditRewardEntry(int credit){
		this.credit = credit;
	}
	
	public void gain(Gain gain) {
		gain.addCredit(credit);
	}

}
