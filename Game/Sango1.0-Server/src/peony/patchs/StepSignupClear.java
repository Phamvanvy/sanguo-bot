package peony.patchs;

import peony.game.stepserver.StepBattleService;

public class StepSignupClear implements Runnable {

	public void run() {
		try {
			StepBattleService.todaySignedTimes.clear();
			System.out.println("______________load todaySignedTimes OK");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
