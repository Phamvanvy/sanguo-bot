package peony.patchs;

import peony.game.Player;

public class SkillTimePatch1 implements Runnable {

	public void run() {
		Player.maxSkillOffsetTime = 500000;
	}

}
