package peony.game.quest;

import peony.game.Gain;

public class SalaryRewardEntry extends AbstractQuestRewardEntity {
	protected int salary;
	public SalaryRewardEntry(int salary){
		this.salary=salary;
	}
	public void gain(Gain gain) {
		super.gain(gain);
		gain.addSalary((int) (salary * (1 + extraRewardRatio)));
	}
}
