package peony.patchs;

import peony.game.Player;

public class SkillTimePatch implements Runnable {

	public void run() {
		Player.maxSkillOffsetTime = 300000;
	}

}
