package peony.game.quest;

import peony.game.Gain;

public class MoneyRewardEntry implements QuestRewardEntry {
	
	protected int money;
	
	public MoneyRewardEntry(int money){
		this.money = money;
	}
	
	public void gain(Gain gain) {
		gain.addMoney((int)(money * gain.getPlayer().tirePercent));
	}

}
